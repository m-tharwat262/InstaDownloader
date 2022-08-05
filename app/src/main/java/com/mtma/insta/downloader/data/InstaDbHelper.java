package com.mtma.insta.downloader.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mtma.insta.downloader.data.InstaContractor.MediaEntry;

public class InstaDbHelper extends SQLiteOpenHelper {


    public static final String LOG_TAG = InstaDbHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "media_saver.db";

    private static final int DATABASE_VERSION = 1;


    public InstaDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {


        String SQL_CREATE_PETS_TABLE =  "CREATE TABLE " + MediaEntry.TABLE_NAME + " ("

                + MediaEntry._ID                             + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + MediaEntry.COLUMN_MEDIA_CODE               + " TEXT NOT NULL DEFAULT \"\", "
                + MediaEntry.COLUMN_MEDIA_TYPE               + " INTEGER NOT NULL DEFAULT 0, "
                + MediaEntry.COLUMN_MEDIA_PRODUCT_TYPE       + " TEXT NOT NULL DEFAULT \"\", "
                + MediaEntry.COLUMN_MEDIA_TEXT_AND_HASHTAG   + " TEXT NOT NULL DEFAULT \"\", "
                + MediaEntry.COLUMN_USER_NAME                + " TEXT NOT NULL DEFAULT \"\", "
                + MediaEntry.COLUMN_UNIX                     + " INTEGER NOT NULL DEFAULT 0);";


        db.execSQL(SQL_CREATE_PETS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to be done here.
    }


}
