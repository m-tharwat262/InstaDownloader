package com.mtma.insta.downloader.data;


import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import com.mtma.insta.downloader.Constants;

public class InstaContractor {

    public static final String CONTENT_AUTHORITY = "com.mtma.insta.downloader"; // the content authority.

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY); // the uri for the database.

    public static final String PATH_MEDIA = "media_saver";






    public static final class MediaEntry implements BaseColumns {


        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_MEDIA);

        public final static String TABLE_NAME = "media"; // table name.



        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_MEDIA_CODE = "media_code";
        public final static String COLUMN_MEDIA_PK_CODE = "pk_code";
        public final static String COLUMN_MEDIA_TYPE = "media_type";
        public final static String COLUMN_MEDIA_PRODUCT_TYPE = "product_type";
        public final static String COLUMN_MEDIA_TEXT_AND_HASHTAG = "text_and_hashtag";
        public final static String COLUMN_MEDIA_TEXT = "text";
        public final static String COLUMN_MEDIA_HASHTAGS = "hashtags";
        public final static String COLUMN_USER_NAME = "user_name";
        public final static String COLUMN_UNIX = "unix";


        public final static String COLUMN_FOLDER_PATH = "unix";
        public final static String COLUMN_MEDIA_PATH = "unix";
        public final static String COLUMN_MEDIA_URL = "unix";



        public static final int STORY_IMAGE_TYPE = 1;
        public static final int STORY_VIDEO_TYPE = 2;
        public static final int POST_IMAGE_TYPE = 3;
        public static final int POST_VIDEO_TYPE = 4;
        public static final int COLLECTION_IMAGE_TYPE = 5;
        public static final int COLLECTION_VIDEO_TYPE = 6;


        public static final int MEDIA_PRODUCT_TYPE_POST = 0;
        public static final int MEDIA_PRODUCT_TYPE_REEL = 1;
        public static final int MEDIA_PRODUCT_TYPE_IGTV = 2;
        public static final int MEDIA_PRODUCT_TYPE_STORY = 3;


        public static final String MEDIA_FILE_PATH_POST = Constants.PATH_APP_DIRECTORY + "/Posts";
        public static final String MEDIA_FILE_PATH_REEL = Constants.PATH_APP_DIRECTORY + "/Reels";
        public static final String MEDIA_FILE_PATH_IGTV = Constants.PATH_APP_DIRECTORY + "/IGTVs";
        public static final String MEDIA_FILE_PATH_STORY = Constants.PATH_APP_DIRECTORY + "/Stories";


        // TODO: make it as 0, 1, 2 to reduce data saved in database.
        public static final int MEDIA_TYPE_IMAGE = 100;
        public static final int MEDIA_TYPE_VIDEO = 101;
        public static final int MEDIA_TYPE_MULTIPLE = 102;









        public static String getMediaProductType(int productType) {

            if (productType == 0) {
                return "POST";
            } else if (productType == 1) {
                return "REEL";
            } else if (productType == 2) {
                return "IGTV";
            } else {
                return "Story";
            }

        }

        public static String getMediaPath(int productType) {

            if (productType == 0) {
                return MEDIA_FILE_PATH_POST;
            } else if (productType == 1) {
                return MEDIA_FILE_PATH_REEL;
            } else if (productType == 2) {
                return MEDIA_FILE_PATH_IGTV;
            } else {
                return MEDIA_FILE_PATH_STORY;
            }

        }





        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MEDIA;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MEDIA;

    }


}
