package com.mtma.insta.downloader.adapters;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mtma.insta.downloader.R;
import com.mtma.insta.downloader.Views.SquareLayout;
import com.mtma.insta.downloader.activities.DisplayingStoryActivity;
import com.mtma.insta.downloader.data.InstaContractor;
import com.mtma.insta.downloader.models.DownloadingObject;

import java.util.ArrayList;


public class StoryDialogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private static final String LOG_TAG = StoryDialogAdapter.class.getSimpleName();
    private Context mContext;


    private ArrayList<DownloadingObject> mDownloadingObjects;


    public StoryDialogAdapter(Context context, ArrayList<DownloadingObject> downloadingObjects) {
        mContext = context;
        mDownloadingObjects = downloadingObjects;
    }




    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        // the view that will work as item view inside the adapter.
        View dataLayoutView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_story, viewGroup, false);

        return new StoriesOverViewHolder(dataLayoutView);

    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int pos) {

        // get holder and the adapter position.
        StoriesOverViewHolder holder = (StoriesOverViewHolder) viewHolder;
        int position = viewHolder.getAdapterPosition();


        holder.setIsRecyclable(false);

        // display a video on the story type video to refere to it.
        int mediaType = mDownloadingObjects.get(holder.getAdapterPosition()).getMediaType();
        if (mediaType == InstaContractor.MediaEntry.MEDIA_TYPE_VIDEO) {
            holder.videoIconImageView.setVisibility(View.VISIBLE);
        } else {
            holder.videoIconImageView.setVisibility(View.GONE);
        }


        // display a thumbnails image fot the story.
        String mediaThumbnailUrl = mDownloadingObjects.get(position).getAllMediaUrls().get(0);
        Glide.with(mContext).load(mediaThumbnailUrl).thumbnail(0.1f).into(holder.mediaImageImageView);


        // handle clicking on the item.
        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // open "DisplayStoryActivity" by intent contains the position of the selected item
                // and all the downloadingObjects which will be used to display all the stories
                // inside a ViewPagerAdapter in that Activity.
                Intent intent = new Intent(mContext, DisplayingStoryActivity.class);
                intent.putExtra("current_position", position);
                intent.putExtra ("downloading_objects", mDownloadingObjects);
                mContext.startActivity(intent);

            }
        });


    }


    @Override
    public int getItemCount() {
        return mDownloadingObjects.size();
    }



    /**
     * ViewHolder for the views we will used inside the item layout to display our data about story.
     */
    public class StoriesOverViewHolder extends RecyclerView.ViewHolder {

        private ImageView mediaImageImageView;
        private ImageView videoIconImageView;
        private SquareLayout mainLayout;

        public StoriesOverViewHolder(View itemView) {
            super(itemView);

            mediaImageImageView = itemView.findViewById(R.id.overview_media_holder);
            videoIconImageView = itemView.findViewById(R.id.overview_is_video);
            mainLayout = itemView.findViewById(R.id.select_stories_overview_item);
        }
    }


}
