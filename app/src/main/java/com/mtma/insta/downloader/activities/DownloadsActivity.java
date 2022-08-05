package com.mtma.insta.downloader.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.mtma.insta.downloader.Constants;
import com.mtma.insta.downloader.R;
import com.mtma.insta.downloader.data.InstaContractor.MediaEntry;
import com.mtma.insta.downloader.adapters.DownloadsCursorAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class DownloadsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    private static final String LOG_TAG = DownloadsActivity.class.getSimpleName();
    private Context mContext;
    private SharedPreferences mSharedPreference;


    private TextView mTitleTextView;
    private ImageView mBackUpButton;
    private LinearLayout mEmptyView;
    private ListView mListView;
    private ProgressBar mProgressBar;


    private DownloadsCursorAdapter mDownloadsCursorAdapter; // adapter for the semester items.


    private static final int MEDIA_LOADER = 0; // number for the semester loader.


    private String mAppLanguage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // TODO: add a thumbnails to any media to the app internal file to reduce lag.

        // initialize global variable values.
        initializeGlobalVariables();


        // set layout direction depend on app language.
        setLayoutLanguage();


        // initialize views ids.
        initializeViewIds();


        // set the correct direction for the back up button and the activity title.
        setToolbarViews();


        // setup the list view and by add a the adapter to it and handle clicking on its items.
        setupListView();


        // start a Loader for displaying data from the database.
        loadDownloadingMedia();


        // handle clicking on back up button.
        setClickingOnBackUpButton();


    }


    /**
     * Initialize global variables that used over the activity.
     */
    private void initializeGlobalVariables() {

        mContext = DownloadsActivity.this;
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(mContext);
        mAppLanguage = mSharedPreference.getString(Constants.KEY_APP_LANGUAGE, Constants.KEY_APP_LANGUAGE_DEFAULT_VALUE);

    }


    /**
     * Check the current language that the user selected from settings and determine the layout
     * direction (RtL or LtR).
     */
    private void setLayoutLanguage() {

        Locale locale = new Locale(mAppLanguage);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

        this.setContentView(R.layout.activity_downloads);

    }


    /**
     * Initialize the views ids from the main layout of the activity.
     */
    private void initializeViewIds() {

        mTitleTextView = findViewById(R.id.tool_bar_title);
        mBackUpButton = findViewById(R.id.tool_bar_back_up_button);
        mListView = findViewById(R.id.activity_downloads_list_view);
        mProgressBar = findViewById(R.id.activity_download_progress_bar);
        mEmptyView = findViewById(R.id.activity_downloads_empty_view);

    }


    /**
     * Display the title name of the Activity &
     * Change the back up direction (RtL or LtR) if the current app language not english.
     */
    private void setToolbarViews() {

        mTitleTextView.setText(R.string.downloads_title);

        if(mAppLanguage.equals(Constants.KEY_APP_LANGUAGE_ARABIC_VALUE)) {
            mBackUpButton.setRotation(-45f);
        }

    }


    /**
     * Add the adapter to the ListView and handle clicking on the items inside it.
     */
    private void setupListView() {

        // add the adapter to ListView (it is empty until the Loader add data to it from Cursor)
        mDownloadsCursorAdapter = new DownloadsCursorAdapter(this, null);
        mListView.setAdapter(mDownloadsCursorAdapter);

        // send the user to a new activity to display teh media file/s.
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // to show the ripple on the button when clicking on it.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Intent intent = new Intent(mContext, DisplayingMediaActivity.class);
                        intent.putExtra ("database_id", id);
                        startActivity(intent);

                    }


                }, Constants.CLICKING_DELAYED_TIME_VERY_SMALL_BUTTON);

            }
        });

    }


    /**
     * Handle clicking on the back up button.
     */
    private void setClickingOnBackUpButton() {

        mBackUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // to show the ripple on the button when clicking on it.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onBackPressed();
                    }
                }, Constants.CLICKING_DELAYED_TIME_SMALL_BUTTON);

            }
        });

    }



    private void loadDownloadingMedia() {

        new CleanDataBase().execute();

    }


    private void startMediaLoader() {

        LoaderManager.getInstance(this).initLoader(MEDIA_LOADER, null, this);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle args) {

        switch (loaderID) {

            case MEDIA_LOADER:

                // to make the new ones at the top of the ListView.
                String sortOrder = MediaEntry.COLUMN_UNIX + " DESC";


                return new CursorLoader(this,
                        MediaEntry.CONTENT_URI,
                        null, // null because all the table columns will be used.
                        null,
                        null,
                        sortOrder);

            default:
                return null;
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        switch (loader.getId()) {

            case MEDIA_LOADER:

                // to show the ripple on the button when clicking on it.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        mProgressBar.setVisibility(View.GONE);
                        mDownloadsCursorAdapter.swapCursor(cursor);

                        // to make the empty view no show directly but let the progress bar showed for
                        // a little bit time.
                        mListView.setEmptyView(mEmptyView);

                    }
                }, Constants.CLICKING_DELAYED_TIME_PROGRESS_BAR);

                break;

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mDownloadsCursorAdapter.swapCursor(null);
    }






    /**
     * Get the User data from Instagram server in background and display his data (user name, real
     * name and profile picture) on the screen (specific inside drawer).
     */
    private class CleanDataBase extends AsyncTask<Void, Void, ArrayList<Long>> {

        private CleanDataBase() {

        }

        @Override
        protected ArrayList<Long> doInBackground(Void... voids) {

            ArrayList<Long> idsWillBeDeleted = null;

            try {

                idsWillBeDeleted = new ArrayList<>();
                for (int i = 0; i < 4; i++) {

                    String[] projection = {MediaEntry._ID, MediaEntry.COLUMN_MEDIA_CODE};
                    String selection = MediaEntry.COLUMN_MEDIA_PRODUCT_TYPE + " = ?";
                    String[] selectionArgs = {String.valueOf(i)};


                    Cursor cursor = getContentResolver().query(
                            MediaEntry.CONTENT_URI,
                            projection,
                            selection,
                            selectionArgs,
                            null);



                    while (cursor.moveToNext()) {


                        int mediaIdColumnIndex = cursor.getColumnIndexOrThrow(MediaEntry._ID);
                        int mediaCodeColumnIndex = cursor.getColumnIndexOrThrow(MediaEntry.COLUMN_MEDIA_CODE);

                        long mediaId = cursor.getLong(mediaIdColumnIndex);
                        String mediaCode = cursor.getString(mediaCodeColumnIndex);


                        idsWillBeDeleted.add(mediaId);

                        // get the folder path contains the media file/s.
                        File folderPath = new File(MediaEntry.getMediaPath(i));
                        if (!folderPath.exists()) {
                            folderPath.mkdirs();
                        }

                        // get the required file/s and put them inside an ArrayList.
                        File[] files = folderPath.listFiles();
                        if (files != null) {

                            for (File file : files) {
                                if (file.getName().startsWith(mediaCode)) {

                                    idsWillBeDeleted.remove(mediaId);

                                }
                            }

                        }


                    }

                    cursor.close();


                }


            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception from try-catch block inside GetAppUserInfo class inside " + LOG_TAG + " class in doInBackground method.", e);
            }

            return idsWillBeDeleted;
        }


        @Override
        protected void onPostExecute(ArrayList<Long> idsWillBeDeleted) {

            if (idsWillBeDeleted != null && idsWillBeDeleted.size() != 0) {


                StringBuilder selection = new StringBuilder(MediaEntry._ID + " IN (");

                for (int i = 0; i < idsWillBeDeleted.size(); i++) {

                    if (i == idsWillBeDeleted.size() -1) {
                        selection.append(idsWillBeDeleted.get(i)).append(")");
                    } else {
                        selection.append(idsWillBeDeleted.get(i)).append(", ");
                    }

                }


                getContentResolver().delete(MediaEntry.CONTENT_URI, selection.toString(), null);


            }


            startMediaLoader();

        }



    }

}