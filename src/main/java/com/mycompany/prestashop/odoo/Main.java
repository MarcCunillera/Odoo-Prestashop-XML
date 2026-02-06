package com.mycompany.prestashop.odoo;

import com.mycompany.prestashop.odoo.Odoo.client.OdooProductTemplates;
import com.mycompany.prestashop.odoo.Odoo.model.OdooToPrestashopMapper;
import com.mycompany.prestashop.odoo.Prestashop.model.ProductDTO;
import com.mycompany.prestashop.odoo.Prestashop.service.OdooStockService;
import com.mycompany.prestashop.odoo.Prestashop.service.ProductXmlSyncService;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Classe principal que gestiona el flux d'execució de l'aplicació, permetent a
 * l'usuari seleccionar productes d'Odoo per sincronitzar-los amb PrestaShop.
 *
 * * @author Marc Cunillera
 */
public class Main {

    /**
     * Punt d'entrada de l'aplicació que controla el cicle de vida de la
     * sincronització: càrrega de dades, interacció amb l'usuari i invocació
     * dels serveis de mapatge i enviament.
     */
    public static void main(String[] args) {

        try (Scanner scanner = new Scanner(System.in)) {

            ProductXmlSyncService syncService = new ProductXmlSyncService();

            System.out.println("Cargando productos desde Odoo...\n");
            JSONArray products = OdooProductTemplates.getProductTemplate();

            if (products.isEmpty()) {
                System.out.println("No hay productos en Odoo.");
                return;
            }

            printProductList(products);

            int selectedId = promptForProductId(scanner);

            JSONObject selectedProduct = findProductById(products, selectedId);

            if (selectedProduct == null) {
                System.out.println("Error: Producto no encontrado con ID: " + selectedId);
                return;
            }

            int stock = calculateStock(selectedProduct, selectedId);

            ProductDTO dto = OdooToPrestashopMapper.map(selectedProduct, stock);

            syncService.syncProduct(dto);

        } catch (Exception e) {
            System.err.println("\nError critico durante la sincronización:");
            e.printStackTrace();
        }
    }

    /**
     * Imprimeix una taula formatada per consola amb la llista de productes
     * recuperats d'Odoo, mostrant l'ID, el nom i la referència tècnica.
     */
    private static void printProductList(JSONArray products) {
        String headerLine = "┌" + "─".repeat(60) + "┐";
        String midLine = "├" + "─".repeat(8) + "┬" + "─".repeat(34) + "┬" + "─".repeat(16) + "┤";
        String crossLine = "├" + "─".repeat(8) + "┼" + "─".repeat(34) + "┼" + "─".repeat(16) + "┤";
        String bottomLine = "└" + "─".repeat(8) + "┴" + "─".repeat(34) + "┴" + "─".repeat(16) + "┘";

        System.out.println(headerLine);
        System.out.printf("│ %-58s │\n", "PRODUCTOS DISPONIBLES EN ODOO");
        System.out.println(midLine);
        System.out.printf("│ %-6s │ %-32s │ %-14s │\n", "ID", "NOMBRE DEL PRODUCTO", "REFERENCIA");
        System.out.println(crossLine);

        for (int i = 0; i < products.length(); i++) {
            JSONObject product = products.getJSONObject(i);
            int id = product.getInt("id");
            String name = product.getString("name");
            String ref = product.optString("default_code", "---");

            System.out.printf("│ [%4d] │ %-32s │ %-14s │\n",
                    id,
                    truncate(name, 32),
                    truncate(ref, 14));
        }

        System.out.println(bottomLine);
    }

    /**
     * Gestiona la lectura de l'entrada de l'usuari per obtenir l'identificador
     * del producte, assegurant que el valor introduït sigui un número enter
     * vàlid.
     */
    private static int promptForProductId(Scanner scanner) {
        System.out.print("\nIntroduce el ID del producto a sincronizar: ");
        while (!scanner.hasNextInt()) {
            System.out.print("Por favor, introduce un ID numerico valido: ");
            scanner.next();
        }
        return scanner.nextInt();
    }

    /**
     * Cerca de forma seqüencial dins de la llista de productes d'Odoo fins a
     * trobar l'objecte JSON que coincideix amb l'ID seleccionat.
     */
    private static JSONObject findProductById(JSONArray products, int targetId) {
        for (int i = 0; i < products.length(); i++) {
            JSONObject product = products.getJSONObject(i);
            if (product.getInt("id") == targetId) {
                return product;
            }
        }
        return null;
    }

    /**
     * Invoca el servei d'estoc d'Odoo per calcular les unitats disponibles de
     * la variant corresponent abans de procedir amb la sincronització.
     */
    private static int calculateStock(JSONObject product, int odooId) throws Exception {
        String reference = product.optString("default_code", "");
        return OdooStockService.getStockByReference(odooId, reference);
    }

    /**
     * Escursa les cadenes de text que superen una longitud màxima determinada,
     * afegint punts suspensius per mantenir l'alineació de la interfície de
     * consola.
     */
    private static String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}
