package com.mtma.insta.downloader.activities;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.mtma.insta.downloader.Constants;
import com.mtma.insta.downloader.R;
import com.mtma.insta.downloader.data.InstaContractor.MediaEntry;
import com.mtma.insta.downloader.models.DownloadingObject;
import com.mtma.insta.downloader.models.SavedPostObject;
import com.mtma.insta.downloader.adapters.SavedPostsAdapter;
import com.mtma.insta.downloader.utils.CollectionUtil;
import com.mtma.insta.downloader.utils.DownloadUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Locale;


public class SavedPostsActivity extends AppCompatActivity {


    private static final String LOG_TAG = SavedPostsActivity.class.getSimpleName();
    private Context mContext;
    private SharedPreferences mSharedPreference;


    private TextView mTitleTextView;
    private ImageView mBackUpButton;
    private GridView mGridView;
    private ProgressBar mProgressBar;
    private LinearLayout mEmptyView;
    private TextView mActivityStateTextView;
    private TextView mActivityStateHelperButton;
    private TextView mDownloadButton;


    private SavedPostsAdapter mSavedPostsAdapter;


    private String mAppLanguage;
    private Boolean mIsLoggedIn;
    ArrayList<Boolean> mCheckedItems = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // initialize global variable values.
        initializeGlobalVariables();


        // set layout direction depend on app language.
        setLayoutLanguage();


        // initialize views ids.
        initializeViewIds();


        // set the correct direction for the back up button and the activity title.
        setToolbarViews();


        // show the activity state on the screen ()
        setupActivityState();


        // handle clicking on back up button.
        setClickingOnBackUpButton();


        // handle clicking on the Download button.
        setClickingOnDownloadButton();


        // handle clicking on the button that exist inside the empty view in case there is not
        // items to load inside the GridView.
        setClickingOnButtonInsideEmptyView();


        // handle clicking on the GridView items.
        handleClickingOnGridViewItems();


//        AdsManager.showBannerAd(this);

    }


    /**
     * Initialize global variables that used over the activity.
     */
    private void initializeGlobalVariables() {

        mContext = SavedPostsActivity.this;
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(mContext);
        mAppLanguage = mSharedPreference.getString(Constants.KEY_APP_LANGUAGE, Constants.KEY_APP_LANGUAGE_DEFAULT_VALUE);
        mIsLoggedIn = mSharedPreference.getBoolean(Constants.KEY_LOGIN, Constants.KEY_LOGIN_DEFAULT_VALUE);

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

        this.setContentView(R.layout.activity_saved_posts);

    }


    /**
     * Initialize the views ids from the main layout of the activity.
     */
    private void initializeViewIds() {

        mTitleTextView = findViewById(R.id.tool_bar_title);
        mBackUpButton = findViewById(R.id.tool_bar_back_up_button);
        mDownloadButton = findViewById(R.id.activity_saved_posts_button_download);
        mGridView = findViewById(R.id.activity_saved_posts_grid_view);
        mProgressBar = findViewById(R.id.activity_saved_posts_progress_bar);
        mEmptyView = findViewById(R.id.activity_saved_posts_empty_view);
        mActivityStateTextView = findViewById(R.id.activity_saved_posts_activity_state);
        mActivityStateHelperButton = findViewById(R.id.activity_saved_posts_text_button);

    }


    /**
     * Display the title name of the Activity &
     * Change the back up direction (RtL or LtR) if the current app language not english.
     */
    private void setToolbarViews() {

        mTitleTextView.setText(R.string.saved_posts_title);

        if(mAppLanguage.equals(Constants.KEY_APP_LANGUAGE_ARABIC_VALUE)) {
            mBackUpButton.setRotation(-45f);
        }

    }


    /**
     * That method show the Activity State:
     * in case the user not logged in
     * in case there is no internet connection
     * otherwise above show the saved posts on the screen (if exist).
     */
    public void setupActivityState() {

        // in case the user not logged in.
        if (!mIsLoggedIn) {

            // to show the ripple on the button when clicking on it.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    mProgressBar.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                    mActivityStateTextView.setText(R.string.login_to_display_saved_posts);
                    mActivityStateHelperButton.setText(R.string.login_button_with_under_line);

                }
            }, Constants.CLICKING_DELAYED_TIME_PROGRESS_BAR);

        }
        // in case there is no internet connection.
        else if (!isNetworkAvailable()) {

            // to show the ripple on the button when clicking on it.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    mProgressBar.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                    mActivityStateTextView.setText(R.string.no_internet_connection);
                    mActivityStateHelperButton.setText(R.string.refresh_button_with_under_line);

                }
            }, Constants.CLICKING_DELAYED_TIME_PROGRESS_BAR);

        }
        // otherwise above try to display the saved posts.
        else {
            new GetSavedPostsData().execute();
        }

    }


    /**
     * Handle clicking on the Download button by downloading the selected items the user choose.
     */
    private void setClickingOnDownloadButton() {

        mDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // the "" value update automatically when there is change in selected items in the
                // GridView and depend on that change and telling the user that he must select some
                // items to start downloading or downloading the selected item.
                if (!mSavedPostsAdapter.hasSelectedItem()) {

                    Toast.makeText(SavedPostsActivity.this, R.string.toast_select_item_first, Toast.LENGTH_SHORT).show();

                } else if(!hasGrantedStoragePermission()) {

                    requestStoragePermission();

                } else {

                    // get the selected items Urls and send them to the background thread to start
                    // downloading them.
                    String[] selectedSavedPostsUrls = mSavedPostsAdapter.getSelectedSavedPostsUrls();
                    new DownloadSelectedSavedPost().execute(selectedSavedPostsUrls);

                }

            }
        });

    }


    /**
     * Ask user to give the app access to the required files to save downloaded media inside it.
     */
    private void requestStoragePermission() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {

            ActivityCompat.requestPermissions(SavedPostsActivity.this, new String[]{
                    Manifest.permission.ACCESS_MEDIA_LOCATION}, 0);

        } else {

            ActivityCompat.requestPermissions(SavedPostsActivity.this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

    }


    /**
     * Check if the user give the app the permission to access to the required files in storage or not.
     *
     * @return true : means the permission granted, false : means not granted.
     */
    private boolean hasGrantedStoragePermission() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {

            int result = ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_MEDIA_LOCATION);

            if (result == PackageManager.PERMISSION_DENIED) {
                return false;
            }

        } else {

            int result1 = ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE);
            int result2 = ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (result1 == PackageManager.PERMISSION_DENIED && result2 == PackageManager.PERMISSION_DENIED) {

                return false;

            }
        }


        return true;

    }



    /**
     * Inside EmptyView layout there is a button with them (can be a LOGIN button when the user not
     * logged in) or (REFRESH button when the user not connecting to the internet).
     */
    private void setClickingOnButtonInsideEmptyView() {

        mActivityStateHelperButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // get the text that put inside the TextView and depend on the text we decide if
                // it is a Login button or a Refresh button.
                String textInButton = mActivityStateHelperButton.getText().toString();
                if (textInButton.equals(getString(R.string.refresh_button_with_under_line))) {

                    // refresh layout and try to display the saved posts.
                    new GetSavedPostsData().execute();

                } else if (textInButton.equals(getString(R.string.login_button_with_under_line))) {

                    // open the Instagram official Login (when he logged in we send him to the MainActivity).
                    Intent intent = new Intent(SavedPostsActivity.this, InstagramOfficialLoginActivity.class);
                    startActivity(intent);
                    finish();

                }

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


    /**
     * Check if there is internet connection or not.
     *
     * @return (true) means there is internet connection, (false) means no internet connection.
     */
    private boolean isNetworkAvailable() {

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }


    /**
     * Handle clicking on the GridView items and that decide select the item or not.
     */
    private void handleClickingOnGridViewItems() {

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // change the item state between selected and not selected.
                mSavedPostsAdapter.changeCheckedItemState(position);

                // change the button background to make visible as enable or disable.
                Drawable drawable;
                if (mSavedPostsAdapter.hasSelectedItem()) {
                    drawable = getResources().getDrawable(R.drawable.download_button);
                } else {
                    drawable = getResources().getDrawable(R.drawable.download_button_disabled);
                }
                mDownloadButton.setBackground(drawable);

            }
        });
    }


    /**
     * Get the SavedPosts Data and Display them on the Screen if exist.
     */
    public class GetSavedPostsData extends AsyncTask<Void, Void, ArrayList<SavedPostObject>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // hide the empty view and show only the progress bar.
            mEmptyView.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);

        }

        @Override
        protected ArrayList<SavedPostObject> doInBackground(Void... voids) {

            // get SavedPostObjects that contains the required data to display inside the
            // items in the GridView.
            ArrayList<SavedPostObject> savedPostObjectArrayList = null;
            try {
                savedPostObjectArrayList = CollectionUtil.getSavedPostsData(mContext);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception from try-catch block inside " + LOG_TAG + " class inside GetSavedPostsData class in doInBackground method.", e);
            }

            return savedPostObjectArrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<SavedPostObject> savedPostObjectArrayList) {

            // hide the progress bar.
            mProgressBar.setVisibility(View.GONE);

            // in case something went wrong when try to get saved posts and the list was equal null.
            if (savedPostObjectArrayList == null) {

                mEmptyView.setVisibility(View.VISIBLE);
                mActivityStateTextView.setText(R.string.unknown_error);
                mActivityStateHelperButton.setText(R.string.refresh_button_with_under_line);

            }
            // in case there is no saved posts.
            else if (savedPostObjectArrayList.size() == 0) {

                mEmptyView.setVisibility(View.VISIBLE);
                mActivityStateTextView.setText(R.string.no_saved_posts);
                mActivityStateHelperButton.setText("");

            }
            // in case there is saved posts we displaying them as items inside the GridView.
            else {


                // for selecting items for downloading process.
                for (int i = 0 ; i < savedPostObjectArrayList.size() ; i++) {
                    mCheckedItems.add(false);
                }

                // initialize the SavedPostsAdapter and add it to the GridView.
                mSavedPostsAdapter = new SavedPostsAdapter(mContext, savedPostObjectArrayList, mCheckedItems);
                mGridView.setAdapter(mSavedPostsAdapter);


            }


        }


    }




    /**
     * Download Selected SavedPosts items.
     */
    public class DownloadSelectedSavedPost extends AsyncTask<String, Integer, ArrayList<DownloadingObject>> {

        private ProgressDialog progressDialog;

        protected void onPreExecute() {
            super.onPreExecute();

            // create and initialize the progress dialog for the downloading process.
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setTitle("Start Downloading");
            progressDialog.setProgress(0);
            progressDialog.setProgressStyle(1);
            progressDialog.setSecondaryProgress(20);
            progressDialog.setMax(100);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

        }


        @Override
        protected ArrayList<DownloadingObject> doInBackground(String... urls) {


            // List for all DownloadingObject data.
            ArrayList<DownloadingObject> downloadingObjectsList = new ArrayList<>();

            for (int i = 0; i < urls.length; i++) {


                // get each DownloadingObject data from server by the url for the each media.
                DownloadingObject downloadingObject = DownloadUtil.getPostOrReelOrIgtvData(mContext, urls[i]);

                // try to save the media files inside the device.
                try {
                    
                    // get all media urls from the DownloadingObject and download each one of them
                    // if there is more one media.
                    ArrayList<String> allMediaUrls = downloadingObject.getAllMediaUrls();
                    for (int j = 0 ; j < allMediaUrls.size() ; j++) {

                        // get the media url and start to downloading it.
                        String urlString = allMediaUrls.get(j);
                        URL url = new URL(urlString);
                        URLConnection connection = url.openConnection();
                        connection.connect();

                        // get the file length which will be use to determine the progress bar process.
                        int lengthOfFile = connection.getContentLength();


                        // create InputStream for downloading the media.
                        InputStream inputStream = new BufferedInputStream(url.openStream());


                        // determine the folder that the media will saved at depend on the product type (Post - Reel - IGTV).
                        int productType = downloadingObject.getProductType();
                        File folderPath = new File(MediaEntry.getMediaPath(productType));
                        if (!folderPath.exists()) {
                            folderPath.mkdirs();
                        }


                        // determine the media extension depend on the media type (image - video).
                        String mediaExtension;
                        if(urlString.contains(".mp4")) {
                            mediaExtension = ".mp4";
                        } else {
                            mediaExtension = ".jpg";
                        }


                        // create the file name (start with unique code for media and the extension above at the end).
                        String mediaCode = downloadingObject.getMediaCode();
                        String fileName = mediaCode + "_" + j + mediaExtension;


                        // the file path for save the media at.
                        File filePath = new File(folderPath, fileName);


                        // save the media inside the file path created above.
                        OutputStream outputStream = new FileOutputStream(filePath);


                        // to determine the downloading progress progress in dialog bar.
                        byte[] data = new byte[1024];
                        long total = 0;
                        int count;
                        while ((count = inputStream.read(data)) != -1) {
                            total += count;
                            publishProgress((int) ((total * 100) / lengthOfFile));
                            outputStream.write(data, 0, count);
                        }


                        // close the InputStream and OutputStream after the downloading process finished.
                        outputStream.flush();
                        outputStream.close();
                        inputStream.close();

                        // scan the file path to make the media visible at the gallery.
                        MediaScannerConnection.scanFile(mContext, new String[]{filePath.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {

                            }
                        });

                    }


                } catch (Exception e) {
                    Log.e(LOG_TAG, "Exception from try-catch block number (2) inside " + LOG_TAG + " class in getPostOrReelOrIgtvDataFromGraphqlNode method in DownloadPost inner class.", e);
                }


                // after finish downloading media we add it to the ArrayList.
                downloadingObjectsList.add(downloadingObject);

            }
            
            
            
            // ArrayList contains all DownloadingObject data.
            return downloadingObjectsList;
        }


        /**
         * For update the progress dialog through the downloading process.
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);

            int i;
            if (values[0] < 10) {
                i = 20;
            } else if (values[0] < 20) {
                i = 18;
            } else if (values[0] < 30) {
                i = 16;
            } else if (values[0] < 40) {
                i = 14;
            } else if (values[0] < 50) {
                i = 12;
            } else if (values[0] < 60) {
                i = 10;
            } else if (values[0] < 70) {
                i = 8;
            } else if (values[0] < 80) {
                i = 6;
            } else if (values[0] < 90) {
                i = 4;
            } else if (values[0] < 95) {
                i = 2;
            } else {
                i = 1;
            }

            progressDialog.setSecondaryProgress(values[0] + i);
        }

        @Override
        protected void onPostExecute(ArrayList<DownloadingObject> downloadingObjectList) {

            // no need to the progressDialog after downloading finished.
            progressDialog.dismiss();

            // handle each case:
            // 1- if something comes wrong and the downloadingObject value equal null.
            // 2- if downloading successful.
            if (downloadingObjectList == null || downloadingObjectList.size() == 0) {
                Toast.makeText(mContext, R.string.something_wrong_please_try_again, Toast.LENGTH_SHORT).show();
            } else {


                // save each media data inside the database and the username and his profile picture.
                for (int i = 0; i < downloadingObjectList.size(); i++) {


                    // get the current object inside the loop.
                    DownloadingObject downloadingObject = downloadingObjectList.get(i);

                    // save the user profile picture inside the app data (not appears inside gallery).
                    saveUserProfilePic(downloadingObject.getUserName(), downloadingObject.getProfilePicUrl());

                    // save the media data (media id - text & hashtags - ...etc).
                    saveMediaDataInsideDatabase(downloadingObject);

                }

                // hint the user that the download successful after finished.
                Toast.makeText(mContext, R.string.toast_media_saved_successfully, Toast.LENGTH_SHORT).show();

            }

        }

    }





    /**
     * Download the user profile picture and save it inside the app data file (not appears inside
     * media apps), the image will be saved with the username as a name of the file.
     *
     * @param userName the user name who own the media.
     * @param profilePicUrl the profile picture url.
     */
    private void saveUserProfilePic(String userName, String profilePicUrl) {

        // access to the app data file and create a file "thumbnails/profile_pictures" (if not created before)
        // which contains all profile picture for users who has downloaded media before.
        File thumbnailPath = new File(Constants.PATH_THUMBNAILS_PROFILE_PICS_DIRECTORY);
        if (!thumbnailPath.exists()) {
            thumbnailPath.mkdirs();
        }


        // create a file with name the username & if there is a file with that name, it will be
        // deleted and create a new one.
        String fileName = userName + ".jpg";
        File newProfilePic = new File(thumbnailPath, fileName);
        if(newProfilePic.exists()) {
            newProfilePic.delete();
        }


        // save the profile picture inside the "thumbnails" file.
        Glide.with(mContext).asBitmap().load(profilePicUrl).thumbnail(1f).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                try {
                    FileOutputStream out = new FileOutputStream(newProfilePic);
                    resource.compress(Bitmap.CompressFormat.JPEG, 25, out);
                    out.flush();
                    out.close();

                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

    }


    /**
     * Store the media data like:
     * media code => ex: Cdf56sj8jLds
     * media type => ex: image - video - multiple media.
     * product type => ex: post - reel - igtv - story.
     * media text & hashtag => ex: caption on the post or the hashtags.
     * username => ex: the username for the media owner.
     * unix time => ex: the time when the media saved to the database.
     *
     * @param downloadingObject object contains all media data.
     */
    private void saveMediaDataInsideDatabase(DownloadingObject downloadingObject) {

        // get the required data from the DownloadingObject and store them in variables.
        String mediaCode = downloadingObject.getMediaCode();
        int mediaType = downloadingObject.getMediaType();
        int productType = downloadingObject.getProductType();
        String textAndHashtags = downloadingObject.getTextAndHashtags();
        String userName = downloadingObject.getUserName();
        long currentTime = System.currentTimeMillis() / 1000;


        // create ContentValues object and put the data above inside it.
        ContentValues values = new ContentValues();
        values.put(MediaEntry.COLUMN_MEDIA_CODE, mediaCode);
        values.put(MediaEntry.COLUMN_MEDIA_TYPE, mediaType);
        values.put(MediaEntry.COLUMN_MEDIA_PRODUCT_TYPE, productType);
        values.put(MediaEntry.COLUMN_MEDIA_TEXT_AND_HASHTAG, textAndHashtags);
        values.put(MediaEntry.COLUMN_USER_NAME, userName);
        values.put(MediaEntry.COLUMN_UNIX, currentTime);



        // insert a new row to the database contains the media data.
        Uri uri = mContext.getContentResolver().insert(MediaEntry.CONTENT_URI, values);
        if (uri == null) {
            Log.i(LOG_TAG, "saving media failed !");
        } else {
            Log.i(LOG_TAG, "saving media successfully");
        }


    }




}