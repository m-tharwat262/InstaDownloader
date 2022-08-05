package com.mtma.insta.downloader.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mtma.insta.downloader.R;
import com.mtma.insta.downloader.models.DownloadingObject;
import com.mtma.insta.downloader.adapters.StoryDialogAdapter;
import com.mtma.insta.downloader.utils.StoryUtil;

import java.util.ArrayList;


public class StoryDialogFragment extends DialogFragment {


    private static final String LOG_TAG = StoryDialogFragment.class.getName();
    private final Context mContext;


    private View mMainView;
    private TextView mUsernameTextView;
    private TextView mStateTextView;
    private LinearLayout mEmptyView;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;


    private StoryDialogAdapter mStoryDialogAdapter;


    private String mUserName;
    private String mUserId;
    private ArrayList<DownloadingObject> mDownloadingObjectList = new ArrayList<>();


    public StoryDialogFragment(Context context, String username, String userId) {

        mContext = context;
        mUserName = username;
        mUserId = userId;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.dialog_fragment_story, container, false);


        // make dialog itself transparent
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        // initialize views ids.
        initializeViewIds();


        // display the stories that the user have
        loadUserStories();


        return mMainView;

    }



    /**
     * Initialize the views ids from the main layout of the Fragment.
     */
    private void initializeViewIds() {

        mUsernameTextView = mMainView.findViewById(R.id.fragment_dialog_story_username);
        mEmptyView = mMainView.findViewById(R.id.fragment_dialog_story_empty_view);
        mStateTextView = mMainView.findViewById(R.id.fragment_dialog_story_state);
        mProgressBar = mMainView.findViewById(R.id.fragment_dialog_story_progress_bar);
        mRecyclerView = mMainView.findViewById(R.id.fragment_dialog_story_recycle_view);

    }


    /**
     * Display the stories that the user.
     */
    private void loadUserStories() {

        // check if there is internet connection before trying to displaying the user stories.
        if (isNetworkAvailable()) {

            // display the user name.
            mUsernameTextView.setText(mUserName);
            // try to get the stories data that the user have.
            new GetStoriesData().execute();

        } else {
            mEmptyView.setVisibility(View.VISIBLE);
            mStateTextView.setText(R.string.no_internet_connection);
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
     * Get each story data and display them inside items in a Dialog Fragment.
     */
    private class GetStoriesData extends AsyncTask<Void, Void, ArrayList<DownloadingObject>> {



        private GetStoriesData() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // display the progress bar.
            mProgressBar.setVisibility(View.VISIBLE);

        }

        @Override
        protected ArrayList<DownloadingObject> doInBackground(Void... voids) {

            // try to get the user stories data.
            ArrayList<DownloadingObject> downloadingObjectList = null;
            try {
                downloadingObjectList = StoryUtil.getUserStoriesData(mUserId, mContext);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception from try-catch block inside " + LOG_TAG + " class inside GetStoriesData class in doInBackground method.", e);
            }

            return downloadingObjectList;

        }

        @Override
        protected void onPostExecute(ArrayList<DownloadingObject> downloadingObjectList) {

            // hide the progress bar.
            mProgressBar.setVisibility(View.GONE);


            // check first id the List is not null or empty.
            if (downloadingObjectList != null && downloadingObjectList.size() != 0) {

                // determine the items in each row(3)
                mRecyclerView.setHasFixedSize(true);
                mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 3));

                // initialize the StoryDialogAdapter and add it to the RecycleView.
                mStoryDialogAdapter = new StoryDialogAdapter(mContext, downloadingObjectList);
                mRecyclerView.setAdapter(mStoryDialogAdapter);

            }
            // in case there is problem happened when getting the user stories data.
            else {
                mEmptyView.setVisibility(View.VISIBLE);
                mStateTextView.setText(R.string.error_loading_instagram_stories);
            }

        }


    }



}
