package com.mtma.insta.downloader.fragments;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mtma.insta.downloader.Constants;
import com.mtma.insta.downloader.R;
import com.mtma.insta.downloader.data.InstaContractor.MediaEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BottomSheetFragment extends BottomSheetDialogFragment {


    private static final String LOG_TAG = BottomSheetFragment.class.getSimpleName();
    private final Context mContext;


    private View mMainView;
    private LinearLayout mRepostButton;
    private LinearLayout mShareButton;
    private LinearLayout mCopyTextAndHashtagsButton;
    private LinearLayout mCopyTextButton;
    private LinearLayout mCopyHashtagsButton;
    private LinearLayout mDeleteButton;


    private final long mDatabaseId;
    private final int mProductType;
    private final String mMediaCode;
    private final String mTextAndHashtags;


    private String mMediaText;
    private String mMediaHashtags;


    public BottomSheetFragment(Context context, long databaseId, int productType, String mediaCode, String textAndHashtags) {

        mContext = context;
        mDatabaseId = databaseId;
        mProductType = productType;
        mMediaCode = mediaCode;
        mTextAndHashtags = textAndHashtags;

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        mMainView = inflater.inflate(R.layout.fragment_bottom_sheet, container, false);


        // initialize global variable values.
        initializeGlobalVariables();


        // initialize views ids.
        initializeViewIds();


        // handle clicking on any buttons inside the bottom sheet.
        setOnClickingOnBottomSheetItems();


        return mMainView;

    }


    /**
     * Initialize global variables that used over the Fragment.
     */
    private void initializeGlobalVariables() {

        // split the mTextAndHashtags variable that comes from the constructor method
        // to initialize the "mMediaText" and "mMediaHashtags" variables.
        if (!mTextAndHashtags.isEmpty()) {

            String regexPattern = "(#\\w+)";
            Pattern pattern = Pattern.compile(regexPattern);
            Matcher matcher = pattern.matcher(mTextAndHashtags);


            mMediaText = mTextAndHashtags;

            StringBuilder allHashtagsStringBuilder = new StringBuilder();

            while (matcher.find()) {

                String hashtag = matcher.group();
                allHashtagsStringBuilder.append(hashtag).append(" ");
                mMediaText = mMediaText.replaceAll(hashtag, "");

            }

            mMediaHashtags = allHashtagsStringBuilder.toString();

        }

    }


    /**
     * Initialize the views ids from the main layout of the activity.
     */
    private void initializeViewIds() {

        mRepostButton = mMainView.findViewById(R.id.fragment_bottom_sheet_repost);
        mShareButton = mMainView.findViewById(R.id.fragment_bottom_sheet_share);
        mCopyHashtagsButton = mMainView.findViewById(R.id.fragment_bottom_sheet_copy_hashtag);
        mCopyTextButton = mMainView.findViewById(R.id.fragment_bottom_sheet_copy_text);
        mCopyTextAndHashtagsButton = mMainView.findViewById(R.id.fragment_bottom_sheet_copy_text_and_hashtags);
        mDeleteButton = mMainView.findViewById(R.id.fragment_bottom_sheet_delete);

    }


    /**
     * Execute all methods related to handle the clicks on any button inside the bottom sheet.
     */
    private void setOnClickingOnBottomSheetItems() {

        setClickingOnRepost();
        setClickingOnShare();
        setClickingOnCopyTextAndHashtags();
        setClickingOnCopyText();
        setClickingOnCopyHashtags();
        setClickingOnDelete();

    }


    /**
     * Handle clicking on the Repost Button inside the bottom sheet, and that make the user can
     * Repost his media again through the instagram app.
     */
    private void setClickingOnRepost() {

        mRepostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // to show the ripple on the button when clicking on it.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        // get file/s related to the media (only we need here is the first
                        // media in case there is more than one), because a problem in instagram app
                        // when it receive data with Intent.
                        ArrayList<File> mediaFiles = getMediaFiles();

                        // start repost the media file or display a toast message if there is
                        // a problem if something comes wrong.
                        if (mediaFiles.size() != 0) {
                            repost(mediaFiles.get(0));
                        } else {
                            Toast.makeText(mContext, R.string.toast_can_not_repost_this_media, Toast.LENGTH_SHORT).show();
                        }

                        // no need to keep the bottom sheet open.
                        closeBottomSheet();
                    }

                }, Constants.CLICKING_DELAYED_TIME_BIG_BUTTON);

            }
        });

    }


    /**
     * Determine the folder that the media saved at depend on the media product type and get media
     * file/s that we interested in.
     *
     *
     * @return ArrayList contains the media file/s.
     */
    private ArrayList<File> getMediaFiles() {

        // get the folder path contains the media file/s.
        File folderPath = new File(MediaEntry.getMediaPath(mProductType));
        if (!folderPath.exists()) {
            folderPath.mkdirs();
        }

        // search in files inside the folder above and check if the files still exist or not
        // and collect them inside and ArrayList.
        File[] files = folderPath.listFiles();
        ArrayList<File> mediaFiles = new ArrayList<>();
        if (files != null) {

            for (File file : files) {
                if (file.getName().startsWith(mMediaCode)) {

                    mediaFiles.add(file);

                }
            }
        }

        // ArrayList contains the media file/s.
        return mediaFiles;

    }


    /**
     * Repost the media file again through the instagram app.
     *
     * @param mediaFile the media file (must be one, and in case the multiple media we always
     *                  use the first one of them).
     */
    private void repost(File mediaFile) {

        // create Intent that will use to send to the instagram app to repost media.
        Intent intent = new Intent(Intent.ACTION_SEND);

        // determine the media type (image - video)
        if (mediaFile.getPath().contains(".mp4")) {
            intent.setType("video/*");
        } else {
            intent.setType("image/*");
        }

        // add the media Uri to the Intent.
        Uri uri = Uri.parse(mediaFile.getPath());
        intent.putExtra(Intent.EXTRA_STREAM, uri);

        // make Intent only send to the instagram app.
        intent.setPackage("com.instagram.android");

        // start the Intent and open the instagram app.
        startActivity(Intent.createChooser(intent, "Share to Instagram app"));

    }


    /**
     * Handle clicking on the Share Button inside the bottom sheet by making the user can share the
     * media file/s to any app can handle the intent with these data.
     */
    private void setClickingOnShare() {

        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // to show the ripple on the button when clicking on it.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        // get file/s related to the media.
                        ArrayList<File> mediaFiles = getMediaFiles();

                        // start share the media file/s or display a toast message if there is
                        // a problem if something comes wrong.
                        if (mediaFiles.size() != 0) {
                            share(mediaFiles);
                        } else {
                            Toast.makeText(mContext, R.string.toast_can_not_share_this_media, Toast.LENGTH_SHORT).show();
                        }

                        // no need to keep the bottom sheet open.
                        closeBottomSheet();

                    }

                }, Constants.CLICKING_DELAYED_TIME_BIG_BUTTON);

            }
        });

    }


    /**
     * Share the media file/s to other apps.
     *
     * @param mediaFiles the media file/s that will be shared.
     */
    public void share(ArrayList<File> mediaFiles) {

        // create Intent that will use to share file/s ot other apps.
        Intent intent = new Intent(Intent.ACTION_SEND);


        // make the files type only images and videos.
        String [] mimeTypes = {"image/*", "video/*"};
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        // get all file/s Uri.
        ArrayList<Uri> uris = new ArrayList<>();
        for (int i = 0; i < mediaFiles.size(); i++) {
            uris.add(Uri.parse(mediaFiles.get(i).getPath()));
        }
        intent.putExtra(Intent.EXTRA_STREAM, uris);

        // start the Intent.
        startActivity(Intent.createChooser(intent, "Share via..."));

    }


    /**
     * Handle clicking on the copyTextAndHashtags button inside the bottom sheet, by storing
     * both the text and hashtags inside the clipboard.
     */
    private void setClickingOnCopyTextAndHashtags() {

        mCopyTextAndHashtagsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // to show the ripple on the button when clicking on it.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        // add the media text and hashtags to the clipboard and hint the user that
                        // with a toast message when it copied successfully or there is nothing to
                        // copy in case there is no text or hashtags for that media.
                        if (mTextAndHashtags != null && !mTextAndHashtags.isEmpty()) {

                            ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clipData = ClipData.newPlainText("instagram_text_and_hashtags", mTextAndHashtags);
                            clipboardManager.setPrimaryClip(clipData);

                            Toast.makeText(mContext, R.string.toast_text_and_hashtags_copied, Toast.LENGTH_SHORT).show();

                        } else {

                            Toast.makeText(mContext, R.string.toast_no_text_or_hashtags_to_copy, Toast.LENGTH_SHORT).show();

                        }

                        // no need to keep the bottom sheet open.
                        closeBottomSheet();

                    }

                }, Constants.CLICKING_DELAYED_TIME_BIG_BUTTON);

            }
        });

    }


    /**
     * Handle clicking on the copyText button inside the bottom sheet, by storing
     * only text (no hashtags) inside the clipboard.
     */
    private void setClickingOnCopyText() {

        mCopyTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // to show the ripple on the button when clicking on it.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        // add the media text to the clipboard and hint the user that with a toast
                        // message when it copied successfully or there is nothing to copy in case
                        // there is no text for that media.
                        if (mMediaText != null && !mMediaText.isEmpty()) {

                            ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clipData = ClipData.newPlainText("instagram_text", mMediaText);
                            clipboardManager.setPrimaryClip(clipData);

                            Toast.makeText(mContext, R.string.toast_text_copied, Toast.LENGTH_SHORT).show();

                        } else {

                            Toast.makeText(mContext, R.string.toast_no_text_to_copy, Toast.LENGTH_SHORT).show();

                        }

                        // no need to keep the bottom sheet open.
                        closeBottomSheet();

                    }

                }, Constants.CLICKING_DELAYED_TIME_BIG_BUTTON);

            }
        });

    }


    /**
     * Handle clicking on the copyHashtags button inside the bottom sheet, by storing
     * only hashtags (no text) inside the clipboard.
     */
    private void setClickingOnCopyHashtags() {
        mCopyHashtagsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // to show the ripple on the button when clicking on it.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        // add the media hashtags to the clipboard and hint the user that with
                        // a toast message when it copied successfully or there is nothing to
                        // copy in case there is no hashtags for that media.
                        if (mMediaHashtags != null && !mMediaHashtags.isEmpty()) {

                            ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clipData = ClipData.newPlainText("instagram_hashtags", mMediaHashtags);
                            clipboardManager.setPrimaryClip(clipData);

                            Toast.makeText(mContext, R.string.toast_hashtags_copied, Toast.LENGTH_SHORT).show();

                        } else {

                            Toast.makeText(mContext, R.string.toast_no_hashtags_to_copy, Toast.LENGTH_SHORT).show();

                        }

                        // no need to keep the bottom sheet open.
                        closeBottomSheet();

                    }

                }, Constants.CLICKING_DELAYED_TIME_BIG_BUTTON);

            }
        });

    }


    /**
     * Handle clicking on Delete button inside the bottom sheet, by deleting the media file/s and
     * its data from the database.
     */
    private void setClickingOnDelete() {

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // to show the ripple on the button when clicking on it.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        // get the folder path contains the media file/s.
                        File folderPath = new File(MediaEntry.getMediaPath(mProductType));
                        if (!folderPath.exists()) {
                            folderPath.mkdirs();
                        }

                        // get the required file/s and put them inside an ArrayList.
                        File[] files = folderPath.listFiles();
                        if (files != null) {

                            ArrayList<File> mediaFiles = new ArrayList<>();
                            for (File file : files) {
                                if (file.getName().startsWith(mMediaCode)) {

                                    mediaFiles.add(file);

                                }
                            }


                            // delete file/s from the folder and hint the user that the deleting
                            // process done by a toast message.
                            for (int i = 0; i < mediaFiles.size(); i++) {

                                // after delete media files, clear its data from the database.
                                if(mediaFiles.get(i).delete()) {

                                    // no need to repeat the deleting the data from the database because
                                    // it saved one time only so we do it after delete the last file.
                                    if (i == (mediaFiles.size() - 1)) {

                                        Uri uri =  ContentUris.withAppendedId(MediaEntry.CONTENT_URI, mDatabaseId);
                                        mContext.getContentResolver().delete(uri, null, null);

                                        Toast.makeText(mContext, R.string.toast_deleted_successfully, Toast.LENGTH_SHORT).show();

                                    }

                                } else {

                                    Toast.makeText(mContext, R.string.something_wrong_please_try_again, Toast.LENGTH_SHORT).show();

                                }
                            }

                        }

                        // scan the file path to refresh all media in that folder and delete it from gallery.
                        MediaScannerConnection.scanFile(mContext, new String[]{folderPath.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {

                            }
                        });

                        // no need to keep the bottom sheet open.
                        closeBottomSheet();

                    }

                }, Constants.CLICKING_DELAYED_TIME_BIG_BUTTON);

            }
        });

    }


    /**
     * Close the bottom sheet.
     */
    private void closeBottomSheet() {
        BottomSheetFragment.this.dismiss();
    }

}


