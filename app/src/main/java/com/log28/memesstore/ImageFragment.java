package com.log28.memesstore;

import android.content.Context;
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

import java.io.FileNotFoundException;


public class ImageFragment extends Fragment {

ImageView memeImageView;
String relativeFilepath;
    Context context;
    int layoutID;
    public ImageFragment(int layoutID,String relativeFilepath) {
        this.layoutID=layoutID;
        this.relativeFilepath=relativeFilepath;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(layoutID, container, false);
        context=view.getContext();
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,null);// savedInstanceState);
        memeImageView = view.findViewById(R.id.memeImageView);
        try {
            memeImageView.setImageBitmap( MemeFileHelper.createFileHelper(context, MainActivity.uriFolder).getPreview(relativeFilepath));
      } catch (Exception e) {
            e.printStackTrace();
        }
    }

}