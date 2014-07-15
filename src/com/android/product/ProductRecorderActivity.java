package com.android.product;

import com.android.product.db.ProductDbService;
import com.android.product.db.TaskType;
import com.android.product.dome.Product;
import com.android.product.utils.ProductUtils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ProductRecorderActivity extends BaseActivity implements
        OnClickListener {
    public static final String TAG = "ProductRecorderActivity";
    public EditText mProductName;
    public EditText mProductPrice;
    public EditText mProductRemaker;
    public EditText mProductSort;
    public Button mOkBut;
    public Button mResetBut;
    public Button mButAll;
    public Button mSeacherBut;
    public TextView uiTopTextView;
    public Context mContext;
    public String mActionType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ProductDbService.activities.put(this.getClass().getName(), this);
        // if first start, show all product.
        if (ProductDbService.isFirstStart) {
            Intent listAllIntent = new Intent("android.intent.product.list");
            listAllIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            listAllIntent.putExtra(TaskType.LISTTYPE, TaskType.LISTALL);
            startActivity(listAllIntent);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addproduct);
        mContext = getApplicationContext();
        Log.i("instance", mProductDbService.toString());
        initUi();
        // update the product
        mActionType = getIntent().getStringExtra(TaskType.ACTIONTYPE);
        if (TaskType.ACTIONUPDATE.equals(mActionType)) {
            Long productId = getIntent().getLongExtra(Product.dbid, -1);
            mProductDbService.receiveCommand(TaskType.SETPRODUCTDETAILS,
                    productId);
        }
    }

    @Override
    protected void onResume() {
        mActityType = TaskType.ADDACTIVITY;
        super.onResume();
    }

    private void initUi() {
        mProductName = (EditText) findViewById(R.id.productnameET);
        mProductPrice = (EditText) findViewById(R.id.productpriceET);
        mProductRemaker = (EditText) findViewById(R.id.productremakeET);
        mProductSort = (EditText) findViewById(R.id.productsortET);
        mSeacherBut = (Button) findViewById(R.id.title_bt_right);
        uiTopTextView = (TextView) findViewById(R.id.uitoptitle);
        mOkBut = (Button) findViewById(R.id.ok);
        mButAll = (Button) findViewById(R.id.title_bt_left);
        mButAll.setOnClickListener(this);
        mButAll.setText(R.string.productall);
        mOkBut.setBackgroundResource(R.drawable.button_ok);
        mResetBut = (Button) findViewById(R.id.reset);
        mResetBut.setBackgroundResource(R.drawable.button_reset);
        uiTopTextView.setText(R.string.addproduct);
        mSeacherBut.setBackgroundResource(R.drawable.search);
        mOkBut.setOnClickListener(this);
        mProductName.setOnClickListener(this);
        mProductPrice.setOnClickListener(this);
        mProductRemaker.setOnClickListener(this);
        mSeacherBut.setOnClickListener(this);
        mResetBut.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.productnameET:

            break;
        case R.id.productpriceET:
            break;
        case R.id.productremakeET:
            break;
        case R.id.productsortET:
            break;
        case R.id.ok:
            String productName = mProductName.getText().toString();
            String productPriceStr = mProductPrice.getText().toString();
            String productRemaker = mProductRemaker.getText().toString();
            String productSort = mProductSort.getText().toString();
            Float productPrice = 0F;
            if (!TextUtils.isEmpty(productPriceStr)) {
                productPrice = Float.parseFloat(productPriceStr);
            }
            boolean canAddProduct = canAddProduct(productName, productPriceStr);
            if (canAddProduct) {
                Product product = new Product();
                product.setProductName(productName);
                product.setProductPrice(productPrice);
                product.setProductRemaker(productRemaker);
                product.setProductSort(productSort);
                product.setProductDate(String.valueOf(System
                        .currentTimeMillis()));
                // jugement the product is unique.
                boolean isProductNameUnique = false;
                for (int i = 0; i < ProductDbService.allProductName.size(); i++) {
                    String nowProductName = ProductDbService.allProductName
                            .get(i);
                    // if not add actionupdate so update is wrong,and add name
                    // unique is wrong.
                    if (!TaskType.ACTIONUPDATE.equals(mActionType)
                            && canAddProduct(productName, productPriceStr)
                            && productName.equals(nowProductName)) {
                        isProductNameUnique = true;
                        break;
                    }
                }
                // if the product is exist so return.
                if (isProductNameUnique) {
                    Toast.makeText(mContext,
                            mContext.getString(R.string.productisexist),
                            Toast.LENGTH_LONG).show();
                    // reset the unique status
                    isProductNameUnique = false;
                    // init();
                    return;
                }

                // save the edited product.
                if (TaskType.ACTIONUPDATE.equals(mActionType)) {
                    // add button run update product or add new product.
                    Long productId = getIntent().getLongExtra(Product.dbid, -1);
                    mProductDbService.receiveCommand(TaskType.UPDATEPRODUCT,
                            product, productId);
                    Intent listIntentFormAdd = new Intent(
                            "android.intent.product.list");
                    // get the date form list view.
                    listIntentFormAdd.putExtra(TaskType.LISTTYPE, getIntent()
                            .getStringExtra(TaskType.LISTTYPE));
                    listIntentFormAdd.putExtra(Product.dbName, getIntent()
                            .getStringExtra(Product.dbName));
                    listIntentFormAdd.putExtra(Product.dbPrice, getIntent()
                            .getStringExtra(Product.dbPrice));
                    listIntentFormAdd.putExtra(Product.productEditedName,
                            product.getProductName());
                    listIntentFormAdd.putExtra(Product.productEditedName,
                            product.getProductPrice());
                    listIntentFormAdd.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(listIntentFormAdd);

                    // when eidted completed,clean the edit product date and
                    // action type.
                    init();
                    mActionType = TaskType.ADDPRODUCT + "";
                    // reset the unique status
                    isProductNameUnique = false;
                    // finish();
                } else {
                    // add product.
                    mProductDbService.receiveCommand(TaskType.ADDPRODUCT,
                            product);
                }
            } else {
                Bundle dialogInfo = new Bundle();
                dialogInfo.putString(TaskType.DIAINFO,
                        getString(R.string.plinput)
                                + getString(R.string.str_name)
                                + getString(R.string.or)
                                + getString(R.string.strprice));
                showDialog(TaskType.DIALOGMUSTINPUT, dialogInfo);
                // clean all date and focus
                init();
            }
            break;
        case R.id.reset:
            // clean all date and focus
            init();
            break;
        case R.id.title_bt_left:
            // list all product
            Intent listAllIntent = new Intent("android.intent.product.list");
            listAllIntent.putExtra(TaskType.LISTTYPE, TaskType.LISTALL);
            listAllIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(listAllIntent);
            break;
        case R.id.title_bt_right:
            // search product you want
            Bundle searchBundle = new Bundle();
            showDialog(TaskType.DIALOGSEARCH, searchBundle);
            break;
        default:
            break;
        }
    }

    // jugement the product can save.
    private boolean canAddProduct(String productName, String productPriceStr) {
        boolean isNameEmpty = TextUtils.isEmpty(productName);
        boolean isPriceEmpty = TextUtils.isEmpty(productPriceStr);
        return !isNameEmpty && !isPriceEmpty;
    }

    // search button click
    @Override
    public void onSearchButClick(AutoCompleteTextView nameSearchEt,
            EditText priceSearchEt) {
        String nameSearchText = nameSearchEt.getText().toString();
        String priceSearchText = priceSearchEt.getText().toString();
        if (!TextUtils.isEmpty(nameSearchText)
                || !TextUtils.isEmpty(priceSearchText)) {

            // send intent of search in add view
            // send a command to search you want

            Intent listSearchedInent = new Intent(mContext,
                    ProductListActivity.class);

            listSearchedInent.putExtra(TaskType.LISTTYPE, TaskType.LISTSEARCH);
            listSearchedInent.putExtra(Product.dbName, nameSearchText);
            listSearchedInent.putExtra(Product.dbPrice, priceSearchText);
            listSearchedInent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(listSearchedInent);
        } else {
            String pleaseInput = getString(R.string.plinput)
                    + getString(R.string.str_name) + getString(R.string.or)
                    + getString(R.string.strprice);
            Toast.makeText(mContext, pleaseInput, Toast.LENGTH_LONG).show();
        }
        // clean the search data when searched.
        /*
         * nameSearchEt.getText().clear(); priceSearchEt.getText().clear();
         */
    }

    @Override
    public void init() {
        if (!TaskType.ACTIONUPDATE.equals(mActionType)) {
            // clean all focus
            mProductName.clearFocus();
            mProductPrice.clearFocus();
            mProductRemaker.clearFocus();
            mProductSort.clearFocus();

            // clean all date
            mProductName.getText().clear();
            mProductPrice.getText().clear();
            mProductRemaker.getText().clear();
            mProductSort.getText().clear();
        }
    }

    @Override
    public void refush(int commandType, Message msg) {
        Intent listIntentFormAdd;
        switch (commandType) {
        case TaskType.ADDPRODUCT:
            // if add success,jump to list act.
            listIntentFormAdd = new Intent("android.intent.product.list");
            if ((msg.arg1) != -1) {
                listIntentFormAdd.putExtra(TaskType.LISTTYPE, TaskType.LISTALL);
                listIntentFormAdd.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(listIntentFormAdd);
                // the product add only once by product name.
            } /*
               * else { Toast.makeText(mContext,
               * mContext.getString(R.string.productisexist),
               * Toast.LENGTH_LONG).show(); init(); }
               */
            break;
        case TaskType.NAMEORPRICESEARCHCOMMANDINLISTACT:
            break;

        // set edit product data
        case TaskType.SETPRODUCTDETAILS:
            Cursor productCursor = (Cursor) msg.obj;
            productCursor.moveToNext();
            Product updateProduct = ProductUtils.getProductObj(productCursor);
            mProductName.setText(updateProduct.getProductName());
            mProductPrice.setText(updateProduct.getProductPrice() + "");
            mProductRemaker.setText(updateProduct.getProductRemaker());
            mProductSort.setText(updateProduct.getProductSort());
            break;
        case TaskType.UPDATEPRODUCT:
            break;
        default:
            break;
        }
    }

    // back button press.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {// 点击退出时所弹出的提示，每个Activity都有这个方法
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            ProductUtils.alertExit(this);
            return true;
        case KeyEvent.KEYCODE_SEARCH:
            showDialog(TaskType.DIALOGSEARCH);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}