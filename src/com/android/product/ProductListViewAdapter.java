package com.android.product;

import java.util.ArrayList;

import com.android.product.db.ProductDbService;
import com.android.product.dome.Product;
import com.android.product.utils.ProductUtils;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ProductListViewAdapter extends BaseAdapter implements
        OnClickListener {

    private Context mContext;
    private Cursor mProductCursor;
    private ArrayList<Product> mProductList = new ArrayList<Product>();

    // call this function the list will be reload, the list view position is
    // changed.this function is abandon.
    public ProductListViewAdapter(Cursor cursor, Context context) {
        this.mProductCursor = cursor;
        this.mContext = context;
    }

    // init context.
    public ProductListViewAdapter(Context context) {
        this.mContext = context;
    }

    // set the data by product list
    public void updateProductList(ArrayList<Product> productList) {
        this.mProductList = productList;
        notifyProductList();
    }

    // reupdata the list
    public void notifyProductList() {
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mProductList.size();// mProductCursor.getCount();
    }

    // return now product obj
    @Override
    public Object getItem(int position) {
        return mProductList.get(position);
    }

    // return now product id in db.
    @Override
    public long getItemId(int position) {
        Log.i("pro", "getitemID=" + mProductList.get(position).getProductId());
        return mProductList.get(position).getProductId();
    }

    // get and set list view's item
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View productListView;
        if (null != convertView) {
            productListView = convertView;
        } else {
            productListView = LayoutInflater.from(mContext).inflate(
                    R.layout.productlistitem, null);
        }

        ImageView productImageView = (ImageView) productListView
                .findViewById(R.id.productimg);
        // the onclick listener is ProductListActivity's listener.
        productImageView.setOnClickListener(ProductDbService
                .getActivityByName("ProductListActivity"));
        // save the image object by product's hascode.
        ProductDbService.poductImageView.put(productImageView.hashCode(),
                productImageView);
        TextView tvID = (TextView) productListView.findViewById(R.id.listid);
        TextView tvName = (TextView) productListView
                .findViewById(R.id.listname);
        TextView tvPrice = (TextView) productListView
                .findViewById(R.id.listprice);
        TextView tvRemaker = (TextView) productListView
                .findViewById(R.id.listremaker);
        TextView tvSort = (TextView) productListView
                .findViewById(R.id.productsort);
        TextView tvDate = (TextView) productListView
                .findViewById(R.id.productaddtime);

        // Product oneProduct = getProductObj(position);
        Product oneProduct = mProductList.get(position);

        // set image view's id by product image's hasmap
        ProductDbService.imageHasMapMatchId.put(productImageView.hashCode(),
                oneProduct.getProductId());

        tvID.setText(oneProduct.getProductId() + "");
        tvName.setText(oneProduct.getProductName());
        tvPrice.setText(oneProduct.getProductPrice() + "");
        tvRemaker.setText(oneProduct.getProductRemaker().equals("") ? mContext
                .getString(R.string.empty) : oneProduct.getProductRemaker());
        tvSort.setText(oneProduct.getProductSort().equals("") ? mContext
                .getString(R.string.empty) : oneProduct.getProductSort());
        tvDate.setText(ProductUtils.getCurrentTimeWithFormat(oneProduct
                .getProductDate()));
        // set product image
        productImageView.setImageBitmap(ProductUtils.getProductBitmap(mContext,
                oneProduct.getProductImageUri()));
        return productListView;
    }

    // make productObj throgh cursor
    public Product getProductObj(int position) {
        Product oneProduct = new Product();
        // get details's vauls
        mProductCursor.moveToPosition(position);
        Integer productId = mProductCursor.getInt(mProductCursor
                .getColumnIndex(Product.dbid));
        String productName = mProductCursor.getString(mProductCursor
                .getColumnIndex(Product.dbName));
        Float productPrice = mProductCursor.getFloat(mProductCursor
                .getColumnIndex(Product.dbPrice));
        String productRemaker = mProductCursor.getString(mProductCursor
                .getColumnIndex(Product.dbRemaker));
        String productSort = mProductCursor.getString(mProductCursor
                .getColumnIndex(Product.dbProductSort));
        String productDate = mProductCursor.getString(mProductCursor
                .getColumnIndex(Product.dbProductDate));
        String productImagePath = mProductCursor.getString(mProductCursor
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.productimg:
            Log.i("imagclick", "image clicked");
            break;

        default:
            break;
        }
    }

}
