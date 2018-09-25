package com.example.classifieds.Home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.classifieds.Models.Post;
import com.example.classifieds.R;
import com.example.classifieds.Utils.PostListAdapter;
import com.example.classifieds.Utils.RecyclerViewMargin;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class WatchListFragment extends Fragment {

    private static final String TAG = "WatchListFragment";
    private static final int NUM_GRID_COLUMNS = 3;
    private static final int GRID_ITEM_MARGIN = 5;

    private RecyclerView mRecyclerView;
    private FrameLayout mFrameLayout;

    private PostListAdapter mPostListAdapter;
    private ArrayList<Post> mPosts;
    private ArrayList<String> mPostIds;
    private DatabaseReference mReference;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_watchlist, container, false);

        initWidgets(view);
        init();
        return view;
    }

    private void setUpPostList() {
        RecyclerViewMargin itemDecorator = new RecyclerViewMargin(NUM_GRID_COLUMNS, GRID_ITEM_MARGIN);
        mRecyclerView.addItemDecoration(itemDecorator);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), NUM_GRID_COLUMNS);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mPostListAdapter = new PostListAdapter(getActivity(), mPosts);
        mRecyclerView.setAdapter(mPostListAdapter);
    }

    public void viewPost(String postId) {
        ViewPostFragment viewPostFragment = new ViewPostFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

        Bundle args = new Bundle();
        args.putString(getString(R.string.get_post_id), postId);
        viewPostFragment.setArguments(args);

        transaction.replace(R.id.watchlistContainer, viewPostFragment, getString(R.string.fragment_post));
        transaction.addToBackStack(getString(R.string.fragment_post));
        transaction.commit();

        mFrameLayout.setVisibility(View.VISIBLE);
    }

    private void getPosts() {
        if(mPostIds.size() > 0) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

            for(int i=0; i<mPostIds.size(); i++) {
                Query query = reference.child(getString(R.string.node_posts))
                        .orderByKey()
                        .equalTo(mPostIds.get(i));

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        DataSnapshot snapshot = dataSnapshot.getChildren().iterator().next();
                        Post post = snapshot.getValue(Post.class);
                        Log.d(TAG, "onDataChange: " + post.toString());
                        mPosts.add(post);
                        mPostListAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        } else {
            mPostListAdapter.notifyDataSetChanged();
        }
    }

    private void getWatchlistPostIds() {
        if(mPosts != null) {
            mPosts.clear();
        }
        if(mPostIds != null) {
            mPostIds.clear();
        }

        mPostIds = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child(getString(R.string.node_watchlist))
                .orderByKey()
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildren().iterator().hasNext()) {
                    DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                    for(DataSnapshot snapshot : singleSnapshot.getChildren()) {
                        String id = snapshot.child(getString(R.string.field_post_id)).getValue().toString();
                        mPostIds.add(id);
                    }
                    getPosts();
                } else {
                    getPosts();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initWidgets(View  view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.watchlistRecyclerView);
        mFrameLayout = (FrameLayout) view.findViewById(R.id.watchlistContainer);
    }

    private void init() {
        mPosts = new ArrayList<>();
        setUpPostList();

        mReference = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.node_watchlist))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mReference.addValueEventListener(mListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mReference.removeEventListener(mListener);
    }

    ValueEventListener mListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            getWatchlistPostIds();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
}
