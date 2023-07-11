package com.log28.memesstore;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import ja.burhanrashid52.photoeditor.BrushDrawingView;
import ja.burhanrashid52.photoeditor.OnPhotoEditorListener;
import ja.burhanrashid52.photoeditor.OnSaveBitmap;
import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;
import ja.burhanrashid52.photoeditor.ViewType;


public class ImageEditorFragment extends Fragment {

    PhotoEditorView mPhotoEditorView;
    PhotoEditor mPhotoEditor;
    TextView text;

    RecyclerView colors;

    int currentcolor = Color.BLACK;
    String relativeFilepath;
    Context context;
    int layoutID;

    Button addText;


    public ImageEditorFragment(int layoutID, String relativeFilepath) {
        this.layoutID = layoutID;
        this.relativeFilepath = relativeFilepath;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(layoutID, container, false);
            context = view.getContext();
            return view;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPhotoEditorView = view.findViewById(R.id.memeEditorView);
        text = view.findViewById(R.id.textToAdd);
        text.setBackgroundColor(Color.WHITE);
        colors = view.findViewById(R.id.colorRecyclerView);
        addText = view.findViewById(R.id.addTextButton);
        addText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String label = text.getText().toString();
                    if (!label.isEmpty())
                        mPhotoEditor.addText(label, currentcolor);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            colors.setLayoutManager(layoutManager);
            colors.setHasFixedSize(true);
            ColorPickerAdapter colorPickerAdapter = new ColorPickerAdapter(context);
            colors.setAdapter(colorPickerAdapter);
            colorPickerAdapter.setOnColorPickerClickListener(new ColorPickerAdapter.OnColorPickerClickListener() {
                @Override
                public void onColorPickerClickListener(int colorCode) {
                    currentcolor = colorCode;
                }
            });
            mPhotoEditorView.getSource().setImageBitmap(MemeFileHelper.createFileHelper(context, MemeUtils.uriFolder).getPreview(relativeFilepath));
            mPhotoEditor = new PhotoEditor.Builder(context, mPhotoEditorView).build();

            mPhotoEditor.onViewAdd(new BrushDrawingView(context));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Uri save() {
        try {
            String newfilepath = relativeFilepath.replaceFirst(".jpg", "1.jpg");
            FileHelper newfile = new FileHelper(context, MemeUtils.uriFolder);

            Uri uri = newfile.getUriFromFile(newfilepath);
            mPhotoEditorView.setDrawingCacheEnabled(true);
            Bitmap bm = mPhotoEditorView.getDrawingCache();

            bm.compress(Bitmap.CompressFormat.JPEG,100,newfile.createFile(newfilepath));
            return uri;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
return null;
    }
}