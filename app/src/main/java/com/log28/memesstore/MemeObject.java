package com.log28.memesstore;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class MemeObject {

    MemesListAdapter memesListAdapter;
    Context context;
    private String memeName;
    private String memeFolder, thumbnailFolder;
    private  String memeMimeType;
    private String memeTag;
    private Bitmap memeBitmap, thumbnailBitmap;
    private int memeType;
    private int memeTab;


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
    //категории вкладок
    public static final int IMAGE_TAB = 0;
    public static final int VIDEO_TAB = 1;

    //подкатегории мемов
    public static final int YOUTUBE=20;
    public static final int VK=21;
    public static final int DISCORD=22;
    MemeObject(MemesListAdapter memesListAdapter,String name) {
        this.memesListAdapter=memesListAdapter;
        init(memesListAdapter.context,name,"");
    }
    MemeObject(Context context,String name) {
        init(context,name,"");
    }
    MemeObject(Context context,String name,String tag) {
        init(context,name,tag);
    }
    MemeObject(MemesListAdapter memesListAdapter,String name,String tag){
        this.memesListAdapter=memesListAdapter;
        init(memesListAdapter.context,name,tag);
    }


    void init(Context context,String name,String tag){
        this.memeName=name;
        this.memeTag=tag;
        this.context=context;
        memeBitmap= BitmapFactory.decodeResource(context.getResources(), R.raw.logo);
        if(memesListAdapter!=null){
            BitmapLoader bitmapLoader=new BitmapLoader();
            bitmapLoader.execute(memeName);
        }
        memeMimeType = getMemeMimeType();
        memeType=classfyByName(memeName);
        memeFolder=FileHelper.getFullPath(memeName);
        memeTab=classifyByTab(memeName);
    }
    public String getPath(){
        return "";
    }
    public String getName(){return memeName;}
    public String getTag(){return memeTag;}
    public int getMemeType(){return memeType;}
    public int getMemeTab(){return memeTab;}
    public String getMemeMimeType()
    {
        switch (classfyByName(memeName)){
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

    public static int classfyByName(String memeName){
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

    public static int classifyByType(Intent incomingIntent){
        String receivedType = incomingIntent.getType();
        if(receivedType.startsWith("text")){
            return YOUTUBE;
        }
        if(receivedType.startsWith("image")){
            return IMAGE;
        }
        if(receivedType.startsWith("video")){
            return VIDEO;
        }

        return TEMP;
    }

    public static int classifyByTab(String memeName){
        switch (classfyByName(memeName)){
            case IMAGE: case GIF: return IMAGE_TAB;
            case VIDEO:return VIDEO_TAB;
            default:return TEMP;
        }
    }
}
