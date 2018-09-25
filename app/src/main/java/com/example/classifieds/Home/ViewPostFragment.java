package com.example.classifieds.Home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.classifieds.Models.Post;
import com.example.classifieds.R;
import com.example.classifieds.Utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ViewPostFragment extends Fragment {

    private static final String TAG = "ViewPostFragment";

    private String mPostId;
    private Post mPost;

    private TextView mContactSeller, mTitle, mDescription, mPrice, mLocation, mSavePost;
    private ImageView mClose, mWatchList, mPostImage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPostId = (String) getArguments().get(getString(R.string.get_post_id));
        Log.d(TAG, "onCreate: PostId : " + mPostId);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_post, container, false);
        initWidgets(view);
        return view;
    }

    private void addPostToWatchList() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        reference.child(getString(R.string.node_watchlist))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mPostId)
                .child(getString(R.string.field_post_id))
                .setValue(mPostId);

        Toast.makeText(getActivity(), getString(R.string.add_watchlist), Toast.LENGTH_SHORT).show();
    }

    private void removePostFromWatchList() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        reference.child(getString(R.string.node_watchlist))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mPostId)
                .removeValue();

        Toast.makeText(getActivity(), getString(R.string.remove_watchlist), Toast.LENGTH_SHORT).show();
    }

    private void getPostInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child(getString(R.string.node_posts))
                .orderByKey()
                .equalTo(mPostId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot snapshot = dataSnapshot.getChildren().iterator().next();
                if(snapshot != null) {
                    mPost = snapshot.getValue(Post.class);
                    Log.d(TAG, "onDataChange: " + mPost.toString());

                    mTitle.setText(mPost.getTitle());
                    mDescription.setText(mPost.getDesc());
                    mPrice.setText(mPost.getPrice());

                    String location = mPost.getCountry() + ", " + mPost.getState() + ", " + mPost.getCity();
                    mLocation.setText(location);

                    mTitle.setText(mPost.getTitle());
                    UniversalImageLoader.setImage(mPost.getImagePath(), mPostImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void checkFragmentForWatchlist() {
        Fragment fragment = (Fragment)((SearchActivity)getActivity()).getSupportFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.viewPager + ":" +
                        ((SearchActivity)getActivity()).mViewPager.getCurrentItem());

        if(fragment != null) {
            if(fragment.getTag().equals("android:switcher:" + R.id.viewPager + ":0")) {

                mSavePost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addPostToWatchList();
                    }
                });

                mWatchList.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addPostToWatchList();
                    }
                });

            }
            else if(fragment.getTag().equals("android:switcher:" + R.id.viewPager + ":1")) {
                mSavePost.setText("Remove from Watchlist");

                mSavePost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removePostFromWatchList();
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                });

                mWatchList.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removePostFromWatchList();
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                });
            }
        }
    }

    private void init() {
        getPostInfo();
        checkFragmentForWatchlist();

        mContactSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("plain/text");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{mPost.getEmail()});
                getActivity().startActivity(emailIntent);
            }
        });

        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        mSavePost.setShadowLayer(5, 0 , 0, Color.WHITE);
        mWatchList.setImageBitmap(createOutline(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_save)));
        mWatchList.setColorFilter(Color.WHITE);
        mClose.setImageBitmap(createOutline(BitmapFactory.decodeResource(getResources(), R.drawable.ic_close)));
        mClose.setColorFilter(Color.WHITE);
    }

    private void initWidgets(View view) {
        mContactSeller = (TextView) view.findViewById(R.id.contactSeller);
        mTitle = (TextView) view.findViewById(R.id.postTitle);
        mDescription = (TextView) view.findViewById(R.id.postDescription);
        mPrice = (TextView) view.findViewById(R.id.postPrice);
        mLocation = (TextView) view.findViewById(R.id.postLocation);
        mClose = (ImageView) view.findViewById(R.id.postClose);
        mWatchList = (ImageView) view.findViewById(R.id.addToWatchList);
        mPostImage = (ImageView) view.findViewById(R.id.postImage);
        mSavePost = (TextView) view.findViewById(R.id.savePost);

        hideSoftKeyboard();

        init();
    }

    private void hideSoftKeyboard(){
        final Activity activity = getActivity();
        final InputMethodManager inputManager = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private Bitmap createOutline(Bitmap src){
        Paint p = new Paint();
        p.setMaskFilter(new BlurMaskFilter(2, BlurMaskFilter.Blur.OUTER));
        return src.extractAlpha(p, null);
    }
}
