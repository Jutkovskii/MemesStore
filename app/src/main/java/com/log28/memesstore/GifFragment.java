package com.log28.memesstore;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.FileInputStream;
import java.io.InputStream;


public class GifFragment extends Fragment {


    String filename;
    Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gif, container, false);
        context=view.getContext();
        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, null);
        InputStream stream = null;
        try {
            String path = FileHelper2.getFullPath(filename);

            //stream = context.getAssets().open(path);
            stream = new FileInputStream(path);

        } catch (Exception e) {
            e.printStackTrace();
        }
        GifView gifView = new GifView(context, stream);
        ConstraintLayout layout = view.findViewById(R.id.gifLayout);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) layout.getLayoutParams();
        params.constrainedWidth=true;

        //gifView.setLayoutParams(params);
        layout.addView(gifView);

    }

    public void setMemeImage(String filename){
        this.filename  = filename;

    }
}