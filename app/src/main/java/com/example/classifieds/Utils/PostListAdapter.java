package com.example.classifieds.Utils;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.classifieds.Home.SearchActivity;
import com.example.classifieds.Home.SearchFragment;
import com.example.classifieds.Home.WatchListFragment;
import com.example.classifieds.Models.Post;
import com.example.classifieds.R;

import java.util.ArrayList;

public class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.ViewHolder>{

    private static final String TAG = "PostListAdapter";
    private static final int NUM_GRID_COLUMNS = 3;

    private ArrayList<Post> mPosts;
    private Context mContext;

    public class ViewHolder extends RecyclerView.ViewHolder{

        SquareImageView mPostImage;

        public ViewHolder(View itemView) {
            super(itemView);
            mPostImage = (SquareImageView) itemView.findViewById(R.id.post_image);

            int gridWidth = mContext.getResources().getDisplayMetrics().widthPixels;
            int imageWidth = gridWidth/NUM_GRID_COLUMNS;
            mPostImage.setMaxHeight(imageWidth);
            mPostImage.setMaxWidth(imageWidth);
        }
    }

    public PostListAdapter(Context context, ArrayList<Post> posts) {
        mPosts = posts;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_view_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final int pos = position;
        UniversalImageLoader.setImage(mPosts.get(pos).getImagePath(), holder.mPostImage);
        
        holder.mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = (Fragment)((SearchActivity)mContext).getSupportFragmentManager()
                        .findFragmentByTag("android:switcher:" + R.id.viewPager + ":" +
                                ((SearchActivity)mContext).mViewPager.getCurrentItem());

                if(fragment != null) {
                    if(fragment.getTag().equals("android:switcher:" + R.id.viewPager + ":0")) {
                        SearchFragment searchFragment = (SearchFragment) ((SearchActivity)mContext).getSupportFragmentManager()
                                .findFragmentByTag("android:switcher:" + R.id.viewPager + ":" +
                                        ((SearchActivity)mContext).mViewPager.getCurrentItem());

                        searchFragment.viewPost(mPosts.get(pos).getPost_id());

                    }
                    else if(fragment.getTag().equals("android:switcher:" + R.id.viewPager + ":1")) {
                        WatchListFragment watchListFragment = (WatchListFragment) ((SearchActivity)mContext).getSupportFragmentManager()
                                .findFragmentByTag("android:switcher:" + R.id.viewPager + ":" +
                                        ((SearchActivity)mContext).mViewPager.getCurrentItem());

                        watchListFragment.viewPost(mPosts.get(pos).getPost_id());
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }


}
