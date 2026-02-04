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
 * Configures Jakarta RESTful Web Services for the application.
 *
 * @author Marc Cunillera
 */

public class Main {

    public static void main(String[] args) {

        try (Scanner scanner = new Scanner(System.in)) {

            // Servicio de sincronización con XML
            ProductXmlSyncService syncService = new ProductXmlSyncService();

            // 1. Cargar productos de Odoo
            System.out.println("Cargando productos desde Odoo...\n");
            JSONArray products = OdooProductTemplates.getProductTemplate();

            if (products.isEmpty()) {
                System.out.println("❌ No hay productos en Odoo.");
                return;
            }

            // 2. Mostrar productos disponibles
            printProductList(products);

            // 3. Seleccionar producto
            int selectedId = promptForProductId(scanner);

            JSONObject selectedProduct = findProductById(products, selectedId);

            if (selectedProduct == null) {
                System.out.println("❌ Producto no encontrado con ID: " + selectedId);
                return;
            }

            // 4. Obtener stock del producto
            int stock = calculateStock(selectedProduct, selectedId);

            // 5. Mapear a DTO
            ProductDTO dto = OdooToPrestashopMapper.map(selectedProduct, stock);

            // 6. Sincronizar con PrestaShop
            System.out.println("\nIniciando sincronización con PrestaShop...\n");
            syncService.syncProduct(dto);

            System.out.println("\nProceso completado exitosamente.");

        } catch (Exception e) {
            System.err.println("\nError durante la sincronización:");
            e.printStackTrace();
        }
    }

    /**
     * Mostrar lista de productos
     */
    private static void printProductList(JSONArray products) {
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║         PRODUCTOS DISPONIBLES EN ODOO                     ║");
        System.out.println("╠═══════════════════════════════════════════════════════════╣");

        for (int i = 0; i < products.length(); i++) {
            JSONObject product = products.getJSONObject(i);
            int id = product.getInt("id");
            String name = product.getString("name");
            String ref = product.optString("default_code", "Sin ref.");

            System.out.printf("║ [%3d] %-40s [%s]%n", id, truncate(name, 40), ref);
        }

        System.out.println("╚═══════════════════════════════════════════════════════════╝");
    }

    /**
     * Solicitar ID del producto al usuario
     */
    private static int promptForProductId(Scanner scanner) {
        System.out.print("\nIntroduce el ID del producto a sincronizar: ");
        return scanner.nextInt();
    }

    /**
     * Buscar producto por ID
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
     * Calcular stock del producto
     */
    private static int calculateStock(JSONObject product, int odooId) throws Exception {
        String reference = product.optString("default_code", "");

        System.out.println("\n--- Calculando stock ---");
        System.out.println("Referencia: " + reference);
        System.out.println("ID Template: " + odooId);

        int stock = OdooStockService.getStockByReference(odooId, reference);

        System.out.println("✓ Stock calculado: " + stock);

        return stock;
    }

    /**
     * Truncar texto a un tamaño máximo
     */
    private static String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}
