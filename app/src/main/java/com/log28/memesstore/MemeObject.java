package com.log28.memesstore;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

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

    //папки для хранения мемов
    private static String previewsFolder = "Previews/";
    private static String imagesFolder =  "Images/";//папка с изображениями
    private static String videosFolder = "Videos/";//папка с видео
    private static String gifsFolder = "Gifs/";//папка с Gif
    private static String thumbnailsFolder= ".thumbnails/";//папка с превьюшками
    private static String tempFolder ="/";//корневая папка хранилища
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
       /* if(memesListAdapter!=null){
            Log.d("OLOLOG","name "+name);
            BitmapLoader bitmapLoader=new BitmapLoader();
            bitmapLoader.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,memeName);
        }*/
        memeType=classfyByName(memeName);
        memeTab=classifyByTab(memeName);
        memeMimeType=FileClassifier.getMimeType(memeName);
        memeFolder=FileClassifier.getMimeFolder(memeName);
       // getMemeMimeType();
       // setFolder();

    }
    public String getFolder(){
        return "";
    }
    public String getName(){return memeName;}
    public String getTag(){return memeTag;}
    public int getMemeType(){return memeType;}
    public int getMemeTab(){return memeTab;}
    private void getMemeMimeType()
    {
        switch (classfyByName(memeName)){
            case IMAGE: memeMimeType = "image/*";break;
            case VIDEO:memeMimeType =  "video/*";break;
            case GIF: memeMimeType = "image/gif";break;
            case HTTPS: memeMimeType = "text/*";break;
            default:  memeMimeType =  "*/*";break;
        }

    }

    public static String getMemeMimeType(int tab)
    {
        if(tab==IMAGE_TAB) return "image/*";
        if(tab==VIDEO_TAB) return "video/*";
        return "*/*";
    }

    private void setFolder(){
        switch (classfyByName(memeName)){
            case IMAGE: memeFolder=imagesFolder; break;
            case VIDEO: memeFolder=videosFolder; break;
            case GIF: memeFolder=gifsFolder; break;
            case HTTPS: memeFolder=previewsFolder; break;
            default:memeFolder=tempFolder; break;
        }
    }

    class BitmapLoader extends AsyncTask<String,Void, Bitmap> {

        MemesListAdapter.ViewHolder holder;
        BitmapLoader(MemesListAdapter.ViewHolder holder)
        {
            this.holder=holder;
            this.holder.memeImageView.setImageBitmap( memeBitmap);
        }
        @RequiresApi(api = Build.VERSION_CODES.R)
        @Override
        protected Bitmap doInBackground(String... strings) {
            return MemeFileHelper.createFileHelper().getPreview(strings[0]);

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            memeBitmap=bitmap;
            holder.memeImageView.setImageBitmap(bitmap);
            //memesListAdapter.notifyDataSetChanged();
        }
    }

    public Bitmap getBitmap(MemesListAdapter.ViewHolder holder){
        BitmapLoader bitmapLoader=new BitmapLoader(holder);
        bitmapLoader.execute(memeName);
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
