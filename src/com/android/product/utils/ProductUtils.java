package com.android.product.utils;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.android.product.BaseActivity;
import com.android.product.R;
import com.android.product.db.ProductDbService;
import com.android.product.dome.Product;

public class ProductUtils {

    // make productObj throgh cursor
    public static Product getProductObj(Cursor oneProductCurosr) {
        Product oneProduct = new Product();
        // get details's vauls
        Integer productId = oneProductCurosr.getInt(oneProductCurosr
                .getColumnIndex(Product.dbid));
        Log.i("pid", "pid == " + productId);
        String productName = oneProductCurosr.getString(oneProductCurosr
                .getColumnIndex(Product.dbName));
        Float productPrice = oneProductCurosr.getFloat(oneProductCurosr
                .getColumnIndex(Product.dbPrice));
        String productRemaker = oneProductCurosr.getString(oneProductCurosr
                .getColumnIndex(Product.dbRemaker));
        String productSort = oneProductCurosr.getString(oneProductCurosr
                .getColumnIndex(Product.dbProductSort));
        String productDate = oneProductCurosr.getString(oneProductCurosr
                .getColumnIndex(Product.dbProductDate));
        String productImagePath = oneProductCurosr.getString(oneProductCurosr
                .getColumnIndex(Product.dbProductImageuri));

        // set the vauls for product
        oneProduct.setProductId(productId);
        oneProduct.setProductName(productName);
        oneProduct.setProductPrice(productPrice);
        oneProduct.setProductRemaker(productRemaker);
        oneProduct.setProductSort(productSort);
        oneProduct.setProductDate(productDate);
        oneProduct.setProductImageUri(productImagePath);
        return oneProduct;
    }

    // get the time format.
    public static String getCurrentTimeWithFormat(String currentTimeStr) {
        Date date = new Date(Long.parseLong(currentTimeStr));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String dateString = dateFormat.format(date);
        return dateString;

    }

    // alert exit app.
    public static void alertExit(final Context context) {
        AlertDialog.Builder ab = new AlertDialog.Builder(context);
        ab.setTitle(context.getString(R.string.alertexit));
        ab.setMessage(context.getString(R.string.confirmexit));
        ab.setPositiveButton(context.getString(R.string.exit),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ProductDbService.mExcurtorService.shutdown();
                        ProductDbService.threadHashMap.clear();
                        ProductDbService.poductImageView.clear();
                        ProductDbService.imageHasMapMatchId.clear();
                        ProductDbService.nameSearchList.clear();
                        ProductDbService.allProductName.clear();
                        Set<String> actNames = ProductDbService.activities
                                .keySet();
                        Iterator<String> actNamesIter = actNames.iterator();
                        // menu exit should finsh all.
                        while (actNamesIter.hasNext()) {
                            String nowActName = actNamesIter.next();
                            BaseActivity nowAct = ProductDbService.activities
                                    .get(nowActName);
                            nowAct.finish();
                        }
                        ProductDbService.activities.clear();
                        System.exit(0);
                    }
                });
        ab.setNegativeButton(context.getString(R.string.cancel), null);
        ab.create().show();
    }

    // make bitmap
    // file:///mnt/sdcard/DCIM/ProductImage/PRODUCT_IMG_20120628_225310.png
    public static Bitmap getProductBitmap(Context context,
            String sProductImageUri) {
        Bitmap productBitmap = null;
        File productImageFile = null;
        if (!TextUtils.isEmpty(sProductImageUri)) {
            Log.i("filepath", sProductImageUri.substring("file:///".length()));
            // if user delete the file,so should justment the image file is
            // exist.
            productImageFile = new File(sProductImageUri.substring("file:///"
                    .length()));
        }
        if (!TextUtils.isEmpty(sProductImageUri) && null != productImageFile
                && productImageFile.exists()) {
            Uri productImageUri = Uri.parse(sProductImageUri);

            try {
                // get the product image.
                InputStream productImageInputStream = context
                        .getContentResolver().openInputStream(productImageUri);
                productBitmap = BitmapFactory
                        .decodeStream(productImageInputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // get the default product image.
            productBitmap = BitmapFactory.decodeResource(
                    context.getResources(), R.drawable.portrait);
        }
        return productBitmap;
    }

    // translate the cursor to product list.
    public static ArrayList<Product> cursorToProductList(Cursor productCursor) {
        ArrayList<Product> productList = new ArrayList<Product>();
        while (productCursor.moveToNext()) {
            Product eachProduct = getProductObj(productCursor);
            productList.add(eachProduct);
        }
        return productList;

    }
}
