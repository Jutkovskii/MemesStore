package com.log28.memesstore;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileHelper {
    Context context;
    Uri persistentUri;
    String appFolder;
    String packageName;
    ArrayList<String> folders;
    String filename;
    DocumentFile root;

    FileHelper(Context context, Uri persistentUri){
        this.context=context;
        this.persistentUri=persistentUri;
        List<String> uriSegments=persistentUri.getPathSegments();
appFolder=uriSegments.get(1).split(":")[1];
packageName=context.getPackageName();
    }

    public OutputStream createFile(String path){
        OutputStream outputStream=null;
        try{
            detectFolders(path);
            for(String folder:folders){
                DocumentFile dir, check=root.findFile(folder);
                if(check==null)
                dir = root.createDirectory(folder);
                else
                dir=check;
                root=dir;
            }
            DocumentFile file = root.createFile(FileClassifier.getMimeType(filename),filename);
            Uri fileUri = file.getUri();
            ContentResolver contentResolver = context.getContentResolver();
            outputStream = contentResolver.openOutputStream(fileUri);

        }
        catch (Exception e){
            e.printStackTrace();

        }
        return outputStream;
    }

    public boolean writeToFile(InputStream inputStream, OutputStream outputStream) {
        try{
            int n;
            byte[] buffer = new byte[1024 * 4];
            while (-1 != (n = inputStream.read(buffer)))
                outputStream.write(buffer, 0, n);
            inputStream.close();
            outputStream.close();
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean deleteFile(String path){
        try{
            detectFolders(path);
            for(String folder:folders){
                DocumentFile dir, check=root.findFile(folder);
                if(check==null)
                    return false;
                else
                    dir=check;
                root=dir;
            }
            DocumentFile file  = root.findFile(filename);
            if(file!=null)
                file.delete();

        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public String zipPack(String zipName,List<String> paths) {
        String zipPath=zipName;
        try {
            OutputStream outputStream = createFile(zipName);
            ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
            for(String path:paths){
                FileInputStream fileInputStream = new FileInputStream(path);

                Pattern p = Pattern.compile(appFolder);
                Matcher m = p.matcher(path);
                if (m.find()) {
                    path=path.substring(m.end()+1);
                }

                p = Pattern.compile("/data/data/"+packageName+"/databases/");
                m = p.matcher(path);
                if (m.find()) {
                    path=path.substring(m.end()+1);
                }

                ZipEntry entry1 = new ZipEntry(path);
                zipOutputStream.putNextEntry(entry1);
                // считываем содержимое файла в массив byte
                byte[] buffer = new byte[fileInputStream.available()];
                fileInputStream.read(buffer);
                // добавляем содержимое к архиву
                zipOutputStream.write(buffer);

                zipOutputStream.flush();
                // закрываем текущую запись для новой записи
                zipOutputStream.closeEntry();
            }
            zipOutputStream.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return zipPath;
    }

    public List<String> unzipPack(InputStream zipinputStream ) {
        List<String> paths=new ArrayList<>();
        try{
            ZipInputStream zipStream = new ZipInputStream(zipinputStream);
            ZipEntry zEntry = null;

            while ((zEntry = zipStream.getNextEntry()) != null) {

                    String name = zEntry.getName();
                    paths.add(name);
                    FileOutputStream fout = (FileOutputStream) createFile(name);

                    BufferedOutputStream bufout = new BufferedOutputStream(fout);
                    byte[] buffer = new byte[1024];
                    int read = 0;
                    while ((read = zipStream.read(buffer)) != -1) {
                        bufout.write(buffer, 0, read);
                    }

                    zipStream.closeEntry();
                    bufout.close();
                    fout.close();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return paths;
    }

    private void detectFolders(String path){
        ArrayList<String> folders= (ArrayList<String>) Arrays.asList(path.split("/"));
        int length=folders.size();
        this.filename=folders.get(length-1);
        this.folders=(ArrayList)folders.subList(0,length-1);
        this.root = DocumentFile.fromTreeUri(context, persistentUri);
    }


    }
