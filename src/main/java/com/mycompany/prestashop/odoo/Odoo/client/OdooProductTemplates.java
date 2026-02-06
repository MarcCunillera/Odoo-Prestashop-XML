package com.mycompany.prestashop.odoo.Odoo.client;

import com.mycompany.prestashop.odoo.Odoo.config.OdooConfig;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Marc Cunillera
 */
public class OdooProductTemplates {

    private static final HttpClient client = HttpClient.newHttpClient();

    /**
     * Obté la llista de plantilles de productes d'Odoo mitjançant una crida
     * JSON-RPC, recuperant camps específics com el nom, preu i imatge.
     */
    public static JSONArray getProductTemplate() throws Exception {

        JSONArray fields = new JSONArray();
        fields.put("id");
        fields.put("name");
        fields.put("list_price");
        fields.put("standard_price");
        fields.put("categ_id");
        fields.put("default_code");
        fields.put("image_1920");

        JSONObject kwargs = new JSONObject();
        kwargs.put("fields", fields);

        JSONArray args = new JSONArray();
        args.put(OdooConfig.getODOO_DB_NAME());
        args.put(Integer.parseInt(OdooConfig.getODOO_USER_ID()));
        args.put(OdooConfig.getODOO_PASSWORD());
        args.put("product.template");
        args.put("search_read");
        args.put(new JSONArray());
        args.put(kwargs);

        JSONObject params = new JSONObject();
        params.put("service", "object");
        params.put("method", "execute_kw");
        params.put("args", args);

        JSONObject payload = new JSONObject();
        payload.put("jsonrpc", "2.0");
        payload.put("method", "call");
        payload.put("params", params);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OdooConfig.getODOO_URL() + "/jsonrpc"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject res = new JSONObject(response.body());

        return res.getJSONArray("result");
    }
}
