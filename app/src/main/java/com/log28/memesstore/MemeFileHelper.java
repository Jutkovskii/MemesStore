package com.log28.memesstore;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.RequiresApi;

import org.jsoup.Jsoup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MemeFileHelper extends FileHelper {

    static MemeFileHelper memeFileHelper;
    public MemeFileHelper(Context context, Uri persistentUri) {
        super(context, persistentUri);
    }

    public static MemeFileHelper createFileHelper(Context context, Uri persistentUri){

return new MemeFileHelper(context,persistentUri);
    }


    @RequiresApi(api = Build.VERSION_CODES.R)
    public Bitmap getPreview(String filePath){
        Bitmap preview=null;

       try{
           InputStream inputStream=context.getContentResolver().openInputStream(getUriFromFile(filePath));
           preview = BitmapFactory.decodeStream(inputStream,new Rect(),this.getOptions(filePath));


       }
       catch (Exception e){
           e.printStackTrace();
       }
       return preview;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public BitmapFactory.Options getOptions(String filename) throws FileNotFoundException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        if (FileClassifier.classfyByName(filename) != FileClassifier.VIDEO) {
            InputStream inputStream=context.getContentResolver().openInputStream(getUriFromFile(filename));
            Bitmap tmp=BitmapFactory.decodeStream(inputStream, new Rect(), options);
           /* int koef = (int) ((float) (options.outWidth) / (float) (context.getDisplay().getWidth()) * 2);*/
            final float WIDTH = 1200;
            final float HEIGHT = 1200;
            int koef = (int)  Math.max(options.outWidth/WIDTH, options.outHeight/HEIGHT);
            if (koef % 2 != 0) koef++;
            options.inSampleSize = koef;
        }
        options.inJustDecodeBounds = false;

        return options;

    }


    public String createFileFromURL(String url){
        URLSaver urlSaver=new URLSaver(url);
        String filename=urlSaver.getTargetFilename();
        urlSaver.execute();
        return filename;
    }

    public class URLSaver extends AsyncTask<Void,Void,Void>{
        String targetFilename;
        String url;
        OutputStream outputStream;
        public URLSaver(String url){
        this.url =url;
        String[] parts=url.split("/");
        this.targetFilename=parts[parts.length-1];
        this.targetFilename=FileClassifier.getRelativePath(getTargetFilename());
        outputStream=createFile(this.targetFilename);
        }
        public String getTargetFilename(){
            if(!targetFilename.contains(".")) targetFilename=targetFilename+".jpg";
            return targetFilename;
        }
        public String parseURL(String url){
            String imageUrl="";
            try {

                String webPage=Jsoup.connect(url).get().text("https").toString();
                Pattern pattern = Pattern.compile("<meta name=\"og:image\" value=\".+?>");
                Matcher matcher = pattern.matcher(webPage);
                if (matcher.find()) {
                    //фиксируем полученное имя
                    imageUrl = webPage.substring(matcher.start())
                            .replace("<meta name=\"og:image\" value=\"", "")
                            .replace("amp;", "")
                            .replace("http", "https")
                            .split("\">")[0];

                }
                else {
                    pattern = Pattern.compile("<meta property=\"og:image\" content=\".+?>");
                    matcher = pattern.matcher(webPage);
                    if (matcher.find()) {
                        //фиксируем полученное имя
                        imageUrl = webPage.substring(matcher.start())
                                .replace("<meta property=\"og:image\" content=\"", "")
                                .replace("amp;", "")
                                .replace("http", "https")
                                .split("\">")[0];

                    }
                }

            }
            catch (Exception e){
                e.printStackTrace();

            }
            return imageUrl;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                if(FileClassifier.classifyByUrl(url)==FileClassifier.VK)
                url =parseURL(url);
                InputStream inputStream = (InputStream) new URL(url).getContent();
                MemeFileHelper.createFileHelper(context, MemeUtils.uriFolder).writeToFile(inputStream,outputStream);
        }
            catch (Exception e){
            e.printStackTrace();

        }
            return null;
        }

    }
}
