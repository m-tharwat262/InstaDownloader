package com.mtma.insta.downloader.fragments;


import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.mtma.insta.downloader.R;
import com.universalvideoview.UniversalMediaController;
import com.universalvideoview.UniversalVideoView;

import java.io.File;


public class DisplayingMediaFragment extends Fragment {


    private static final String LOG_TAG = DisplayingMediaFragment.class.getSimpleName();
    private final Context mContext;


    private View mMainView;
    private PhotoView mPhotoView;
    private FrameLayout mVideoFrameLayout;
    private UniversalVideoView mUniversalVideoView;
    private UniversalMediaController mUniversalMediaController;
    private ImageView mPlayButton;


    private final Uri mUri;


    public DisplayingMediaFragment(Context context, Uri uri) {
        mContext = context;
        mUri = Uri.fromFile(new File(uri.getPath()));
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_display_media, container, false);


        // initialize views ids.
        initializeViewIds();


        // display the media file (image or video) on the screen.
        displayMediaFile();


        return mMainView;

    }


    /**
     * Initialize the views ids from the main layout of the Fragment.
     */
    private void initializeViewIds() {

        mPhotoView = mMainView.findViewById(R.id.fragment_display_media_photo_view);
        mVideoFrameLayout = mMainView.findViewById(R.id.fragment_display_media_video_frame_layout);
        mUniversalVideoView = mMainView.findViewById(R.id.fragment_display_media_universal_video_view);
        mUniversalMediaController = mMainView.findViewById(R.id.fragment_display_media_universal_media_controller);
        mPlayButton = mMainView.findViewById(R.id.fragment_display_media_play_video_button);

    }


    /**
     * Display the picture if the Uri for an image file or as a thumbnail if the Uri was for
     * a video file.
     * & Handle the process for the video case by display play button and add the media controller
     * to the videoView and the callbacks for the video.
     */
    private void displayMediaFile(){

        // display a Picture for the Uri provide (can be a thumbnails for videos case).
        Glide.with(mContext).load(mUri).into(mPhotoView);

        // in case the file refer to a video file, so we need to display the play button and
        // setup some properties for the VideoView that will display the video.
        if(mUri.getPath().contains(".mp4")) {

            mPlayButton.setVisibility(View.VISIBLE);

            setupVideoProperties();

            setupClickingOnPlayButton();

        }

    }


    /**
     * Handle Clicking on the play button by making it plays the video and display the frameLayout
     * that contains the UniversalVideoView and the UniversalMediaController.
     */
    private void setupClickingOnPlayButton() {

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mUniversalVideoView.start();

                mVideoFrameLayout.setVisibility(View.VISIBLE);

            }
        });

    }


    /**
     * Add the required properties to the video view and handle cases like when the video
     * (start - pause - finish) what should happened.
     */
    private void setupVideoProperties() {

        // add the media controller to the video view and the uri for the video file we want to display.
        mUniversalVideoView.setMediaController(mUniversalMediaController);
        mUniversalVideoView.setVideoURI(mUri);


        // handle what should happen in case the video start and pause.
        mUniversalVideoView.setVideoViewCallback(new UniversalVideoView.VideoViewCallback() {
            @Override
            public void onStart(MediaPlayer mediaPlayer) {

                // no need to make the thumbnails picture displayed so we hide it.
                mPhotoView.setVisibility(View.GONE);

                // hide both play button and the media controller.
                mPlayButton.setVisibility(View.GONE);
                mUniversalMediaController.hide();

            }

            @Override
            public void onPause(MediaPlayer mediaPlayer) {

                // display both play button and the media controller.
                mPlayButton.setVisibility(View.VISIBLE);
                mUniversalMediaController.hide();

            }

            @Override
            public void onScaleChange(boolean isFullscreen) {
            }

            @Override
            public void onBufferingStart(MediaPlayer mediaPlayer) {
            }

            @Override
            public void onBufferingEnd(MediaPlayer mediaPlayer) {
            }

        });



        // after the video finished we will add the uri again as a new file to display
        // (this step important to hide the play button that appears from the controller even
        // the video run or stop).
        mUniversalVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                // (important) add the Uri for the file so it is look like that it plays a new file
                // because there is problem in showing the play button for the controller.
                mUniversalVideoView.setVideoURI(mUri);

                // display the play button.
                mPlayButton.setVisibility(View.VISIBLE);

                // hide the FrameLayout that contains to make the thumbnails picture appears.
                mPhotoView.setVisibility(View.VISIBLE);
                mVideoFrameLayout.setVisibility(View.GONE);



            }
        });



    }



    @Override
    public void onResume() {
        super.onResume();

        if(mUniversalVideoView.isPlaying())
            mUniversalVideoView.stopPlayback();

    }

    @Override
    public void onPause() {
        super.onPause();

        if(mUniversalVideoView.isPlaying())
            mUniversalVideoView.stopPlayback();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if(mUniversalVideoView.isPlaying())
            mUniversalVideoView.stopPlayback();

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if(mUniversalVideoView.isPlaying())
            mUniversalVideoView.stopPlayback();

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(this.isVisible()) {

            if(!isVisibleToUser) {
                mUniversalVideoView.pause();
            }

        }

    }






}
