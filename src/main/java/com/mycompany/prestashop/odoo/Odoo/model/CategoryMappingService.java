/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.prestashop.odoo.Odoo.model;

import com.mycompany.prestashop.odoo.Odoo.client.OdooProductCategoryMapping;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author marccunillera
 */
public class CategoryMappingService {

    private static final int DEFAULT_CAT = 2;

    public static int getPrestashopCategory(int odooCategoryId) {

        try {
            JSONArray mappings = OdooProductCategoryMapping.getProductCategoryMapping();

            for (int i = 0; i < mappings.length(); i++) {

                JSONObject obj = mappings.getJSONObject(i);

                JSONArray odooArray = obj.getJSONArray("id_categ_odoo");
                int odooId = odooArray.getInt(0);

                int prestaId = obj.getInt("id_categ_presta");

                if (odooId == odooCategoryId) {
                    return prestaId;
                }
            }

        } catch (Exception e) {

            System.err.println("Error leyendo mapping categorías");
            e.printStackTrace();
        }

        return DEFAULT_CAT;
    }
}
