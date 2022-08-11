package com.log28.memesstore;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;

import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileHelper {

    Context context;
    Uri persistentUri;
    static String appFolder;
    String packageName;
    List<String> folders;
    String filename;
    DocumentFile root;

    public FileHelper(Context context, Uri persistentUri) {
        this.context = context;
        this.persistentUri = persistentUri;
        List<String> uriSegments = persistentUri.getPathSegments();
        if (uriSegments.get(1).endsWith(":"))
            appFolder = uriSegments.get(1).replace(":", "");
        else
            appFolder = uriSegments.get(1).split(":")[1];
        packageName = context.getPackageName();
    }

    public String getAppFolder() {
        return appFolder + "/";
    }

    public OutputStream createFile(String path) {
        if (isExist(path)) return null;
        OutputStream outputStream = null;
        try {
            DocumentFile file;
            if (!isExist(path)) {
                detectFolders(path);
                if (folders != null)
                    for (String folder : folders) {
                        DocumentFile dir, check = root.findFile(folder);
                        if (check == null)
                            dir = root.createDirectory(folder);
                        else
                            dir = check;
                        root = dir;
                    }
                file = root.createFile(FileClassifier.getMimeType(filename), filename);
            } else
                file = DocumentFile.fromFile(new File(getAbsolutePath(path)));
            Uri fileUri = file.getUri();
            ContentResolver contentResolver = context.getContentResolver();
            outputStream = contentResolver.openOutputStream(fileUri);

        } catch (Exception e) {
            e.printStackTrace();

        }
        return outputStream;
    }

    /* public void openFileForWriteng(Uri){

     }*/
    public OutputStream createCacheFile(String path) {

        OutputStream outputStream = null;
        try {
            DocumentFile cache = DocumentFile.fromFile(context.getCacheDir());
            path = path.substring(path.lastIndexOf("/") + 1);
            DocumentFile file = cache.createFile("Application/x-sqlite3", path);
            Uri fileUri = file.getUri();
            ContentResolver contentResolver = context.getContentResolver();
            outputStream = contentResolver.openOutputStream(fileUri);

        } catch (Exception e) {
            e.printStackTrace();

        }
        return outputStream;
    }

    public boolean writeToFile(InputStream inputStream, OutputStream outputStream) {
        try {
            int n;
            byte[] buffer = new byte[1024 * 4];
            while (-1 != (n = inputStream.read(buffer)))
                outputStream.write(buffer, 0, n);
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public InputStream readFromFile(String path) {

        try {
            return context.getContentResolver().openInputStream(getUriFromFile(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Uri getUriFromFile(String path) {

        try {
            if (isExist(path)) {
                //DocumentFile.fromFile(new File(getAbsolutePath(path))).getUri();
                return FileProvider.getUriForFile(context, context.getPackageName(), new File(getAbsolutePath(path)));
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }

    public Uri getSingleUri(String path) {
        String uripath = DocumentFile.fromTreeUri(context, persistentUri).getUri().toString();
        if (!path.startsWith("/"))
            path = "/" + path;
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        uripath = uripath + path.replace("/", "%2F").replace("@", "%40");
        return Uri.parse(uripath);
    }

    public boolean deleteFile(String path) {
        try {
            if (isExist(path))
                DocumentFile.fromSingleUri(context, getSingleUri(path)).delete();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean isExist(String path) {
        try {
            return DocumentFile.fromFile(new File(getAbsolutePath(path))).exists();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public static boolean isEmpty(String path) {
        try {
            if (DocumentFile.fromFile(new File(getAbsolutePath(path))).length() > 0)
                return false;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
        return true;
    }

    public static String getPathFromUri(Uri uri) {
        String path;
        List<String> uriSegments = uri.getPathSegments();
        path = uriSegments.get(1).split(":")[1];
        return path;
    }

    public static String getAbsolutePath(String path) {
        return Environment.getExternalStorageDirectory() + "/" + appFolder + "/" + path;
    }

    public String zipPack(Uri zipName, List<String> paths) {
        String zipPath = zipName.getPath();
        try {
            //OutputStream outputStream = createFile(zipName);
            ContentResolver contentResolver = context.getContentResolver();
            OutputStream outputStream = contentResolver.openOutputStream(zipName);

            ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
            for (String path : paths) {
                if (FileClassifier.classfyByName(path) != FileClassifier.HTTPS) {
                    FileInputStream fileInputStream;
                    if (FileClassifier.classfyByName(path) != FileClassifier.DB)
                        fileInputStream = (FileInputStream) readFromFile(path);//new FileInputStream(path);
                    else
                        fileInputStream = new FileInputStream(path);
                  /*  Pattern p = Pattern.compile(appFolder);
                    Matcher m = p.matcher(path);
                    if (m.find()) {
                        path = path.substring(m.end());
                    }*/

                    Pattern p = Pattern.compile("/data/data/" + packageName + "/databases/");
                    Matcher m = p.matcher(path);
                    if (m.find()) {
                        path = path.substring(m.end());
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
            }
            zipOutputStream.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return zipPath;
    }

    public List<String> unzipPack(InputStream zipinputStream) {
        List<String> paths = new ArrayList<>();
        try {
            ZipInputStream zipStream = new ZipInputStream(zipinputStream);
            ZipEntry zEntry = null;

            while ((zEntry = zipStream.getNextEntry()) != null) {
                FileOutputStream fout = null;
                String name = zEntry.getName();

                if (FileClassifier.classfyByName(name) == FileClassifier.DB) {
                    name = context.getCacheDir() + "/" + name;
                    paths.add(name);
                    fout = (FileOutputStream) createCacheFile(name);
                } else {
                    if (!name.contains("/"))
                        name = FileClassifier.getRelativePath(name);
                    fout = (FileOutputStream) createFile(name);
                }
                if (isExist(name) && !isEmpty(name))
                    continue;
                BufferedOutputStream bufout = new BufferedOutputStream(fout);
                byte[] buffer = new byte[1024];
                int read = 0;
                while ((read = zipStream.read(buffer)) != -1) {
                    if (bufout == null)
                        read = 0;
                    bufout.write(buffer, 0, read);
                }

                zipStream.closeEntry();
                bufout.close();
                fout.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return paths;
    }

    private void detectFolders(String path) {
        if (path.contains("/")) {
            ArrayList<String> folders = new ArrayList<>(Arrays.asList(path.split("/")));
            int length = folders.size();
            this.filename = folders.get(length - 1);
            this.folders = folders.subList(0, length - 1);
        } else
            this.filename = path;
        this.root = DocumentFile.fromTreeUri(context, persistentUri);
    }


}
