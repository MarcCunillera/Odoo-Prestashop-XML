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
     * Realitza una petició GET a l'API de PrestaShop per obtenir la
     * representació XML d'un recurs específic.
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
     * Envia una petició POST amb un cos XML per crear un nou recurs a la base
     * de dades de PrestaShop.
     */
    public static String postXml(String resourcePath, String xmlBody) throws Exception {
        String url = BASE_API_URL + resourcePath + "?ws_key=" + API_KEY;

        HttpsURLConnection conn = createInsecureConnection(url);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/xml");
        conn.setRequestProperty("Accept", "application/xml");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(xmlBody.trim().getBytes("UTF-8"));
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
     * Executa una petició PUT per actualitzar les dades d'un recurs existent
     * mitjançant el seu XML.
     */
    public static void putXml(String resourcePath, String xmlBody) throws Exception {
        String url = BASE_API_URL + resourcePath + "?ws_key=" + API_KEY;

        HttpsURLConnection conn = createInsecureConnection(url);
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/xml");
        conn.setRequestProperty("Accept", "application/xml");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(xmlBody.trim().getBytes("UTF-8"));
        }

        int code = conn.getResponseCode();

        if (code < 200 || code >= 300) {
            String errorDetail = "";
            try (InputStream err = conn.getErrorStream()) {
                if (err != null) {
                    errorDetail = ": " + new String(err.readAllBytes(), "UTF-8");
                }
            }
            throw new RuntimeException("Error " + code + " al actualizar recurso" + errorDetail);
        }
    }

    /**
     * Envia una petició DELETE per eliminar un recurs específic de PrestaShop.
     */
    public static void deleteXml(String resourcePath) throws Exception {
        String url = BASE_API_URL + resourcePath + "?ws_key=" + API_KEY;

        HttpsURLConnection conn = createInsecureConnection(url);
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Accept", "application/xml");

        int code = conn.getResponseCode();

        if (code < 200 || code >= 300) {
            throw new RuntimeException("Error al eliminar recurso: " + code);
        }
    }

    /**
     * Cerca un producte a PrestaShop mitjançant la seva referència i en retorna
     * l'identificador numèric.
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
     * Obté l'identificador únic del registre d'estoc (stock_available) associat
     * a un producte concret.
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
     * Processa un XML de resposta per extreure l'ID d'un producte utilitzant un
     * analitzador DOM.
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
        }
        return null;
    }

    /**
     * Processa un XML de resposta per extreure l'ID d'un registre d'estoc
     * disponible.
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
        }
        return null;
    }

    /**
     * Puja una imatge per a un producte específic utilitzant una petició
     * multipart/form-data.
     */
    public static void uploadProductImage(int productId, byte[] imageBytes, boolean isPrimary) throws Exception {
        String urlStr = BASE_API_URL + "/images/products/" + productId + "?ws_key=" + API_KEY;
        String boundary = "---------------------------" + System.currentTimeMillis();

        HttpsURLConnection conn = createInsecureConnection(urlStr);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(("--" + boundary + "\r\n").getBytes());
            os.write(("Content-Disposition: form-data; name=\"image\"; filename=\"product_" + productId + ".jpg\"\r\n").getBytes());
            os.write(("Content-Type: image/jpeg\r\n\r\n").getBytes());
            os.write(imageBytes);
            os.write(("\r\n--" + boundary + "--\r\n").getBytes());
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200 && responseCode != 201) {
            String errorMsg = "";
            try (InputStream err = conn.getErrorStream()) {
                if (err != null) {
                    errorMsg = new String(err.readAllBytes(), "UTF-8");
                }
            }
            throw new RuntimeException("Error al subir imagen: " + responseCode + " - " + errorMsg);
        }
    }

    /**
     * Configura una connexió HTTPS que omet la validació de certificats SSL per
     * permetre connexions en entorns de proves.
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
