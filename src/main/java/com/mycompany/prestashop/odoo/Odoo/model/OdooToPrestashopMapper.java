/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.prestashop.odoo.Odoo.model;

import com.mycompany.prestashop.odoo.Prestashop.model.ProductDTO;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author marccunillera
 */
public class OdooToPrestashopMapper {

    public static ProductDTO map(JSONObject odooProduct, int stockQuantity) {
        ProductDTO dto = new ProductDTO();

        // Referencia
        dto.setReference(odooProduct.optString("default_code", ""));

        // Nombre
        dto.setNames(odooProduct.optString("name", ""));

        // Precio de venta
        dto.setPriceTaxExcluded(odooProduct.optDouble("list_price", 0.0));

        // Precio de compra 
        dto.setWholesalePrice(odooProduct.optDouble("standard_price", 0.0));

        // Estado activado
        dto.setEnabled(true);

        int odooCatId = extractCategoryId(odooProduct);

        int psCatId = CategoryMappingService.getPrestashopCategory(odooCatId);

        dto.setCategoryId(psCatId);

        System.out.println("Categoría Odoo ID: " + odooCatId + " -> PS: " + psCatId);

        // Stock del producto
        dto.setQuantity(stockQuantity);

        return dto;
    }

    private static int extractCategoryId(JSONObject product) {

        Object obj = product.opt("categ_id");

        if (obj instanceof JSONArray) {
            return ((JSONArray) obj).getInt(0);
        }

        return -1;
    }
}
