package com.mycompany.prestashop.odoo.Odoo.model;

import com.mycompany.prestashop.odoo.Odoo.client.OdooProductCategoryMapping;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author marccunillera
 */
public class CategoryMappingService {

    private static final int HOME_CATEGORY_ID = 2;

    /**
     * Construeix el camí jeràrquic complet de categories per a PrestaShop a
     * partir d'una categoria d'Odoo, recorrent l'arbre des de la categoria
     * filla fins a l'arrel i invertint el resultat final.
     */
    public static List<Integer> getFullCategoryPath(int odooLeafCategoryId) {
        List<Integer> path = new ArrayList<>();

        try {
            JSONArray mappings = OdooProductCategoryMapping.getProductCategoryMapping();

            int currentOdooId = odooLeafCategoryId;
            boolean keepSearching = true;

            while (keepSearching) {
                JSONObject currentMapping = findMappingByOdooId(mappings, currentOdooId);

                if (currentMapping != null) {
                    int prestaId = currentMapping.getInt("id_categ_presta");
                    if (!path.contains(prestaId)) {
                        path.add(prestaId);
                    }

                    Object parentObj = currentMapping.opt("parent_categ_odoo");

                    if (parentObj instanceof JSONArray) {
                        currentOdooId = ((JSONArray) parentObj).getInt(0);
                    } else {
                        keepSearching = false;
                    }
                } else {
                    keepSearching = false;
                }

                if (path.size() > 10) {
                    keepSearching = false;
                }
            }

        } catch (Exception e) {
            System.err.println("Error construyendo árbol de categorías: " + e.getMessage());
        }

        Collections.reverse(path);

        if (!path.contains(HOME_CATEGORY_ID)) {
            path.add(0, HOME_CATEGORY_ID);
        } else {
            if (path.get(0) != HOME_CATEGORY_ID) {
                path.remove((Integer) HOME_CATEGORY_ID);
                path.add(0, HOME_CATEGORY_ID);
            }
        }

        return path;
    }

    /**
     * Cerca un mapeig específic dins del llistat de categories utilitzant
     * l'identificador d'Odoo, gestionant si l'ID arriba com un valor numèric o
     * dins d'un array.
     */
    private static JSONObject findMappingByOdooId(JSONArray mappings, int odooId) {
        for (int i = 0; i < mappings.length(); i++) {
            JSONObject obj = mappings.getJSONObject(i);

            int mappedOdooId = -1;
            Object idVal = obj.get("id_categ_odoo");

            if (idVal instanceof JSONArray) {
                mappedOdooId = ((JSONArray) idVal).getInt(0);
            } else if (idVal instanceof Number) {
                mappedOdooId = ((Number) idVal).intValue();
            }

            if (mappedOdooId == odooId) {
                return obj;
            }
        }
        return null;
    }
}
