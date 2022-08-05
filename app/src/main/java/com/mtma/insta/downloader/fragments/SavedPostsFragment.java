package com.mtma.insta.downloader.fragments;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.mtma.insta.downloader.Constants;
import com.mtma.insta.downloader.R;
import com.mtma.insta.downloader.activities.SavedPostsActivity;
import com.mtma.insta.downloader.adapters.SavedPostsAdapter;
import com.mtma.insta.downloader.models.SavedPostObject;
import com.mtma.insta.downloader.utils.CollectionUtil;

import java.util.ArrayList;

import it.sephiroth.android.library.widget.AdapterView;
import it.sephiroth.android.library.widget.HListView;


public class SavedPostsFragment extends Fragment {


    private static final String LOG_TAG = SavedPostsFragment.class.getSimpleName();
    private final Context mContext;
    private SharedPreferences mSharedPreference;


    private View mMainView;
    private LinearLayout mHeaderLayout;
    private TextView mSeeAllButton;
    private HListView mListView;


    public SavedPostsFragment(Context context) {
        mContext = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_saved_posts, container, false);


        // initialize global variable values.
        initializeGlobalVariables();


        // initialize views ids.
        initializeViewIds();


        // display saved Posts if exist on the main screen inside the Fragment.
        loadSavedPosts();


        // handle clicking on see all button.
        setClickingOnSeeAllButton();


        // handle clicking on the ListView items.
        handleClickingOnListViewItems();


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

        mListView = mMainView.findViewById(R.id.fragment_saved_posts_horizontal_list_view);
        mHeaderLayout = mMainView.findViewById(R.id.fragment_saved_posts_header_layout);
        mSeeAllButton = mMainView.findViewById(R.id.fragment_saved_posts_see_all_button);

    }


    /**
     * Load SavedPosts on the screen if the user logged in and have saved posts in his account.
     */
    private void loadSavedPosts() {

        boolean isUserLogin = mSharedPreference.getBoolean(Constants.KEY_LOGIN, Constants.KEY_LOGIN_DEFAULT_VALUE);

        // TODO: delete the line below.
//        isUserLogin = false;

        if(isUserLogin) {

            if (isNetworkAvailable()) {
                new GetSavedPostsData().execute();
            }

        } else {
            mHeaderLayout.setVisibility(View.GONE);
        }

    }


    /**
     * Handle clicking on see all button by opening the SavedPostsActivity.
     */
    private void setClickingOnSeeAllButton() {

        mSeeAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mContext, SavedPostsActivity.class);
                startActivity(intent);

            }

        });

    }


    /**
     * Handle clicking on the ListView items by send the user to the SavedPostsActivity.
     */
    private void handleClickingOnListViewItems() {

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // change the item state between selected and not selected.
                Intent intent = new Intent(mContext, SavedPostsActivity.class);
                startActivity(intent);

            }
        });
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
     * Get the SavedPosts Data and Display them on the Screen if exist.
     */
    public class GetSavedPostsData extends AsyncTask<Void, Void, ArrayList<SavedPostObject>> {

        public GetSavedPostsData() {

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

            // if there is no wrong when get data and the user has saved post.
            if (savedPostObjectArrayList != null && savedPostObjectArrayList.size() != 0) {

                // initialize the SavedPostsAdapter and add it to the ListView.
                SavedPostsAdapter savedPostsAdapter = new SavedPostsAdapter(mContext, savedPostObjectArrayList);
                mListView.setAdapter(savedPostsAdapter);


            }


        }


    }


}
