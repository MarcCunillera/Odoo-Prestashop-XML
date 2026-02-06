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
 * @author marccunillera
 */
public class OdooStockQuant {

    private static final HttpClient client = HttpClient.newHttpClient();

    /**
     * Consulta l'estat de l'estoc a Odoo mitjançant el model stock.quant,
     * retornant un llistat amb l'identificador del producte i la seva quantitat
     * disponible.
     */
    public static JSONArray getStockQuant() throws Exception {

        JSONArray fields = new JSONArray();
        fields.put("product_id");
        fields.put("quantity");

        JSONObject kwargs = new JSONObject();
        kwargs.put("fields", fields);

        JSONArray args = new JSONArray();
        args.put(OdooConfig.getODOO_DB_NAME());
        args.put(Integer.parseInt(OdooConfig.getODOO_USER_ID()));
        args.put(OdooConfig.getODOO_PASSWORD());
        args.put("stock.quant");
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
