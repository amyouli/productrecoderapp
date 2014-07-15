package com.android.product.db;

import com.android.product.dome.Product;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ProductDbOpenHelp extends SQLiteOpenHelper {

    private final String createTableSql = "CREATE TABLE "+Product.newRoductDatabaseName+" ("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT," + "productname TEXT,"
            + "productprice decimal(18,2)," + "productremaker TEXT,"
            + "productsort TEXT," + "productdate TEXT," + "imageuri TEXT"
            + ");";

    //private final String renameTable = "alter table product rename to temp_db";
    private final String copyDateFromOldTable = "insert into "+Product.newRoductDatabaseName+" select * from "+Product.oldRoductDatabaseName;
    private final String dropOldDataBaseSql = "DROP TABLE "+Product.oldRoductDatabaseName;

    public ProductDbOpenHelp(Context context) {
        super(context, Product.newRoductDatabaseName+".db", null, 10);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createTableSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.equals(createTableSql);
        db.execSQL(copyDateFromOldTable);
        db.execSQL(dropOldDataBaseSql);
    }
}
