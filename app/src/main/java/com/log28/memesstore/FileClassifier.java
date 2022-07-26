package com.log28.memesstore;

import android.content.Intent;

public class FileClassifier {


    static String imageformats[]= new String[]{".jpg",".jpeg",".png", ".bmp", ".webp", ".tiff"};
    static String gifformats[]= new String[]{".gif"};
    static String videoformats[]= new String[]{".mp4", ".webm"};
    static String archformats[]= new String[]{".zip"};
    static String dbformats[]= new String[]{"db"};

    //MIME-типы файлов
    public static final String MIME_IMAGE = "image/";
    public static final String MIME_VIDEO = "video/";
    public static final String MIME_HTTPS = "text/*";
    public static final String MIME_GIF = "image/gif";
    public static final String MIME_ARCH = "application/zip";
    public static final String MIME_DEFAULT = "*/*";

    //категории файлов
    public static final int IMAGE = 0;
    public static final int VIDEO = 1;
    public static final int HTTPS = 2;
    public static final int GIF = 3;
    public static final int ARCH = 4;
    public static final int DB = 5;
    public static final int TEMP = -1;

    private static String previewsFolder = "Previews/";
    private static String imagesFolder =  "Images/";//папка с изображениями
    private static String videosFolder = "Videos/";//папка с видео
    private static String gifsFolder = "Gifs/";//папка с Gif
    private static String thumbnailsFolder= ".thumbnails/";//папка с превьюшками
    private static String tempFolder ="/";//корневая папка хранилища
    //категории вкладок
    public static final int IMAGE_TAB = 0;
    public static final int VIDEO_TAB = 1;
    //подкатегории мемов
    public static final int YOUTUBE=20;
    public static final int VK=21;
    public static final int DISCORD=22;

    public static String getMimeType(String memeName){
        memeName=memeName.toLowerCase();
        for(String name: imageformats){
            if(memeName.contains(name))
                return MIME_IMAGE+getFormat(memeName);
        }
        for(String name: gifformats){
            if(memeName.contains(name))
                return MIME_GIF;
        }
        for(String name: videoformats){
            if(memeName.contains(name))
                return MIME_VIDEO+getFormat(memeName);

        }

        for(String name: archformats){
            if(memeName.contains(name))
                return MIME_ARCH;
        }

        if(!memeName.contains("."))
            return MIME_HTTPS;
        return MIME_DEFAULT;
    }
    public static String getRelativePath(String memeName){
        switch (classfyByName(memeName)){
            case IMAGE: return imagesFolder+memeName;
            case VIDEO: return videosFolder+memeName;
            case GIF: return gifsFolder+memeName;
            case HTTPS: return previewsFolder+memeName+".jpg";
            default:return tempFolder+memeName;
        }
    }

    public static String getMemeMimeType(int tab)
    {
        if(tab==IMAGE_TAB) return "image/*";
        if(tab==VIDEO_TAB) return "video/*";
        return "*/*";
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
            return classifyByUrl(incomingIntent.getClipData().getItemAt(0).getText().toString());
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
            case VIDEO:case HTTPS:return VIDEO_TAB;
            default:return TEMP;
        }
    }

    public static String getFormat(String memeName){
        return memeName.substring(memeName.lastIndexOf(".")+1);
    }

    public static int classifyByUrl(String memeURL){
        if(memeURL.contains("youtube")||memeURL.contains("youtu.be")||!memeURL.contains("."))
            return YOUTUBE;
        if(memeURL.contains("vk.com"))
            return VK;
        if(memeURL.contains("discord"))
            return DISCORD;
        return TEMP;
    }

}
