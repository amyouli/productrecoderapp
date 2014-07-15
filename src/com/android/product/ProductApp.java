package com.android.product;

import com.android.product.db.ProductDbService;
import com.android.product.db.ProductDbServiceThread;
import com.android.product.db.TaskType;

import android.app.Application;

public class ProductApp extends Application {

    public ProductDbService mProductDbService;

    @Override
    public void onCreate() {
        super.onCreate();
        // init the object.
        ProductDbServiceThread.initDbService(ProductDbService
                .getDbServiceInstance(getApplicationContext()));
        mProductDbService = ProductDbService
                .getDbServiceInstance(getApplicationContext());
        mProductDbService.receiveCommand(TaskType.GETALLPRODUCTNAME);
        // first start flag
        ProductDbService.isFirstStart = true;
    }

}
