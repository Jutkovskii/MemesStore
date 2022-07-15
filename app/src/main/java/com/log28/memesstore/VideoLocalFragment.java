package com.log28.memesstore;

import android.content.Context;
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
    String relativeFilepath;
    Context context;
    int layoutID;
    public VideoLocalFragment(int layoutID,String relativeFilepath) {
        this.layoutID=layoutID;
        this.relativeFilepath=relativeFilepath;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(layoutID, container, false);

         context = view.getContext();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, null);// savedInstanceState);
        videoPlayer = view.findViewById(R.id.memeLocalVideoView);
        videoPlayer.setVideoURI(new FileHelper2(context).getVideoUri(filepath));
        videoPlayer.start();
    }

}