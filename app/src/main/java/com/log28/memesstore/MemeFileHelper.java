package com.log28.memesstore;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.InputStream;

public class MemeFileHelper extends FileHelper {
    public MemeFileHelper(Context context, Uri persistentUri) {
        super(context, persistentUri);
    }

    //private static FileHelper fileHelper;

    /*private static MemeFileHelper memeFileHelper;
    private MemeFileHelper(Context context, Uri persistentUri){
        super(context,persistentUri);
        //new FileHelper(context,persistentUri);
    }
    public static MemeFileHelper createFileHelper()
    {
        return memeFileHelper;
    }*/
    public static MemeFileHelper createFileHelper(Context context, Uri persistentUri){
        /*if(memeFileHelper==null)
        {
            memeFileHelper = new MemeFileHelper(context,persistentUri);
        }*/
        return new MemeFileHelper(context,persistentUri);
    }


    @RequiresApi(api = Build.VERSION_CODES.R)
    public Bitmap getPreview(String filePath){
        Bitmap preview=null;
       try{
           InputStream inputStream=context.getContentResolver().openInputStream(readFromFile(filePath));
           preview = BitmapFactory.decodeStream(inputStream,new Rect(),this.getOptions(filePath));
       }
       catch (Exception e){
           e.printStackTrace();
       }
       return preview;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public BitmapFactory.Options getOptions(String filename) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        if (MemeObject.classfyByName(filename) != MemeObject.VIDEO) {
            BitmapFactory.decodeFile(filename, options);
            int koef = (int) ((float) (options.outWidth) / (float) (context.getDisplay().getWidth()) * 2);
            if (koef % 2 != 0) koef++;
            options.inSampleSize = koef;
        }
        options.inJustDecodeBounds = false;

        return options;

    }
}
