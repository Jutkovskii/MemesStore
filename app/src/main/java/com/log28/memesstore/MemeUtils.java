package com.log28.memesstore;

import android.net.Uri;
import android.view.Menu;


public class MemeUtils {
    public static Uri uriFolder;
    public static String defaultSign = "Отправлено через МЕМОХРАНИЛИЩЕ: https://t.me/log28dev";
    public static boolean isDefaultSingEnabled=true;
    //коды идентификации входящих Intent'ов
    public static final int REQUEST_DB = 53;//импорт БД
    public static final int REQUEST_EXDB = 54;//экспорт БД
    public static final int REQUEST_GALLERY = 84;//добавление из галереи
    public static final int REQUEST_PERSISTENT = 145;//добавление из галереи
    //параметр для извлечения данных из интента
    public static String FILENAME_EXTRA = "memeFilename";
    //код удаления
    public static int DELETED_MEME_CODE = 55;
    //параметр для извлечения тегов
    public static String FILETAG_EXTRA = "memeFiletag";
    //код запроса данный активности
    public static int REQUEST_CODE = 104;

    public static int CHANGE_CODE = 38;
    //флаг режима мультивыбора для удалени
    public static boolean deletingMode = false;
    public static Menu mainMenu;

}
