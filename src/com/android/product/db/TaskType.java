package com.android.product.db;

public class TaskType {
    // Search Type
    public static final int PRODUCTINSERT = 0;
    public static final int ALLSEARCH = 1;
    public static final int IDSEARCHCOMMANDINDETAILACT = 23;
    public static final int ALLSEARCHCOMMANDINLISTACT = 21;
    public static final int NAMEORPRICESEARCHCOMMANDINLISTACT = 22;
    public static final int ADDPRODUCT = 20;
    public static final int NAMEORPRICESEARCH = 2;
    public static final int UPDATEPRODUCT = 3;
    public static final int DELETEPRODUCT = 4;
    public static final int GETALLPRODUCTNAME = 6;
    public static  final String LISTTYPE = "listtype";
    public static  final String LISTALL = "listall";
    public static  final String LISTSEARCH = "listsearch";
    public static final String ACTIONTYPE = "actiontype";
    public static final String ACTIONUPDATE = "actionupdate";
    public static final int SETPRODUCTDETAILS = 5;
    public static final int NOPRODUCTFIND = -1;
    public static final int SAVEPRODUCTIMAGE = 30;
    // show dialog type
    public static final int DIALOGMUSTINPUT = 1;
    public static final int DIALOGSEARCH = 2;
    public static final int DIOLOGCHOOSEIMAGE = 3;
    public static final int DIOLAGSHOWCONFIMDELETE = 4;
    public static final String DIAINFO = "diainfo";
    public static final String DELETEID = "deleteid";
    // set image
    public static final int REQUEST_CODE_CAMERA_WITH_DATA = 1;
    public static final int REQUEST_CODE_PHOTO_PICKED_WITH_DATA = 2;
    // auto name search text query form data
    public static final int SEARCHNAMEINPUTTQUERY = 51;
    public static final int ADDACTIVITY = 61;
    public static final int LISTACTIVITY = 62;
}
