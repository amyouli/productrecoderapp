package com.android.product.db;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.android.product.BaseActivity;
import com.android.product.dome.Product;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

public class ProductDbService {

    public ProductDbOpenHelp mProductDbOpenHelp;
    public SQLiteDatabase mSQLiteDatabase;
    public Cursor mProductCursor;
    public static int mProductId;
    public static Context mContext;
    public static ProductDbService mProductDbService;
    public static ExecutorService mExcurtorService;
    // product image map,save by image's hascode.
    public static HashMap<Integer, ImageView> poductImageView = new HashMap<Integer, ImageView>();
    // product id by product image view's hashcode
    public static HashMap<Integer, Integer> imageHasMapMatchId = new HashMap<Integer, Integer>();
    public static HashMap<Integer, ProductDbServiceThread> threadHashMap = new HashMap<Integer, ProductDbServiceThread>();
    public static HashMap<String, BaseActivity> activities = new HashMap<String, BaseActivity>();
    public static ArrayList<String> nameSearchList = new ArrayList<String>();
    public static ArrayList<String> allProductName = new ArrayList<String>();
    private static final File PHOTO_DIR = new File(
            Environment.getExternalStorageDirectory() + "/DCIM/ProductImage");
    public static boolean mIsInstallNotSucByNameUnique = false;
    public static boolean isFirstStart = false;

    // single instance mode.
    private static class InitDbService {
        static final ProductDbService productDbService = new ProductDbService(
                mContext);
    }

    // retern the ProductDbService intance.
    public static ProductDbService getDbServiceInstance(Context context) {
        mContext = context;
        // init the thread pool.
        mExcurtorService = Executors.newFixedThreadPool(5);
        // create image flord.
        if (!PHOTO_DIR.exists()) {
            PHOTO_DIR.mkdirs();
        }
        return InitDbService.productDbService;
    }

    private ProductDbService(Context context) {
        mContext = context;
        mProductDbOpenHelp = new ProductDbOpenHelp(context);
        mSQLiteDatabase = mProductDbOpenHelp.getWritableDatabase();
    }

    // reciver the handle msg to fush the ui
    public static void receiveHandlerMessage(int what, Message msg) {
        switch (what) {
        // update the ui when all search has finished.
        case TaskType.ALLSEARCHCOMMANDINLISTACT:
            ProductDbService.getActivityByName("ProductListActivity").refush(
                    what, msg);
            break;

        // update the ui when name or price search has finished
        case TaskType.NAMEORPRICESEARCHCOMMANDINLISTACT:
            ProductDbService.getActivityByName("ProductListActivity").refush(
                    what, msg);
            break;

        // update the ui when add product has finished
        case TaskType.ADDPRODUCT:
            ProductDbService.getActivityByName("ProductRecorderActivity")
                    .refush(what, msg);
            break;
        // search product by product id
        case TaskType.IDSEARCHCOMMANDINDETAILACT:
            ProductDbService.getActivityByName("ProductDetailActivity").refush(
                    what, msg);
            break;

        // delete product bye id
        case TaskType.DELETEPRODUCT:
            ProductDbService.getActivityByName("ProductListActivity").refush(
                    what, msg);
            break;

        // save the product image
        case TaskType.SAVEPRODUCTIMAGE:
            ProductDbService.getActivityByName("ProductListActivity").refush(
                    what, msg);
            break;

        // set the edit product details in edit page details texts.
        case TaskType.SETPRODUCTDETAILS:
            ProductDbService.getActivityByName("ProductRecorderActivity")
                    .refush(what, msg);
            break;

        // comfim the edited product
        case TaskType.UPDATEPRODUCT:
            ProductDbService.getActivityByName("ProductListActivity").refush(
                    what, msg);
            break;
        case TaskType.SEARCHNAMEINPUTTQUERY:
            int activityType = msg.arg1;
            if (TaskType.ADDACTIVITY == activityType) {
                ProductDbService.getActivityByName("ProductRecorderActivity")
                        .receiveSearchData(what, msg);
            }
            if (TaskType.LISTACTIVITY == activityType) {
                ProductDbService.getActivityByName("ProductListActivity")
                        .receiveSearchData(what, msg);
            }

            break;
        default:
            break;
        }
    }

    public void receiveCommand(int commandType, Object... objects) {
        threadHashMap.put(commandType, new ProductDbServiceThread(commandType,
                objects));
        switch (commandType) {
        // All product list commond
        case TaskType.ALLSEARCHCOMMANDINLISTACT:
            mExcurtorService.execute(threadHashMap
                    .get(TaskType.ALLSEARCHCOMMANDINLISTACT));
            break;
        // search product commond
        case TaskType.NAMEORPRICESEARCHCOMMANDINLISTACT:
            mExcurtorService.execute(threadHashMap
                    .get(TaskType.NAMEORPRICESEARCHCOMMANDINLISTACT));
            break;

        // add product
        case TaskType.ADDPRODUCT:
            mExcurtorService.execute(threadHashMap.get(TaskType.ADDPRODUCT));
            break;

        // in new thread to do search product by id.
        case TaskType.IDSEARCHCOMMANDINDETAILACT:
            mExcurtorService.execute(threadHashMap
                    .get(TaskType.IDSEARCHCOMMANDINDETAILACT));
            break;

        // in new thread to do save product image
        case TaskType.SAVEPRODUCTIMAGE:
            mExcurtorService.execute(threadHashMap
                    .get(TaskType.SAVEPRODUCTIMAGE));
            break;

        // in new thread to do delete product by id
        case TaskType.DELETEPRODUCT:
            mExcurtorService.execute(threadHashMap.get(TaskType.DELETEPRODUCT));
            break;

        // in new thread to do comfim edited product
        case TaskType.UPDATEPRODUCT:
            mExcurtorService.execute(threadHashMap.get(TaskType.UPDATEPRODUCT));
            break;

        // in new thread to do get edit product deatils
        case TaskType.SETPRODUCTDETAILS:
            mExcurtorService.execute(threadHashMap
                    .get(TaskType.SETPRODUCTDETAILS));
            break;
        case TaskType.SEARCHNAMEINPUTTQUERY:
            mExcurtorService.execute(threadHashMap
                    .get(TaskType.SEARCHNAMEINPUTTQUERY));
            break;
        case TaskType.GETALLPRODUCTNAME:
            mExcurtorService.execute(threadHashMap
                    .get(TaskType.GETALLPRODUCTNAME));
            break;
        default:
            break;
        }
    }

    public Cursor getNowCursor() {
        return mProductCursor;
    }

    // insert a new product.
    public long insertProduct(Product product) {

        ContentValues productContentValues = new ContentValues();
        productContentValues.put(Product.dbName, product.getProductName());
        productContentValues.put(Product.dbPrice, product.getProductPrice());
        productContentValues
                .put(Product.dbRemaker, product.getProductRemaker());
        productContentValues.put(Product.dbProductSort,
                product.getProductSort());
        productContentValues.put(Product.dbProductDate,
                product.getProductDate());
        Long insertRowId = mSQLiteDatabase.insert(Product.newRoductDatabaseName, "none",
                productContentValues);
        // mProductDbThread.reciverCommend(TaskType.PRODUCTINSERT, product);
        System.out.println("dbversion====" + mSQLiteDatabase.getVersion());
        return insertRowId;
    }

    // update the edit product
    public void updataProduct(Product mUpdateProduct, long productLongId) {
        // update product set productname="abcdefg",
        // productprice="2323",productremaker="sdfds",
        // productsort="sdfdsff" where _id="8"
        String selection = "update "+Product.newRoductDatabaseName+" set " + Product.dbName + "=" + "\'"
                + mUpdateProduct.getProductName() + "\'" + ","
                + Product.dbPrice + "=" + "\'"
                + mUpdateProduct.getProductPrice() + "\'" + ","
                + Product.dbProductSort + "=" + "\'"
                + mUpdateProduct.getProductSort() + "\'" + ","
                + Product.dbRemaker + "=" + "\'"
                + mUpdateProduct.getProductRemaker() + "\'" + ","
                + Product.dbProductDate + "=" + "\'"
                + mUpdateProduct.getProductDate() + "\'" + " where "
                + Product.dbid + "=" + "\'" + String.valueOf(productLongId)
                + "\'";
        mSQLiteDatabase.execSQL(selection);
    }

    // get all product
    public Cursor findAllProduct() {
        mProductCursor = mSQLiteDatabase.query(Product.newRoductDatabaseName, new String[] { "*" },
                null, null, null, null, Product.dbProductDate + " " + "DESC");
        return mProductCursor;
    }

    // get product by id
    public Cursor findProductById(String id) {
        String selection = Product.dbid + "=" + "\'" + id + "\'";
        mProductCursor = mSQLiteDatabase.query(Product.newRoductDatabaseName, new String[] { "*" },
                selection, null, null, null, null);
        return mProductCursor;
    }

    // get product by name or price
    public Cursor findProductByNameOrPrice(String productName,
            String productPrice) {
        String selection = "";
        if (!TextUtils.isEmpty(productName)) {
            selection = Product.dbName + " " + "like" + " \"%" + productName
                    + "%\"";
        } else {
            selection = Product.dbPrice + " " + "like" + " \"%" + productPrice
                    + "%\"";
        }
        if (!TextUtils.isEmpty(productPrice) && !TextUtils.isEmpty(productName)) {
            selection = Product.dbName + " " + "like" + " \"%" + productName
                    + "%\"" + " AND " + Product.dbPrice + " " + "like" + " \"%"
                    + productPrice + "%\"";
        }
        mProductCursor = mSQLiteDatabase.query(Product.newRoductDatabaseName, new String[] { "*" },
                selection, null, null, null, Product.dbProductDate + " "
                        + "DESC");
        Log.i("cs", mProductCursor.getCount() + "");
        return mProductCursor;

    }

    // delte product by id.
    public int deleteProductByProductId(long productId) {
        /*
         * return mSQLiteDatabase.delete("product", Product.dbid, new String[] {
         * String.valueOf(productId) });
         */
        String deleteProductSql = "delete from "+Product.newRoductDatabaseName+" where _id=" + "\""
                + String.valueOf(productId) + "\"";

        mSQLiteDatabase.execSQL(deleteProductSql);
        return 1;
    }

    public Cursor searchNameInputText(String searchNameInputText) {
        String searchNameInputTextSelect = Product.dbName + " " + "like"
                + " \"%" + searchNameInputText + "%\"";
        mProductCursor = mSQLiteDatabase.query(Product.newRoductDatabaseName, new String[] { "*" },
                searchNameInputTextSelect, null, null, null,
                Product.dbProductDate + " " + "DESC");
        return mProductCursor;
    }

    // save product image
    public void saveProductImage(int prouductId, Bitmap productBitmap,
            File currentFile) {
        // delete the image in camera
        if (null != currentFile && currentFile.exists()) {
            currentFile.delete();
        }
        // save the image by io stream.
        FileOutputStream productFops = null;
        ByteArrayOutputStream baops = null;
        try {
            String productImageName = getPhotoFileName();
            int productImageSize = productBitmap.getWidth()
                    * productBitmap.getHeight() * 4;
            baops = new ByteArrayOutputStream(productImageSize);
            baops.flush();
            File productImageFile = new File(PHOTO_DIR, productImageName);
            productFops = new FileOutputStream(productImageFile);
            productBitmap.compress(Bitmap.CompressFormat.PNG, 100, baops);
            byte[] productImgeByte = baops.toByteArray();
            productFops.write(productImgeByte);
            Uri productUri = Uri.fromFile(productImageFile);
            // update product set imageuri="asdfadsf" where _id="1"
            ContentValues productImageValues = new ContentValues();
            productImageValues.put(Product.dbProductImageuri,
                    productUri.toString());
            /*
             * mSQLiteDatabase.update("product", productImageValues,
             * Product.dbid, new String[] { String.valueOf(prouductId) });
             */
            String updateProductImage = "update "+Product.newRoductDatabaseName+" set "
                    + Product.dbProductImageuri + "=" + "\""
                    + productUri.toString() + "\" " + "where " + Product.dbid
                    + "=" + "\"" + String.valueOf(prouductId) + "\"";
            mSQLiteDatabase.execSQL(updateProductImage);
            Log.i("db", updateProductImage);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != productFops) {
                    productFops.close();
                }
                if (null != baops) {
                    baops.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // delete product image.
    public void deleteProductImage(String productImageUri) {
        if (!TextUtils.isEmpty(productImageUri)) {
            String productImagePath = productImageUri.substring("file:///"
                    .length());
            File productImageFile = new File(productImagePath);
            if (productImageFile.exists()) {
                productImageFile.delete();
            }
        }

    }

    /**
     * Create a file name for the icon photo using current time.
     */
    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "'PRODUCT_IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date) + ".png";
    }

    // 跟据名从Activity集合中找到对应的Activity返回到任务处理方法中去，用于ui的更新：
    public static BaseActivity getActivityByName(String activityName) {
        activityName = "com.android.product." + activityName;
        BaseActivity activity = ProductDbService.activities.get(activityName);

        return activity;
    }
}
