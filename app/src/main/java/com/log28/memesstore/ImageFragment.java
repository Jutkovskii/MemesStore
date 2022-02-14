package com.log28.memesstore;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


public class ImageFragment extends Fragment {

ImageView memeImageView;
    Bitmap bmp;
String filename;
    public ImageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_image, container, false);
Context context=container.getContext();

        try {
            FileHelper fileHelper = new FileHelper(context);
            bmp = BitmapFactory.decodeFile(fileHelper.getFullPath(filename),fileHelper.getOptions(filename));
            if(bmp==null)
                bmp=fileHelper.getPreview(filename);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return view;
    }

    public void setMemeImage(String filename){
this.filename =filename;

    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,null);// savedInstanceState);
        memeImageView = view.findViewById(R.id.memeImageView);
        memeImageView.setImageBitmap(bmp);
    }

}