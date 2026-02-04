/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
    
    public static JSONArray getStockQuant() throws Exception {

        // Definim els camps que volem obtenir dels productes
        JSONArray fields = new JSONArray();
        fields.put("product_id");
        fields.put("quantity");

        // Construïm l'objecte kwargs amb els camps que volem
        JSONObject kwargs = new JSONObject();
        kwargs.put("fields", fields);

        // Construïm els arguments per a execute_kw
        JSONArray args = new JSONArray();
        args.put(OdooConfig.getODOO_DB_NAME());
        args.put(Integer.parseInt(OdooConfig.getODOO_USER_ID()));
        args.put(OdooConfig.getODOO_PASSWORD());
        args.put("stock.quant");
        args.put("search_read");                          // Mètode per buscar i llegir registres
        args.put(new JSONArray());                        // Sense filtres: agafa tots els productes
        args.put(kwargs);                                 // Indica els camps que volem obtenir

        // Construim els paràmetres del JSON-RPC
        JSONObject params = new JSONObject();
        params.put("service", "object");       // Treballem sobre un model d'Odoo
        params.put("method", "execute_kw");
        params.put("args", args);

        // Construïm el payload final per enviar a Odoo
        JSONObject payload = new JSONObject();
        payload.put("jsonrpc", "2.0"); // Versió del protocol JSON-RPC
        payload.put("method", "call");
        payload.put("params", params);

        // Creem la petició HTTP cap a l'endpoint JSON-RPC d'Odoo
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OdooConfig.getODOO_URL() + "/jsonrpc"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        // Enviem la petició i guardem la resposta
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parsejem la resposta JSON
        JSONObject res = new JSONObject(response.body());

        // Retornem directament l'array amb els productes
        return res.getJSONArray("result");
    }
}
