package com.mycompany.prestashop.odoo.Prestashop.service;

import com.mycompany.prestashop.odoo.Prestashop.client.PrestashopXmlClient;
import com.mycompany.prestashop.odoo.Prestashop.model.ProductDTO;
import com.mycompany.prestashop.odoo.Prestashop.xml.ProductXmlMapper;
import com.mycompany.prestashop.odoo.Prestashop.xml.StockXmlMapper;

public class ProductXmlSyncService {

    /**
     * Coordina el flux principal de sincronització d'un producte, gestionant la
     * cerca prèvia a PrestaShop i decidint si cal crear un registre nou o
     * actualitzar un existent.
     */
    public void syncProduct(ProductDTO product) throws Exception {
        String reference = normalize(product.getReference());
        String productName = normalize(product.getNames());

        printHeader(productName, reference);

        Integer existingId = findExistingProduct(reference);

        if (existingId != null) {
            updateExistingProduct(existingId, product);
        } else {
            createNewProduct(product);
        }

        printFooter();
    }

    /**
     * Gestiona la sincronització de fitxers multimèdia mitjançant el servei
     * d'imatges, informant de l'estat del procés per consola.
     */
    private void syncImages(int productId, ProductDTO product) {
        try {
            ImageSyncService.syncProductImage(productId, product);
            System.out.println("   - Imagenes:        [ OK ] Multimedia vinculada");
        } catch (Exception e) {
            System.out.println("   - Imagenes:        [ SALTADO ] Sin imagen o error");
        }
    }

    /**
     * Cerca si un producte ja està donat d'alta a PrestaShop utilitzant la seva
     * referència única i retorna el seu identificador en cas afirmatiu.
     */
    private Integer findExistingProduct(String reference) {
        try {
            System.out.printf(" > %-35s", "Busqueda en PrestaShop...");
            if (reference == null || reference.isEmpty()) {
                System.out.println("[ VACIO ]");
                return null;
            }

            Integer productId = PrestashopXmlClient.findProductByReference(reference);
            if (productId != null) {
                System.out.println("[ EXISTE ] ID: " + productId);
            } else {
                System.out.println("[ NUEVO ]");
            }
            return productId;
        } catch (Exception e) {
            System.out.println("[ ERROR ] " + e.getMessage());
            return null;
        }
    }

    /**
     * Executa l'actualització de les dades base, la jerarquia de categories,
     * l'estoc i les imatges d'un producte que ja existeix a la plataforma.
     */
    private void updateExistingProduct(int productId, ProductDTO product) throws Exception {
        System.out.println(" > Accion: ACTUALIZAR");

        String updatedXml = updateProductXml(product, productId);
        PrestashopXmlClient.putXml("/products/" + productId, updatedXml);
        System.out.println("   - Datos base:      [ ACTUALIZADOS ]");

        printCategories(product);
        updateStock(productId, product.getQuantity());
        syncImages(productId, product);
    }

    /**
     * Registra un nou producte a PrestaShop a partir d'un XML generat, extreu
     * l'ID assignat i procedeix a sincronitzar el seu estoc i les seves
     * imatges.
     */
    private void createNewProduct(ProductDTO product) throws Exception {
        System.out.println(" > Accion: CREAR");

        String productXml = ProductXmlMapper.mapProductToXml(product);
        String responseXml = PrestashopXmlClient.postXml("/products", productXml);

        Integer newId = extractProductIdFromResponse(responseXml);
        System.out.println("   - Registro:        [ GENERADO ] ID: " + newId);

        printCategories(product);
        updateStock(newId, product.getQuantity());
        syncImages(newId, product);
    }

    /**
     * Actualitza la quantitat disponible d'un producte a PrestaShop, gestionant
     * el retard necessari perquè el recurs stock_available estigui a punt en
     * cas de creació recent.
     */
    private void updateStock(int productId, int quantity) throws Exception {
        Integer stockId = PrestashopXmlClient.getStockAvailableId(productId);

        if (stockId == null) {
            Thread.sleep(1500);
            stockId = PrestashopXmlClient.getStockAvailableId(productId);
        }

        if (stockId != null) {
            String stockXml = StockXmlMapper.mapStock(stockId, productId, quantity);
            PrestashopXmlClient.putXml("/stock_availables/" + stockId, stockXml);
            System.out.printf("   - Inventario:      [ OK ] Cantidad: %d\n", quantity);
        } else {
            System.out.println("   - Inventario:      [ ERROR ] ID de stock no encontrado");
        }
    }

    /**
     * Genera l'estructura XML necessària per a l'actualització del producte,
     * incloent la informació de preus, taxonomies i el mapeig complet de
     * categories.
     */
    private String updateProductXml(ProductDTO product, int productId) {
        String reference = normalize(product.getReference());
        String name = product.getNames();
        double price = (product.getPriceTaxExcluded() != null) ? product.getPriceTaxExcluded() : 0.0;
        double wholesalePrice = (product.getWholesalePrice() != null) ? product.getWholesalePrice() : 0.0;
        int categoryIdDefault = product.getCategoryId();

        String slug = name.toLowerCase().trim()
                .replaceAll("[^a-z0-9]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        StringBuilder categoriesXml = new StringBuilder();
        if (product.getCategoryIds() != null && !product.getCategoryIds().isEmpty()) {
            for (Integer catId : product.getCategoryIds()) {
                categoriesXml.append("        <category><id>").append(catId).append("</id></category>\n");
            }
        } else {
            categoriesXml.append("        <category><id>").append(categoryIdDefault).append("</id></category>\n");
        }

        return String.format(java.util.Locale.US, """
    <?xml version="1.0" encoding="UTF-8"?>
    <prestashop xmlns:xlink="http://www.w3.org/1999/xlink">
      <product>
        <id>%d</id>
        <id_category_default>%d</id_category_default>
        <id_shop_default>1</id_shop_default>
        <id_tax_rules_group>1</id_tax_rules_group>
        <type><![CDATA[simple]]></type>
        <reference><![CDATA[%s]]></reference>
        <price>%.6f</price>
        <wholesale_price>%.6f</wholesale_price>
        <active>1</active>
        <state>1</state>
        <available_for_order>1</available_for_order>
        <show_price>1</show_price>
        <visibility><![CDATA[both]]></visibility>
        <indexed>1</indexed>
        <name><language id="1"><![CDATA[%s]]></language></name>
        <link_rewrite><language id="1"><![CDATA[%s]]></language></link_rewrite>
        <associations>
          <categories>
    %s
          </categories>
        </associations>
      </product>
    </prestashop>
    """,
                productId, categoryIdDefault, escapeXml(reference), price, wholesalePrice,
                escapeXml(name), slug, categoriesXml.toString()
        );
    }

    /**
     * Utilitza expressions regulars per extreure l'identificador d'un recurs a
     * partir d'una cadena de text amb format XML.
     */
    private Integer extractProductIdFromResponse(String xml) {
        try {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("<id>(?:<!\\[CDATA\\[)?(\\d+)(?:\\]\\]>)?</id>");
            java.util.regex.Matcher matcher = pattern.matcher(xml);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        } catch (Exception e) {
            System.out.println("Error al extraer ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Neteja i normalitza les cadenes de text d'entrada per evitar errors en el
     * processament posterior.
     */
    private String normalize(String value) {
        return (value == null) ? "" : value.trim();
    }

    /**
     * Substitueix caràcters especials pels seus equivalents en format d'entitat
     * XML per garantir la validesa del document generat.
     */
    private String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }

    /**
     * Mostra per pantalla el camí complet de categories que s'ha assignat al
     * producte durant la sincronització.
     */
    private void printCategories(ProductDTO product) {
        if (product.getCategoryIds() != null && !product.getCategoryIds().isEmpty()) {
            String path = product.getCategoryIds().stream()
                    .map(String::valueOf)
                    .reduce((a, b) -> a + " / " + b)
                    .orElse("");
            System.out.printf("   - Categorias:      [ %s ]\n", path);
        } else {
            System.out.println("   - Categorias:      [ DEFAULT ] ID: " + product.getCategoryId());
        }
    }

    /**
     * Imprimeix la capçalera visual informativa al log de la consola abans de
     * començar la gestió del producte.
     */
    private void printHeader(String name, String ref) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println(" INICIO DE PROCESO DE SINCRONIZACION");
        System.out.println(" - PRODUCTO: " + name);
        System.out.println(" - REF:      " + ref);
        System.out.println("-".repeat(60));
    }

    /**
     * Imprimeix el peu de pàgina visual per tancar el bloc d'informació de la
     * sincronització actual.
     */
    private void printFooter() {
        System.out.println("-".repeat(60));
        System.out.println(" PROCESO FINALIZADO");
        System.out.println("=".repeat(60) + "\n");
    }
}
