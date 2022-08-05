package com.mtma.insta.downloader.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.mtma.insta.downloader.Constants;
import com.mtma.insta.downloader.R;
import com.mtma.insta.downloader.adapters.CircleStoriesAdapter;
import com.mtma.insta.downloader.models.UserHasStoryObject;
import com.mtma.insta.downloader.utils.StoryUtil;

import java.util.ArrayList;

import it.sephiroth.android.library.widget.HListView;


public class StoriesFragment extends Fragment {


    private static final String LOG_TAG = StoriesFragment.class.getSimpleName();
    private final Context mContext;
    private SharedPreferences mSharedPreference;


    private View mMainView;
    private HListView mListView;
    private EditText mSearchView;
    private TextView mLoginHintTextView;


    private CircleStoriesAdapter mCircleStoriesAdapter;


    public StoriesFragment(Context context) {
        mContext = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_stories, container, false);


        // initialize global variable values.
        initializeGlobalVariables();


        // initialize views ids.
        initializeViewIds();


        // display saved Posts if exist on the main screen inside the Fragment.
        loadStories();


        // filter the list view and display the users who match the text that the user type
        // inside the search field.
        setupSearchFilterField();


        return mMainView;

    }



    /**
     * Initialize global variables that used over the Fragment.
     */
    private void initializeGlobalVariables() {

        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(mContext);

    }


    /**
     * Initialize the views ids from the main layout of the Fragment.
     */
    private void initializeViewIds() {

        mListView = mMainView.findViewById(R.id.fragment_stories_horizontal_list_view);
        mSearchView = mMainView.findViewById(R.id.fragment_stories_search_field);
        mLoginHintTextView = mMainView.findViewById(R.id.fragment_stories_login_hint);

    }


    /**
     * Load Stories on the screen if the user logged in and have users who has stories.
     */
    private void loadStories() {

        // get the user Login state.
        boolean isUserLogin = mSharedPreference.getBoolean(Constants.KEY_LOGIN, Constants.KEY_LOGIN_DEFAULT_VALUE);
        // TODO: delete the line below.
//        isUserLogin = false;
        // depend on the user login state we decide if we need to display the user real stories or
        // a fake list with placeholder image.
        if(isUserLogin) {

            Log.i(LOG_TAG, "  UNIX  here  :  " + 1);

            // if the user logged in and there is a connection to the network we run a background
            // process try to get and display the stories on the screen.
            if (isNetworkAvailable()) {
                new GetStoriesData().execute();
            }

        }
        // if the user not logged in no need to display the search field and we display fake data
        // represent to how the stories will look like when the user logged in.
        else {

            mLoginHintTextView.setVisibility(View.VISIBLE);
            mSearchView.setVisibility(View.GONE);

            ArrayList<UserHasStoryObject> userHasStories = new ArrayList<>();

            for(int i = 0 ; i < 10 ;i++) {

                userHasStories.add(new UserHasStoryObject());

            }

            mCircleStoriesAdapter = new CircleStoriesAdapter(mContext, userHasStories, CircleStoriesAdapter.MODE_FAKE_DATA);
            mListView.setAdapter(mCircleStoriesAdapter);

        }

    }



    /**
     * Check if the user is connecting to internet or not.
     *
     * @return boolean refer to connecting to internet or not.
     */
    private boolean isNetworkAvailable() {

        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }


    /**
     * Add listener to the EditText responsible to search on stories for specific user by filtering
     * the items inside the adapter and display only the matching user name or real name with the
     * text that the user insert to the EditText.
     */
    private void setupSearchFilterField() {

        mSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mCircleStoriesAdapter.getFilter().filter(s);
            }
        });

    }


    /**
     * Class Responsible on Displaying the users who has stories on the Screen.
     */
    public class GetStoriesData extends AsyncTask<Void, String, ArrayList<UserHasStoryObject>> {

        private GetStoriesData() {

        }


        @Override
        protected ArrayList<UserHasStoryObject> doInBackground(Void... voids) {

            // try to get the users who has stories and the required data to display it on the screen.
            ArrayList<UserHasStoryObject> userHasStoriesObjects = null;
            try {
                userHasStoriesObjects = StoryUtil.getListOfUsersHasStories(mContext);
                Log.i(LOG_TAG, "  UNIX  here  :  " + 2);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception from try-catch block inside " + LOG_TAG + " class inside GetStoriesData class in doInBackground method.", e);
            }

            return userHasStoriesObjects;
        }

        @Override
        protected void onPostExecute(ArrayList<UserHasStoryObject> userHasStoriesObjects) {

            // in case the the userHasStories not a null object and not empty List, we display
            // the users which have stories.
            if (userHasStoriesObjects != null && userHasStoriesObjects.size() != 0) {
                Log.i(LOG_TAG, "  UNIX  here  :  " + 3);
                mCircleStoriesAdapter = new CircleStoriesAdapter(mContext, userHasStoriesObjects, CircleStoriesAdapter.MODE_REAL_DATA);
                mListView.setAdapter(mCircleStoriesAdapter);
            }

        }
    }


}
