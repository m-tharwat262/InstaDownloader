package com.mtma.insta.downloader.adapters;

import android.content.Context;
import android.net.Uri;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.mtma.insta.downloader.fragments.DisplayingMediaFragment;

import java.util.ArrayList;

/**
 * Used to display the files inside the media downloaded before.
 */
public class DisplayMediaViewPagerAdapter extends FragmentPagerAdapter {


    private Context mContext;
    private ArrayList<Uri> mUris;


    public DisplayMediaViewPagerAdapter(Context context, FragmentManager fm , ArrayList<Uri> uri) {
        super(fm);
        mContext = context;
        mUris = uri;
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

        // get the file Uri for the image or the video and send it to a DisplayingMediaFragment.
        // to display it on the screen.
        Uri uri = mUris.get(position);
        DisplayingMediaFragment DisplayingMediaFragment = new DisplayingMediaFragment(mContext, uri);

        return DisplayingMediaFragment;

    }


    @Override
    public int getCount() {
        return mUris.size();
    }


}
