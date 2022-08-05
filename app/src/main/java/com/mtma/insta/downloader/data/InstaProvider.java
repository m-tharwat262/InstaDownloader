package com.mtma.insta.downloader.data;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.mtma.insta.downloader.data.InstaContractor.MediaEntry;


public class InstaProvider extends ContentProvider {


    public static final String LOG_TAG = InstaProvider.class.getSimpleName(); // class name.


    private InstaDbHelper mInstaDbHelper; // get instance of the semester database.



    private static final int MEDIA = 100; // URI pattern to all the semester_gpa table.
    private static final int MEDIA_ID = 101; // URI pattern to single column in the semester_gpa table.

    // initialize the UriMatcher.
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // add the table paths and his pattern number in the uri matcher.
    static {
        sUriMatcher.addURI(InstaContractor.CONTENT_AUTHORITY, InstaContractor.PATH_MEDIA, MEDIA);
        sUriMatcher.addURI(InstaContractor.CONTENT_AUTHORITY, InstaContractor.PATH_MEDIA + "/#", MEDIA_ID);

    }


    @Override
    public boolean onCreate() {

        // initialize databases (semester - cumulative).
        mInstaDbHelper = new InstaDbHelper(getContext());

        return false;

    }




    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // access to databases (semester - cumulative) to read the data from it.
        SQLiteDatabase mediaDatabase = mInstaDbHelper.getReadableDatabase();

        // for the return result from the database.
        Cursor cursor;

        // get the pattern that the uri equal.
        int match = sUriMatcher.match(uri);

        // setup functions for every uri pattern.
        switch (match) {

            // access to semester database with no id.
            case MEDIA:

                // setup the input inside the query function.
                cursor = mediaDatabase.query(MediaEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);

                break;

            // access to semester database with id.
            case MEDIA_ID:

                // setup the input inside the query function (after the WHERE word).
                selection = MediaEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = mediaDatabase.query(MediaEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);

                break;


            // to handle if the is no match for the uri inserted with the uri patterns.
            default:

                throw new IllegalArgumentException("Cannot query unknown URI " + uri);

        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // return Cursor contain the data from the database.
        return cursor;
    }




    @Override
    public Uri insert( Uri uri, ContentValues values) {

        // get the pattern that the uri equal.
        final int match = sUriMatcher.match(uri);

        // setup functions for every uri pattern (no single uris by id).
        switch (match) {

            // semester_gpa table inside semester database.
            case MEDIA:

                // execute helper method to insert the data inside the semester database.
                // return the uri that refer to the row place inside the semester database.
                return insertMedia(uri, values);

                // to handle if the is no match for the uri inserted with the uri patterns.
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }



    private Uri insertMedia(Uri uri, ContentValues values) {


        // access to the database to write inside it.
        SQLiteDatabase mediaDatabase = mInstaDbHelper.getWritableDatabase();

        // insert the data inside the database and get the row number that the data inserted at.
        long id = mediaDatabase.insert(MediaEntry.TABLE_NAME, null, values);

        // if the insertion failed show a Log(e) for that.
        // make the uri return value equal null in case the insertion failed.
        if (id == -1) {

            Log.e(LOG_TAG, "Failed to insert row in year database for " + uri);

            return null;
        }

        // Notify that there is changing happened in the database to sync changes to the network or activities.
        getContext().getContentResolver().notifyChange(uri, null);

        // return the uri for the place that the data inserted inside database.
        return ContentUris.withAppendedId(uri, id);

    }




    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        // get the pattern that the uri equal.
        final int match = sUriMatcher.match(uri);

        // setup functions for every uri pattern.
        switch (match) {

            // semester_gpa database with no id.
            case MEDIA:

                // execute helper method to update the data inside the database.
                // return number of the rows that updated.
                return updateMedia(uri, contentValues, selection, selectionArgs);

            // semester_gpa database with id.
            case MEDIA_ID:

                // setup the input inside the update function (after the WHERE word).
                selection = MediaEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // execute helper method to update the data inside the database.
                // return number of the rows that updated.
                return updateMedia(uri, contentValues, selection, selectionArgs);


            // to handle if the is no match for the uri inserted with the uri patterns.
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }



    private int updateMedia(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {


        // if there is not keys inside the ContentValues return from the functions early.
        // the number of the rows that updated in this case will equal zero.
        if (contentValues.size() == 0) {
            return 0;
        }

        // access to the database to write inside it.
        SQLiteDatabase semesterDatabase = mInstaDbHelper.getWritableDatabase();

        // update the data inside the database and get the number of the rows that updated.
        int rowsUpdated = semesterDatabase.update(MediaEntry.TABLE_NAME, contentValues, selection, selectionArgs);

        // Notify that there is changing happened in the database to sync changes to the network or activities.
        getContext().getContentResolver().notifyChange(uri, null);

        // return the number of the rows that updated.
        return rowsUpdated;
    }



    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        // access to databases (semester - cumulative) to write inside it.
        SQLiteDatabase mediaDatabase = mInstaDbHelper.getWritableDatabase();


        // get the pattern that the uri equal.
        final int match = sUriMatcher.match(uri);

        // number of the rows that will be deleted
        int rowsDeleted;

        // setup functions for every uri pattern.
        switch (match) {

            // semester_gpa database with no id.
            case MEDIA:

                // delete the data from the database.
                // return number of the rows that deleted from the database.
                rowsDeleted = mediaDatabase.delete(MediaEntry.TABLE_NAME, selection, selectionArgs);
                break;

            // semester_gpa database with id.
            case MEDIA_ID:

                // setup the input inside the update function (after the WHERE word).
                selection = MediaEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // delete the data from the database.
                // return number of the rows that deleted from the database.
                rowsDeleted = mediaDatabase.delete(MediaEntry.TABLE_NAME, selection, selectionArgs);
                break;

            // to handle if the is no match for the uri inserted with the uri patterns.
            default:

                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // notify the network or the activity when there is changing happened inside the database.
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // return number of the rows that deleted from the database.
        return rowsDeleted;
    }



    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {

            case MEDIA:
                return MediaEntry.CONTENT_LIST_TYPE;

            case MEDIA_ID:
                return MediaEntry.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);

        }

    }


}
