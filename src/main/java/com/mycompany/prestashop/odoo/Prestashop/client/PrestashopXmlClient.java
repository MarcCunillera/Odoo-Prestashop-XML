package com.mycompany.prestashop.odoo.Prestashop.client;


import static com.mycompany.prestashop.odoo.Prestashop.config.PrestashopConfig.API_KEY;
import static com.mycompany.prestashop.odoo.Prestashop.config.PrestashopConfig.BASE_API_URL;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;

import java.net.URL;
import java.security.SecureRandom;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.cert.X509Certificate;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.X509TrustManager;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class PrestashopXmlClient {

    /**
     * GET - Obtener XML de un recurso
     */
    public static String getXml(String resourcePath) throws Exception {
        String url = BASE_API_URL + resourcePath + "?ws_key=" + API_KEY;

        HttpsURLConnection conn = createInsecureConnection(url);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/xml");

        try (InputStream is = conn.getInputStream()) {
            return new String(is.readAllBytes(), "UTF-8");
        }
    }

    /**
     * POST - Crear un nuevo recurso
     */
    public static String postXml(String resourcePath, String xmlBody) throws Exception {
        String url = BASE_API_URL + resourcePath + "?ws_key=" + API_KEY;

        HttpsURLConnection conn = createInsecureConnection(url);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/xml");
        conn.setRequestProperty("Accept", "application/xml");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(xmlBody.getBytes("UTF-8"));
        }

        int code = conn.getResponseCode();

        if (code >= 200 && code < 300) {
            try (InputStream is = conn.getInputStream()) {
                return new String(is.readAllBytes(), "UTF-8");
            }
        } else {
            String errorMsg = "Error " + code + " al crear recurso";
            try (InputStream err = conn.getErrorStream()) {
                if (err != null) {
                    errorMsg += ": " + new String(err.readAllBytes(), "UTF-8");
                }
            }
            throw new RuntimeException(errorMsg);
        }
    }

    /**
     * PUT - Actualizar un recurso existente
     */
    public static void putXml(String resourcePath, String xmlBody) throws Exception {
        String url = BASE_API_URL + resourcePath + "?ws_key=" + API_KEY;

        HttpsURLConnection conn = createInsecureConnection(url);
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/xml");
        conn.setRequestProperty("Accept", "application/xml");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(xmlBody.getBytes("UTF-8"));
        }

        int code = conn.getResponseCode();

        if (code >= 200 && code < 300) {
            System.out.println("✓ Recurso actualizado correctamente");
        } else {
            System.out.println("✗ Error al actualizar: " + code);
            try (InputStream err = conn.getErrorStream()) {
                if (err != null) {
                    System.out.println(new String(err.readAllBytes(), "UTF-8"));
                }
            }
            throw new RuntimeException("Error al actualizar recurso: " + code);
        }
    }

    /**
     * DELETE - Eliminar un recurso
     */
    public static void deleteXml(String resourcePath) throws Exception {
        String url = BASE_API_URL + resourcePath + "?ws_key=" + API_KEY;

        HttpsURLConnection conn = createInsecureConnection(url);
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Accept", "application/xml");

        int code = conn.getResponseCode();

        if (code >= 200 && code < 300) {
            System.out.println("✓ Recurso eliminado correctamente");
        } else {
            System.out.println("✗ Error al eliminar: " + code);
            throw new RuntimeException("Error al eliminar recurso: " + code);
        }
    }

    /**
     * Buscar productos por referencia o nombre
     */
    public static Integer findProductByReference(String reference) throws Exception {
        String filter = "?filter[reference]=[" + reference + "]&display=[id]&ws_key=" + API_KEY;
        String url = BASE_API_URL + "/products" + filter;

        HttpsURLConnection conn = createInsecureConnection(url);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/xml");

        try (InputStream is = conn.getInputStream()) {
            String xml = new String(is.readAllBytes(), "UTF-8");
            return extractProductId(xml);
        }
    }

    /**
     * Obtener el stock_available ID de un producto
     */
    public static Integer getStockAvailableId(int productId) throws Exception {
        String filter = "?filter[id_product]=[" + productId + "]&display=full&ws_key=" + API_KEY;
        String url = BASE_API_URL + "/stock_availables" + filter;

        HttpsURLConnection conn = createInsecureConnection(url);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/xml");

        try (InputStream is = conn.getInputStream()) {
            String xml = new String(is.readAllBytes(), "UTF-8");
            return extractStockId(xml);
        }
    }

    /**
     * Extraer el ID del producto desde XML
     */
    private static Integer extractProductId(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));

            NodeList products = doc.getElementsByTagName("product");
            if (products.getLength() > 0) {
                NodeList idNodes = doc.getElementsByTagName("id");
                if (idNodes.getLength() > 0) {
                    return Integer.parseInt(idNodes.item(0).getTextContent().trim());
                }
            }
        } catch (Exception e) {
            System.out.println("Error al parsear XML de producto: " + e.getMessage());
        }
        return null;
    }

    /**
     * Extraer el ID del stock desde XML
     */
    private static Integer extractStockId(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));

            NodeList stocks = doc.getElementsByTagName("stock_available");
            if (stocks.getLength() > 0) {
                NodeList idNodes = doc.getElementsByTagName("id");
                if (idNodes.getLength() > 0) {
                    return Integer.parseInt(idNodes.item(0).getTextContent().trim());
                }
            }
        } catch (Exception e) {
            System.out.println("Error al parsear XML de stock: " + e.getMessage());
        }
        return null;
    }

    /**
     * Crear conexión HTTPS sin verificación de certificado (solo para
     * desarrollo)
     */
    private static HttpsURLConnection createInsecureConnection(String urlStr) throws Exception {
        TrustManager[] trustAll = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAll, new SecureRandom());

        URL url = new URL(urlStr);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

        conn.setSSLSocketFactory(sc.getSocketFactory());
        conn.setHostnameVerifier((h, s) -> true);

        return conn;
    }
}
