package com.mycompany.prestashop.odoo.Prestashop.xml;

/**
 * @author marccunillera
 */
public class StockXmlMapper {

    /**
     * Genera l'estructura XML necessària per actualitzar l'estoc disponible
     * d'un producte a PrestaShop, vinculant l'identificador de l'inventari, el
     * producte i la quantitat física final.
     */
    public static String mapStock(int stockId, int productId, int quantity) {
        return String.format("""
        <?xml version="1.0" encoding="UTF-8"?>
        <prestashop xmlns:xlink="http://www.w3.org/1999/xlink">
          <stock_available>
            <id>%d</id>
            <id_product>%d</id_product>
            <id_product_attribute>0</id_product_attribute>
            <id_shop>1</id_shop>
            <id_shop_group>0</id_shop_group>
            <quantity>%d</quantity>
            <depends_on_stock>0</depends_on_stock>
            <out_of_stock>2</out_of_stock>
          </stock_available>
        </prestashop>
        """,
                stockId,
                productId,
                quantity
        );
    }
}
