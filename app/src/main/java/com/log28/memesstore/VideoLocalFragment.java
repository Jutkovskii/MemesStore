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
    String filename;
    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_local, container, false);
        //filepath = new FileHelper(container.getContext()).getFullPath(filename);
        filepath=filename;
        context = view.getContext();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, null);// savedInstanceState);
        videoPlayer = view.findViewById(R.id.memeLocalVideoView);
        /*videoPlayer.setVideoPath(filepath);
        videoPlayer.start();*/

        /*String[] selectionArgs = new String[]{Environment.DIRECTORY_MOVIES + "/" +"MemesStore2/" + "Videos/"};
        Uri contentUri = MediaStore.Files.getContentUri("external");

        String selection = MediaStore.MediaColumns.RELATIVE_PATH+ "=?";



        Cursor cursor = context.getContentResolver().query(contentUri, null, selection, selectionArgs, null);

        Uri uri = null;

        if (cursor.getCount() == 0) {
            Toast.makeText(context, "No file found", Toast.LENGTH_LONG).show();
        } else {
            while (cursor.moveToNext()) {
                String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));

                if (fileName.equals(filename)) {
                    long id = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID));

                    uri = ContentUris.withAppendedId(contentUri, id);

                    break;
                }
            }

            if (uri == null) {
                Toast.makeText(context, "file not found", Toast.LENGTH_SHORT).show();
            } else {


                videoPlayer.setVideoURI(uri);
                videoPlayer.start();
            }*/

        videoPlayer.setVideoURI(new FileHelper2(context).getVideoUri(filepath));
        videoPlayer.start();
    }



    public void setMemeImage(String filename){
        this.filename  = filename;

    }
}