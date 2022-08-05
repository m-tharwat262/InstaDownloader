package com.mtma.insta.downloader.activities;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mtma.insta.downloader.Constants;
import com.mtma.insta.downloader.R;
import com.mtma.insta.downloader.models.FeedbackObject;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class FeedbackActivity extends AppCompatActivity {


    private static final String LOG_TAG = FeedbackActivity.class.getSimpleName();
    private Context mContext;
    private SharedPreferences mSharedPreference;


    private TextView mTitleTextView;
    private ImageView mBackUpButton;
    private ProgressBar mProgressBar;
    private EditText mEmailAddressEditText;
    private EditText mMessageEditText;
    private TextView mSendButton;


    private String mDeviceId;
    private String mAppLanguage;


    private DatabaseReference mFeedbackDatabaseReference;


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
        setClickingOnBackUpButton();


        // handle clicking on send button.
        setClickingOnSendButton();


    }

    /**
     * Initialize global variables that used over the activity.
     */
    private void initializeGlobalVariables() {

        mContext = FeedbackActivity.this;
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(mContext);

        // set the Device id depend on the device MAC.
        mDeviceId = getMacAddress();

        // initialize the Firebase required objects.
        mFeedbackDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.NODE_FEEDBACK);

    }


    /**
     * Check the current language that the user selected from settings and determine the layout
     * direction (RtL or LtR).
     */
    private void setLayoutLanguage() {

        mAppLanguage = mSharedPreference.getString(Constants.KEY_APP_LANGUAGE, Constants.KEY_APP_LANGUAGE_DEFAULT_VALUE);

        Locale locale = new Locale(mAppLanguage);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

        this.setContentView(R.layout.activity_feedback);

    }

    /**
     * Initialize the views ids from the main layout of the activity.
     */
    private void initializeViewIds() {

        mTitleTextView = findViewById(R.id.tool_bar_title);
        mBackUpButton = findViewById(R.id.tool_bar_back_up_button);
        mProgressBar = findViewById(R.id.activity_feedback_progress_bar);
        mEmailAddressEditText = findViewById(R.id.activity_feedback_email_address);
        mMessageEditText = findViewById(R.id.activity_feedback_message);
        mSendButton = findViewById(R.id.activity_feedback_button_send);

    }


    /**
     * Display the title name of the Activity &
     * Change the back up direction (RtL or LtR) if the current app language not english.
     */
    private void setToolbarViews() {

        mTitleTextView.setText(R.string.feedback_title);

        if(mAppLanguage.equals(Constants.KEY_APP_LANGUAGE_ARABIC_VALUE)) {
            mBackUpButton.setRotation(-45f);
        }

    }


    /**
     * Get the user unique MAC address.
     *
     * @return the device MAC address
     */
    private String getMacAddress() {

        try {

            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception from try-catch block inside " + LOG_TAG + " class in getMacAddress method.", e);
        }

        return "02:00:00:00:00:00";

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
     * Handle clicking on the send button.
     */
    private void setClickingOnSendButton() {

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // get data from fields
                String emailAddress = mEmailAddressEditText.getText().toString().trim();
                String messageContent = mMessageEditText.getText().toString().trim();


                // user can't send empty feedback message.
                // but he can send a feedback message with or not his email address.
                if (emailAddress.isEmpty() && messageContent.isEmpty()) {
                    Toast.makeText(mContext, R.string.can_not_send_empty_message, Toast.LENGTH_SHORT).show();
                } else if (!emailAddress.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
                    mEmailAddressEditText.setError(getString(R.string.enter_valid_email));
                    mEmailAddressEditText.requestFocus();
                } else if (isNetworkAvailable()) {
                    sendMessage(emailAddress, messageContent);
                } else {
                    Toast.makeText(mContext, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                }

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
     * Send the feedback message to the firebase contains the email address if the user provide
     * and the current time in his device.
     *
     * NOTE: the time that the user provide is not stable from user to other because it is depend on
     * his country.
     *
     * @param emailAddress user email address (can be null).
     * @param messageContent feedback message (can't be null).
     */
    private void sendMessage(String emailAddress, String messageContent) {

        // show the progressBar.
        mProgressBar.setVisibility(View.VISIBLE);

        // get current unix time.
        long currentUnixTime = System.currentTimeMillis() / 1000;

        // create the FeedbackObject contains the email address, feedback message and current time.
        FeedbackObject feedbackObject = new FeedbackObject(emailAddress, messageContent, currentUnixTime);


        // add new feedback to the firebase inside:
        // specific_mac_address => {MAC ADDRESS} => {PUSH ID} => FeedbackObject
        mFeedbackDatabaseReference.child(Constants.NODE_MAC_ADDRESS_MESSAGES).child(mDeviceId).push().setValue(feedbackObject).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {


                // add new feedback to another node inside the firebase:
                // all_messages => {PUSH ID} => {MAC ADDRESS} => FeedbackObject
                mFeedbackDatabaseReference.child(Constants.NODE_ALL_MESSAGES).push().child(mDeviceId).setValue(feedbackObject);

                // to make the progressbar appears for a while.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        mProgressBar.setVisibility(View.GONE);

                        // if the message sent successfully:
                        // clear the editText fields.
                        mEmailAddressEditText.setText("");
                        mEmailAddressEditText.clearFocus();

                        mMessageEditText.setText("");
                        mMessageEditText.clearFocus();

                        // hint the user that his message sent
                        Toast.makeText(mContext, R.string.thank_you, Toast.LENGTH_SHORT).show();

                    }
                }, Constants.CLICKING_DELAYED_TIME_PROGRESS_BAR);


            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // if the message not send
                Toast.makeText(mContext, R.string.something_wrong_please_try_again , Toast.LENGTH_SHORT).show();
            }
        });

    }


}