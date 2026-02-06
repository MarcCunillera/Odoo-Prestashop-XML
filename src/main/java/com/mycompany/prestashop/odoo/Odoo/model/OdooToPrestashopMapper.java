/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.prestashop.odoo.Odoo.model;

import com.mycompany.prestashop.odoo.Prestashop.model.ProductDTO;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author marccunillera
 */
public class OdooToPrestashopMapper {

    /**
     * Transforma un objecte JSON procedent d'Odoo en un objecte ProductDTO
     * compatible amb PrestaShop, assignant referències, preus, imatges en
     * Base64 i calculant la jerarquia de categories.
     */
    public static ProductDTO map(JSONObject odooProduct, int stockQuantity) {
        ProductDTO dto = new ProductDTO();

        dto.setReference(odooProduct.optString("default_code", ""));
        dto.setNames(odooProduct.optString("name", ""));

        Object imgObj = odooProduct.get("image_1920");
        if (imgObj instanceof String) {
            dto.setImage1920((String) imgObj);
        } else {
            dto.setImage1920(null);
        }

        dto.setPriceTaxExcluded(odooProduct.optDouble("list_price", 0.0));
        dto.setWholesalePrice(odooProduct.optDouble("standard_price", 0.0));
        dto.setEnabled(true);

        int odooCatId = extractCategoryId(odooProduct);

        List<Integer> fullPath = CategoryMappingService.getFullCategoryPath(odooCatId);
        dto.setCategoryIds(fullPath);

        if (!fullPath.isEmpty()) {
            dto.setCategoryId(fullPath.get(fullPath.size() - 1));
        } else {
            dto.setCategoryId(2);
        }

        dto.setQuantity(stockQuantity);

        return dto;
    }

    /**
     * Extrau l'identificador numèric de la categoria d'un producte d'Odoo,
     * gestionant el format de matriu [id, nom] que retorna l'API.
     */
    private static int extractCategoryId(JSONObject product) {

        Object obj = product.opt("categ_id");

        if (obj instanceof JSONArray) {
            return ((JSONArray) obj).getInt(0);
        }

        return -1;
    }
}
