package com.mtma.insta.downloader.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.mtma.insta.downloader.R;
import com.mtma.insta.downloader.models.UserHasStoryObject;
import com.mtma.insta.downloader.fragments.StoryDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;


public class CircleStoriesAdapter extends ArrayAdapter<UserHasStoryObject> {


    private final Context mContext;

    public static final int MODE_FAKE_DATA = 0;
    public static final int MODE_REAL_DATA = 1;
    private final int mMode;

    ArrayList<UserHasStoryObject> mUserHasStories;
    ArrayList<UserHasStoryObject> mUserHasStoriesFull = new ArrayList<>();



    public CircleStoriesAdapter(Context context, ArrayList<UserHasStoryObject> userHasStoriesObjects, int mode) {
        super(context, 0, userHasStoriesObjects);
        mContext = context;
        mUserHasStories = userHasStoriesObjects;
        mUserHasStoriesFull.addAll(mUserHasStories); // (very important to the filter process).
        mMode = mode;
    }




    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {


        // inflate the item view that will the adapter use.
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView =  LayoutInflater.from(mContext).inflate(R.layout.item_story_circle, parent, false);
        }


        // determine the views from the inflated layout.
        TextView userNameTextView = listItemView.findViewById(R.id.item_main_stories_user_name);
        CircleImageView circleImageView = listItemView.findViewById(R.id.item_main_stories_media_image);



        // get the current object.
        UserHasStoryObject userHasStoryObject = getItem(position);



        // display the username.
        String userName = userHasStoryObject.getUserName();
        if (!userName.isEmpty()) {
            userNameTextView.setText(userName);
        }




        // Because the adapter can be used for two phases with (Real - Fake) data.
        // only the Real Data case need this situation for display the real user profile picture
        // otherwise the default image is a placeholder picture will be displayed and that already
        // added inside the xml file.
        if (mMode != MODE_FAKE_DATA) {

            // display the user profile picture.
            Glide.with(mContext).load(userHasStoryObject.getProfilePictureUrl()).thumbnail(0.1f).into(circleImageView);

            // handle clicking on the user profile picture.
            circleImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // get the username and the userId for the item that the user clicked on.
                    String userName = userHasStoryObject.getUserName();
                    String userId = userHasStoryObject.getUserId();

                    // inflate a Dialog Fragment to display the stories that the user owen.
                    StoryDialogFragment storyDialogFragment = new StoryDialogFragment(mContext, userName, userId);
                    storyDialogFragment.show(((AppCompatActivity) mContext).getSupportFragmentManager(), storyDialogFragment.getTag());

                }
            });

        }


        return listItemView;

    }


    /**
     * Filter the List that the adapter display by make search on the username and real name provided
     * in each item with the text that the user type inside the search field.
     *
     * @return ArrayList contains the new List must the adapter display.
     */
    @Override
    public Filter getFilter() {

        Filter filteredBusiness = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                List<UserHasStoryObject> filteredList = new ArrayList<>();

                if (charSequence == null || charSequence.length() == 0){

                    filteredList.addAll(mUserHasStoriesFull);

                }else{

                    String filteredPattern = charSequence.toString().toLowerCase(Locale.ROOT).trim();
                    for (UserHasStoryObject userHasStory : mUserHasStoriesFull){

                        String usernameWithLowerCase = userHasStory.getUserName().toLowerCase(Locale.ROOT);
                        String realNameWithLowerCase = userHasStory.getRealName().toLowerCase(Locale.ROOT);

                        if (usernameWithLowerCase.contains(filteredPattern) || realNameWithLowerCase.contains(filteredPattern)) {
                            filteredList.add(userHasStory);
                        }

                    }

                }

                FilterResults results = new FilterResults();
                results.values = filteredList;

                return results;

            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

                mUserHasStories.clear();
                mUserHasStories.addAll((List) filterResults.values);
                notifyDataSetChanged();

            }

        };


        return filteredBusiness;
    }


}
