package com.log28.memesstore;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.InputStream;
import java.util.List;

public class MemeObject {

    MemesListAdapter memesListAdapter;
    Context context;
    String memeName;
    String memeFolder, thumbnailFolder;
    String memeType;
    String memeTag;
    Bitmap memeBitmap, thumbnailBitmap;

    static String imageformats[]= new String[]{".jpg",".jpeg",".png", ".bmp", ".webp", ".tiff"};
    static String gifformats[]= new String[]{".gif"};
    static String videoformats[]= new String[]{".mp4"};
    static String archformats[]= new String[]{".zip"};
    static String dbformats[]= new String[]{"db"};

    //категории файлов
    public static final int IMAGE = 0;
    public static final int VIDEO = 1;
    public static final int HTTPS = 2;
    public static final int GIF = 3;
    public static final int ARCH = 4;
    public static final int DB = 5;
    public static final int TEMP = -1;

    MemeObject(MemesListAdapter memesListAdapter,String name) {
        init(memesListAdapter,name,"");
    }
    MemeObject(MemesListAdapter memesListAdapter,String name,String tag){
        init(memesListAdapter,name,tag);
    }
    MemeObject(MemesListAdapter memesListAdapter,MemeGroup group){
        init(memesListAdapter,group.name,group.tag);
    }

    void init(MemesListAdapter memesListAdapter,String name,String tag){
        this.memeName=name;
        this.memeTag=tag;
        this.memesListAdapter=memesListAdapter;
        this.context=memesListAdapter.context;
        memeBitmap= BitmapFactory.decodeResource(context.getResources(), R.raw.logo);
        BitmapLoader bitmapLoader=new BitmapLoader();
        bitmapLoader.execute(memeName);
    }
    public String getPath(){
        return "";
    }
    public String getName(){return memeName;}
    public String getTag(){return memeTag;}
    public String getMemeType()
    {
        switch (classifier(memeName)){
            case IMAGE: return "image/*";
            case VIDEO: return "video/*";
            case GIF: return "image/gif";
            case HTTPS: return "text/*";

        }
        return "*/*";
    }

    class BitmapLoader extends AsyncTask<String,Void, Bitmap> {

        @RequiresApi(api = Build.VERSION_CODES.R)
        @Override
        protected Bitmap doInBackground(String... strings) {
            return new FileHelper(context).getPreview(strings[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            memeBitmap=bitmap;
            memesListAdapter.notifyDataSetChanged();
        }
    }

    public Bitmap getBitmap(){

        return memeBitmap;
    }

    public Bitmap getThumbnailBitmap(){
        return thumbnailBitmap;
    }

    public static int classifier(String memeName){
        memeName=memeName.toLowerCase();
        for(String name: imageformats){
            if(memeName.contains(name))
                return IMAGE;
        }
        for(String name: gifformats){
            if(memeName.contains(name))
                return GIF;
        }
        for(String name: videoformats){
            if(memeName.contains(name))
                return VIDEO;
        }

        for(String name: archformats){
            if(memeName.contains(name))
                return ARCH;
        }
        for(String name: dbformats){
            if(memeName.contains(name))
                return DB;
        }
        if(!memeName.contains("."))
            return HTTPS;
        return TEMP;
    }

}
