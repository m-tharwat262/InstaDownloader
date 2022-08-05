package com.mtma.insta.downloader.activities;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.mtma.insta.downloader.Constants;
import com.mtma.insta.downloader.R;

import java.io.File;
import java.util.Locale;


public class SettingsActivity extends AppCompatActivity {


    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();
    private Context mContext;
    private SharedPreferences mSharedPreference;
    private SharedPreferences.Editor mSharedPreferenceEditor;

    private TextView mTitleTextView;
    private ImageView mBackUpButton;
    private SwitchMaterial mSavedPostsSwitchButton;
    private TextView mLanguageValueTextView;
    private LinearLayout mSavedPostsButton;
    private LinearLayout mLanguageButton;
    private LinearLayout mFeedbackButton;
    private LinearLayout mTermOfUseButton;
    private LinearLayout mPrivacyPoliceButton;
    private LinearLayout mClearCacheButton;

    private String mAppLanguage;
    private boolean mCurrentSwitchValue = true;
    private String LanguageFromRadio;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // TODO: add app version at the bottom of the layout.

        // initialize global variable values.
        initializeGlobalVariables();


        // set layout direction depend on app language.
        checkLanguage();


        // initialize views ids.
        initializeViewIds();


        // set the correct direction for the back up button and the activity title.
        setToolbarViews();


        // set the switch value depend on the value in app reference.
        setupSavedPostsSwitchButton();


        // display the current language that the app use.
        setupLanguageField();


        // handle clicking on settings items.
        setSettingItemsClicks();


        // handle clicking on back up button.
        setClickingOnBackUpButton();


    }


    /**
     * Initialize global variables that used over the activity.
     */
    private void initializeGlobalVariables() {

        mContext = SettingsActivity.this;
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(mContext);
        mSharedPreferenceEditor = mSharedPreference.edit();
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

        this.setContentView(R.layout.activity_settings);

    }


    /**
     * Initialize the views ids from the main layout of the activity.
     */
    private void initializeViewIds() {

        mTitleTextView = findViewById(R.id.tool_bar_title);
        mBackUpButton = findViewById(R.id.tool_bar_back_up_button);
        mSavedPostsButton = findViewById(R.id.activity_settings_saved_posts);
        mLanguageButton = findViewById(R.id.activity_settings_language);
        mTermOfUseButton = findViewById(R.id.activity_settings_terms_of_use);
        mPrivacyPoliceButton = findViewById(R.id.activity_settings_privacy_police);
        mFeedbackButton = findViewById(R.id.activity_settings_feedback);
        mLanguageValueTextView = findViewById(R.id.activity_settings_language_value);
        mSavedPostsSwitchButton = findViewById(R.id.activity_settings_my_group_switch);
        mClearCacheButton = findViewById(R.id.activity_settings_clear_cache);

    }

    /**
     * Display the title name of the Activity &
     * Change the back up direction (RtL or LtR) if the current app language not english.
     */
    private void setToolbarViews() {

        mTitleTextView.setText(R.string.settings_title);

        if(mAppLanguage.equals(Constants.KEY_APP_LANGUAGE_ARABIC_VALUE)) {
            mBackUpButton.setRotation(-45f);
        }

    }


    /**
     * Set the switch button for the display Saved Posts or not depend the value in the reference.
     */
    private void setupSavedPostsSwitchButton() {

        // get the current switch value from the preference.
        mCurrentSwitchValue = mSharedPreference.getBoolean(Constants.KEY_ENABLE_SAVED_POSTS, Constants.KEY_ENABLE_SAVED_POSTS_DEFAULT_VALUE);

        // set the switch button to the value comes from the preference.
        mSavedPostsSwitchButton.setChecked(mCurrentSwitchValue);

    }

    /**
     * Display the current language that the user choose on the language item.
     */
    private void setupLanguageField() {

        String language;
        if (mAppLanguage.equals(Constants.KEY_APP_LANGUAGE_ARABIC_VALUE)) {
            language = getString(R.string.settings_category_general_language_arabic);
        } else {
            language = getString(R.string.settings_category_general_language_default);
        }


        mLanguageValueTextView.setText(language);

    }



    /**
     * Handle Clicking on settings items
     */
    private void setSettingItemsClicks() {

        setClickingOnSwitchButton();
        setClickingOnSavedPosts();
        setClickingOnLanguage();
        setClickingOnFeedback();
        setClickingOnTermsOfUse();
        setClickingOnPrivacyPolice();
        setClickingOnClearCache();

    }



    /**
     * Handle clicking on switch button for change the app language.
     */
    private void setClickingOnSwitchButton() {

        mSavedPostsSwitchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                // update the current switch value to the new one.
                mCurrentSwitchValue = isChecked;

                // add the new value to teh reference.
                mSharedPreferenceEditor.putBoolean(Constants.KEY_ENABLE_SAVED_POSTS, mCurrentSwitchValue);
                mSharedPreferenceEditor.apply();

            }
        });

    }

    /**
     * Handle clicking on the Saved Posts item.
     */
    private void setClickingOnSavedPosts() {

        mSavedPostsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // if the value was true, make it false or the opposite.
                mCurrentSwitchValue = !mCurrentSwitchValue;

                // update the switch button with the new value.
                mSavedPostsSwitchButton.setChecked(mCurrentSwitchValue);

                // add the new value to teh reference.
                mSharedPreferenceEditor.putBoolean(Constants.KEY_ENABLE_SAVED_POSTS, mCurrentSwitchValue);
                mSharedPreferenceEditor.apply();

            }
        });

    }

    /**
     * Handle clicking on the Language item.
     */
    private void setClickingOnLanguage() {

        mLanguageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                // create a dialog to display the available language to the user to choose form it.
                Dialog dialog = new Dialog(mContext);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_language);
                dialog.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                RadioGroup radioGroup = dialog.findViewById(R.id.setting_language_dialog_radios_group);
                TextView textViewOk = dialog.findViewById(R.id.setting_language_dialog_ok_button);
                TextView textViewCancel = dialog.findViewById(R.id.setting_language_dialog_cancel_button);


                // set the radio buttons to select the correct one.
                if (mAppLanguage.equals(Constants.KEY_APP_LANGUAGE_DEFAULT_VALUE) ) {
                    radioGroup.check(radioGroup.getChildAt(0).getId());
                    LanguageFromRadio = getString(R.string.settings_category_general_language_default);
                } else if (mAppLanguage.equals(Constants.KEY_APP_LANGUAGE_ARABIC_VALUE)) {
                    radioGroup.check(radioGroup.getChildAt(1).getId());
                    LanguageFromRadio = getString(R.string.settings_category_general_language_arabic);
                }


                // if user change the selected radio button.
                radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                    RadioButton rb = group.findViewById(checkedId);
                    if (null != rb && checkedId > -1) {
                        switch (checkedId) {
                            case R.id.setting_language_dialog_english_button:
                                LanguageFromRadio = getString(R.string.settings_category_general_language_default);
                                break;
                            case R.id.setting_language_dialog_arabic_button:
                                LanguageFromRadio = getString(R.string.settings_category_general_language_arabic);
                                break;
                            default:
                                break;
                        }
                    }
                });


                // handle clicking on OK button.
                textViewOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // to show the ripple on the button when clicking on it.
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                // close the dialog.
                                dialog.dismiss();

                                // if only the language changed:
                                if (!mLanguageValueTextView.getText().toString().equals(LanguageFromRadio)) {

                                    // change the preference value for the current selected language to the new one.
                                    String newPreferenceValue;
                                    if(LanguageFromRadio.equals(getString(R.string.settings_category_general_language_arabic))) {
                                        newPreferenceValue = Constants.KEY_APP_LANGUAGE_ARABIC_VALUE;
                                    } else {
                                        newPreferenceValue = Constants.KEY_APP_LANGUAGE_DEFAULT_VALUE;
                                    }
                                    mSharedPreferenceEditor.putString(Constants.KEY_APP_LANGUAGE, newPreferenceValue);
                                    mSharedPreferenceEditor.commit();

                                    // recreate the activity to submit the change in the language selected
                                    // and change the layout and views direction.
                                    Intent intent = new Intent(SettingsActivity.this, SettingsActivity.class);
                                    finish();
                                    startActivity(intent);

                                }

                            }
                        }, Constants.CLICKING_DELAYED_TIME_BIG_BUTTON);

                    }

                });


                // handle clicking on the CANCEL button
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
                        }, Constants.CLICKING_DELAYED_TIME_BIG_BUTTON);

                    }
                });

                dialog.show();



            }

        });

    }

    /**
     * Handel clicking on Feedback item.
     */
    private void setClickingOnFeedback() {

        mFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // to show the ripple on the button when clicking on it.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        // send user to the FeedbackActivity.
                        Intent intent = new Intent(SettingsActivity.this, FeedbackActivity.class);
                        startActivity(intent);

                    }
                }, Constants.CLICKING_DELAYED_TIME_BIG_BUTTON);

            }
        });

    }

    /**
     * Handel clicking on Term of Use item.
     */
    private void setClickingOnTermsOfUse() {

        mTermOfUseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // to show the ripple on the button when clicking on it.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        // send user to the TermOfUseActivity.
                        Intent intent = new Intent(SettingsActivity.this, TermsOfUseActivity.class);
                        startActivity(intent);

                    }
                }, Constants.CLICKING_DELAYED_TIME_BIG_BUTTON);


            }
        });

    }

    /**
     * Handel clicking on Privacy Police item.
     */
    private void setClickingOnPrivacyPolice() {

        mPrivacyPoliceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // to show the ripple on the button when clicking on it.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        // send user to the PrivacyPoliceActivity.
                        Intent intent = new Intent(SettingsActivity.this, PrivacyPoliceActivity.class);
                        startActivity(intent);

                    }
                }, Constants.CLICKING_DELAYED_TIME_BIG_BUTTON);


            }
        });

    }


    /**
     * Handel clicking on Clear Cache item.
     */
    private void setClickingOnClearCache() {

        mClearCacheButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // to show the ripple on the button when clicking on it.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        // get the cache size and covert it from long to double.
                        double byt = (double) getCacheSize();

                        // convert the cache size from byte to MB.
                        double megaByte = byt / (1024*1024) ;

                        // delete the cache from the appl.
                        deleteCache();

                        // create a statement with the cache size that deleted and hint the user
                        // that the app cache deleted.
                        String cacheMessage = getString(R.string.clear_cache, String.format("%.2f", megaByte));
                        Toast.makeText(mContext, cacheMessage, Toast.LENGTH_SHORT).show();

                    }
                }, Constants.CLICKING_DELAYED_TIME_BIG_BUTTON);

            }

        });

    }


    /**
     * Get the cache size (byte value) in the app.
     *
     * @return Cache size as long value.
     */
    private long getCacheSize() {

        long size = 0; size += getDirSize(this.getCacheDir());

        size += getDirSize(this.getExternalCacheDir());

        return size;

    }

    /**
     * Get the directory size in byte.
     *
     * @param dir the directory that you want to know it's size.
     *
     * @return the dirctory size in byte.
     */
    public long getDirSize(File dir){

        long size = 0; for (File file : dir.listFiles()) {

            if (file != null && file.isDirectory()) {
                size += getDirSize(file);

            } else if (file != null && file.isFile()) {
                size += file.length();
            }
        }
        return size;
    }

    /**
     * Delete the app cache.
     */
    public void deleteCache() {

        try {
            File dir = mContext.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, R.string.toast_error_when_deleting_cache, Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Delete a directory and what it is containing.
     *
     * @param dir the directory which you want delete.
     *
     * @return (true) if the directory deleted successfully, (false) if no.
     */
    public boolean deleteDir(File dir) {

        if (dir != null && dir.isDirectory()) {

            String[] children = dir.list();

            for (int i = 0; i < children.length; i++) {

                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {

                    return false;
                }

            }

            return dir.delete();

        } else if(dir!= null && dir.isFile()) {

            return dir.delete();

        } else {

            return false;

        }

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

