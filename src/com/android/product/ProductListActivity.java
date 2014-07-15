package com.android.product;

import java.util.ArrayList;

import com.android.product.R;
import com.android.product.db.ProductDbService;
import com.android.product.db.TaskType;
import com.android.product.dome.Product;
import com.android.product.utils.ProductUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ProductListActivity extends BaseActivity implements
        OnClickListener, OnItemClickListener, OnLongClickListener {
    public Context mContext;
    public SQLiteDatabase mProductDatabase;
    public Cursor mProductCursor;
    public ListView productListView;
    public Button buttonLeft;
    public Button buttonRight;
    public TextView uiTopTextView;
    public ImageView mProductImage;
    public ProductListViewAdapter myProductListAdapter;

    public String listType;
    public Long productId;
    public LinearLayout emptyProductLayout;
    // search parms
    public String mNameSearchText;
    public String mPriceSearchText;

    public AdapterContextMenuInfo mProductItemInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ProductDbService.activities.put(this.getClass().getName(), this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.productlistview);
        mContext = getApplicationContext();
        Log.i("instance", mProductDbService.toString());
        // get the list type and search product parms.
        mNameSearchText = getIntent().getStringExtra(Product.dbName);
        mPriceSearchText = getIntent().getStringExtra(Product.dbPrice);
        // get edited product name
        /*
         * String editedProductName = getIntent().getStringExtra(
         * Product.productEditedName); String editedProductPrice =
         * getIntent().getStringExtra( Product.productEditedPrice);
         */
        initListUi();
        String addPageProductListType = getIntent().getStringExtra(
                TaskType.LISTTYPE);
        // list all product command
        if (ProductDbService.isFirstStart || TaskType.LISTALL.equals(addPageProductListType)) {
            mProductDbService
                    .receiveCommand(TaskType.ALLSEARCHCOMMANDINLISTACT);
            ProductDbService.isFirstStart = false;
            // list searched product command
        } else if (TaskType.LISTSEARCH.equals(addPageProductListType)) {
            mProductDbService.receiveCommand(
                    TaskType.NAMEORPRICESEARCHCOMMANDINLISTACT,
                    mNameSearchText, mPriceSearchText);
        }
    }

    @Override
    protected void onResume() {
        mActityType = TaskType.LISTACTIVITY;
        super.onResume();
    }

    // call when onStart called.
    @Override
    public void init() {
        // get the list type when return the list view page.
        listType = getIntent().getStringExtra(TaskType.LISTTYPE);
    }

    // when the command have competed,so it will be call through handler
    @Override
    public void refush(int commandType, Message msg) {
        emptyProductLayout = (LinearLayout) findViewById(R.id.emptyproductlayout);
        ArrayList<Product> productList;
        if (msg.arg1 == TaskType.NOPRODUCTFIND) {
            // set the view is empty
            productListView.setVisibility(View.GONE);
            emptyProductLayout.setVisibility(View.VISIBLE);
            Toast.makeText(mContext, R.string.searchnoonematch,
                    Toast.LENGTH_SHORT).show();
            TextView emptyProductText = (TextView) emptyProductLayout
                    .findViewById(R.id.emptyproducttext);
            emptyProductText.setOnClickListener(this);
            mProductCursor = null;
            return;
        }
        // if has product so set it visible.
        productListView.setVisibility(View.VISIBLE);
        emptyProductLayout.setVisibility(View.GONE);

        switch (commandType) {
        // refush the list page when add new product
        case TaskType.ADDPRODUCT:
            mProductCursor = (Cursor) msg.obj;
            Log.i("c", mProductCursor.getCount() + "");
            // update the list.
            productList = ProductUtils.cursorToProductList(mProductCursor);
            myProductListAdapter.updateProductList(productList);
            break;
        // refush the list page when list all
        case TaskType.ALLSEARCHCOMMANDINLISTACT:
            mProductCursor = (Cursor) msg.obj;
            // update the list
            productList = ProductUtils.cursorToProductList(mProductCursor);
            Log.i("c", mProductCursor.getCount() + "");
            myProductListAdapter.updateProductList(productList);
            break;

        // refush the list page when search product by name or price.
        case TaskType.NAMEORPRICESEARCHCOMMANDINLISTACT:
            mProductCursor = (Cursor) msg.obj;
            productList = ProductUtils.cursorToProductList(mProductCursor);
            myProductListAdapter.updateProductList(productList);
            break;

        // refush the list after delete product.
        case TaskType.DELETEPRODUCT:
            // in list all product delete product
            if (TaskType.LISTALL.equals(listType)) {
                mProductDbService
                        .receiveCommand(TaskType.ALLSEARCHCOMMANDINLISTACT);
                // in list search delete product
            } else if (TaskType.LISTSEARCH.equals(listType)) {
                mProductDbService.receiveCommand(
                        TaskType.NAMEORPRICESEARCHCOMMANDINLISTACT,
                        mNameSearchText, mPriceSearchText);
            }
            break;
        // refush after set the product image.
        case TaskType.SAVEPRODUCTIMAGE:
            // set product image in list all page.
            if (TaskType.LISTALL.equals(listType)) {
                mProductDbService
                        .receiveCommand(TaskType.ALLSEARCHCOMMANDINLISTACT);
                // set product image in list search page.
            } else if (TaskType.LISTSEARCH.equals(listType)) {
                mProductDbService.receiveCommand(
                        TaskType.NAMEORPRICESEARCHCOMMANDINLISTACT,
                        mNameSearchText, mPriceSearchText);
            }
            // refush list when update the product.
        case TaskType.UPDATEPRODUCT:
            // after update product,list product by list type.
            if (TaskType.LISTALL.equals(listType)) {
                mProductDbService
                        .receiveCommand(TaskType.ALLSEARCHCOMMANDINLISTACT);
            } else if (TaskType.LISTSEARCH.equals(listType)) {
                mProductDbService.receiveCommand(
                        TaskType.NAMEORPRICESEARCHCOMMANDINLISTACT,
                        mNameSearchText, mPriceSearchText);
            }
            break;
        default:
            break;
        }
    }

    // load ui
    private void initListUi() {
        buttonLeft = (Button) findViewById(R.id.title_bt_left);
        buttonRight = (Button) findViewById(R.id.title_bt_right);
        uiTopTextView = (TextView) findViewById(R.id.uitoptitle);
        buttonLeft.setBackgroundResource(R.drawable.title_back);
        buttonLeft.setText(R.string.addback);
        buttonRight.setBackgroundResource(R.drawable.search);
        uiTopTextView.setText(R.string.productlisttitle);
        buttonLeft.setOnClickListener(this);
        buttonRight.setOnClickListener(this);
        // list produt details
        productListView = (ListView) findViewById(R.id.listviewproduct);
        // add on item click listener
        productListView.setOnItemClickListener(this);
        registerForContextMenu(productListView);
        // set the adapter.
        myProductListAdapter = new ProductListViewAdapter(mContext);
        productListView.setAdapter(myProductListAdapter);
    }

    // search button click
    @Override
    public void onSearchButClick(AutoCompleteTextView nameSearchEt,
            EditText priceSearchEt) {
        mNameSearchText = nameSearchEt.getText().toString();
        mPriceSearchText = priceSearchEt.getText().toString();
        if (!TextUtils.isEmpty(mNameSearchText)
                || !TextUtils.isEmpty(mPriceSearchText)) {
            // send a command to search you want
            mProductDbService.receiveCommand(
                    TaskType.NAMEORPRICESEARCHCOMMANDINLISTACT,
                    mNameSearchText, mPriceSearchText);
        } else {
            String pleaseInput = getString(R.string.plinput)
                    + getString(R.string.str_name) + getString(R.string.or)
                    + getString(R.string.strprice);
            Toast.makeText(mContext, pleaseInput, Toast.LENGTH_LONG).show();
        }
        // if in all list view search and delete product,so set it listsearch
        listType = TaskType.LISTSEARCH;
        getIntent().putExtra(TaskType.LISTTYPE, TaskType.LISTSEARCH);
        // clean the search data when searched.
        /*nameSearchEt.getText().clear();
        priceSearchEt.getText().clear();*/
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        mProductItemInfo = (AdapterContextMenuInfo) menuInfo;
        menu.setHeaderTitle(R.string.contextmenutitle);
        menu.add(1, 1, 1, R.string.contextmenuproductdelete);
        menu.add(1, 2, 1, R.string.contextmenuproductdedit);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // set the product id in all place, bundle is not good to use.
        productId = mProductItemInfo.id;
        // set delete id, the bundle can not send the id in two time.
        deleteProductId = mProductItemInfo.id;
        Log.i("id", "id=="+productId);
        switch (item.getItemId()) {
        case 1:
            // delete product
            Bundle deleteBundle = new Bundle();
            deleteBundle.putLong(TaskType.DELETEID, productId);
            // create dialog to confirm to delete the product.
            showDialog(TaskType.DIOLAGSHOWCONFIMDELETE, deleteBundle);
            break;
        case 2:
            // edit the product and set the listtype and search type parms.
            Intent updateProduct = new Intent(
                    "android.intent.product.editproduct");
            updateProduct.putExtra(TaskType.ACTIONTYPE, TaskType.ACTIONUPDATE);
            updateProduct.putExtra(Product.dbid, productId);
            updateProduct.putExtra(TaskType.LISTTYPE, listType);
            updateProduct.putExtra(Product.dbName, mNameSearchText);
            updateProduct.putExtra(Product.dbPrice, mPriceSearchText);
            updateProduct.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(updateProduct);
            Log.i("m", "edit product==" + productId);
            break;
        default:
            break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        Intent detailsIntent = new Intent(
                "android.intent.product.product.detail");
        Log.i("pro", "long id=" + id);
        detailsIntent.putExtra(Product.dbid, id);
        detailsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(detailsIntent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.title_bt_left:
            Intent listActIntent = new Intent(
                    "android.intent.product.editproduct");
            listActIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(listActIntent);
            finish();
            break;
        case R.id.title_bt_right:
            Bundle searchBundle = new Bundle();
            showDialog(TaskType.DIALOGSEARCH, searchBundle);
            break;
        case R.id.productimg:
            // get imageview by hashcode.
            mProductImage = ProductDbService.poductImageView.get(v.hashCode());
            showDialog(TaskType.DIOLOGCHOOSEIMAGE);
            break;
        case R.id.emptyproducttext:
            Intent formEmpteToAddPageIntent = new Intent(mContext,
                    ProductRecorderActivity.class);
            formEmpteToAddPageIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            // if search produt is null, so show diffrent page by list type
            if (TaskType.LISTSEARCH.equals(listType) && mProductCursor == null) {
                startActivity(formEmpteToAddPageIntent);
                finish();
                // if in searched product page search product is null and cusor
                // is no null.
            } else if (TaskType.LISTSEARCH.equals(listType)) {
                emptyProductLayout.setVisibility(View.GONE);
                productListView.setVisibility(View.VISIBLE);
                // if all product list page searched product is null.
            } else if (TaskType.LISTALL.equals(listType)
                    && mProductCursor == null) {
                startActivity(formEmpteToAddPageIntent);
                finish();
            } else if (TaskType.LISTALL.equals(listType)) {
                emptyProductLayout.setVisibility(View.GONE);
                productListView.setVisibility(View.VISIBLE);
            }
            break;
        default:
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case TaskType.REQUEST_CODE_PHOTO_PICKED_WITH_DATA: {
            // Ignore failed requests
            if (resultCode != Activity.RESULT_OK)
                return;
            // As we are coming back to this view, the editor will be reloaded
            // automatically,
            // which will cause the photo that is set here to disappear. To
            // prevent this,
            // we remember to set a flag which is interpreted after loading.
            // This photo is set here already to reduce flickering.
            Bitmap mPhoto = data.getParcelableExtra("data");
            // get id by product image view's hasmap.
            mProductDbService.receiveCommand(TaskType.SAVEPRODUCTIMAGE, String
                    .valueOf(ProductDbService.imageHasMapMatchId
                            .get(mProductImage.hashCode())), mPhoto,
                    mCurrentPhotoFile);
            mProductImage.setImageBitmap(mPhoto);
            break;
        }
        case TaskType.REQUEST_CODE_CAMERA_WITH_DATA: {
            // Ignore failed requests
            if (resultCode != Activity.RESULT_OK)
                return;
            doCropPhoto();
            break;
        }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    // search button press.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_SEARCH:
            showDialog(TaskType.DIALOGSEARCH);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
