package com.mycompany.prestashop.odoo.Prestashop.xml;

import com.mycompany.prestashop.odoo.Prestashop.model.ProductDTO;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class ProductXmlMapper {

    public static String mapProductToXml(ProductDTO dto) {

        // Cálculo de precios con IVA
        double priceExcl = (dto.getPriceTaxExcluded() != null) ? dto.getPriceTaxExcluded() : 0.0;
        BigDecimal bdExcl = new BigDecimal(priceExcl).setScale(6, RoundingMode.HALF_UP);

        BigDecimal bdIncl = bdExcl.multiply(new BigDecimal("1.21")).setScale(6, RoundingMode.HALF_UP);

        double wholesalePrice = (dto.getWholesalePrice() != null) ? dto.getWholesalePrice() : 0.0;
        String reference = (dto.getReference() != null) ? dto.getReference().trim() : "";

        // Generación del link_rewrite (slug)
        String slug = dto.getNames()
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        int categoryId = dto.getCategoryId();

        return String.format(java.util.Locale.US, """
        <?xml version="1.0" encoding="UTF-8"?>
        <prestashop xmlns:xlink="http://www.w3.org/1999/xlink">
          <product>
            <id_shop_default>1</id_shop_default>
            <id_tax_rules_group>1</id_tax_rules_group>
            <type>simple</type>
            <id_category_default>%d</id_category_default>
            <reference><![CDATA[%s]]></reference>
            <active>1</active>
            <available_for_order>1</available_for_order>
            <show_price>1</show_price>
            <price>%s</price>
            <wholesale_price>%s</wholesale_price>
            <visibility>both</visibility>
            <state>1</state>
            <name>
              <language id="1"><![CDATA[%s]]></language>
            </name>
            <link_rewrite>
              <language id="1"><![CDATA[%s]]></language>
            </link_rewrite>
            <description>
              <language id="1"><![CDATA[]]></language>
            </description>
            <description_short>
              <language id="1"><![CDATA[]]></language>
            </description_short>
            <associations>
              <categories>
                <category>
                  <id>%d</id>
                </category>
              </categories>
            </associations>
          </product>
        </prestashop>
        """,
                categoryId,
                escapeXml(reference),
                bdExcl.toString(),
                wholesalePrice,
                escapeXml(dto.getNames()),
                slug,
                categoryId
        );
    }

    /**
     * Escapa caracteres especiales para XML
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
