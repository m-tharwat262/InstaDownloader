package com.mtma.insta.downloader.activities;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mtma.insta.downloader.Constants;
import com.mtma.insta.downloader.R;

import java.util.Locale;


public class PrivacyPoliceActivity extends AppCompatActivity {


    private static final String LOG_TAG = PrivacyPoliceActivity.class.getSimpleName();
    private Context mContext;
    private SharedPreferences mSharedPreference;


    private TextView mTitleTextView;
    private ImageView mBackUpButton;
    private LinearLayout mContainerLayout;


    private String mAppLanguage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // initialize global variable values.
        initializeGlobalVariables();


        // set layout direction depend on app language.
        checkLanguage();


        // initialize views ids.
        initializeViewIds();


        // set the correct direction for the back up button and the activity title.
        setToolbarViews();


        // display the Privacy Police as categories.
        displayPrivacyPoliceItems();


        // handle clicking on back up button.
        setClickingOnBackUpButton();

    }

    /**
     * Initialize global variables that used over the activity.
     */
    private void initializeGlobalVariables() {

        mContext = PrivacyPoliceActivity.this;
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(mContext);
        mAppLanguage = mSharedPreference.getString(Constants.KEY_APP_LANGUAGE, Constants.KEY_APP_LANGUAGE_DEFAULT_VALUE);

    }


    /**
     * Check the current language that the user selected from settings and determine the layout
     * direction (RtL or LtR).
     */
    private void checkLanguage() {

        Locale locale = new Locale(mAppLanguage);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

        this.setContentView(R.layout.activity_privacy_police);

    }


    /**
     * Initialize the views ids from the main layout of the activity.
     */
    private void initializeViewIds() {

        mTitleTextView = findViewById(R.id.tool_bar_title);
        mBackUpButton = findViewById(R.id.tool_bar_back_up_button);
        mContainerLayout = findViewById(R.id.activity_privacy_police_container);

    }

    /**
     * Display the title name of the Activity &
     * Change the back up direction (RtL or LtR) if the current app language not english.
     */
    private void setToolbarViews() {

        mTitleTextView.setText(R.string.privacy_police_title);

        if(mAppLanguage.equals(Constants.KEY_APP_LANGUAGE_ARABIC_VALUE)) {
            mBackUpButton.setRotation(-45f);
        }

    }

    /**
     * Display the Privacy Police as categories in items on the screen.
     */
    private void displayPrivacyPoliceItems() {

        int[] itemsTitle = getItemsTitles(); // all categories titles.
        int[] itemsBodies = getItemsBodies(); // all categories contents.


        // add each category title and content in one item and add it to the container LinearLayout.
        for (int i = 0 ; i < itemsTitle.length ; i++) {

            View itemView = View.inflate(this, R.layout.item_privacy, null);

            TextView itemTitleTextView = itemView.findViewById(R.id.item_privacy_police_title);
            itemTitleTextView.setText(itemsTitle[i]);

            TextView itemBodyTextView = itemView.findViewById(R.id.item_privacy_police_body);
            itemBodyTextView.setText(itemsBodies[i]);

            mContainerLayout.addView(itemView);

        }

    }

    /**
     * Get the Privacy Police categories title.
     *
     * @return Array contains the content for each category.
     */
    private int[] getItemsTitles() {

        int[] titles = {
                R.string.privacy_police_introduction_title,
                R.string.privacy_police_data_controller_title,
                R.string.privacy_police_personal_data_title,
                R.string.privacy_police_how_use_personal_data_title,
                R.string.privacy_police_provide_personal_data_title,
                R.string.privacy_police_rights_and_controls_title,
                R.string.privacy_police_cookies_title,
                R.string.privacy_police_security_of_personal_data_title,
                R.string.privacy_police_store_personal_data_title,
                R.string.privacy_police_remaining_again_title,
                R.string.privacy_police_contact_us_title};

        return titles;

    }


    /**
     * Get the Privacy Police categories content.
     *
     * @return Array contains the content for each category.
     */
    private int[] getItemsBodies() {

        int[] bodies = {
                R.string.privacy_police_introduction_body,
                R.string.privacy_police_data_controller_body,
                R.string.privacy_police_personal_data_body,
                R.string.privacy_police_how_use_personal_data_body,
                R.string.privacy_police_provide_personal_data_body,
                R.string.privacy_police_rights_and_controls_body,
                R.string.privacy_police_cookies_body,
                R.string.privacy_police_security_of_personal_data_body,
                R.string.privacy_police_store_personal_data_body,
                R.string.privacy_police_remaining_again_body,
                R.string.privacy_police_contact_us_body};

        return bodies;

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