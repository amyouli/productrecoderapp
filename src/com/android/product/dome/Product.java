package com.android.product.dome;

public class Product {
    //DB String
    public static final String dbid = "_id";
    public static final String dbName = "productname";
    public static final String dbPrice = "productprice";
    public static final String dbRemaker = "productremaker";
    public static final String dbProductSort = "productsort";
    public static final String dbProductDate = "productdate";
    public static final String dbProductImageuri = "imageuri";
    public static final String productEditedName = "editedname";
    public static final String productEditedPrice = "editedprice";
    public static final String oldRoductDatabaseName = "product";
    public static final String newRoductDatabaseName = "product_up01";
    //Product Attribute
    public Integer mProductId;
    public String mProductName;
    public Float mProductPrice;
    public String mProductRemaker;
    public String mProductSort;
    public String mProductDate;
    public String mProductImageUri; 

    public String getProductImageUri() {
        return mProductImageUri;
    }

    public void setProductImageUri(String productImageUri) {
        this.mProductImageUri = productImageUri;
    }

    public String getProductDate() {
        return mProductDate;
    }

    public void setProductDate(String productDate) {
        this.mProductDate = productDate;
    }

    public String getProductSort() {
        return mProductSort;
    }

    public void setProductSort(String productSort) {
        this.mProductSort = productSort;
    }

    public Integer getProductId() {
        return mProductId;
    }

    public void setProductId(Integer productId) {
        this.mProductId = productId;
    }

    public String getProductName() {
        return mProductName;
    }

    public void setProductName(String productName) {
        this.mProductName = productName;
    }

    public Float getProductPrice() {
        return mProductPrice;
    }

    public void setProductPrice(Float productPrice) {
        this.mProductPrice = productPrice;
    }

    public String getProductRemaker() {
        return mProductRemaker;
    }

    public void setProductRemaker(String productRemaker) {
        this.mProductRemaker = productRemaker;
    }

}
