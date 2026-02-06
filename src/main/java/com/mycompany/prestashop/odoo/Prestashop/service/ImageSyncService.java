package com.mycompany.prestashop.odoo.Prestashop.service;

import com.mycompany.prestashop.odoo.Prestashop.client.PrestashopXmlClient;
import com.mycompany.prestashop.odoo.Prestashop.model.ProductDTO;
import java.util.logging.Logger;

/**
 *
 * @author marccunillera
 */
public class ImageSyncService {

    private static final Logger LOGGER = Logger.getLogger(ImageSyncService.class.getName());

    /**
     * Coordina la sincronització de la imatge principal i les imatges
     * addicionals d'un producte cap a PrestaShop, verificant que les dades no
     * estiguin buides abans de processar-les.
     */
    public static void syncProductImage(int productId, ProductDTO product) {
        if (product == null) {
            return;
        }

        if (product.getImage1920() != null && !product.getImage1920().isEmpty()) {
            uploadImage(productId, product.getImage1920(), true);
        }

        if (product.getAdditionalImages() != null) {
            for (String imageBase64 : product.getAdditionalImages()) {
                if (imageBase64 != null && !imageBase64.isEmpty()) {
                    uploadImage(productId, imageBase64, false);
                }
            }
        }
    }

    /**
     * Decodifica la imatge en format Base64, valida la seva mida i integritat,
     * i crida al client XML per realitzar la pujada efectiva al servidor de
     * PrestaShop.
     */
    private static void uploadImage(int productId, String base64Image, boolean isPrimary) {
        try {
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Image);

            if (imageBytes.length == 0) {
                LOGGER.warning("Imagen vacía para producto " + productId);
                return;
            }

            if (imageBytes.length > 10 * 1024 * 1024) {
                LOGGER.warning("Imagen muy grande (" + imageBytes.length + " bytes) para producto " + productId);
                return;
            }

            PrestashopXmlClient.uploadProductImage(productId, imageBytes, isPrimary);

        } catch (IllegalArgumentException e) {
            LOGGER.warning("Base64 inválido para producto " + productId + ": " + e.getMessage());
        } catch (Exception e) {
            LOGGER.severe("Error subiendo imagen: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
