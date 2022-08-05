package com.mtma.insta.downloader.activities;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.tabs.TabLayout;
import com.mtma.insta.downloader.Constants;
import com.mtma.insta.downloader.R;
import com.mtma.insta.downloader.data.InstaContractor.MediaEntry;
import com.mtma.insta.downloader.models.DownloadingObject;
import com.mtma.insta.downloader.adapters.DisplayStoryViewPagerAdapter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Locale;


public class DisplayingStoryActivity extends AppCompatActivity {


    private static final String LOG_TAG = DisplayingStoryActivity.class.getSimpleName();
    private Context mContext;
    private SharedPreferences mSharedPreference;


    private ImageView mBackUpButton;
    private ImageView mDownloadButton;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;


    private DisplayStoryViewPagerAdapter mViewPagerAdapter;


    private String mAppLanguage;
    private int mCurrentPosition;
    private ArrayList<DownloadingObject> mDownloadingObjects;


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


        // handle clicking on back up button.
        setupClickingOnBackUpButton();


        // handle clicking on the Download button.
        setupClickingOnDownloadButton();


        // Setup the ViewPager and the Adapter and the TabLayout.
        setupViewPagerAndAdapter();


    }


    /**
     * Initialize global variables that used over the activity.
     */
    private void initializeGlobalVariables() {

        // initialize the activity context and the preference and the current app language.
        mContext = DisplayingStoryActivity.this;
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(mContext);
        mAppLanguage = mSharedPreference.getString(Constants.KEY_APP_LANGUAGE, Constants.KEY_APP_LANGUAGE_DEFAULT_VALUE);



        mCurrentPosition = getIntent().getIntExtra("current_position", 0);
        mDownloadingObjects = (ArrayList<DownloadingObject>) getIntent().getSerializableExtra("downloading_objects");


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

        this.setContentView(R.layout.activity_displaying_story);

    }


    /**
     * Initialize the views ids from the main layout of the activity.
     */
    private void initializeViewIds() {

        mBackUpButton = findViewById(R.id.activity_displaying_story_back_button);
        mDownloadButton = findViewById(R.id.activity_displaying_story_download_button);
        mViewPager = findViewById(R.id.activity_displaying_story_view_pager);
        mTabLayout = findViewById(R.id.activity_displaying_story_tab_layout);

    }


    /**
     * Display the title name of the Activity &
     * Change the back up direction (RtL or LtR) if the current app language not english.
     */
    private void setToolbarViews() {

        if(mAppLanguage.equals(Constants.KEY_APP_LANGUAGE_ARABIC_VALUE)) {
            mBackUpButton.setRotation(-45f);
        }

    }


    /**
     * Handle clicking on the back up button.
     */
    private void setupClickingOnBackUpButton() {

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
     * Handle clicking on the back up button.
     */
    private void setupClickingOnDownloadButton() {

        mDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // to show the ripple on the button when clicking on it.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (!hasGrantedStoragePermission()) {

                            requestStoragePermission();

                        } else {

                            int currentPosition = mViewPager.getCurrentItem();
                            new DownloadStory().execute(mDownloadingObjects.get(currentPosition));

                        }



                    }
                }, Constants.CLICKING_DELAYED_TIME_SMALL_BUTTON);

            }
        });

    }



    /**
     * Ask user to give the app access to the required files to save downloaded media inside it.
     */
    private void requestStoragePermission() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {

            ActivityCompat.requestPermissions(DisplayingStoryActivity.this, new String[]{
                    Manifest.permission.ACCESS_MEDIA_LOCATION}, 0);

        } else {

            ActivityCompat.requestPermissions(DisplayingStoryActivity.this, new String[]{
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
     * Setup the relation between the ViewPager and the Adapter and the TabLayout.
     */
    private void setupViewPagerAndAdapter() {

        // add the adapter to the ViewPager.
        mViewPagerAdapter = new DisplayStoryViewPagerAdapter(getSupportFragmentManager(), mContext, mDownloadingObjects);
        mViewPager.setAdapter(mViewPagerAdapter);

        // make the adapter save 4 pages in memory to make scroll process more smooth.
        mViewPager.setOffscreenPageLimit(4);

        // set the adapter at the story position that the user clicked on.
        mViewPager.setCurrentItem(mCurrentPosition);

        // add the ViewPager to the TabLayout.
        mTabLayout.setupWithViewPager(mViewPager, true);

    }


    /**
     * Download the Story and the data for it in the device.
     */
    private class DownloadStory extends AsyncTask<DownloadingObject, Integer, DownloadingObject> {


        private ProgressDialog progressDialog;

        private DownloadStory() {
        }

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
        protected DownloadingObject doInBackground(DownloadingObject... downloadingObjects) {


            try {

                // get the first Object from the Array (always there is only one object provide).
                DownloadingObject downloadingObject = downloadingObjects[0];
                Log.i(LOG_TAG, "Unix   " + "downloadingObject contains  :  " + downloadingObject);

                // if the downloadingObject is empty or not and if the response from the server
                // said that the user need to login first, then no need to complete method.
                if (downloadingObject == null || downloadingObject.isNeedLogin()) {
                    return downloadingObject;
                }

                // get the story url and because there is only one url inside the ArrayList we get
                // the one in position 0 and get start to download it.
                String urlString = downloadingObject.getAllMediaUrls().get(0);
                URL url = new URL(urlString);
                URLConnection connection = url.openConnection();
                connection.connect();

                // get the file length which will be use to determine the progress bar process.
                int lengthOfFile = connection.getContentLength();


                // create InputStream for downloading the media.
                InputStream inputStream = new BufferedInputStream(url.openStream());


                // determine the folder that the media will saved at and it will be always Stories.
                File folderPath = new File(MediaEntry.MEDIA_FILE_PATH_STORY);
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
                String fileName = mediaCode + mediaExtension;


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


                return downloadingObject;


            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception from try-catch block number (2) inside " + LOG_TAG + " class in getPostOrReelOrIgtvDataFromGraphqlNode method in DownloadPost inner class.", e);
            }

            return null;
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
        protected void onPostExecute(DownloadingObject downloadingObject) {

            // no need to the progressDialog after downloading finished.
            progressDialog.dismiss();

            // handle each case:
            // 1- if something comes wrong and the downloadingObject value equal null.
            // 3- if downloading successful.
            if (downloadingObject == null) {
                Toast.makeText(mContext, R.string.something_wrong_please_try_again, Toast.LENGTH_SHORT).show();
            } else {

                // save the user profile picture inside the app data (not appears inside gallery).
                saveUserProfilePic(downloadingObject.getUserName(), downloadingObject.getProfilePicUrl());

                // save the media data (media id - text & hashtags - ...etc).
                saveMediaDataInsideDatabase(downloadingObject);

                // hint the user that the download successful after finished.
                Toast.makeText(mContext, R.string.toast_media_saved_successfully, Toast.LENGTH_SHORT).show();

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



}










