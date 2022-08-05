package com.mtma.insta.downloader.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.mtma.insta.downloader.Constants;
import com.mtma.insta.downloader.R;
import com.mtma.insta.downloader.adapters.DisplayMediaViewPagerAdapter;
import com.mtma.insta.downloader.data.InstaContractor.MediaEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DisplayingMediaActivity extends AppCompatActivity {


    private static final String LOG_TAG = DisplayingMediaActivity.class.getName();
    private Context mContext;
    private SharedPreferences mSharedPreference;


    private ImageView mBackUpButton;
    private ImageView mRepostButton;
    private ImageView mShareButton;
    private ImageView mCopyTextAndHashtagsButton;
    private ImageView mCopyTextButton;
    private ImageView mCopyHashtagsButton;
    private ImageView mGoToInstagramButton;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;


    private DisplayMediaViewPagerAdapter mDisplayMediaViewPagerAdapter;


    private int mProductType;
    private String mMediaCode;
    private String mTextAndHashtags;
    private String mMediaText;
    private String mMediaHashtags;
    private String mUserName;


    private String mAppLanguage;
    private final ArrayList<Uri> mFileUris = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_displaying_media);


        // initialize global variable values.
        initializeGlobalVariables();


        // set layout direction depend on app language.
        setLayoutLanguage();


        // initialize views ids.
        initializeViewIds();


        // set the correct direction for the back up button and the activity title.
        setToolbarViews();


        // handle clicking on back up button.
        setClickingOnBackUpButton();


        // handle clicking on any buttons inside the top part in the layout.
        setClickingOnAllTopButtons();


        // Setup the ViewPager and the Adapter and the TabLayout.
        setupViewPagerAndAdapter();


    }


    /**
     * Initialize global variables that used over the activity.
     */
    private void initializeGlobalVariables() {

        // initialize the activity context and the preference and the current app language.
        mContext = DisplayingMediaActivity.this;
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(mContext);
        mAppLanguage = mSharedPreference.getString(Constants.KEY_APP_LANGUAGE, Constants.KEY_APP_LANGUAGE_DEFAULT_VALUE);



        // initialize mProductType, mMediaCode and mTextAndHashtags variables by getting its
        // values from the database.
        long databaseId = getIntent().getLongExtra("database_id", -1);

        String[] projection = new String[]{
                MediaEntry.COLUMN_MEDIA_CODE,
                MediaEntry.COLUMN_MEDIA_PRODUCT_TYPE,
                MediaEntry.COLUMN_MEDIA_TEXT_AND_HASHTAG,
                MediaEntry.COLUMN_USER_NAME};

        Uri uri = ContentUris.withAppendedId(MediaEntry.CONTENT_URI, databaseId);

        Cursor cursor = getContentResolver().query(uri,
                projection,
                null,
                null,
                null);


        cursor.moveToFirst();

        int mediaCodeColumnIndex = cursor.getColumnIndexOrThrow(MediaEntry.COLUMN_MEDIA_CODE);
        int productTypeColumnIndex = cursor.getColumnIndexOrThrow(MediaEntry.COLUMN_MEDIA_PRODUCT_TYPE);
        int textAndHashtagColumnIndex = cursor.getColumnIndexOrThrow(MediaEntry.COLUMN_MEDIA_TEXT_AND_HASHTAG);
        int userNameColumnIndex = cursor.getColumnIndexOrThrow(MediaEntry.COLUMN_USER_NAME);


        mProductType = cursor.getInt(productTypeColumnIndex);
        mMediaCode = cursor.getString(mediaCodeColumnIndex);
        mTextAndHashtags = cursor.getString(textAndHashtagColumnIndex);
        mUserName = cursor.getString(userNameColumnIndex);

        cursor.close();





        // initialize mTextAndHashtags, mMediaText and mediaHashtags variables by split
        // mTextAndHashtags variable that comes from above from the database.
        if (!mTextAndHashtags.isEmpty()) {

            String regexPattern = "(#\\w+)";
            Pattern pattern = Pattern.compile(regexPattern);
            Matcher matcher = pattern.matcher(mTextAndHashtags);


            mMediaText = mTextAndHashtags;

            StringBuilder allHashtagsStringBuilder = new StringBuilder();

            while (matcher.find()) {

                String hashtag = matcher.group();
                allHashtagsStringBuilder.append(hashtag).append(" ");
                mMediaText = mMediaText.replaceAll(hashtag, "");

            }

            mMediaHashtags = allHashtagsStringBuilder.toString();

        }






        // initialize mFileUris ArrayList variable by getting the file/s related to the media from
        // the folder (comes from the media product type).
        File folderPath = new File(MediaEntry.getMediaPath(mProductType));
        if (!folderPath.exists()) {
            folderPath.mkdirs();
        }

        // get the required file/s and collect them inside an ArrayList.
        File[] files = folderPath.listFiles();
        if (files != null) {

            for (File file : files) {
                if (file.getName().startsWith(mMediaCode)) {

                    mFileUris.add(Uri.parse(file.getPath()));

                }
            }

        }

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

        this.setContentView(R.layout.activity_displaying_media);

    }


    /**
     * Initialize the views ids from the main layout of the activity.
     */
    private void initializeViewIds() {

        mBackUpButton = findViewById(R.id.activity_displaying_media_back_button);
        mRepostButton = findViewById(R.id.activity_displaying_media_repost);
        mShareButton = findViewById(R.id.activity_displaying_media_share);
        mCopyTextAndHashtagsButton = findViewById(R.id.activity_displaying_media_copy_text_and_hashtags);
        mCopyTextButton = findViewById(R.id.activity_displaying_media_copy_text);
        mCopyHashtagsButton = findViewById(R.id.activity_displaying_media_copy_hashtags);
        mGoToInstagramButton = findViewById(R.id.activity_displaying_media_go_to_instagram);
        mViewPager = findViewById(R.id.activity_displaying_media_view_pager);
        mTabLayout = findViewById(R.id.activity_displaying_media_tab_layout);

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
     * Setup the relation between the ViewPager and the Adapter and the TabLayout.
     */
    private void setupViewPagerAndAdapter() {

        // add the adapter to the ViewPager.
        mDisplayMediaViewPagerAdapter = new DisplayMediaViewPagerAdapter(mContext, getSupportFragmentManager(), mFileUris);
        mViewPager.setAdapter(mDisplayMediaViewPagerAdapter);

        // make the adapter save 4 pages in memory to make scroll process more smooth.
        mViewPager.setOffscreenPageLimit(4);

        // always we need to start from the first file (in case there is more than one file).
        mViewPager.setCurrentItem(0);

        // add the ViewPager to the TabLayout.
        mTabLayout.setupWithViewPager(mViewPager, true);

    }


    /**
     * Execute all methods related to handle the clicks on any button inside the top buttons
     * (repost - share - copy textAndHashtags - copy onlyText - copy onlyHashtags - go to instagram).
     */
    private void setClickingOnAllTopButtons() {

        setupClickingOnRepostButton();
        setupClickingOnShareButton();
        setupClickingOnCopyTextAndHashtagsButton();
        setupClickingOnCopyTextButton();
        setupClickingOnCopyHashtagsButton();
        setupClickingOnCToInstagramButton();

    }


    /**
     * Handle clicking on the Repost Button, by making the user can repost his media again
     * through the instagram app.
     */
    private void setupClickingOnRepostButton() {

        mRepostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // get the uri for the current file displayed on the screen and start the repost
                // process and hint the user if there is something worn happened.
                Uri fileUri = mFileUris.get(mViewPager.getCurrentItem());
                if (fileUri != null) {
                    repostMedia(fileUri);
                } else {
                    Toast.makeText(mContext, R.string.toast_can_not_repost_this_media, Toast.LENGTH_SHORT).show();
                }

            }
        });

    }


    /**
     * Repost the media file again through the instagram app.
     *
     * @param fileUri the media file Uri that will be repost to instagram again.
     */
    private void repostMedia(Uri fileUri) {

        // create Intent that will use to send to the instagram app to repost media.
        Intent intent = new Intent(Intent.ACTION_SEND);

        // determine the media type (image - video)
        if (fileUri.getPath().contains(".mp4")) {
            intent.setType("video/*");
        } else {
            intent.setType("image/*");
        }

        // add the media Uri to the Intent.
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);

        // make Intent only send to the instagram app.
        intent.setPackage("com.instagram.android");

        // start the Intent and open the instagram app.
        startActivity(Intent.createChooser(intent, "Share to Instagram app"));

    }

    /**
     * Handle clicking on the Share button, by making the user can share the media file to any app
     * can handle the intent with these data.
     */
    private void setupClickingOnShareButton() {

        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // get the uri for the current file displayed on the screen and start the share
                // process and hint the user if there is something worn happened.
                Uri fileUri = mFileUris.get(mViewPager.getCurrentItem());
                if (fileUri != null) {
                    shareMedia(fileUri);
                } else {
                    Toast.makeText(mContext, R.string.toast_can_not_share_this_media, Toast.LENGTH_SHORT).show();
                }

            }
        });

    }


    /**
     * Share the media file/s to other apps.
     *
     * @param fileUri the media file Uri that will be shared.
     */
    public void shareMedia(Uri fileUri) {

        // create Intent that will use to share file/s ot other apps.
        Intent intent = new Intent(Intent.ACTION_SEND);


        // determine the media type (image - video)
        if (fileUri.getPath().contains(".mp4")) {
            intent.setType("video/*");
        } else {
            intent.setType("image/*");
        }

        // add the file Uri to the Intent.
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);

        // start the Intent.
        startActivity(Intent.createChooser(intent, "Share via..."));

    }


    /**
     * Handle clicking on the copyTextAndHashtags button inside the bottom sheet, by store both the
     * text and hashtags inside the clipboard.
     */
    private void setupClickingOnCopyTextAndHashtagsButton() {

        mCopyTextAndHashtagsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // add the media text and hashtags to the clipboard and hint the user that
                // with a toast message when it copied successfully or there is nothing to
                // copy in case there is no text or hashtags for that media.
                if (mTextAndHashtags != null && !mTextAndHashtags.isEmpty()) {

                    ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("instagram_text_and_hashtags", mTextAndHashtags);
                    clipboardManager.setPrimaryClip(clipData);

                    Toast.makeText(mContext, R.string.toast_text_and_hashtags_copied, Toast.LENGTH_SHORT).show();

                } else {

                    Toast.makeText(mContext, R.string.toast_no_text_or_hashtags_to_copy, Toast.LENGTH_SHORT).show();

                }

            }
        });

    }


    /**
     * Handle clicking on the copyText button, by storing only text (no hashtags)
     * inside the clipboard.
     */
    private void setupClickingOnCopyTextButton() {

        mCopyTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // add the media text to the clipboard and hint the user that with a toast
                // message when it copied successfully or there is nothing to copy in case
                // there is no text for that media.
                if (mMediaText != null && !mMediaText.isEmpty()) {

                    ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("instagram_text", mMediaText);
                    clipboardManager.setPrimaryClip(clipData);

                    Toast.makeText(mContext, R.string.toast_text_copied, Toast.LENGTH_SHORT).show();

                } else {

                    Toast.makeText(mContext, R.string.toast_no_text_to_copy, Toast.LENGTH_SHORT).show();

                }

            }
        });

    }


    /**
     * Handle clicking on the copyHashtags button by storing only hashtags (no text)
     * inside the clipboard.
     */
    private void setupClickingOnCopyHashtagsButton() {
        mCopyHashtagsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // add the media hashtags to the clipboard and hint the user that with
                // a toast message when it copied successfully or there is nothing to
                // copy in case there is no hashtags for that media.
                if (mMediaHashtags != null && !mMediaHashtags.isEmpty()) {

                    ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("instagram_hashtags", mMediaHashtags);
                    clipboardManager.setPrimaryClip(clipData);

                    Toast.makeText(mContext, R.string.toast_hashtags_copied, Toast.LENGTH_SHORT).show();

                } else {

                    Toast.makeText(mContext, R.string.toast_no_hashtags_to_copy, Toast.LENGTH_SHORT).show();

                }

            }
        });

    }


    /**
     * Handle clicking on the goToInstagram button to display the media inside the Instagram app.
     */
    private void setupClickingOnCToInstagramButton() {

        mGoToInstagramButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // get the media URL for the media inside the Instagram app as Uri.
                Uri uri = getMediaURL();

                // send Intent to Instagram app with the Url to open the media inside it.
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setPackage("com.instagram.android");
                startActivity(intent);

            }
        });


    }


    /**
     * Get the link for the media depend on the product type (post - reel - igtv - story) we decide
     * some elements inside the link.
     * (ex for a story: https://www.instagram.com/stories/{username}/{id}).
     *
     * @return Uri refer to the media URL.
     */
    private Uri getMediaURL() {

        String mediaUrl = "https://www.instagram.com/";

        if (mProductType == MediaEntry.MEDIA_PRODUCT_TYPE_POST) {
            mediaUrl += "p/" + mMediaCode;
        } else if (mProductType == MediaEntry.MEDIA_PRODUCT_TYPE_REEL) {
            mediaUrl += "reel/" + mMediaCode;
        } else if (mProductType == MediaEntry.MEDIA_PRODUCT_TYPE_IGTV) {
            mediaUrl += "tv/" + mMediaCode;
        } else {
            mediaUrl += "stories/" + mUserName + "/" + mMediaCode;
        }

        return Uri.parse(mediaUrl);
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


}