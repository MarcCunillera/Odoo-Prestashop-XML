package com.mycompany.prestashop.odoo.Prestashop.service;

import com.mycompany.prestashop.odoo.Prestashop.client.PrestashopXmlClient;
import com.mycompany.prestashop.odoo.Prestashop.model.ProductDTO;
import com.mycompany.prestashop.odoo.Prestashop.xml.ProductXmlMapper;
import com.mycompany.prestashop.odoo.Prestashop.xml.StockXmlMapper;

public class ProductXmlSyncService {

    /**
     * Método principal de sincronización
     */
    public void syncProduct(ProductDTO product) throws Exception {

        String reference = normalize(product.getReference());
        String productName = normalize(product.getNames());

        printHeader(productName, reference);

        // Buscar si el producto ya existe en PrestaShop
        Integer existingId = findExistingProduct(reference);

        if (existingId != null) {
            updateExistingProduct(existingId, product);
        } else {
            createNewProduct(product);
        }

        printFooter();
    }

    /**
     * Buscar producto existente por referencia
     */
    private Integer findExistingProduct(String reference) {
        try {
            System.out.println("Buscando producto con referencia: " + reference);

            if (reference == null || reference.isEmpty()) {
                System.out.println("Referencia vacía, se creará nuevo producto");
                return null;
            }

            Integer productId = PrestashopXmlClient.findProductByReference(reference);

            if (productId != null) {
                System.out.println("Producto encontrado con ID: " + productId);
            } else {
                System.out.println("Producto no encontrado, se creará uno nuevo");
            }

            return productId;

        } catch (Exception e) {
            System.out.println("Error al buscar producto: " + e.getMessage());
            return null;
        }
    }

    /**
     * Actualizar producto existente
     */
    private void updateExistingProduct(int productId, ProductDTO product) throws Exception {
        System.out.println("\n═══ ACTUALIZANDO PRODUCTO ID: " + productId + " ═══");

        // 1. Obtener el esquema del producto existente
        String existingXml = PrestashopXmlClient.getXml("/products/" + productId);

        // 2. Actualizar los campos necesarios en el XML
        String updatedXml = updateProductXml(existingXml, product, productId);

        System.out.println("\n--- XML a enviar ---");
        System.out.println(updatedXml);
        System.out.println("--- Fin XML ---\n");

        // 3. Enviar actualización
        PrestashopXmlClient.putXml("/products/" + productId, updatedXml);
        System.out.println("Producto actualizado");

        // 4. Actualizar stock
        updateStock(productId, product.getQuantity());

        System.out.println("Sincronización completada para ID: " + productId);
    }

    /**
     * Crear nuevo producto
     */
    private void createNewProduct(ProductDTO product) throws Exception {
        System.out.println("\n═══ CREANDO NUEVO PRODUCTO ═══");

        // 1. Generar XML del producto
        String productXml = ProductXmlMapper.mapProductToXml(product);

        System.out.println("\n--- XML a enviar ---");
        System.out.println(productXml);
        System.out.println("--- Fin XML ---\n");

        // 2. Crear el producto
        String responseXml = PrestashopXmlClient.postXml("/products", productXml);
        System.out.println("Producto creado");

        // 3. Extraer el ID del producto creado (con manejo de CDATA)
        Integer newProductId = extractProductIdFromResponse(responseXml);

        if (newProductId == null) {
            System.out.println("Respuesta XML recibida:");
            System.out.println(responseXml);
            throw new RuntimeException("No se pudo obtener el ID del producto creado");
        }

        System.out.println("ID del nuevo producto: " + newProductId);

        // 4. Actualizar stock
        updateStock(newProductId, product.getQuantity());

        System.out.println("Producto creado exitosamente con ID: " + newProductId);
    }

    /**
     * Actualizar el stock de un producto
     */
    private void updateStock(int productId, int quantity) throws Exception {
        System.out.println("\n--- Actualizando stock ---");
        System.out.println("Producto ID: " + productId + " | Cantidad: " + quantity);

        // 1. Obtener el ID del registro stock_available
        Integer stockId = PrestashopXmlClient.getStockAvailableId(productId);

        if (stockId == null) {
            System.out.println("No se encontró registro de stock, intentando de nuevo en 2 segundos...");

            // Esperar y reintentar
            Thread.sleep(2000);
            stockId = PrestashopXmlClient.getStockAvailableId(productId);

            if (stockId == null) {
                System.out.println("Aún no se encontró registro de stock, saltando actualización");
                return;
            }
        }

        System.out.println("Stock ID encontrado: " + stockId);

        // 2. Generar XML de stock
        String stockXml = StockXmlMapper.mapStock(stockId, productId, quantity);

        System.out.println("\n--- XML Stock a enviar ---");
        System.out.println(stockXml);
        System.out.println("--- Fin XML Stock ---\n");

        // 3. Actualizar stock
        PrestashopXmlClient.putXml("/stock_availables/" + stockId, stockXml);
        System.out.println("✓ Stock actualizado a: " + quantity);
    }

    /**
     * Actualizar XML existente con nuevos datos
     */
    private String updateProductXml(String existingXml, ProductDTO product, int productId) {
        // Extraer valores necesarios
        String reference = (product.getReference() != null) ? product.getReference().trim() : "";
        String name = product.getNames();
        double price = (product.getPriceTaxExcluded() != null) ? product.getPriceTaxExcluded() : 0.0;
        double wholesalePrice = (product.getWholesalePrice() != null) ? product.getWholesalePrice() : 0.0;
        int categoryId = product.getCategoryId();

        String slug = name.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        // Construir XML con campos de visibilidad para que no desaparezca del catálogo
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
        <name>
          <language id="1"><![CDATA[%s]]></language>
        </name>
        <link_rewrite>
          <language id="1"><![CDATA[%s]]></language>
        </link_rewrite>
        <associations>
          <categories>
            <category>
              <id>%d</id>
            </category>
          </categories>
        </associations>
      </product>
    </prestashop>
    """,
                productId,
                categoryId,
                escapeXml(reference),
                price,
                wholesalePrice,
                escapeXml(name),
                slug,
                categoryId
        );
    }

    private Integer extractProductIdFromResponse(String xml) {
        try {
            // Método 1: Buscar patrón simple <id>NUMBER</id>
            int idStart = xml.indexOf("<id>") + 4;
            int idEnd = xml.indexOf("</id>");

            if (idStart > 3 && idEnd > idStart) {
                String idContent = xml.substring(idStart, idEnd).trim();

                // Limpiar CDATA si existe
                idContent = idContent.replace("<![CDATA[", "").replace("]]>", "").trim();

                // Intentar parsear
                try {
                    return Integer.parseInt(idContent);
                } catch (NumberFormatException e) {
                    System.out.println("Error al parsear ID: " + idContent);
                }
            }

            // Método 2: Buscar en el XML completo usando regex más robusto
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                    "<id>(?:<!\\[CDATA\\[)?(\\d+)(?:\\]\\]>)?</id>"
            );
            java.util.regex.Matcher matcher = pattern.matcher(xml);

            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }

        } catch (Exception e) {
            System.out.println("Error al extraer ID: " + e.getMessage());
        }
        return null;
    }

    // === MÉTODOS AUXILIARES ===
    private String normalize(String value) {
        return (value == null) ? "" : value.trim();
    }

    private String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private void printHeader(String name, String ref) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("SINCRONIZACIÓN: " + name + " [Ref: " + ref + "]");
        System.out.println("=".repeat(60));
    }

    private void printFooter() {
        System.out.println("=".repeat(60) + "\n");
    }
}
