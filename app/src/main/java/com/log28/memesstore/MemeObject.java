package com.log28.memesstore;

import android.graphics.Bitmap;

import java.io.InputStream;
import java.util.List;

public class MemeObject {
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

    MemeObject(String name) {
        this.memeName=name;
    }
    MemeObject(String name,String tag){
        this.memeName=name;
        this.memeTag=tag;
    }
    MemeObject(MemeGroup group){
        this.memeName=group.name;
        this.memeTag=group.tag;
    }

    public String getPath(){
return "";
    }

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
