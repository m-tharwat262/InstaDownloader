package com.mtma.insta.downloader.activities;


import android.Manifest;
import android.app.Dialog;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mtma.insta.downloader.BuildConfig;
import com.mtma.insta.downloader.Constants;
import com.mtma.insta.downloader.R;
import com.mtma.insta.downloader.data.InstaContractor;
import com.mtma.insta.downloader.data.InstaContractor.MediaEntry;
import com.mtma.insta.downloader.models.AppUserObject;
import com.mtma.insta.downloader.models.DownloadingObject;
import com.mtma.insta.downloader.fragments.SavedPostsFragment;
import com.mtma.insta.downloader.fragments.StoriesFragment;
import com.mtma.insta.downloader.utils.DownloadUtil;
import com.mtma.insta.downloader.utils.DownloadStoryUtil;
import com.mtma.insta.downloader.utils.UserUtil;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity {


    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private Context mContext;
    private SharedPreferences mSharedPreference;
    private SharedPreferences.Editor mSharedPreferenceEditor;


    private DrawerLayout mDrawer;
    private Toolbar mToolbar;
    private EditText mUrlEditText;
    private TextView mDownloadButton;
    private TextView mPastLinkButton;
    private TextView mLoginButton;
    private LinearLayout mLoginButtonLayout;
    private CircleImageView mProfilePictureImageView;
    private TextView mLoginButtonInDrawer;
    private TextView mRealNameTextView;
    private TextView mUserNameTextView;
    private LinearLayout mRateButton;
    private LinearLayout mShareButton;
    private LinearLayout mHowToUseButton;
    private LinearLayout mSettingsButton;
    private LinearLayout mDownloadsButton;
    private LinearLayout mSavedPostsButton;
    private LinearLayout mLogoutButton;
    private FrameLayout mSavedPostsFrameLayout;
    private ProgressBar mProgressBar;


    private String mAppLanguage;
    private boolean mIsUserLoggedIn;
    private boolean mIsSavedPostsEnabled;
    private boolean doubleBackToExitPressedOnce = false;


    private DatabaseReference mFeedbackDatabaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // initialize global variable values.
        initializeGlobalVariables();


        // set layout direction depend on app language.
        setLayoutLanguage();


        // initialize views ids.
        initializeViewIds();


        // initialize global variables inside the class.
        initializeVariables();


        // initialize the Firebase required objects and variables.
        addDefaultCookieFromFirebase();


        // set the toolbar & the navigation drawer button.
        setToolbar();


        // display stories on the screen.
        setupStoriesFragment();


        // display saved posts on the screen.
        setupSavedPostsFragment();


        // determine if we need to displaying the hint text to tell the user he need to login to
        // displaying his tories and saved posts or not depending on login statue (logged in or not).
        controlDisplayingLoginHintPart();


        // handle clicking on the Past Link button.
        setClickingOnPastLinkButton();


        // handle clicking on the Download button.
        setClickingOnDownloadButton();


        // handle clicking on login button which exist in the main layout (not in drawer).
        setClickingOnLogin();


        // display the user data (real name & user name & profile pid) on the screen (inside drawer).
        displayUserData();


        // determine if we need to displaying the logout button inside drawer or not.
        controlDisplayingLogoutInsideDrawer();


        // handle clicking on any buttons inside the drawer.
        setupClickingOnDrawerButtons();


        // handle coming intent form the instagram app.
        handleInstagramInComingIntent();


        // ask the user if he don't give the app the permission to access to the Storage.
        if(!hasGrantedStoragePermission()) {
            requestStoragePermission();
        }



//        AdsManager.showBannerAd(this);




    }


    /**
     * Initialize global variables that used over the activity.
     */
    private void initializeGlobalVariables() {

        mContext = MainActivity.this;
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(mContext);
        mSharedPreferenceEditor = mSharedPreference.edit();
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

        this.setContentView(R.layout.activity_main);

    }


    /**
     * Initialize the views ids from the main layout of the activity.
     */
    private void initializeViewIds() {

        mDrawer = findViewById(R.id.activity_main_drawer_layout);
        mToolbar = findViewById(R.id.activity_main_layout_toolbar);

        mUrlEditText = findViewById(R.id.activity_main_layout_link_field);
        mDownloadButton = findViewById(R.id.activity_main_layout_download_button);
        mPastLinkButton = findViewById(R.id.activity_main_layout_paste_link_button);

        mLoginButtonLayout = findViewById(R.id.activity_main_layout_layout_contain_login);
        mLoginButton = findViewById(R.id.activity_main_layout_login_button);

        mSavedPostsFrameLayout = findViewById(R.id.activity_main_layout_saved_posts_frame_layout);

        mProfilePictureImageView = findViewById(R.id.navigation_drawer_profile_picture);
        mLoginButtonInDrawer = findViewById(R.id.navigation_drawer_login_button);
        mRealNameTextView = findViewById(R.id.navigation_drawer_user_real_name);
        mUserNameTextView = findViewById(R.id.navigation_drawer_user_name);
        mRateButton = findViewById(R.id.navigation_drawer_rate_button);
        mShareButton = findViewById(R.id.navigation_drawer_share_button);
        mHowToUseButton = findViewById(R.id.navigation_drawer_how_to_use_button);
        mSettingsButton = findViewById(R.id.navigation_drawer_settings_button);
        mDownloadsButton = findViewById(R.id.navigation_drawer_downloads_button);
        mSavedPostsButton = findViewById(R.id.navigation_drawer_saved_posts_button);
        mLogoutButton = findViewById(R.id.navigation_drawer_logout_button);

        mProgressBar = findViewById(R.id.activity_main_layout_progress_bar);

    }

    /**
     * Initialize the global variables inside the class.
     */
    private void initializeVariables() {

        mIsUserLoggedIn =  mSharedPreference.getBoolean(Constants.KEY_LOGIN, Constants.KEY_LOGIN_DEFAULT_VALUE);
        mIsSavedPostsEnabled = mSharedPreference.getBoolean(Constants.KEY_ENABLE_SAVED_POSTS, Constants.KEY_ENABLE_SAVED_POSTS_DEFAULT_VALUE);

    }


    /**
     * Get a default value for cookie from real account login that added to the database
     * inside firebase.
     */
    private void addDefaultCookieFromFirebase() {

        String cookie = mSharedPreference.getString(Constants.KEY_COOKIE,null);

        // in case the cookie inside preference is null and user not logged in.
        if (!mIsUserLoggedIn && cookie == null) {

            // initialize the Firebase required objects.
            mFeedbackDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.NODE_LOGIN);

            // add default value to userId and sessionId in preference.
            mFeedbackDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {


                    // check first if the firebase give us the permission to use the cookie form it
                    // or not before going to get and save it inside SharedPreference.
                    boolean isValidCookie = false;
                    try {
                        isValidCookie = snapshot.child(Constants.NODE_IS_VALID_COOKIE).getValue(Boolean.class);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Exception from try-catch block inside " + LOG_TAG + " class in addDefaultCookieFromFirebase method.", e);
                    }

                    if (isValidCookie) {

                        String userId = snapshot.child(Constants.NODE_USER_ID).getValue(String.class);
                        String sessionId = snapshot.child(Constants.NODE_SESSION_ID).getValue(String.class);
                        String cookie = snapshot.child(Constants.NODE_COOKIE).getValue(String.class);

                        mSharedPreferenceEditor.putString(Constants.KEY_DEFAULT_USER_ID, userId);
                        mSharedPreferenceEditor.putString(Constants.KEY_DEFAULT_SESSION_ID, sessionId);
                        mSharedPreferenceEditor.putString(Constants.KEY_COOKIE, cookie);
                        mSharedPreferenceEditor.commit();

                    }


                }

                @Override
                public void onCancelled(DatabaseError error) {

                }
            });

        }


    }


    /**
     * Set the toolbar view and the navigation drawer button.
     */
    private void setToolbar() {

        setSupportActionBar(mToolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.open_drawer_action, R.string.close_drawer_action);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
        mToolbar.setNavigationIcon(R.drawable.navigation_icon);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

    }


    /**
     * Display stories Fragment content on the screen that display the user stories if he logged in
     * or a fake items if he don't.
     */
    private void setupStoriesFragment() {

        StoriesFragment storiesFragment = new StoriesFragment(mContext);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_main_layout_stories_frame_layout, storiesFragment)
                .commit();

    }

    /**
     * Display saved posts Fragment content on the screen that display the user stories if he logged in
     * or a fake items if he don't.
     * Sometimes user want to hide his saved posts (done inside settings) then the FrameLayout for
     * the fragment must be empty.
     */
    private void setupSavedPostsFragment() {

        if (!mIsSavedPostsEnabled) {

            mSavedPostsFrameLayout.removeAllViews();
            mSavedPostsFrameLayout.setVisibility(View.GONE);

        } else {

            mSavedPostsFrameLayout.removeAllViews();

            SavedPostsFragment savedPostsFragment = new SavedPostsFragment(MainActivity.this);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_main_layout_saved_posts_frame_layout, savedPostsFragment)
                    .commit();

        }

    }


    /**
     * Determine if the user logged in so we need to display to tell him a hint that he need to
     * login to display his stories or his saved posts.
     */
    private void controlDisplayingLoginHintPart() {

        if(!mIsUserLoggedIn) {
            mLoginButtonLayout.setVisibility(View.VISIBLE);
        } else {
            mLoginButtonLayout.setVisibility(View.GONE);
        }

    }

    /**
     * Handle Clicking on the Login button in the main screen below the hint text which tell the
     * user that he need to login for displaying his stories and saved posts.
     */
    private void setClickingOnLogin() {

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, InstagramOfficialLoginActivity.class);
                startActivity(intent);
            }
        });

    }

    /**
     * Handle Clicking on Paste Link button by getting the text form the clipboard (if there is)
     * and paste it to the link field EditText.
     */
    private void setClickingOnPastLinkButton() {

        mPastLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // get the clipboard form system service.
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                // hint the user if there is no url in clipboard ,  otherwise paste
                // the text to the url field EditText.
                if ( !(clipboard.hasPrimaryClip()) ) {

                    Toast.makeText(mContext, R.string.toast_no_link_in_clipboard, Toast.LENGTH_SHORT).show();

                } else if ( !(clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) ) {

                    ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                    String pastData = item.getText().toString();
                    mUrlEditText.setText(pastData);

                } else {

                    ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                    String pastData = item.getText().toString();
                    mUrlEditText.setText(pastData);

                }

            }
        });


    }

    /**
     * Handle Clicking on Download button by starting the @startDownloadingProcess which responsible
     * to the downloading process and the requirements for it.
     */
    private void setClickingOnDownloadButton() {

        mDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startDownloadingProcess();

            }
        });

    }

    /**
     * Check if all requirements provided and start downloading media after that by the following steps:
     * 1- check if the storage permission granted or not (if not request to grant it).
     * 2- check if the EditText field has text (url) (if not hint the user with that by toast).
     * 3- check if the EditText field has valid url (if not hint the user with that by toast).
     * 4- check if there is internet connection (if not hint the user about it by toast).
     * 5- determine the type of media that the user is up to download (stories or post).
     *
     * Hint : type post in step 5 including (reels- IGTVs).
     */
    private void startDownloadingProcess() {

        if(!hasGrantedStoragePermission()) {

            requestStoragePermission();

        } else {

            String url = mUrlEditText.getText().toString().trim();

            if (url.isEmpty()) {

                Toast.makeText(MainActivity.this, R.string.toast_paste_the_url_first, Toast.LENGTH_SHORT).show();

            } else if ( !url.matches("https://www.instagram.com/(.*)") && !url.matches("https://instagram.com/(.*)") ) {

                Toast.makeText(MainActivity.this, R.string.toast_not_valid_url, Toast.LENGTH_SHORT).show();

            } else if (!isNetworkAvailable()) {

                Toast.makeText(mContext, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();

            } else if (isMediaDownloadedBefore(url)) {

                // Hint: There is nothing to do here because the method isMediaDownloadedBefore(url)
                // in the condition statement will control what should happen here and i do it like
                // that to make the code shorter.

            }

            else {

                // make the url ready to use it to get data from api.
                String[] data = url.split(Pattern.quote("?"));
                String finalUrl = data[0];
                Log.i("Unix", "the post url is : " + finalUrl);
                // if the url contains "stories" word means the media comes form story.
                // else means it is post including (reels & IGTVs).
                if (finalUrl.contains("stories")) {

                    // the user must be logged in to download any story.
                    if(!mIsUserLoggedIn) {

                        // take a little time before tell the user that he must login to download the story.
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                displayLoginDialog(R.string.ask_user_to_log_in_for_download_story);

                            }
                        }, 500);

                    } else {
                        // start downloading the story in background.
                        new DownloadStory(finalUrl).execute();
                    }

                } else {
                    // start downloading the post in background.
                    new DownloadPost(finalUrl).execute();
                }


            }

        }


    }

    /**
     * Check if the media is downloaded before or not and handle what happen in this situation by
     * alert user with that and let him choose if he want to download it again or cancel the process
     *
     * @param url the media url.
     *
     * @return boolean value {true} means the media already downloaded before, {false} means not.
     */
    private boolean isMediaDownloadedBefore(String url) {

        boolean isMediaAlreadyExist = false;

        // split the url and get both the media code and the media product type (post - reel - igtv - story).
        String[] data = url.split(Pattern.quote("?"));
        String firstPartUrl = data[0];
        String[] finalUrl = firstPartUrl.split("/");

        // for get media unique code.
        String mediaCode = finalUrl[finalUrl.length - 1];

        // for get the media product type.
        int mediaProductType;
        if (firstPartUrl.contains("/stories/")) {
            mediaProductType = MediaEntry.MEDIA_PRODUCT_TYPE_STORY;
        } else if (firstPartUrl.contains("/reel/")) {
            mediaProductType = MediaEntry.MEDIA_PRODUCT_TYPE_REEL;
        } else if (firstPartUrl.contains("/tv/")) {
            mediaProductType = MediaEntry.MEDIA_PRODUCT_TYPE_IGTV;
        } else {
            mediaProductType = MediaEntry.MEDIA_PRODUCT_TYPE_POST;
        }



        // access to the database and search inside it if there is matching between
        // the media code and the product type above and the data inside the database.
        String[] projection = {InstaContractor.MediaEntry._ID};
        String selection = MediaEntry.COLUMN_MEDIA_PRODUCT_TYPE + " = ? AND " + MediaEntry.COLUMN_MEDIA_CODE + " = ?";
        String[] selectionArgs = {String.valueOf(mediaProductType), mediaCode};

        Cursor cursor = getContentResolver().query(
                MediaEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);



        // if the cursor not empty means that there is matching, so we check again if the media files
        // still exist too (because maybe the user delete the files from the gallery and the database not refreshed).
        if (cursor.getCount() > 0) {

            // get the folder path contains the media.
            File folderPath = new File(MediaEntry.getMediaPath(mediaProductType));
            if (!folderPath.exists()) {
                folderPath.mkdirs();
            }

            // search in files inside the folder above and check if the files still exist or not.
            File[] files = folderPath.listFiles();
            if (files != null) {

                ArrayList<File> mediaFiles = new ArrayList<>();
                for (File file : files) {
                    if (file.getName().startsWith(mediaCode)) {

                        isMediaAlreadyExist =  true;
                        mediaFiles.add(file);

                    }
                }


                // in case the files still exist, we display a dialog to hint the user about that.
                if (isMediaAlreadyExist) {
                    displayMediaDownloadedBeforeDialog(mediaCode, mediaProductType, folderPath, mediaFiles);
                }

            }


        }

        // close cursor after finish to clear data from the ram.
        cursor.close();

        return isMediaAlreadyExist;
    }

    /**
     * Display a dialog to hint the user that the media already downloaded before and ask him if he
     * want to delete and download it again or cancel the process.
     *
     * @param mediaCode the media unique code.
     * @param mediaProductType the media product type (post - reel - IGTV - story).
     * @param folderPath the folder path contains the media file/s.
     * @param mediaFiles the media files (sometimes more than one because there is posts with
     *                   more than noe media)
     */
    private void displayMediaDownloadedBeforeDialog(String mediaCode, int mediaProductType, File folderPath, ArrayList<File> mediaFiles) {

        Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_default);
        dialog.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        TextView dialogTitle = dialog.findViewById(R.id.log_in_and_out_dialog_title_name);
        TextView textViewOk = dialog.findViewById(R.id.log_in_and_out_dialog_ok_button);
        TextView textViewCancel = dialog.findViewById(R.id.log_in_and_out_dialog_cancel_button);


        // display a message to the user tell him that the media already downloaded before
        // and ask him if he want to delete and download it again.
        dialogTitle.setText(R.string.media_already_downloaded_message);

        // change OK word to DOWNLOAD AGAIN word in the button.
        textViewOk.setText(R.string.download_again_button);


        // if the user click on DOWNLOAD AGAIN button.
        textViewOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // to show the ripple on the button when clicking on it.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {


                        // delete all files has the unique code for the media (sometimes there is
                        // more than one because there is Posts contains more than one media).
                        for (int i = 0; i < mediaFiles.size(); i++) {

                            // after delete media files, clear its data from the database.
                            // and start downloading process again to download the media.
                            if(mediaFiles.get(i).delete()) {


                                // no need to repeat the deleting the data from the database because
                                // it saved one time only so we do it after delete the last file.
                                if (i == (mediaFiles.size() - 1)) {

                                    String selection = MediaEntry.COLUMN_MEDIA_PRODUCT_TYPE + " = ? AND " + MediaEntry.COLUMN_MEDIA_CODE + " = ?";
                                    String[] selectionArgs = {String.valueOf(mediaProductType), mediaCode};
                                    getContentResolver().delete(MediaEntry.CONTENT_URI, selection, selectionArgs);

                                    startDownloadingProcess();

                                }



                            }

                        }


                        // scan the file path to refresh all media in that folder and delete it from gallery.
                        MediaScannerConnection.scanFile(mContext, new String[]{folderPath.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {

                            }
                        });


                        // close the dialog.
                        dialog.dismiss();



                    }
                }, Constants.CLICKING_DELAYED_TIME_SMALL_BUTTON);


            }
        });


        // if the user click on CANCEL button.
        textViewCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // to show the ripple on the button when clicking on it.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        // close the dialog.
                        dialog.dismiss();

                    }
                }, Constants.CLICKING_DELAYED_TIME_SMALL_BUTTON);

            }
        });

        dialog.show();

    }

    /**
     * Display a dialog to hint the user that he must login for downloading the media he wants
     * because it is may be for a private account or a story.
     *
     * @param stringResourceId for the message that will be displayed to the user (sometimes it can
     *                         be post or a story).
     */
    private void displayLoginDialog(int stringResourceId) {

        Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_default);
        dialog.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        TextView dialogTitle = dialog.findViewById(R.id.log_in_and_out_dialog_title_name);
        TextView textViewOk = dialog.findViewById(R.id.log_in_and_out_dialog_ok_button);
        TextView textViewCancel = dialog.findViewById(R.id.log_in_and_out_dialog_cancel_button);


        // display a message to the user tell him he must login to download the media.
        dialogTitle.setText(stringResourceId);

        // change OK word to LOGIN word in the button.
        textViewOk.setText(R.string.login_button);


        // if the user click on Login button.
        textViewOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // to show the ripple on the button when clicking on it.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        // close the dialog.
                        dialog.dismiss();

                        // open the InstagramOfficialLoginActivity to login.
                        Intent intent = new Intent(MainActivity.this, InstagramOfficialLoginActivity.class);
                        startActivity(intent);

                    }
                }, Constants.CLICKING_DELAYED_TIME_SMALL_BUTTON);


            }
        });


        // if the user click on CANCEL button.
        textViewCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // to show the ripple on the button when clicking on it.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        // close the dialog.
                        dialog.dismiss();

                    }
                }, Constants.CLICKING_DELAYED_TIME_SMALL_BUTTON);

            }
        });

        dialog.show();

    }






    /**
     * Display the user data (real name & user name & profile picture)
     */
    private void displayUserData() {

        if(mIsUserLoggedIn) {

            if (isNetworkAvailable()) {

                // make the real and user name views appears and hide the login button.
                mRealNameTextView.setVisibility(View.VISIBLE);
                mUserNameTextView.setVisibility(View.VISIBLE);
                mLoginButtonInDrawer.setVisibility(View.GONE);

                // get the user data from the instagram server and display it on the screen.
                new GetAppUserInfo().execute();

            }

        }

    }

    /**
     * Clear all user data (real name & user name & profile pic) from the screen.
     */
    private void clearUserDataFromScreen() {

        // make the real and user name views disappears and display the login button.
        mRealNameTextView.setVisibility(View.INVISIBLE);
        mUserNameTextView.setVisibility(View.INVISIBLE);
        mLoginButtonInDrawer.setVisibility(View.VISIBLE);

        // display the placeholder image for the person on the screen.
        mProfilePictureImageView.setImageResource(R.drawable.person_placeholder);

    }

    /**
     * Determine if the user logged in so we need to display the logout button inside drawer and
     * hide it if not.
     */
    private void controlDisplayingLogoutInsideDrawer() {

        if(mIsUserLoggedIn) {
            mLogoutButton.setVisibility(View.VISIBLE);
        } else {
            mLogoutButton.setVisibility(View.GONE);
        }

    }



    /**
     * Execute all methods related to handle the clicks on any button on the drawer.
     */
    private void setupClickingOnDrawerButtons() {

        setClickingOnLoginInsideDrawer();
        setClickingOnDownloads();
        setClickingOnSavedPosts();
        setClickingOnHowToUse();
        setClickingOnSettings();
        setClickingOnShare();
        setClickingOnRateUs();
        setClickingOnLogout();

    }

    /**
     * Handle clicking on the login button inside the drawer.
     *
     * Hint : there is another button on the main screen.
     */
    private void setClickingOnLoginInsideDrawer() {

        mLoginButtonInDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Intent intent = new Intent(MainActivity.this, InstagramOfficialLoginActivity.class);
                        startActivity(intent);

                    }
                }, Constants.CLICKING_DELAYED_TIME_SMALL_BUTTON);

            }
        });

    }

    /**
     * Handle clicking on the Downloads item inside the drawer by sending the user to
     * DownloadsActivityOld and close the drawer.
     */
    private void setClickingOnDownloads() {

        mDownloadsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // to show the ripple on the button when clicking on it.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        opensDownloadsActivity();
                        mDrawer.close();

                    }
                }, Constants.CLICKING_DELAYED_TIME_SMALL_BUTTON);

            }
        });

    }

    /**
     * Check if the user give the app access to phone files before go to DownloadsActivityOld which mainly
     * using some files from the user device to display it on the screen.
     */
    private void opensDownloadsActivity() {

        // if the user not give the app access to the phone file, ask him for that
        // and if he already give the app that permission, then open DownloadsActivityOld.
        if(!hasGrantedStoragePermission()) {

            requestStoragePermission();

        } else {

            Intent instant = new Intent(MainActivity.this, DownloadsActivity.class);
            startActivity(instant);

        }

    }

    /**
     * Handle clicking on the Saved Posts item inside the drawer by sending the user to
     * SavedPostsActivity and close the drawer.
     */
    private void setClickingOnSavedPosts() {

        mSavedPostsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // to show the ripple on the button when clicking on it.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Intent instant = new Intent(MainActivity.this, SavedPostsActivity.class);
                        startActivity(instant);
                        mDrawer.close();

                    }
                }, Constants.CLICKING_DELAYED_TIME_SMALL_BUTTON);

            }
        });

    }

    /**
     * Handle clicking on the How to Use item inside the drawer by sending the user to
     * HowToUseActivity and close the drawer.
     */
    private void setClickingOnHowToUse() {

        mHowToUseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // to show the ripple on the button when clicking on it.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Intent instant = new Intent(MainActivity.this, HowToUseActivity.class);
                        startActivity(instant);
                        mDrawer.close();

                    }
                }, Constants.CLICKING_DELAYED_TIME_SMALL_BUTTON);

            }
        });

    }

    /**
     * Handle clicking on the Settings item inside the drawer by sending the user to
     * SettingsActivity and close the drawer.
     */
    private void setClickingOnSettings() {

        mSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // to show the ripple on the button when clicking on it.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Intent instant = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(instant);
                        mDrawer.close();

                    }
                }, Constants.CLICKING_DELAYED_TIME_SMALL_BUTTON);

            }
        });

    }

    /**
     * Handle clicking on the Share item inside the drawer.
     */
    private void setClickingOnShare() {

        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                    String shareMessage = "Just found the best app to download Instagram Stories, photos, post, IGTV videos! Check it out:\n";
                    shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n";
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(shareIntent, "Share via..."));

                } catch (Exception e) {
                    Log.e(LOG_TAG, "Exception from try-catch block inside " + LOG_TAG + " class in setClickingOnShare method.", e);

                }

            }
        });

    }

    /**
     * Handle clicking on the Rate Us item inside the drawer.
     */
    private void setClickingOnRateUs() {

        mRateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    String storeUrl = "https://play.google.com/store/apps/details?id=";
                    Uri uri = Uri.parse(storeUrl + getPackageName());

                    Intent rateIntent = new Intent(Intent.ACTION_VIEW, uri);

                    rateIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                            Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                    startActivity(rateIntent);

                } catch (Exception e) {

                    String storeUrl = "https://play.google.com/store/apps/details?id=";
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(storeUrl + getPackageName())));
                }

            }
        });

    }

    /**
     * Handle clicking on the logout button by inflate a dialog that tell the user that he is up to
     * logout from his instagram account & to get confirmation from him that he truly want to logout.
     */
    private void setClickingOnLogout() {

        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // close the drawer and inflate a dialog to get confirm from the user that he want to logout.
                mDrawer.close();

                Dialog dialog = new Dialog(MainActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_default);
                dialog.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


                TextView dialogTitle = dialog.findViewById(R.id.log_in_and_out_dialog_title_name);
                TextView textViewOk = dialog.findViewById(R.id.log_in_and_out_dialog_ok_button);
                TextView textViewCancel = dialog.findViewById(R.id.log_in_and_out_dialog_cancel_button);

                // display a message ask the user if he want truly logout or not.
                dialogTitle.setText(R.string.confirm_log_out);

                // if the user click on OK button (he want logout).
                textViewOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // to show the ripple on the button when clicking on it.
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                // close dialog and display a progress bar for the logout process.
                                dialog.dismiss();
                                mProgressBar.setVisibility(View.VISIBLE);

                                // update the variable
                                mIsUserLoggedIn = false;

                                // clear the user data that saved inside preference.
                                mSharedPreferenceEditor.putString(Constants.KEY_USER_ID, null);
                                mSharedPreferenceEditor.putString(Constants.KEY_SESSION_ID, null);
                                mSharedPreferenceEditor.putString(Constants.KEY_COOKIE, null);
                                mSharedPreferenceEditor.putString(Constants.KEY_CSRF_TOKEN, null);
                                mSharedPreferenceEditor.putBoolean(Constants.KEY_LOGIN, false);
                                mSharedPreferenceEditor.apply();


                                // to make the progressbar appears for a while.
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        // add a default cookie to preference from firebase.
                                        addDefaultCookieFromFirebase();

                                        // to remove the real name, user name and profile pic from the drawer.
                                        clearUserDataFromScreen();

                                        // refresh the StoriesFragment to remove the real stories data and display a fake ones.
                                        setupStoriesFragment();

                                        // refresh SavedPostsFragment to remove the real saved posts data and display a fake ones.
                                        setupSavedPostsFragment();

                                        // for displaying the hint message that tell user he should login to display his stories and saved posts.
                                        controlDisplayingLoginHintPart();

                                        // for displaying the logout button from the drawer.
                                        controlDisplayingLogoutInsideDrawer();

                                        // hide the progressBar and hint the user that he successfully logout.
                                        mProgressBar.setVisibility(View.GONE);
                                        Toast.makeText(MainActivity.this, R.string.toast_successfully_signed_out, Toast.LENGTH_SHORT).show();

                                    }
                                }, Constants.CLICKING_DELAYED_TIME_PROGRESS_BAR);

                            }
                        }, Constants.CLICKING_DELAYED_TIME_SMALL_BUTTON);


                    }
                });


                // if the user click on CANCEL button (he want logout).
                textViewCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // to show the ripple on the button when clicking on it.
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                // close the dialog.
                                dialog.dismiss();

                            }
                        }, Constants.CLICKING_DELAYED_TIME_SMALL_BUTTON);

                    }
                });


                // display the dialog to the screen.
                dialog.show();


            }
        });

    }


    /**
     * Handle What happened if the opened by intent comes from the Instagram app.
     */
    private void handleInstagramInComingIntent() {

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action)) {

            if("text/plain".equals(type)) {
                String url = intent.getStringExtra(Intent.EXTRA_TEXT);

                // if there is a url comes from the instagram intent
                // paste it to the Url field EditText & start download process to download media
                // in that link.
                if (url != null) {
                    mUrlEditText.setText(url);
                    startDownloadingProcess();
                }

            }

        }

    }


    /**
     * Inflate a custom menu in toolbar.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Handle clicking on buttons inside the custom menu.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

             // for instagram icon : open Instagram application.
            case R.id.action_open_instagram:

                Intent intentToInstagram = new Intent(Intent.ACTION_VIEW);
                intentToInstagram.setPackage("com.instagram.android");

                try {
                    startActivity(intentToInstagram);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Exception from try-catch block inside " + LOG_TAG + " class in onOptionsItemSelected method.", e);

                }

                return true;

        }

        return super.onOptionsItemSelected(item);

    }

    /**
     * Handle Clicking on Back button.
     */
    @Override
    public void onBackPressed() {

        // in case the drawer opened : the drawer must be closed first.
        // in case teh drawer closed : make the user click again to close the entire app.
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.close();
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            doubleBackToExitPressedOnce = true;
            // hint the user that he must click the Back button again to close the entire app.
            Toast.makeText(this, getResources().getString(R.string.toast_click_back_again_to_exit), Toast.LENGTH_SHORT).show();

            // if the user no click Back button again before two seconds spent from the first click
            // then he need click two times again.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }

    }


    /**
     * Handle what should happen if the activity pause and open again.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // check if the user login state changed, so the layout must refresh some parts inside it.
        boolean isLoggedIn = mSharedPreference.getBoolean(Constants.KEY_LOGIN, Constants.KEY_LOGIN_DEFAULT_VALUE);
        if(mIsUserLoggedIn != isLoggedIn) {
            mIsUserLoggedIn = isLoggedIn;
            setupStoriesFragment();
            setupSavedPostsFragment();
            controlDisplayingLoginHintPart();
            displayUserData();
            controlDisplayingLogoutInsideDrawer();
        }


        // check if there is change in Saved Posts Enabling, so the layout must refresh SavedPostsFragment.
        boolean isSavedPostsEnabled = mSharedPreference.getBoolean(Constants.KEY_ENABLE_SAVED_POSTS, Constants.KEY_ENABLE_SAVED_POSTS_DEFAULT_VALUE);
        if (mIsSavedPostsEnabled != isSavedPostsEnabled) {
            setupSavedPostsFragment();
        }


        // check if there is change in app language, so the layout must recreate again to refresh the views direction.
        String appLanguage = mSharedPreference.getString(Constants.KEY_APP_LANGUAGE, Constants.KEY_APP_LANGUAGE_DEFAULT_VALUE);
        if (!appLanguage.equals(mAppLanguage)) {
            recreate();
        }


    }


    /**
     * Ask user to give the app access to the required files to save downloaded media inside it.
     */
    private void requestStoragePermission() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.ACCESS_MEDIA_LOCATION}, 0);

        } else {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
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
     * Get the User data from Instagram server in background and display his data (user name, real
     * name and profile picture) on the screen (specific inside drawer).
     */
    public class GetAppUserInfo extends AsyncTask<Void, Void, AppUserObject> {

        private GetAppUserInfo() {

        }

        @Override
        protected AppUserObject doInBackground(Void... voids) {

            AppUserObject appUserObject = null;

            try {
                appUserObject = UserUtil.getUserDataByUserId(mContext);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception from try-catch block inside GetAppUserInfo class inside " + LOG_TAG + " class in doInBackground method.", e);
            }

            return appUserObject;
        }


        @Override
        protected void onPostExecute(AppUserObject appUserObject) {

            if (appUserObject != null) {
                displayUserInfo(appUserObject);
            }
            // in this case there is a problem with the user cookie (expired) so we make the app
            // delete the cookie data and refresh it like the user logout.
            else {

                // update the variable
                mIsUserLoggedIn = false;

                // clear the user data that saved inside preference.
                mSharedPreferenceEditor.putString(Constants.KEY_USER_ID, null);
                mSharedPreferenceEditor.putString(Constants.KEY_SESSION_ID, null);
                mSharedPreferenceEditor.putString(Constants.KEY_COOKIE, null);
                mSharedPreferenceEditor.putString(Constants.KEY_CSRF_TOKEN, null);
                mSharedPreferenceEditor.putBoolean(Constants.KEY_LOGIN, false);
                mSharedPreferenceEditor.apply();


                // add a default cookie to preference from firebase.
                addDefaultCookieFromFirebase();

                // to remove the real name, user name and profile pic from the drawer.
                clearUserDataFromScreen();

                // refresh the StoriesFragment to remove the real stories data and display a fake ones.
                setupStoriesFragment();

                // refresh SavedPostsFragment to remove the real saved posts data and display a fake ones.
                setupSavedPostsFragment();

                // for displaying the hint message that tell user he should login to display his stories and saved posts.
                controlDisplayingLoginHintPart();

                // for displaying the logout button from the drawer.
                controlDisplayingLogoutInsideDrawer();


            }

        }

        /**
         * Display the user data inside drawer.
         *
         * @param appUserObject Object contains required user data to display it on the screen.
         */
        private void displayUserInfo(AppUserObject appUserObject) {

            // display the real name on the screen in drawer.
            mRealNameTextView.setText(appUserObject.getRealName());

            // display the user name on the screen in drawer.
            mUserNameTextView.setText(appUserObject.getUserName());

            // display the profile picture on the screen in drawer.
            Glide.with(mContext)
                    .asBitmap()
                    .load(appUserObject.getProfilePicUrl())
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource,Transition<? super Bitmap> transition) {
                            mProfilePictureImageView.setImageBitmap(resource);
                        }

                        @Override
                        public void onLoadCleared(Drawable placeholder) {
                        }
                    });


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




    public class DownloadPost extends AsyncTask<Void, Integer, DownloadingObject> {

        private final String mPostUrl;
        private ProgressDialog progressDialog;

        private DownloadPost(String url) {
            mPostUrl = url;
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
        protected DownloadingObject doInBackground(Void... voids) {


            // try to start downloading media and save them on the device.
            try {

                DownloadingObject downloadingObject = DownloadUtil.getPostOrReelOrIgtvData(mContext, mPostUrl);

                // if the downloadingObject is empty or not and if the response from the server
                // said that the user need to login first, then no need to complete method.
                if (downloadingObject == null || downloadingObject.isNeedLogin()) {
                    return downloadingObject;
                }



                // get all media urls from the DownloadingObject and download each one of them
                // if there is more one media.
                ArrayList<String> allMediaUrls = downloadingObject.getAllMediaUrls();
                for (int i = 0 ; i < allMediaUrls.size() ; i++) {

                    // get the media url and start to downloading it.
                    String urlString = allMediaUrls.get(i);
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
                    String fileName = mediaCode + "_" + i + mediaExtension;


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
            // 2- if the response from the server need to login first to download media.
            // 3- if downloading successful.
            if (downloadingObject == null) {
                Toast.makeText(mContext, R.string.something_wrong_please_try_again, Toast.LENGTH_SHORT).show();
            } else if (downloadingObject.isNeedLogin()) {
                displayLoginDialog(R.string.ask_user_to_log_in_for_download_post);
            } else {

                // clear the url editText field.
                mUrlEditText.setText("");
                mUrlEditText.clearFocus();


                // save the user profile picture inside the app data (not appears inside gallery).
                saveUserProfilePic(downloadingObject.getUserName(), downloadingObject.getProfilePicUrl());

                // save the media data (media id - text & hashtags - ...etc).
                saveMediaDataInsideDatabase(downloadingObject);

                // hint the user that the download successful after finished.
                Toast.makeText(mContext, R.string.toast_media_saved_successfully, Toast.LENGTH_SHORT).show();

            }

        }

    }















    private class DownloadStory extends AsyncTask<Void, Integer, DownloadingObject> {

        private String mStoryUrl;
        private ProgressDialog progressDialog;

        private DownloadStory(String storyUrl) {

            mStoryUrl = storyUrl;
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
        protected DownloadingObject doInBackground(Void... voids) {


            try {

                DownloadingObject downloadingObject = DownloadStoryUtil.getStoryData(mContext, mStoryUrl);

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
            // 2- if the response from the server need to login first to download media.
            // 3- if downloading successful.
            if (downloadingObject == null) {
                Toast.makeText(mContext, R.string.something_wrong_please_try_again, Toast.LENGTH_SHORT).show();
            } else if (downloadingObject.isNeedLogin()) {
                displayLoginDialog(R.string.ask_user_to_log_in_for_download_post);
            } else {

                // clear the url editText field.
                mUrlEditText.setText("");
                mUrlEditText.clearFocus();


                // save the user profile picture inside the app data (not appears inside gallery).
                saveUserProfilePic(downloadingObject.getUserName(), downloadingObject.getProfilePicUrl());

                // save the media data (media id - text & hashtags - ...etc).
                saveMediaDataInsideDatabase(downloadingObject);

                // hint the user that the download successful after finished.
                Toast.makeText(mContext, R.string.toast_media_saved_successfully, Toast.LENGTH_SHORT).show();

            }

        }

    }


















    /**
     * Check if there is text inside the clipboard match a url for instagram post or story and
     * automatically download it.
     */
    private void checkUrlFromClipboard() {

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        if ( !(clipboard.hasPrimaryClip()) ) {

        } else if ( !(clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) ) {

            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            String textFromClipboard = item.getText().toString();

            String urlFromPreference = mSharedPreference.getString("url_from_clipboard", "");

            if(urlFromPreference == null || urlFromPreference.isEmpty()) {

                if ( textFromClipboard.matches("https://www.instagram.com/(.*)")
                        || textFromClipboard.matches("https://instagram.com/(.*)") ) {

                    mUrlEditText.setText(textFromClipboard);
                    startDownloadingProcess();

                    mSharedPreferenceEditor.putString("url_from_clipboard", textFromClipboard);
                    mSharedPreferenceEditor.apply();

                }
            } else if (!urlFromPreference.equals(textFromClipboard)) {

                if ( textFromClipboard.matches("https://www.instagram.com/(.*)")
                        || textFromClipboard.matches("https://instagram.com/(.*)") ) {

                    mUrlEditText.setText(textFromClipboard);
                    startDownloadingProcess();

                    mSharedPreferenceEditor.putString("url_from_clipboard", textFromClipboard);
                    mSharedPreferenceEditor.apply();

                }

            }


        } else {

            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            String textFromClipboard = item.getText().toString();

            String urlFromPreference = mSharedPreference.getString("url_from_clipboard", "");

            if(urlFromPreference == null || urlFromPreference.isEmpty()) {

                if ( textFromClipboard.matches("https://www.instagram.com/(.*)")
                        || textFromClipboard.matches("https://instagram.com/(.*)") ) {

                    mUrlEditText.setText(textFromClipboard);
                    startDownloadingProcess();

                    mSharedPreferenceEditor.putString("url_from_clipboard", textFromClipboard);
                    mSharedPreferenceEditor.apply();

                }
            } else if (!urlFromPreference.equals(textFromClipboard)) {

                if ( textFromClipboard.matches("https://www.instagram.com/(.*)")
                        || textFromClipboard.matches("https://instagram.com/(.*)") ) {

                    mUrlEditText.setText(textFromClipboard);
                    startDownloadingProcess();

                    mSharedPreferenceEditor.putString("url_from_clipboard", textFromClipboard);
                    mSharedPreferenceEditor.apply();

                }

            }


        }

    }

}