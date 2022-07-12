package com.log28.memesstore;

public class FileClassifier {


    static String imageformats[]= new String[]{".jpg",".jpeg",".png", ".bmp", ".webp", ".tiff"};
    static String gifformats[]= new String[]{".gif"};
    static String videoformats[]= new String[]{".mp4"};
    static String archformats[]= new String[]{".zip"};
    static String dbformats[]= new String[]{"db"};

    //категории файлов
    public static final String IMAGE = "image/*";
    public static final String VIDEO = "video/*";
    public static final String HTTPS = "text/*";
    public static final String GIF = "image/gif";
    public static final String ARCH = "application/zip";
    public static final String DEFAULT = "*/*";


    public static String getMimeType(String memeName){
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

        if(!memeName.contains("."))
            return HTTPS;
        return DEFAULT;
    }


}
