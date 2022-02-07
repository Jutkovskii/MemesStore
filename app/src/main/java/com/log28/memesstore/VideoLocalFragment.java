package com.log28.memesstore;

import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;


public class VideoLocalFragment extends Fragment {
    VideoView videoPlayer;
String filepath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video_local, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,null);// savedInstanceState);
        videoPlayer = view.findViewById(R.id.memeLocalVideoView);
        videoPlayer.setVideoPath(filepath);

        videoPlayer.start();
    }

    public void setMemeImage(String filePath){

        filepath  = filePath;


    }
}