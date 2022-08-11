package com.log28.memesstore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    public static Uri uriFolder;
    public static String defaultSign = "Отправлено через МЕМОХРАНИЛИЩЕ: https://t.me/log28dev";
    String searchMemeTag = "";
    //Список с базами данных с мемами
    ArrayList<MemeDatabaseHelper> databases;
    SharedPreferences userData;
    final String dbKey = "dbKey";
    final String SWITCH = "SWITCH";
    public static boolean isDefaultSingEnabled=true;
    ArrayList<String> dbNames = new ArrayList<>();
    final String imagedb = "imagedb";
    final String videodb = "videodb";
    //номера вкладок
    private final int IMAGE_POS = 0;
    private final int VIDEO_POS = 1;
    //номер текущей вкладки
    int tabNum = IMAGE_POS;
    //слой для вкладок
    TabLayout memesCategories;
    //объект для работы с памятью
    //FileHelper2 fileHelper2;
    FileHelper memeFileHelper;
    //фрагменты с отображением списков мемов
    ArrayList<MemeListFragment> memeListFragments;
    //коды идентификации входящих Intent'ов
    public static final int REQUEST_DB = 53;//импорт БД
    public static final int REQUEST_EXDB = 54;//экспорт БД
    public static final int REQUEST_GALLERY = 84;//добавление из галереи
    public static final int REQUEST_PERSISTENT = 145;//добавление из галереи
    //флаг режима мультивыбора для удалени
    public static boolean deletingMode = false;
    //меню в ActionBar
    public static Menu mainMenu;
    public static ProgressBar loadingProgress;
    ViewPager2 pagerSlider;
    private FragmentStateAdapter pagerAdapter;
    //создание тулбара для главного окна
    Toolbar toolbar;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler(new LocalExceptionHandler(MainActivity.this));
        savedInstanceState = null;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //проверка доступа к памяти
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

        }
        //объект с вкладками
        memesCategories = findViewById(R.id.categoriesLayout);
        toolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);
        loadingProgress = findViewById(R.id.loadingProcess);
        loadingProgress.setVisibility(ProgressBar.INVISIBLE);
        //проверка наличия папки для хранения мемов
        if (checkPersistentUri())
            init();
    }

    public void init() {
        //создание и заполнение списка БД
        databases = new ArrayList<>();
        getPreferences();
        for (String s : dbNames)
            databases.add(new MemeDatabaseHelper(this, s, 1));
        //создание фрагментов с мемами
        memeListFragments = new ArrayList<>();
        for (int i = 0; i < databases.size(); i++)
            memeListFragments.add(new MemeListFragment(databases.get(i)));
        //обработчик выбора вкладок
        memesCategories.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //перелистывание при выборе вкладки
                tabNum = tab.getPosition();
                pagerSlider.setCurrentItem(tabNum);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        //создание слайдера
        pagerSlider = findViewById(R.id.pagerSlider);
        pagerAdapter = new ScreenSlidePagerAdapter(this, memeListFragments);
        pagerSlider.setAdapter(pagerAdapter);
        pagerSlider.setSaveEnabled(false);
        //уменьшение чувствительности слайдера
        try {
            Field ff = ViewPager2.class.getDeclaredField("mRecyclerView");
            ff.setAccessible(true);
            RecyclerView recyclerView = (RecyclerView) ff.get(pagerSlider);
            Field touchSlopField = RecyclerView.class.getDeclaredField("mTouchSlop");
            touchSlopField.setAccessible(true);
            int touchSlop = (int) touchSlopField.get(recyclerView);
            touchSlopField.set(recyclerView, touchSlop * 4);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //получение интента
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
  new BackgroundLoader(this).execute(intent);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setPreferences();
    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {

        //список мемов
        List<MemeListFragment> memeLists;

        public ScreenSlidePagerAdapter(FragmentActivity fa, List<MemeListFragment> memeLists) {
            super(fa);
            this.memeLists = memeLists;
        }

        @Override
        public long getItemId(int position) {
            //обновление при смене вкладки
            memesCategories.selectTab(memesCategories.getTabAt(position));
            memeListFragments.get(position).setFilter(searchMemeTag);
            return super.getItemId(position);
        }

        @Override
        public Fragment createFragment(int position) {
            //обновление при создании
            memeListFragments.get(position).setFilter(searchMemeTag);
            return memeListFragments.get(position);
        }

        @Override
        public int getItemCount() {
            return memeLists.size();
        }


    }

    //создание меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        //создание поисковика
        final SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryText) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //сохранение поисковой фразы
                searchMemeTag = newText;
                //определение вкладки для поиска
                memeListFragments.get(tabNum).memesListAdapter.getFilter().filter(newText);
                return true;
            }
        });

        mainMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    //опции выбора меню
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        try {

            Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();

            if (item.getTitle().toString().matches("Удалить")) {
                new BackgroundDeleter(this, tabNum).execute();
            }

            if (item.getTitle().toString().matches("Настройки")) {
                startActivityForResult(new Intent(this, SettingsActivity.class), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onOptionsItemSelected(item);
    }
    //получение/сохранение настроек
    void getPreferences() {
        dbNames.add(imagedb);
        dbNames.add(videodb);
        userData = getPreferences(MODE_PRIVATE);
        Set<String> usersbd = userData.getStringSet(dbKey, null);
        if (usersbd != null) {
            for (String userbd : usersbd)
                if (!dbNames.contains(userbd))
                    dbNames.add(userbd);
            isDefaultSingEnabled = userData.getBoolean(SWITCH, true);
        }
        userData.edit().clear().commit();


    }

    void setPreferences() {
        SharedPreferences.Editor editor = userData.edit();
        editor.putStringSet(dbKey, new HashSet<String>(dbNames));
        editor.putBoolean(SWITCH, isDefaultSingEnabled);
        editor.apply();
    }

    //вызов галерени для добавления мема
    public void callGallery(View v) {

        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        photoPickerIntent.addCategory(Intent.CATEGORY_OPENABLE);
        photoPickerIntent.setType(FileClassifier.getMemeMimeType(tabNum));
        //Запускаем переход с ожиданием обратного результата в виде информации об изображении:
        startActivityForResult(photoPickerIntent, REQUEST_GALLERY);//посмотреть, как нынче надо
    }

    //получение результата от дочерней активности
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //проверка кода запроса и кода результата
        //результат: файл нужно удалить
        if (requestCode == MemeViewerActivity.REQUEST_CODE)
            if (resultCode == MemeViewerActivity.DELETED_MEME_CODE) {
                Log.d("OLOLOG", "Активность удалить мем ");
                //удаление файла
                MemeFileHelper.createFileHelper(this, MainActivity.uriFolder).deleteFile(data.getDataString());
                tabNum = FileClassifier.classifyByTab(data.getDataString());
                databases.get(tabNum).delete(data.getDataString());
                memesCategories.selectTab(memesCategories.getTabAt(tabNum));
                memeListFragments.get(tabNum).changeFragment();
            }
        //результат: подпись нужно изменить
        if (resultCode == MemeViewerActivity.CHANGE_CODE) {

            String filetag = data.getStringExtra(MemeViewerActivity.FILETAG_EXTRA);
            String filename = data.getStringExtra(MemeViewerActivity.FILENAME_EXTRA);
            tabNum = FileClassifier.classifyByTab(filename);
            databases.get(tabNum).update(filename, filetag);
            memesCategories.selectTab(memesCategories.getTabAt(tabNum));
            memeListFragments.get(tabNum).changeFragment();
        }

        //результат: файл нужно добавить
        if (requestCode == REQUEST_GALLERY)
            if (resultCode == RESULT_OK) {
                //получаем и сохраняем мем из uri
                //выбор вкладки согласно типу файла
                //tabNum =getMemeFromIntent(data) ;
                new BackgroundLoader(this).execute(data);
                memesCategories.selectTab(memesCategories.getTabAt(tabNum));
                memeListFragments.get(tabNum).memesListAdapter.notifyDataSetChanged();
            }
        //  if (requestCode == REQUEST_DB)
        if (resultCode == REQUEST_DB) {
            new BackgroundLoader(this).execute(data);
        }
        // if (requestCode == REQUEST_EXDB)
        if (resultCode == REQUEST_EXDB) {
            try {
                //Uri файла для экспорта
                Uri uri = data.getData();
                //получение списка путей к мемам для экпорта
                ArrayList<String> memepaths = new ArrayList<>();
                for (MemeDatabaseHelper thisdb : databases) {
                    memepaths.add(thisdb.getDbPath());
                    Cursor localcursor = thisdb.getCursor();
                    localcursor.moveToFirst();
                    for (int i = 0; i < localcursor.getCount(); i++) {
                        memepaths.add(localcursor.getString(1));
                        localcursor.moveToNext();
                    }
                }
                new MemeFileHelper(this, uriFolder).zipPack(uri, memepaths);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        if (requestCode == REQUEST_PERSISTENT
                && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                uriFolder = data.getData();
                final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(uriFolder, takeFlags);
                grantUriPermission(this.getPackageName(), uriFolder, takeFlags);
                memeFileHelper = MemeFileHelper.createFileHelper(this, uriFolder);
                init();

            }
        }

    }


    public class BackgroundLoader extends AsyncTask<Intent, Integer, ArrayList<String>> {
        Context context;

        BackgroundLoader(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            loadingProgress.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected ArrayList<String> doInBackground(Intent... inents) {
            try {
                getMemeFromIntent(inents[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        String saveFromUri(Uri uri) {

            String filename = "";
            InputStream inputStream;
            try {
                Cursor returnCursor = getContentResolver().query(uri, null, null, null, null);
                if (returnCursor != null) {
                    //Определение столбца, содержащего имя файла
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    returnCursor.moveToFirst();
                    //определение имени файла
                    filename = returnCursor.getString(nameIndex);
                } else
                    filename = uri.getPath().substring(uri.getPath().lastIndexOf("/") + 1);

                //получение потока входных данных
                inputStream = getContentResolver().openInputStream(uri);

                //fileHelper2.createnew(inputStream,filename);
                //создание локального файла
                if (FileClassifier.classfyByName(filename) != FileClassifier.ARCH) {
                    String relativeFilepath = FileClassifier.getRelativePath(filename);
                    memeFileHelper.writeToFile(inputStream, memeFileHelper.createFile(relativeFilepath));
                    databases.get(FileClassifier.classifyByTab(relativeFilepath)).insert(relativeFilepath);
                } else {

                    ArrayList<String> imported = (ArrayList<String>) new MemeFileHelper(context, uriFolder).unzipPack(inputStream);
                    for (String db : imported) {
                        String dbName = db;

                        SQLiteDatabase importedDB = SQLiteDatabase.openOrCreateDatabase(db, null);
                        Cursor cursor = importedDB.rawQuery("SELECT * FROM memesTable", null);

                        if (cursor.moveToFirst()) {
                            do {
                                String filepath = cursor.getString(1);
                                if (filepath.contains(".") && !filepath.contains("/"))
                                    filepath = FileClassifier.getRelativePath(filepath);
                                if (dbName.contains(imagedb)) {
                                    databases.get(0).insert(filepath, cursor.getString(2));
                                }
                                if (dbName.contains(videodb)) {
                                    databases.get(1).insert(filepath, cursor.getString(2));
                                }
                            } while (cursor.moveToNext());

                        }
                    }

                }
             } catch (Exception e) {

                e.printStackTrace();
            }
            return filename;
        }
        int getMemeFromIntent(Intent intent) {

            //Если интент получен из вызванной галереи, тип null
            if (intent.getType() == null) {
                //запрос курсора из БД контента всея ОС
                ArrayList<Uri> uriArrayList = new ArrayList<>();
                //если выбран одиночный объект
                if (intent.getClipData() == null)
                    uriArrayList.add(intent.getData());
                else
                    for (int i = 0; i < intent.getClipData().getItemCount(); i++)
                        uriArrayList.add(intent.getClipData().getItemAt(i).getUri());

                for (Uri uri : uriArrayList) {
                    saveFromUri(uri);
                }

            }
            //Если интент получен из другого приложения
            else {
                switch (FileClassifier.classifyByType(intent)) {
                    //мем - видео с ютуба
                    case FileClassifier.YOUTUBE:
                        //Получение ссылки из интента
                        String localFilename = intent.getClipData().getItemAt(0).getText().toString();
                        //определение имени видеофайла по анализу ключевых символов в ссылке
                        Pattern p = Pattern.compile("([\\w_-]{11})");
                        Matcher m = p.matcher(localFilename);
                        if (m.find()) {
                            //фиксируем полученное имя
                            String filename = localFilename.substring(m.start());
                            databases.get(FileClassifier.classifyByTab(filename)).insert(filename);
                        }
                        break;
                        //мем - прямая ссылка на файл или ссылка из дискорда
                    case FileClassifier.DISCORD:
                        String filename = intent.getClipData().getItemAt(0).getText().toString();
                        filename = MemeFileHelper.createFileHelper(context, MainActivity.uriFolder).createFileFromURL(filename);
                        databases.get(FileClassifier.classifyByTab(filename)).insert(filename);
                        break;
                        //мем - ссылка на альбом ВК
                    case FileClassifier.VK:
                        String vkfilename = intent.getClipData().getItemAt(0).getText().toString();
                        vkfilename = MemeFileHelper.createFileHelper(context, MainActivity.uriFolder).createFileFromURL(vkfilename);
                        databases.get(FileClassifier.classifyByTab(vkfilename)).insert(vkfilename);
                        break;
                    //мем - картинка, gif или видео
                    case FileClassifier.IMAGE:
                    case FileClassifier.GIF:
                    case FileClassifier.VIDEO:
                        // извлекаем uri одним из методов
                        Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                        if (uri == null)
                            uri = intent.getData();
                        saveFromUri(uri);
                        break;


                    default:
                        break;
                }

            }

            return tabNum;
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            loadingProgress.setVisibility(ProgressBar.INVISIBLE);
            memeListFragments.get(tabNum).changeFragment();
        }
    }

    public class BackgroundDeleter extends AsyncTask<Void, Void, Void> {
        MemeListFragment currentFragment;
        MemeDatabaseHelper currentDatabase;
        Context context;

        public BackgroundDeleter(Context context, int tabNum) {
            this.context = context;
            currentFragment = memeListFragments.get(tabNum);
            currentDatabase = databases.get(tabNum);
        }

        @Override
        protected void onPreExecute() {
            loadingProgress.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... integers) {
            try {
                //сортировка номеров удаляемых мемов
                currentFragment.memesListAdapter.selected.sort((a, b) -> b.compareTo(a));
                for (Integer pos :
                        currentFragment.memesListAdapter.selected) {

                    String toDelete = currentFragment.memesListAdapter.memeObjects.get(pos).getMemeRelativePath();
                    MemeFileHelper.createFileHelper(context, MainActivity.uriFolder).deleteFile(toDelete);
                    currentDatabase.delete(toDelete);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            currentFragment.memesListAdapter.getDB();
            currentFragment.memesListAdapter.selected.clear();
            currentFragment.memesListAdapter.deletingMode = false;
            mainMenu.getItem(2).setVisible(false);
            mainMenu.getItem(1).setVisible(true);
            currentFragment.memesListAdapter.notifyDataSetChanged();
            loadingProgress.setVisibility(ProgressBar.INVISIBLE);
        }
    }

    public boolean checkPersistentUri() {

        ArrayList<UriPermission> qwe = (ArrayList<UriPermission>) this.getContentResolver().getPersistedUriPermissions();
        if (qwe.size() != 0) {
            uriFolder = qwe.get(0).getUri();
            memeFileHelper = MemeFileHelper.createFileHelper(this, qwe.get(0).getUri());
            return true;
        } else {
            Intent intent1 = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent1.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent1.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(intent1, REQUEST_PERSISTENT);
            return false;
        }


    }
}