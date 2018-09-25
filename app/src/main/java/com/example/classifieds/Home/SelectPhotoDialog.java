package com.example.classifieds.Home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.classifieds.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class SelectPhotoDialog extends DialogFragment {

    public interface OnPhotoSelectedListener {
        void getImageFromGallery(Uri imagePath);
        void getImageBitMap(Bitmap bitmap);
    }

    private static final String TAG = "SelectPhotoDialog";

    private static final int GET_FROM_GALLERY_REQUEST_CODE = 1;
    private static final int CAMERA_REQUEST_CODE = 2;

    TextView mSelectPhoto, mOpenCamera;
    OnPhotoSelectedListener mOnPhotoSelectedListener;

    public static ImageLoader imageLoader = ImageLoader.getInstance();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_select_photo, container, false);
        SelectPhotoDialog.imageLoader.init(ImageLoaderConfiguration.createDefault(getContext()));

        initWidgets(view);
        init();

        return view;
    }

    private void initWidgets(View view) {
        mSelectPhoto = (TextView) view.findViewById(R.id.dialogChoosePhoto);
        mOpenCamera = (TextView) view.findViewById(R.id.dialogOpenCamera);
    }

    private void init() {
        mSelectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, GET_FROM_GALLERY_REQUEST_CODE);
            }
        });

        mOpenCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GET_FROM_GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            Log.d(TAG, "onActivityResult: " + selectedImage);

            mOnPhotoSelectedListener.getImageFromGallery(selectedImage);
            getDialog().dismiss();
        }
        else if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");

            mOnPhotoSelectedListener.getImageBitMap(bitmap);
            getDialog().dismiss();
        }
    }

    @Override
    public void onAttach(Context context) {
        try {
            mOnPhotoSelectedListener = (OnPhotoSelectedListener) getTargetFragment();
        } catch (ClassCastException e) {
            Log.e(TAG, "onAttach: ClassCastException " + e.getMessage());
        }
        super.onAttach(context);
    }
}
