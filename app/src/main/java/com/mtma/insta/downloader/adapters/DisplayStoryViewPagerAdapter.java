package com.mtma.insta.downloader.adapters;


import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.mtma.insta.downloader.models.DownloadingObject;
import com.mtma.insta.downloader.fragments.DisplayStoryFragment;

import java.util.ArrayList;


public class DisplayStoryViewPagerAdapter extends FragmentStatePagerAdapter {


    private Context mContext;
    private ArrayList<DownloadingObject> mDownloadingObjects;


    public DisplayStoryViewPagerAdapter(FragmentManager fm, Context context, ArrayList<DownloadingObject> downloadingObjects) {
        super(fm);
        mContext = context;
        mDownloadingObjects = downloadingObjects;
    }


    /**
     * Display a new Fragment to show the current media file (Image or Video).
     *
     * @param position the current position in tabLayout.
     *
     * @return Fragment to display the current media file.
     */
    @Override
    public Fragment getItem(int position) {

        // get the current DownloadingObject and send it to a DisplayingMediaFragment to display
        // the media URL that stored inside it on the screen.
        DownloadingObject downloadingObject = mDownloadingObjects.get(position);
        DisplayStoryFragment displayStoryFragment = new DisplayStoryFragment(mContext, downloadingObject);

        return displayStoryFragment;
    }


    @Override
    public int getCount() {
        return mDownloadingObjects.size();
    }



}
