package com.log28.memesstore;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class MemeObject implements Parcelable{

    MemesListAdapter memesListAdapter;
    Context context;
    private String memeRelativePath;
    private  String memeMimeType;
    private String memeTag;
    private Bitmap memeBitmap;
    private int memeTab;
    private Uri memeUri;
public static String memeObjectParcelTag="MemeObject";



    MemeObject(MemesListAdapter memesListAdapter,String relativeFilepath) {

        init(memesListAdapter.context,relativeFilepath,"");
    }
    MemeObject(Context context,String relativeFilepath) {
        init(context,relativeFilepath,"");
    }
    MemeObject(Context context,String relativeFilepath,String tag) {
        init(context,relativeFilepath,tag);
    }
    MemeObject(MemesListAdapter memesListAdapter,String relativeFilepath,String tag){
this.memesListAdapter=memesListAdapter;
        init(memesListAdapter.context,relativeFilepath,tag);
    }


    protected MemeObject(Parcel in) {
        memeRelativePath = in.readString();
        memeMimeType = in.readString();
        memeTag = in.readString();
        if(FileClassifier.classfyByName(memeRelativePath)!=FileClassifier.HTTPS)
        memeUri=Uri.parse(in.readString());
    }

    public static final Creator<MemeObject> CREATOR = new Creator<MemeObject>() {
        @Override
        public MemeObject createFromParcel(Parcel in) {
            return new MemeObject(in);
        }

        @Override
        public MemeObject[] newArray(int size) {
            return new MemeObject[size];
        }
    };

    void init(Context context, String relativeFilepath, String tag){
        this.memeRelativePath =relativeFilepath;
        this.memeTag=tag;
        this.context=context;
        memeTab=FileClassifier.classifyByTab(memeRelativePath );
        memeMimeType=FileClassifier.getMimeType(memeRelativePath );
        memeUri=MemeFileHelper.createFileHelper(context,MainActivity.uriFolder).getUriFromFile(memeRelativePath);
        memeBitmap= BitmapFactory.decodeResource(context.getResources(), R.raw.logo);
        if(memesListAdapter!=null){
            BitmapLoader bitmapLoader=new BitmapLoader();
            bitmapLoader.execute(memeRelativePath);
        }

    }
    public String getMemeRelativePath(){
        return this.memeRelativePath;
    }
    public String getTag(){return memeTag;}
    public int getMemeTab(){return memeTab;}
    public String getMemeMimeType(){return memeMimeType;}
    public Uri getMemeUri(){return memeUri;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(memeRelativePath);
        dest.writeString(memeMimeType);
        dest.writeString(memeTag);
        if(memeUri!=null)
        dest.writeString(memeUri.toString());
    }


    class BitmapLoader extends AsyncTask<String,Void, Bitmap> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //memeBitmap= BitmapFactory.decodeResource(context.getResources(), R.raw.logo);
        }

        @RequiresApi(api = Build.VERSION_CODES.R)
        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap local=null;
            try {
                switch (FileClassifier.classfyByName(strings[0])) {
                    case FileClassifier.IMAGE:
                    case FileClassifier.GIF:
                        local= MemeFileHelper.createFileHelper(context, MainActivity.uriFolder).getPreview(strings[0]);
                        break;
                    case FileClassifier.VIDEO:

                        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                        mediaMetadataRetriever.setDataSource(context, memeUri);
                        local = mediaMetadataRetriever.getFrameAtTime(1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                        mediaMetadataRetriever.release();
                        break;
                    case FileClassifier.HTTPS:

                        local= MemeFileHelper.createFileHelper(context, MainActivity.uriFolder).getPreview(strings[0]);
                        break;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return local;
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




}
