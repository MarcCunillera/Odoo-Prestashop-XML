package com.mycompany.prestashop.odoo.Prestashop.xml;

import com.mycompany.prestashop.odoo.Prestashop.model.ProductDTO;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class ProductXmlMapper {

    /**
     * Converteix un objecte de transferència de dades (DTO) de producte en un
     * document XML compatible amb l'API de PrestaShop, gestionant preus,
     * taxonomies i jerarquies de categories.
     */
    public static String mapProductToXml(ProductDTO dto) {

        double priceExcl = (dto.getPriceTaxExcluded() != null) ? dto.getPriceTaxExcluded() : 0.0;
        BigDecimal bdExcl = new BigDecimal(priceExcl).setScale(6, RoundingMode.HALF_UP);

        double wholesalePrice = (dto.getWholesalePrice() != null) ? dto.getWholesalePrice() : 0.0;
        String reference = (dto.getReference() != null) ? dto.getReference().trim() : "";

        String slug = dto.getNames()
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        int categoryIdDefault = dto.getCategoryId();

        StringBuilder categoryXml = new StringBuilder();
        if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            for (Integer catId : dto.getCategoryIds()) {
                categoryXml.append("        <category>\n");
                categoryXml.append("          <id>").append(catId).append("</id>\n");
                categoryXml.append("        </category>\n");
            }
        } else {
            categoryXml.append("        <category>\n");
            categoryXml.append("          <id>").append(categoryIdDefault).append("</id>\n");
            categoryXml.append("        </category>\n");
        }

        return String.format(java.util.Locale.US, """
<?xml version="1.0" encoding="UTF-8"?>
<prestashop xmlns:xlink="http://www.w3.org/1999/xlink">
  <product>
    <id_shop_default>1</id_shop_default>
    <id_tax_rules_group>1</id_tax_rules_group>
    <type><![CDATA[simple]]></type>
    <id_category_default>%d</id_category_default>
    <reference><![CDATA[%s]]></reference>
    <active>1</active>
    <available_for_order>1</available_for_order>
    <show_price>1</show_price>
    <price>%s</price>
    <wholesale_price>%.6f</wholesale_price>
    <visibility><![CDATA[both]]></visibility>
    <state>1</state>
    <name>
      <language id="1"><![CDATA[%s]]></language>
    </name>
    <link_rewrite>
      <language id="1"><![CDATA[%s]]></language>
    </link_rewrite>
    <associations>
      <categories>
%s
      </categories>
    </associations>
  </product>
</prestashop>
""".trim(),
                categoryIdDefault,
                escapeXml(reference),
                bdExcl.toString(),
                wholesalePrice,
                escapeXml(dto.getNames()),
                slug,
                categoryXml.toString()
        );
    }

    /**
     * Escapa caràcters reservats de XML per evitar que el document generat
     * sigui invàlid o provoqui errors en ser processat per l'API de destinació.
     */
    private static String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
