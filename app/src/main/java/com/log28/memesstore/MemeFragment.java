package com.log28.memesstore;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class MemeFragment extends Fragment {

    Context context;
    private int layoutID;
    String relativeFilepath;
    public void ImageFragment() {
        // Required empty public constructor
    }

    public void ImageFragment(int layoutID) {
        this.layoutID=layoutID;
    }



    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(layoutID, container, false);
         context=view.getContext();
        return view;
    }

    public void setMemeRelativeFilepath(String relativeFilepath){
        this.relativeFilepath =relativeFilepath;

    }


}