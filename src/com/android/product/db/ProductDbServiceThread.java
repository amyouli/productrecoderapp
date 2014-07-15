package com.android.product.db;

import java.io.File;

import com.android.product.dome.Product;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

public class ProductDbServiceThread implements Runnable {

    private int mCommandType;
    private static ProductDbService mProductDbService;
    public Message mMsg;
    public Object[] mParms;
    public Product mProduct;
    public String mProductId;
    public String mSearchNameInputText;
    public long mProductLongId;
    public Bitmap mProductImage;
    public int affectProducts;
    public Product mUpdateProduct;
    public File mCurrentFile;
    public int mActityType;

    // init ProductDbService in product db service
    public static void initDbService(ProductDbService ProductDbService) {
        mProductDbService = ProductDbService;
    }
 
    public ProductDbServiceThread(int commandType, Object... parms) {
        this.mCommandType = commandType;
        // search parms, parms[0] is product name, parms[1] is product price
        switch (commandType) {

        // get name and price
        case TaskType.NAMEORPRICESEARCHCOMMANDINLISTACT:
            mParms = new Object[10];
            mParms[0] = parms[0];
            mParms[1] = parms[1];
            break;

        // get product object
        case TaskType.ADDPRODUCT:
            mProduct = (Product) parms[0];
            break;

        // get product by id
        case TaskType.IDSEARCHCOMMANDINDETAILACT:
            mProductId = (String) parms[0];
            break;

        case TaskType.SAVEPRODUCTIMAGE:
            mProductId = (String) parms[0];
            mProductImage = (Bitmap) parms[1];
            mCurrentFile = (File) parms[2];
            break;

        case TaskType.DELETEPRODUCT:
            mProductLongId = (Long) parms[0];
            break;

        case TaskType.UPDATEPRODUCT:
            mUpdateProduct = (Product) parms[0];
            mProductLongId = (Long) parms[1];
            break;

        case TaskType.SETPRODUCTDETAILS:
            mProductId = String.valueOf((Long) parms[0]);
            break;
        case TaskType.SEARCHNAMEINPUTTQUERY:
            mSearchNameInputText = String.valueOf(parms[0]);
            mActityType = (Integer) parms[1];
            break;
        case TaskType.GETALLPRODUCTNAME:
            
        default:
            break;
        }

    }

    Handler productHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            try {
                int productCount = ((Cursor) (msg.obj)).getCount();
                // if no product match,so return -1;
                if (!(productCount > 0)) {
                    msg.arg1 = TaskType.NOPRODUCTFIND;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            ProductDbService.receiveHandlerMessage(what, msg);
        }

    };

    @Override
    public void run() {
        Cursor mProductCursor;
        switch (mCommandType) {
        // run all product search
        case TaskType.ALLSEARCHCOMMANDINLISTACT:
            mProductCursor = mProductDbService.findAllProduct();
            mMsg = productHandler.obtainMessage();
            mMsg.what = TaskType.ALLSEARCHCOMMANDINLISTACT;
            // obj is cursor
            mMsg.obj = mProductCursor;
            productHandler.sendMessage(mMsg);
            System.out.println("allproduct=="
                    + Thread.currentThread().getName());
            break;

        // run name or price search
        case TaskType.NAMEORPRICESEARCHCOMMANDINLISTACT:
            mProductCursor = mProductDbService.findProductByNameOrPrice(
                    mParms[0].toString(), (String) mParms[1].toString());
            mMsg = productHandler.obtainMessage();
            mMsg.what = TaskType.ALLSEARCHCOMMANDINLISTACT;
            // obj is cursor
            mMsg.obj = mProductCursor;
            productHandler.sendMessage(mMsg);
            System.out.println("nameorpricesearch=="
                    + Thread.currentThread().getName());
            break;

        // run add product.
        case TaskType.ADDPRODUCT:
            long insertRowId = mProductDbService.insertProduct(mProduct);
            mProductCursor = mProductDbService.findAllProduct();
            mMsg = productHandler.obtainMessage();
            mMsg.what = TaskType.ADDPRODUCT;

            // object is insert id
            // mMsg.obj = mProductCursor;
            mMsg.arg1 = Integer.parseInt(String.valueOf(insertRowId));
            productHandler.sendMessage(mMsg);
            updateProductNameAfterProductChanged();
            System.out.println("add==" + Thread.currentThread().getName());
            break;

        // run search product by id
        case TaskType.IDSEARCHCOMMANDINDETAILACT:
            mProductCursor = mProductDbService.findProductById(mProductId);
            mMsg = productHandler.obtainMessage();
            mMsg.what = TaskType.IDSEARCHCOMMANDINDETAILACT;

            // object is insert id
            mMsg.obj = mProductCursor;
            productHandler.sendMessage(mMsg);
            System.out.println("details==" + Thread.currentThread().getName());
            break;

        case TaskType.SAVEPRODUCTIMAGE:
            // delete the product image if change the product image.
            mProductCursor = mProductDbService.findProductById(mProductId);
            mProductCursor.moveToNext();
            String productImageUriIfExist = mProductCursor.getString(mProductCursor
                    .getColumnIndex(Product.dbProductImageuri));
            mProductDbService.deleteProductImage(productImageUriIfExist);

            
            // save the product image
            mProductDbService.saveProductImage(Integer.valueOf(mProductId),
                    mProductImage, mCurrentFile); 
            mMsg = productHandler.obtainMessage();
            mMsg.what = TaskType.SAVEPRODUCTIMAGE;
            productHandler.sendMessage(mMsg);
            System.out.println("saveimage==" + Thread.currentThread().getName());
            break;

        case TaskType.DELETEPRODUCT:
            // delete the image
            mProductCursor = mProductDbService.findProductById(String
                    .valueOf(mProductLongId));
            mProductCursor.moveToNext();
            String productImageUri = mProductCursor.getString(mProductCursor
                    .getColumnIndex(Product.dbProductImageuri));
            mProductDbService.deleteProductImage(productImageUri);

            // go to list view after delete the product.
            affectProducts = mProductDbService
                    .deleteProductByProductId(mProductLongId);
            mMsg = productHandler.obtainMessage();
            mMsg.what = TaskType.DELETEPRODUCT;
            mMsg.obj = affectProducts;
            productHandler.sendMessage(mMsg);
            updateProductNameAfterProductChanged();
            
            System.out.println("delete==" + Thread.currentThread().getName());
            break;

        case TaskType.SETPRODUCTDETAILS:
            mProductCursor = mProductDbService.findProductById(mProductId);
            mMsg = productHandler.obtainMessage();
            mMsg.what = TaskType.SETPRODUCTDETAILS;
            mMsg.obj = mProductCursor;
            productHandler.sendMessage(mMsg);
            System.out.println("setdailsupdate==" + Thread.currentThread().getName());
            break;

        case TaskType.UPDATEPRODUCT:
            mProductDbService.updataProduct(mUpdateProduct, mProductLongId);
            mMsg = productHandler.obtainMessage();
            mMsg.what = TaskType.UPDATEPRODUCT;
            productHandler.sendMessage(mMsg);
            updateProductNameAfterProductChanged();
            System.out.println("update==" + Thread.currentThread().getName());
            break;
        case TaskType.SEARCHNAMEINPUTTQUERY:
            ProductDbService.nameSearchList.clear();
            mProductCursor = mProductDbService.searchNameInputText(mSearchNameInputText);
            mMsg = productHandler.obtainMessage();
            mMsg.what = TaskType.SEARCHNAMEINPUTTQUERY;
            mMsg.obj = mProductCursor;
            mMsg.arg1 = mActityType;
            productHandler.sendMessage(mMsg);
            System.out.println("SEARCHNAMEINPUTTQUERY==" + Thread.currentThread().getName());
            break;
        case TaskType.GETALLPRODUCTNAME:
            mProductCursor = mProductDbService.findAllProduct();
            while (mProductCursor.moveToNext()) {
                String productName = mProductCursor.getString(mProductCursor
                        .getColumnIndex(Product.dbName));
                ProductDbService.allProductName.add(productName);
            }
            break;
        default:
            break;
        }
    }

    // update the all product name after product changed.
    private void updateProductNameAfterProductChanged() {
        Cursor mProductCursor;
        ProductDbService.allProductName.clear();
        mProductCursor = mProductDbService.findAllProduct();
        while (mProductCursor.moveToNext()) {
            String productName = mProductCursor.getString(mProductCursor
                    .getColumnIndex(Product.dbName));
            ProductDbService.allProductName.add(productName);
        }
    }

}
