package com.mycompany.prestashop.odoo.Prestashop.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author marccunillera
 */
public class ProductDTO {

    private int productId;
    private String names;
    private Double priceTaxExcluded;
    private Double wholesalePrice;
    private int categoryId;
    private boolean enabled;
    private String reference;
    private int quantity;
    private String image1920;

    private List<Integer> categoryIds = new ArrayList<>();

    private List<String> additionalImages = new ArrayList<>();

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getNames() {
        return names;
    }

    public void setNames(String names) {
        this.names = names;
    }

    public Double getPriceTaxExcluded() {
        return priceTaxExcluded;
    }

    public String getImage1920() {
        return image1920;
    }

    public void setImage1920(String image1920) {
        this.image1920 = image1920;
    }

    public void setPriceTaxExcluded(Double priceTaxExcluded) {
        this.priceTaxExcluded = priceTaxExcluded;
    }

    public Double getWholesalePrice() {
        return wholesalePrice;
    }

    public void setWholesalePrice(Double wholesalePrice) {
        this.wholesalePrice = wholesalePrice;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public List<String> getAdditionalImages() {
        return additionalImages;
    }

    public void setAdditionalImages(List<String> additionalImages) {
        this.additionalImages = additionalImages;
    }

    public List<Integer> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<Integer> categoryIds) {
        this.categoryIds = categoryIds;
    }
}
