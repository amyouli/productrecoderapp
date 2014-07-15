package com.android.product;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.android.product.db.ProductDbService;
import com.android.product.db.TaskType;
import com.android.product.dome.Product;
import com.android.product.utils.ProductUtils;

public abstract class BaseActivity extends Activity implements OnClickListener {

    private Context mContext;
    public ProductDbService mProductDbService;
    private static final File PHOTO_DIR = new File(
            Environment.getExternalStorageDirectory() + "/DCIM/Camera");
    public File mCurrentPhotoFile;
    public ArrayAdapter<String> autoNameTextAdpter = null;
    public AutoCompleteTextView nameSearchEt;
    public EditText priceSearchEt;
    public View dialogView;
    public AlertDialog mAlertDialog = null;
    public Long deleteProductId;
    public MyTextWatch myTextWatch = new MyTextWatch();
    private static final int REQUEST_CODE_CAMERA_WITH_DATA = 1;
    private static final int REQUEST_CODE_PHOTO_PICKED_WITH_DATA = 2;
    private static int mPhotoPickSize = 90;
    // use this var to choose call which receiveSearchData function
    // because auto name text has some problem.
    public static int mActityType;

    @SuppressWarnings("static-access")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        mProductDbService = ProductDbService.getDbServiceInstance(mContext);
        // dialogBuilder.setInverseBackgroundForced(true);
        Iterator<String> keynames = ProductDbService.activities.keySet()
                .iterator();
        while (keynames.hasNext()) {
            String keyname = keynames.next();
            Log.i("ac", ProductDbService.activities.get(keyname).getClass()
                    .getName());
        }
        // set display mode£¬must before setContentView(R.layout.main) to set£º
        Window window = getWindow();
        // set windows no title
        requestWindowFeature(window.FEATURE_NO_TITLE);
        // set all screen display,remout the title.
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // set window highlight
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initUi();
    }

    // init the search ui.
    private void initUi() {
        // init the searchd dialog ui.
        dialogView = View.inflate(this, R.layout.searchdialogview, null);
        nameSearchEt = (AutoCompleteTextView) dialogView
                .findViewById(R.id.namesearchET);
        priceSearchEt = (EditText) dialogView.findViewById(R.id.pricesearchET);
        nameSearchEt.addTextChangedListener(myTextWatch);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // load the product date.
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // must set the adapter here.the auto text can display on first time.
        autoNameTextAdpter = new ArrayAdapter<String>(this,
                R.layout.autotextshowitem, ProductDbService.nameSearchList);
        nameSearchEt.setAdapter(autoNameTextAdpter);
    }

    public abstract void init();

    public abstract void refush(int commandType, Message msg);

    public abstract void onSearchButClick(AutoCompleteTextView nameSearchEt,
            EditText priceSearchEt);

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1, 1, 1, getString(R.string.exit));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case 1:
            // exit app
            ProductUtils.alertExit(this);
            break;

        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

    }

    // must override the function,because get the data form dialg has problem.
    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        super.onPrepareDialog(id, dialog, args);
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle diaInfor) {
        // builder must define here.
        Builder dialogBuilder = new AlertDialog.Builder(this);
        switch (id) {
        case TaskType.DIALOGMUSTINPUT:
            dialogBuilder.setTitle(R.string.warn);
            dialogBuilder.setMessage(diaInfor.getString(TaskType.DIAINFO));
            dialogBuilder.setNegativeButton(R.string.cancel,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // DO NOTHING
                        }
                    });
            dialogBuilder.setPositiveButton(R.string.ok,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            break;
        case TaskType.DIALOGSEARCH:
            dialogBuilder.setTitle(R.string.search_title);
            dialogBuilder.setView(dialogView);
            dialogBuilder.setPositiveButton(R.string.search,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // search button click
                            onSearchButClick(nameSearchEt, priceSearchEt);
                            dismissDialog(TaskType.DIALOGSEARCH);
                        }
                    });
            dialogBuilder.setNegativeButton(R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismissDialog(TaskType.DIALOGSEARCH);
                        }
                    });
            break;
        case TaskType.DIOLOGCHOOSEIMAGE:
            Context dialogContext = new ContextThemeWrapper(mContext,
                    android.R.style.Theme_Light);
            String[] choices;
            choices = new String[2];
            choices[0] = getString(R.string.dialogsearchtakepic);
            choices[1] = getString(R.string.dialogsearchfromgallery);
            ListAdapter adapter = new ArrayAdapter<String>(dialogContext,
                    android.R.layout.simple_list_item_1, choices);
            dialogBuilder.setTitle(R.string.dialogsearchtitle);
            dialogBuilder.setSingleChoiceItems(adapter, -1,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                            case 0:
                                onTakePhotoChosen();
                                // dismissDialog(DIOLOGCHOOSEIMAGE),because in
                                // list view set image.
                                dismissDialog(TaskType.DIOLOGCHOOSEIMAGE);
                                break;
                            case 1:
                                onPickFromGalleryChosen();
                                dismissDialog(TaskType.DIOLOGCHOOSEIMAGE);
                                break;

                            default:
                                break;
                            }
                        }

                    });
            break;
        case TaskType.DIOLAGSHOWCONFIMDELETE:
            // deleteProductId = diaInfor.getLong(TaskType.DELETEID);
            dialogBuilder.setTitle(getString(R.string.warn));
            dialogBuilder.setMessage(getString(R.string.configdeleteproduct));
            dialogBuilder.setPositiveButton(R.string.ok,
                    new DialogInterface.OnClickListener() {
                        // confirm delete the product.
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mProductDbService.receiveCommand(
                                    TaskType.DELETEPRODUCT, deleteProductId);
                            Log.i("m", "delete product==" + deleteProductId);
                        }
                    });
            dialogBuilder.setNegativeButton(R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            break;
        default:
            break;
        }
        mAlertDialog = dialogBuilder.create();
        return mAlertDialog;
    }

    // Image form carmar
    public void onTakePhotoChosen() {
        try {
            // Launch camera to take photo for selected contact
            PHOTO_DIR.mkdirs();
            mCurrentPhotoFile = new File(PHOTO_DIR, getPhotoFileName());
            Intent intent = getTakePickIntent(mCurrentPhotoFile);
            startActivityForResult(intent, REQUEST_CODE_CAMERA_WITH_DATA);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(mContext, R.string.appnotfind, Toast.LENGTH_LONG)
                    .show();
        }
    }

    // Choose Fomm Ablum
    public void onPickFromGalleryChosen() {
        try {
            // Launch picker to choose photo for selected contact
            Intent intent = getPhotoPickIntent();
            startActivityForResult(intent, REQUEST_CODE_PHOTO_PICKED_WITH_DATA);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(mContext, R.string.appnotfind, Toast.LENGTH_LONG)
                    .show();
        }
    }

    // Create a file name for the icon photo using current time.
    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "'IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date) + ".jpg";
    }

    public static Intent getTakePickIntent(File f) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        return intent;
    }

    // Reutn Chosse From Ablum's Intent
    public Intent getPhotoPickIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", mPhotoPickSize);
        intent.putExtra("outputY", mPhotoPickSize);
        intent.putExtra("return-data", true);
        return intent;
    }

    protected void doCropPhoto() {
        try {
            // Add the image to the media store
            MediaScannerConnection.scanFile(mContext,
                    new String[] { mCurrentPhotoFile.getAbsolutePath() },
                    new String[] { null }, null);

            // Launch gallery to crop the photo
            Intent intent = getCropImageIntent(Uri.fromFile(mCurrentPhotoFile));
            startActivityForResult(intent, REQUEST_CODE_PHOTO_PICKED_WITH_DATA);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, R.string.appnotfind, Toast.LENGTH_LONG)
                    .show();
        }
    }

    // get search names for auto show blow the autocompletedtextview.
    public void receiveSearchData(int what, Message msg) {
        // clear the old data.
        ProductDbService.nameSearchList.clear();
        switch (what) {
        case TaskType.SEARCHNAMEINPUTTQUERY:
            Cursor nameSearchCursor = (Cursor) msg.obj;
            while (nameSearchCursor.moveToNext()) {
                String nameSearch = nameSearchCursor.getString(nameSearchCursor
                        .getColumnIndex(Product.dbName));
                ProductDbService.nameSearchList.add(nameSearch);
            }
            // set the adapter.
            autoNameTextAdpter = new ArrayAdapter<String>(this,
                    R.layout.autotextshowitem, ProductDbService.nameSearchList);
            nameSearchEt.setAdapter(autoNameTextAdpter);
            // when the product name include what you input in other index.
            String nameTextFromEt = nameSearchEt.getEditableText().toString();
            // hide the softkeybaord.
            if (ProductDbService.nameSearchList.size() > 0 && isContaintTheIput(nameTextFromEt)) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                inputMethodManager.hideSoftInputFromWindow(
                        nameSearchEt.getWindowToken(), 0);
            }
            break;

        default:
            break;
        }
    }

    private boolean isContaintTheIput(String nameTextFromEt) {
        for (String nameText : ProductDbService.allProductName) {
            if (nameText.startsWith(nameTextFromEt)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Constructs an intent for image cropping.
     */
    public Intent getCropImageIntent(Uri photoUri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(photoUri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", mPhotoPickSize);
        intent.putExtra("outputY", mPhotoPickSize);
        intent.putExtra("return-data", true);
        return intent;
    }

    // product search name edit text watch.
    class MyTextWatch implements TextWatcher {

        public MyTextWatch() {
            super();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
            Log.i("autextsear", s.toString());
            if (s.length() == 1) {
                Log.i("autextsear", "request db " + s.toString());
                mProductDbService.receiveCommand(
                        TaskType.SEARCHNAMEINPUTTQUERY, s, mActityType);
            }
            //
            String nameTextFromEt = s.toString();
            // hide the softkeybaord.
            if (!TextUtils.isEmpty(s.toString())
                    && ProductDbService.nameSearchList.size() > 0
                    && isContaintTheIput(nameTextFromEt)) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                inputMethodManager.hideSoftInputFromWindow(
                        nameSearchEt.getWindowToken(), 0);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}
