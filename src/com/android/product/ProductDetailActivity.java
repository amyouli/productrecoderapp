package com.android.product;

import com.android.product.db.ProductDbService;
import com.android.product.db.TaskType;
import com.android.product.dome.Product;
import com.android.product.utils.ProductUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class ProductDetailActivity extends BaseActivity {

    public Button buttonLeft;
    public Button buttonRight;
    public TextView uiTopTextView;
    public Context mContext;
    public TextView tvID;
    public TextView tvName;
    public TextView tvPrice;
    public TextView tvRemaker;
    public TextView tvSort;
    public TextView tvDate;
    public ImageView mProductImage;
    public long productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ProductDbService.activities.put(this.getClass().getName(), this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.productdetail);
        mContext = getApplicationContext();
        productId = getIntent().getLongExtra(Product.dbid, -1);
        Log.i("pro", "detail--" + productId);
        // get the db and now curosr
        Log.i("instance", mProductDbService.toString());
        initUi();
        // must write here.
        mProductDbService.receiveCommand(TaskType.IDSEARCHCOMMANDINDETAILACT,
                String.valueOf(productId));
    }

    private void initUi() {
        // get title button and text view
        mProductImage = (ImageView) findViewById(R.id.detail_image);
        mProductImage.setOnClickListener(this);
        buttonLeft = (Button) findViewById(R.id.title_bt_left);
        buttonRight = (Button) findViewById(R.id.title_bt_right);
        uiTopTextView = (TextView) findViewById(R.id.uitoptitle);
        uiTopTextView.setText(R.string.product_details);
        buttonLeft.setText(R.string.productlistback);
        buttonLeft.setBackgroundResource(R.drawable.title_back);
        buttonRight.setBackgroundResource(R.drawable.title_new);
        buttonLeft.setOnClickListener(this);
        buttonRight.setOnClickListener(this);

        // get details textview
        tvID = (TextView) findViewById(R.id.listid);
        tvName = (TextView) findViewById(R.id.listname);
        tvPrice = (TextView) findViewById(R.id.listprice);
        tvRemaker = (TextView) findViewById(R.id.listremaker);
        tvSort = (TextView) findViewById(R.id.productsort);
        tvDate = (TextView) findViewById(R.id.productaddtime);
    }

    private void setDetailsDate(Cursor oneProductCurosr) {
        // get product obj
        Product oneProduct = ProductUtils.getProductObj(oneProductCurosr);

        // set product detail in to this textview
        tvID.setText(oneProduct.getProductId() + "");
        tvName.setText(oneProduct.getProductName());
        tvPrice.setText(oneProduct.getProductPrice() + "");

        tvRemaker
                .setText(oneProduct.getProductRemaker().equals("") ? getString(R.string.empty)
                        : oneProduct.getProductRemaker());
        tvSort.setText(oneProduct.getProductSort().equals("") ? getString(R.string.empty)
                : oneProduct.getProductSort());

        tvDate.setText(ProductUtils.getCurrentTimeWithFormat(oneProduct
                .getProductDate()));
        mProductImage.setImageBitmap(ProductUtils.getProductBitmap(mContext,
                oneProduct.getProductImageUri()));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.title_bt_left:
            finish();
            break;
        case R.id.title_bt_right:
            Intent addProductIntent = new Intent(
                    "android.intent.product.editproduct");
            startActivity(addProductIntent);
            finish();
            break;
        case R.id.detail_image:
            showDialog(TaskType.DIOLOGCHOOSEIMAGE);
            break;
        default:
            break;
        }
    }

    @Override
    public void init() {
        // do nothings
    }

    @Override
    public void refush(int commandType, Message msg) {
        Cursor oneProductCurosr = (Cursor) msg.obj;
        oneProductCurosr.moveToFirst();
        setDetailsDate(oneProductCurosr);
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
            mProductDbService.receiveCommand(TaskType.SAVEPRODUCTIMAGE,
                    String.valueOf(productId), mPhoto, mCurrentPhotoFile);
            mProductImage.setImageBitmap(mPhoto);
            break;
        }
        case TaskType.REQUEST_CODE_CAMERA_WITH_DATA: {

            // Ignore failed requests
            if (resultCode != Activity.RESULT_OK)
                return;
            // cut the image.
            doCropPhoto();
            break;
        }
        }
    }

    @Override
    public void onSearchButClick(AutoCompleteTextView nameSearchEt, EditText priceSearchEt) {
        
    }
}
