package com.mtma.insta.downloader.adapters;


import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.mtma.insta.downloader.Constants;
import com.mtma.insta.downloader.R;
import com.mtma.insta.downloader.data.InstaContractor.MediaEntry;
import com.mtma.insta.downloader.fragments.BottomSheetFragment;

import java.io.File;
import java.util.ArrayList;


public class DownloadsCursorAdapter extends CursorAdapter {


    public static final String LOG_TAG = DownloadsCursorAdapter.class.getSimpleName(); // class name.
    private final Context mContext;


    public DownloadsCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        mContext = context;
    }



    /**
     * Inflate our custom item to use it inside the ListView.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        // use our custom list item inside the adapter.
        return LayoutInflater.from(context).inflate(R.layout.item_downloads, parent, false);

    }



    @Override
    public void bindView(View view, Context context, final Cursor cursor) {


        // determine the views from the inflated layout.
        ImageView mediaImageImageView = view.findViewById(R.id.item_downloads_media_image);
        ImageView mediaTypeImageView = view.findViewById(R.id.item_downloads_media_type);
        TextView productTypeTextView = view.findViewById(R.id.item_downloads_product_type);
        TextView textAndHashtagsTextView = view.findViewById(R.id.item_downloads_media_text);
        ImageView profilePicImageView = view.findViewById(R.id.item_downloads_user_image);
        TextView userNameTextView = view.findViewById(R.id.item_downloads_user_name);
        ImageView menuIconImageView = view.findViewById(R.id.item_downloads_menu_icon);



        // get the column position inside the table in the database.
        int uniqueIdColumnIndex = cursor.getColumnIndexOrThrow(MediaEntry._ID);
        int productTypeColumnIndex = cursor.getColumnIndexOrThrow(MediaEntry.COLUMN_MEDIA_PRODUCT_TYPE);
        int mediaCodeColumnIndex = cursor.getColumnIndexOrThrow(MediaEntry.COLUMN_MEDIA_CODE);
        int mediaTypeColumnIndex = cursor.getColumnIndexOrThrow(MediaEntry.COLUMN_MEDIA_TYPE);
        int textAndHashtagColumnIndex = cursor.getColumnIndexOrThrow(MediaEntry.COLUMN_MEDIA_TEXT_AND_HASHTAG);
        int userNameColumnIndex = cursor.getColumnIndexOrThrow(MediaEntry.COLUMN_USER_NAME);


        // get the data from cursor and save them inside a variable to user them below.
        long uniqueId = cursor.getLong(uniqueIdColumnIndex);
        int productType = cursor.getInt(productTypeColumnIndex);
        int mediaType = cursor.getInt(mediaTypeColumnIndex);
        String mediaCode = cursor.getString(mediaCodeColumnIndex);
        String textAndHashtag = cursor.getString(textAndHashtagColumnIndex);
        String userName = cursor.getString(userNameColumnIndex);



        // display the media file image.
        String mediaPath = getMediaPath(mediaCode, productType);
        Glide.with(mContext).load(mediaPath).thumbnail(0.1f).into(mediaImageImageView);


        // display the profile picture for the user who won the media.
        String userProfilePicPath = Constants.PATH_THUMBNAILS_PROFILE_PICS_DIRECTORY + userName + ".jpg";
        Glide.with(mContext).load(userProfilePicPath).thumbnail(0.1f).into(profilePicImageView);


        // display an icon refer to the type of the media (image - video - multiple media).
        mediaTypeImageView.setImageResource(getMediaTypeImage(mediaType));


        // display the product type (Post - Reel - IGTV - Story) as a title inside the item.
        productTypeTextView.setText(MediaEntry.getMediaProductType(productType));


        // display the text and hashtags if exist.
        if (textAndHashtag.isEmpty()) {
            textAndHashtagsTextView.setText(mContext.getString(R.string.media_has_no_text_ot_hashtags));
        } else {
            textAndHashtagsTextView.setText(textAndHashtag);
        }



        // display the user name who own the media.
        userNameTextView.setText(userName);


        // handle clicking on the menu three dots icon.
        menuIconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // open the bottom sheet with options on the media like (repost - share -
                // copy text and hashtags - delete)
                BottomSheetFragment bottomSheetFragment = new BottomSheetFragment(mContext, uniqueId, productType, mediaCode, textAndHashtag);
                bottomSheetFragment.show(((AppCompatActivity) mContext).getSupportFragmentManager(), bottomSheetFragment.getTag());

            }
        });


    }


    /**
     * Access to the folder which contains the media file/s and get the first one of them (in case
     * there is more than one file for this media).
     *
     * @param mediaCode the media unique code.
     * @param productType the media product type (Post - Reel - IGTV - Story)
     *
     * @return the first media file path (if there is more than one file we choose the first).
     */
    private String getMediaPath(String mediaCode, int productType) {


        // get the folder path which contains the media file/s.
        File folderPathFile = new File(MediaEntry.getMediaPath(productType));
        if (!folderPathFile.exists()) {
            folderPathFile.mkdirs();
        }

        // search in files inside the folder above and the required file (always contains a
        // media code at the first of its names).
        File[] files = folderPathFile.listFiles();
        if (files != null) {

            ArrayList<File> mediaFiles = new ArrayList<>();
            for (File file : files) {
                if (file.getName().startsWith(mediaCode)) {

                    mediaFiles.add(file);

                }
            }

            // only we need one file to display it to the user in the item so
            // we always choose the first one.
            if (mediaFiles.size() != 0) {

                String mediaPath = mediaFiles.get(0).getPath();

                return mediaPath;

            }

        }

        // in case something went wrong from above the method will return null.
        return null;

    }


    /**
     * Get the media type icon resource depend on the media type as integer comes from the database.
     *
     * @param mediaType the media type number comes from the database.
     *
     * @return the icon resource id.
     */
    private int getMediaTypeImage(int mediaType) {

        if (mediaType == MediaEntry.MEDIA_TYPE_IMAGE) {
            return R.drawable.media_type_image;
        } else if (mediaType == MediaEntry.MEDIA_TYPE_VIDEO) {
            return R.drawable.media_type_video;
        } else {
            return R.drawable.media_type_multiple;
        }

    }


}
