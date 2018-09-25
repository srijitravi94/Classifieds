package com.example.classifieds.Home;


import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.classifieds.Models.Post;
import com.example.classifieds.R;
import com.example.classifieds.Utils.RotateBitmap;
import com.example.classifieds.Utils.UniversalImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class PostFragment extends Fragment implements SelectPhotoDialog.OnPhotoSelectedListener {

    private static final String TAG = "PostFragment";

    @Override
    public void getImageFromGallery(Uri imagePath) {
        UniversalImageLoader.setImage(imagePath.toString(), mPostImage);
        mSelectedUri = imagePath;
        mSelectedBitmap = null;
    }

    @Override
    public void getImageBitMap(Bitmap bitmap) {
        mPostImage.setImageBitmap(bitmap);
        mSelectedUri = null;
        mSelectedBitmap = bitmap;
    }

    private ImageView mPostImage;
    private EditText mTitle, mDescription, mPrice, mCountry, mState, mCity, mEmail;
    private Button mPostBtn;
    private ProgressBar mProgressBar;

    private Uri mSelectedUri;
    private Bitmap mSelectedBitmap;
    private byte[] mUploadBytes;
    private double mProgress = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        initWidgets(view);
        init();

        return view;
    }

    private void resetFields(){
        UniversalImageLoader.setImage("", mPostImage);
        mTitle.setText("");
        mDescription.setText("");
        mPrice.setText("");
        mCountry.setText("");
        mState.setText("");
        mCity.setText("");
        mEmail.setText("");
    }

    private void showProgressBar(){
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        if(mProgressBar.getVisibility() == View.VISIBLE){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void toastService(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private boolean validateFields(String field) {
        return field.equals("");
    }

    public static byte[] getBytesFromBitmap(Bitmap bitmap, Integer quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }

    private void executeImageUploadTask() {
        toastService(getString(R.string.uploading_image));

        final String postId = FirebaseDatabase.getInstance().getReference().push().getKey();

        final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("posts/users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + postId + "/post_image");

        UploadTask uploadTask = storageReference.putBytes(mUploadBytes);

        Log.d(TAG, "executeImageUploadTask: " + postId);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                hideProgressBar();
                Uri firebaseUri = taskSnapshot.getDownloadUrl();

                Log.d(TAG, "onSuccess: firebase download url: " + firebaseUri.toString());

                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
                Post post = new Post();
                post.setImagePath(firebaseUri.toString());
                post.setCity(mCity.getText().toString());
                post.setEmail(mEmail.getText().toString());
                post.setCountry(mCountry.getText().toString());
                post.setDesc(mDescription.getText().toString());
                post.setPost_id(postId);
                post.setPrice(mPrice.getText().toString());
                post.setState(mState.getText().toString());
                post.setTitle(mTitle.getText().toString());
                post.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

                myRef.child(getString(R.string.node_posts))
                        .child(postId)
                        .setValue(post);

                toastService(getString(R.string.image_upload_success));

                resetFields();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                toastService(getString(R.string.something_wrong));
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double currentProgress = (100 * (taskSnapshot.getBytesTransferred())) / taskSnapshot.getTotalByteCount();
                if(currentProgress > (mProgress + 10)){
                    mProgress = (100 * (taskSnapshot.getBytesTransferred())) / taskSnapshot.getTotalByteCount();
                    Log.d(TAG, "onProgress: " + mProgress);
                    toastService(mProgress + "%");
                }
            }
        });
    }

    public class ResizeImageInBackground extends AsyncTask<Uri, Integer, byte[]> {
        Bitmap mBitmap;

        public ResizeImageInBackground(Bitmap mBitmap) {
            if(mBitmap != null) {
                this.mBitmap = mBitmap;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            toastService(getString(R.string.compressing_image));
            showProgressBar();
        }

        @Override
        protected byte[] doInBackground(Uri... uris) {
            if (mBitmap == null) {
                try {
                    RotateBitmap bitmap = new RotateBitmap();
                    mBitmap = bitmap.HandleSamplingAndRotationBitmap(getActivity(), uris[0]);
                } catch (Exception e) {
                    Log.d(TAG, "doInBackground: Exception " + e.getMessage());
                }
            }
            byte[] bytes = null;
            Log.d(TAG, "doInBackground: megabytes before compression: " + mBitmap.getByteCount() / 1000000 );
            bytes = getBytesFromBitmap(mBitmap, 100);
            Log.d(TAG, "doInBackground: megabytes after compression: " + bytes.length / 1000000 );
            return bytes;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            mUploadBytes = bytes;
            executeImageUploadTask();
        }
    }

    private void uploadImage(Uri imagePath){
        ResizeImageInBackground resize = new ResizeImageInBackground(null);
        resize.execute(imagePath);
    }

    private void uploadImage(Bitmap bitmap){
        ResizeImageInBackground resize = new ResizeImageInBackground(bitmap);
        Uri uri = null;
        resize.execute(uri);
    }

    private void initWidgets(View view) {
        mPostImage = view.findViewById(R.id.postImage);
        mTitle = view.findViewById(R.id.postTitle);
        mDescription = view.findViewById(R.id.postDescription);
        mPrice = view.findViewById(R.id.postPrice);
        mCountry = view.findViewById(R.id.postCountry);
        mState = view.findViewById(R.id.postState);
        mCity = view.findViewById(R.id.postCity);
        mEmail = view.findViewById(R.id.postEmail);
        mPostBtn = view.findViewById(R.id.postBtn);
        mProgressBar = view.findViewById(R.id.progressBar);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    private void init(){

        mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: opening dialog to choose new photo");
                mProgress = 0;
                SelectPhotoDialog dialog = new SelectPhotoDialog();
                dialog.show(getFragmentManager(), getString(R.string.select_photo));
                dialog.setTargetFragment(PostFragment.this, 1);
            }
        });

        mPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mTitle.getText().toString().trim();
                String desc = mDescription.getText().toString().trim();
                String price = mPrice.getText().toString().trim();
                String country = mCountry.getText().toString().trim();
                String state = mState.getText().toString().trim();
                String city = mCity.getText().toString().trim();
                String email = mEmail.getText().toString().trim();

                if(validateFields(title)) {
                    toastService(getString(R.string.title_empty));
                }
                else if(validateFields(desc)) {
                    toastService(getString(R.string.desc_empty));
                }
                else if(validateFields(price)) {
                    toastService(getString(R.string.price_empty));
                }
                else if(validateFields(country)) {
                    toastService(getString(R.string.country_empty));
                }
                else if(validateFields(state)) {
                    toastService(getString(R.string.state_empty));
                }
                else if(validateFields(city)) {
                    toastService(getString(R.string.city_empty));
                }
                else if(validateFields(email)) {
                    toastService(getString(R.string.email_empty));
                }
                else {
                    if(mSelectedBitmap == null && mSelectedUri != null){
                        uploadImage(mSelectedUri);
                    } 
                    else if(mSelectedBitmap != null && mSelectedUri == null) {
                        uploadImage(mSelectedBitmap);
                    } 
                    else {
                        toastService(getString(R.string.image_empty));
                    }
                }

            }
        });
    }
}
