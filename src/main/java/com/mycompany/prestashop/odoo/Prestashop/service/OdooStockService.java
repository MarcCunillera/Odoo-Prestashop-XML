package com.mycompany.prestashop.odoo.Prestashop.service;

import com.mycompany.prestashop.odoo.Odoo.client.OdooProductProduct;
import com.mycompany.prestashop.odoo.Odoo.client.OdooStockQuant;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio de cálculo de stock desde Odoo
 */
public class OdooStockService {

    // MÉTODO PRINCIPAL
    public static int getStockByReference(int templateId, String templateRef) throws Exception {

        JSONArray allProducts = OdooProductProduct.getProductProduct();
        JSONArray allQuants = OdooStockQuant.getStockQuant();

        String targetRef = normalize(templateRef);

        Map<Integer, ProductInfo> productMap = buildProductMap(allProducts, templateId, targetRef);

        double totalStock = calculateStock(allQuants, productMap);

        return (int) Math.round(totalStock);
    }

    // MAPA DE PRODUCTOS
    private static Map<Integer, ProductInfo> buildProductMap(JSONArray products, int templateId, String ref) {

        Map<Integer, ProductInfo> map = new HashMap<>();

        for (int i = 0; i < products.length(); i++) {

            JSONObject product = products.getJSONObject(i);

            int productId = product.getInt("id");
            int tmplId = extractId(product.get("product_tmpl_id"));
            String productRef = normalize(product.optString("default_code"));

            if (tmplId == templateId && productRef.equals(ref)) {

                map.put(productId, new ProductInfo(productId, productRef));
            }
        }

        return map;
    }

    // CÁLCULO DE STOCK
    private static double calculateStock(JSONArray quants, Map<Integer, ProductInfo> productMap) {

        double total = 0.0;

        for (int i = 0; i < quants.length(); i++) {

            JSONObject quant = quants.getJSONObject(i);

            int productId = extractId(quant.get("product_id"));

            if (!productMap.containsKey(productId)) {
                continue;
            }

            double qty = quant.optDouble("quantity", 0.0);

            if (qty > 0) {

                ProductInfo info = productMap.get(productId);

                total += qty;

                System.out.println("Stock real encontrado: " + qty + " (Ref: " + info.reference + ")");
            }
        }

        return total;
    }

    // HELPERS
    private static String normalize(String value) {

        if (value == null) {
            return "";
        }

        return value.trim();
    }

    private static int extractId(Object obj) {

        if (obj instanceof JSONArray) {
            return ((JSONArray) obj).getInt(0);
        }

        if (obj instanceof Integer) {
            return (Integer) obj;
        }

        if (obj instanceof Long) {
            return ((Long) obj).intValue();
        }

        return -1;
    }

    // CLASE INTERNA
    private static class ProductInfo {

        final int id;
        final String reference;

        ProductInfo(int id, String reference) {
            this.id = id;
            this.reference = reference;
        }
    }

}
