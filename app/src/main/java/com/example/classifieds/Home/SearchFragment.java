package com.example.classifieds.Home;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.classifieds.Models.Hits;
import com.example.classifieds.Models.HitsList;
import com.example.classifieds.Models.Post;
import com.example.classifieds.R;
import com.example.classifieds.Utils.ElasticSearchAPI;
import com.example.classifieds.Utils.PostListAdapter;
import com.example.classifieds.Utils.RecyclerViewMargin;
import com.example.classifieds.Utils.UniversalImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Credentials;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchFragment extends Fragment{

    private static final String TAG = "SearchFragment";
    private static final int NUM_GRID_COLUMNS = 3;
    private static final int GRID_ITEM_MARGIN = 5;

    private ImageView mFilters;
    private EditText mSearchText;
    private FrameLayout mFrameLayout;

    private String mElasticSearchPassword;
    private String mPreferredCountry;
    private String mPreferredState;
    private String mPreferredCity;
    private RecyclerView mRecyclerView;
    private PostListAdapter mPostListAdapter;

    private static final String BASE_URL = "http://104.198.38.206//elasticsearch/posts/post/";
    private ArrayList<Post> mPosts;

    public static ImageLoader imageLoader = ImageLoader.getInstance();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        SelectPhotoDialog.imageLoader.init(ImageLoaderConfiguration.createDefault(getContext()));

        retrieveElasticSearchPassword();
        initWidgets(view);
        init();
        return view;
    }

    private void toastService(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private void getFilters() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mPreferredCountry = preferences.getString(getString(R.string.preferences_country), "");
        mPreferredState = preferences.getString(getString(R.string.preferences_state), "");
        mPreferredCity = preferences.getString(getString(R.string.preferences_city), "");

        Log.d(TAG, "getFilters: \nCountry: " + mPreferredCountry + "\nState: " + mPreferredState
                + "\nCity: " + mPreferredCity);
    }

    @Override
    public void onResume() {
        super.onResume();
        getFilters();
    }

    private void setUpPostList() {
        RecyclerViewMargin itemDecorator = new RecyclerViewMargin(NUM_GRID_COLUMNS, GRID_ITEM_MARGIN);
        mRecyclerView.addItemDecoration(itemDecorator);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), NUM_GRID_COLUMNS);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mPostListAdapter = new PostListAdapter(getActivity(), mPosts);
        mRecyclerView.setAdapter(mPostListAdapter);
    }

    private void initWidgets(View view) {
        mFilters = (ImageView) view.findViewById(R.id.filterBtn);
        mSearchText = (EditText) view.findViewById(R.id.inputSearch);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mFrameLayout = (FrameLayout) view.findViewById(R.id.container);
    }

    private void init() {
        mFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to filters activity.");
                Intent intent = new Intent(getActivity(), FiltersActivity.class);
                startActivity(intent);
            }
        });

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if(actionId == EditorInfo.IME_ACTION_DONE ||
                   actionId == EditorInfo.IME_ACTION_SEARCH ||
                   event.getAction() == KeyEvent.ACTION_DOWN ||
                   event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

                    mPosts = new ArrayList<Post>();

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    ElasticSearchAPI searchAPI = retrofit.create(ElasticSearchAPI.class);

                    HashMap<String, String> headerMap = new HashMap<String, String>();
                    headerMap.put("Authorization", Credentials.basic("user", mElasticSearchPassword));

                    String searchString = "";

                    if(!mSearchText.equals("")){
                        searchString = searchString + mSearchText.getText().toString() + "*";
                    }
                    if(!mPreferredCountry.equals("")){
                        searchString = searchString + " country:" + mPreferredCountry;
                    }
                    if(!mPreferredState.equals("")){
                        searchString = searchString + " state:" + mPreferredState;
                    }
                    if(!mPreferredCity.equals("")){
                        searchString = searchString + " city:" + mPreferredCity;
                    }

                    Call<Hits> call = searchAPI.search(headerMap, "AND", searchString);

                    call.enqueue(new Callback<Hits>() {
                        @Override
                        public void onResponse(Call<Hits> call, Response<Hits> response) {

                            HitsList hitsList = new HitsList();
                            String jsonResponse = "";
                            try{
                                Log.d(TAG, "onResponse: server response: " + response.toString());

                                if(response.isSuccessful()){
                                    hitsList = response.body().getHitsList();
                                }else{
                                    jsonResponse = response.errorBody().string();
                                }

                                Log.d(TAG, "onResponse: hits: " + hitsList);

                                for(int i = 0; i < hitsList.getPostSources().size(); i++){
                                    Log.d(TAG, "onResponse: Posts [" + i + "]" + hitsList.getPostSources().get(i).getPost().toString());
                                    mPosts.add(hitsList.getPostSources().get(i).getPost());
                                }

                                Log.d(TAG, "onResponse: size: " + mPosts.size());
                                setUpPostList();

                            }catch (NullPointerException e){
                                Log.e(TAG, "onResponse: NullPointerException: " + e.getMessage() );
                            }
                            catch (IndexOutOfBoundsException e){
                                Log.e(TAG, "onResponse: IndexOutOfBoundsException: " + e.getMessage() );
                            }
                            catch (IOException e){
                                Log.e(TAG, "onResponse: IOException: " + e.getMessage() );
                            }
                        }

                        @Override
                        public void onFailure(Call<Hits> call, Throwable t) {
                            toastService(getString(R.string.something_wrong));
                        }
                    });

                }
                return false;
            }
        });
    }

    private void retrieveElasticSearchPassword() {
        Query query = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.node_elasticsearch))
                .orderByValue();

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                mElasticSearchPassword = singleSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void viewPost(String postId) {
        ViewPostFragment viewPostFragment = new ViewPostFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

        Bundle args = new Bundle();
        args.putString(getString(R.string.get_post_id), postId);
        viewPostFragment.setArguments(args);

        transaction.replace(R.id.container, viewPostFragment, getString(R.string.fragment_post));
        transaction.addToBackStack(getString(R.string.fragment_post));
        transaction.commit();

        mFrameLayout.setVisibility(View.VISIBLE);
    }
}
