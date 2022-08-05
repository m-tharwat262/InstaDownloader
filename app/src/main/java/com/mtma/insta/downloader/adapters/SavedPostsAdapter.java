package com.mtma.insta.downloader.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.mtma.insta.downloader.R;
import com.mtma.insta.downloader.data.InstaContractor.MediaEntry;
import com.mtma.insta.downloader.models.SavedPostObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class SavedPostsAdapter extends ArrayAdapter<SavedPostObject> {


    private static final String LOG_TAG = SavedPostsAdapter.class.getSimpleName();
    private final Context mContext;

    private ArrayList<Boolean> mCheckedItems = new ArrayList<>();

    public static final int MODE_DISPLAY_INSIDE_MAIN_ACTIVITY = 0;
    public static final int MODE_DISPLAY_INSIDE_SAVED_POSTS_ACTIVITY = 1;
    private final int mAdapterMode;



    public SavedPostsAdapter(Context context, ArrayList<SavedPostObject> savedPostObjects, ArrayList<Boolean> checkedItem) {
        super(context, 0, savedPostObjects);

        mContext = context;
        mCheckedItems = checkedItem;
        mAdapterMode = MODE_DISPLAY_INSIDE_SAVED_POSTS_ACTIVITY;

    }


    public SavedPostsAdapter(Context context, ArrayList<SavedPostObject> savedPostObjects) {
        super(context, 0, savedPostObjects);

        mContext = context;
        mAdapterMode = MODE_DISPLAY_INSIDE_MAIN_ACTIVITY;

    }



    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {


        View itemView = convertView;

        if (itemView == null) {

            // there is two items for the adapter :
            // one for the item displayed on the MainActivity.
            // and the other for the SavedPostsActivity.
            if (mAdapterMode == MODE_DISPLAY_INSIDE_MAIN_ACTIVITY) {
                itemView = LayoutInflater.from(mContext).inflate(R.layout.item_saved_post_for_main_activity, null);
            } else {
                itemView = LayoutInflater.from(mContext).inflate(R.layout.item_saved_post, null);
            }



        }


        // determine the views from the inflated layout.
        ImageView mediaImageView = itemView.findViewById(R.id.item_saved_post_media_image);
        View mediaImageShadow = itemView.findViewById(R.id.item_saved_post_media_image_shadow);
        ImageView checkCircleView = itemView.findViewById(R.id.item_saved_post_check_circle);
        ImageView mediaTypeIconImageView = itemView.findViewById(R.id.item_saved_post_media_type);
        CircleImageView profilePicImageView = itemView.findViewById(R.id.item_saved_post_user_profile_picture);



        // get the current object.
        SavedPostObject savedPostObject = getItem(position);



        // display media file (first media if it was multiple post).
        Glide.with(mContext).load(savedPostObject.getThumbnailUrl()).thumbnail(0.1f).into(mediaImageView);

        // display the user profile Picture.
        Glide.with(mContext).load(savedPostObject.getProfilePicUrl()).thumbnail(0.1f).into(profilePicImageView);



        // display an icon refer to the media type (image - video - multiple).
        if (savedPostObject.getMediaType() == MediaEntry.MEDIA_TYPE_IMAGE) {
            mediaTypeIconImageView.setImageResource(R.drawable.media_type_image);
        } else if (savedPostObject.getMediaType() == MediaEntry.MEDIA_TYPE_VIDEO) {
            mediaTypeIconImageView.setImageResource(R.drawable.media_type_video);
        } else {
            mediaTypeIconImageView.setImageResource(R.drawable.media_type_multiple);
        }


        // Because the adapter can be used in two places (MainActivity - SavedPostsActivity)
        // only the SavedActivity need this situation for (enable and disable) the download button.
        if (mAdapterMode == MODE_DISPLAY_INSIDE_SAVED_POSTS_ACTIVITY) {

            if (mCheckedItems.get(position)) {
                checkCircleView.setVisibility(View.VISIBLE);
                mediaImageShadow.setVisibility(View.VISIBLE);
            } else {
                checkCircleView.setVisibility(View.GONE);
                mediaImageShadow.setVisibility(View.GONE);
            }

        }


        return itemView;

    }


    /**
     * Get all the media urls for oly selected items.
     *
     * @return Array contains the urls for each media the user select.
     */
    public String[] getSelectedSavedPostsUrls() {

        ArrayList<String> allSelectedUrls = new ArrayList<>();
        for (int i = 0 ; i < mCheckedItems.size() ; i ++) {

            // determine some part inside the url depend on the media product type (post - reel - igtv).
            if (mCheckedItems.get(i)) {

                String mediaUrl = "https://www.instagram.com/";

                if (getItem(i).getProductType() == MediaEntry.MEDIA_PRODUCT_TYPE_REEL) {
                    mediaUrl += "reel/" + getItem(i).getMediaCode();
                } else if (getItem(i).getProductType() == MediaEntry.MEDIA_PRODUCT_TYPE_IGTV) {
                    mediaUrl += "tv/" + getItem(i).getMediaCode();
                } else {
                    mediaUrl += "p/" + getItem(i).getMediaCode();
                }

                allSelectedUrls.add(mediaUrl);

            }

        }


        //convert the arrayList to array
        String[] allSelectedUrlsArray = new String[allSelectedUrls.size()];
        for (int j = 0 ; j < allSelectedUrls.size() ; j++) {

            allSelectedUrlsArray[j] = allSelectedUrls.get(j);

        }


        // return the Array contains the urls for the selected media.
        return allSelectedUrlsArray;

    }


    /**
     * Check if there is selected item inside the adapter or not.
     *
     * @return (true) means there is one selected or more and (false) means no selected item.
     */
    public boolean hasSelectedItem() {

        for (int i = 0 ; i < mCheckedItems.size(); i++) {

            // if there is one selected item no need to complete the loop.
            if (mCheckedItems.get(i)) {
                return true;
            }

        }

        return false;
    }


    /**
     * Change the item state (selected - not selected) if the user click on it.
     *
     * @param position the item position that he clicked on.
     */
    public void changeCheckedItemState(int position) {

        if (mCheckedItems.get(position)) {
            mCheckedItems.set(position, false);
        } else {
            mCheckedItems.set(position, true);
        }

        notifyDataSetChanged();

    }
}

