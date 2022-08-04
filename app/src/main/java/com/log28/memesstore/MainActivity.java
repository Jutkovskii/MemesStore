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
import android.os.Handler;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    public  static  Uri uriFolder;
    public static String defaultSign;
    String searchMemeTag="";
    //Список с базами данных с мемами
    ArrayList<MemeDatabaseHelper> databases;
    SharedPreferences userData;
    final String dbKey="dbKey";
    final String SIGN="SIGN";
    final String SWITCH="SWITCH";
    public static boolean isDefaultSingEnabled;
    ArrayList<String> dbNames=new ArrayList<>();
    final String imagedb="imagedb";
    final String videodb="videodb";
ArrayList<ArrayList<MemeObject>> allMemesList;
    //номера вкладок
    private final int IMAGE_POS=0;
    private final int VIDEO_POS=1;
    //номер текущей вкладки
    int tabNum = IMAGE_POS;
    //слой для вкладок
    TabLayout memesCategories;
    //объект для работы с памятью
    FileHelper memeFileHelper;
    //фрагменты с отображением списков мемов
    ArrayList<MemeListFragment> memeListFragments;
    //коды идентификации входящих Intent'ов
    public static final int REQUEST_DB = 53;//импорт БД
    public static final int REQUEST_EXDB = 54;//экспорт БД
    public static final int REQUEST_GALLERY = 84;//добавление из галереи
    public static final int REQUEST_PERSISTENT = 145;//добавление из галереи
    //флаг режима мультивыбора для удалени
    public static boolean deletingMode=false;
    //меню в ActionBar
    public static Menu mainMenu;
ProgressBar loadingProgress;
    ViewPager2 pagerSlider;
    private FragmentStateAdapter pagerAdapter;
    //создание тулбара для главного окна
    Toolbar toolbar;
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler(new LocalExceptionHandler(MainActivity.this));
        savedInstanceState=null;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //проверка доступа к памяти
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

        }
        //объект с вкладками
        memesCategories = findViewById(R.id.categoriesLayout);
        toolbar=findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);
loadingProgress=findViewById(R.id.loadingProcess);
loadingProgress.setVisibility(ProgressBar.INVISIBLE);
        if(checkPersistentUri())
        init();
    }

    public  void  init(){
        //создание и заполнение списка БД
        databases= new ArrayList<>();
        allMemesList=new ArrayList<ArrayList<MemeObject>>();
        //проверка пользовательских БД (на будущее)
        getPreferences();
        for(String s:dbNames) {
            databases.add(new MemeDatabaseHelper(this, s, 1));
            ArrayList<MemeObject> memesList=new ArrayList<>();
            Cursor cursor=databases.get(databases.size()-1).getCursor();
            cursor.moveToFirst();
            int listSize=cursor.getCount();
            for(int i=0;i<listSize;i++) {


                String currentFile = cursor.getString(1);
                String currentTag = cursor.getString(2);
                memesList.add(new MemeObject(getApplicationContext(),currentFile,currentTag));
                cursor.moveToNext();
            }
        allMemesList.add(memesList);
        }

        //создание фрагментов с мемами
        memeListFragments=new ArrayList<>();
        for(int i=0;i<databases.size();i++)
           // memeListFragments.add(new MemeListFragment(databases.get(i)));
            memeListFragments.add(new MemeListFragment(allMemesList.get(i)));


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
        //создание объекта для работы с файловой системой
        // fileHelper2 = new FileHelper2(this);

        //создание слайдера
        pagerSlider = findViewById(R.id.pagerSlider);
        pagerAdapter = new ScreenSlidePagerAdapter(this, memeListFragments);
        pagerSlider.setAdapter(pagerAdapter);
        pagerSlider.setSaveEnabled(false);

        Intent intent = getIntent();
        if(intent!=null&&intent.getAction()!="android.intent.action.MAIN")
            getMemeFromIntent(intent);
    }
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getMemeFromIntent(intent);
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
    public boolean onCreateOptionsMenu(Menu menu)  {
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
                searchMemeTag=newText;
                //определение вкладки для поиска
                memeListFragments.get(tabNum).memesListAdapter.getFilter().filter(newText);
                return true;
            }
        });

        mainMenu =menu;
        return super.onCreateOptionsMenu(menu);
    }

    //опции выбора меню
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        try {

            Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
            String qwe=item.getTitle().toString();
            if (qwe.matches("Удалить")) {
                MemeListFragment currentFragment;
                MemeDatabaseHelper currentDatabase;

                currentFragment=memeListFragments.get(tabNum);
                currentDatabase=databases.get(tabNum);
                //Collections.reverse(currentFragment.memesListAdapter.selected);
                currentFragment.memesListAdapter.selected.sort((a, b) -> b.compareTo(a));
                for (Integer pos :
                        currentFragment.memesListAdapter.selected) {
                    String toDelete = currentFragment.memesListAdapter.memeObjects.get(pos).getMemeRelativePath();
                    deleteFromMemesList(toDelete);
                 /*   MemeFileHelper.createFileHelper(this, MainActivity.uriFolder).deleteFile(toDelete);
                    currentDatabase.delete(toDelete);
                    currentFragment.memesListAdapter.getDB();
                    currentFragment.memesListAdapter.notifyItemRemoved(pos);
*/

                }
                currentFragment.memesListAdapter.selected.clear();
                currentFragment.memesListAdapter.deletingMode = false;
                //mainMenu.getItem(3).setVisible(false);
                mainMenu.getItem(2).setVisible(false);
                mainMenu.getItem(1).setVisible(true);
                currentFragment.memesListAdapter.notifyDataSetChanged();

            }
            if (item.getTitle().toString().matches("Настройки")){
                startActivityForResult(new Intent(this,SettingsActivity.class),0);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return super.onOptionsItemSelected(item);
    }

    //получение мема из входящего Intent'а
    @RequiresApi(api = Build.VERSION_CODES.R)
    int getMemeFromIntent(Intent intent) {
        //объект мема
        MemeObject newMeme=null;
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
            //memeListFragments.get(tabNum).changeFragment();
        }
        //Если интент получен из другого приложения
        else {
            switch (FileClassifier.classifyByType(intent)){
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
                        insertToDB(filename);
                    }
                    break;
                case FileClassifier.DISCORD:
                    String filename= intent.getClipData().getItemAt(0).getText().toString();
                    filename=MemeFileHelper.createFileHelper(this, MainActivity.uriFolder).createFileFromURL(filename);
                    String relativeFilepath =filename;
                    insertToDB(relativeFilepath);
                    break;
                case FileClassifier.VK:
                    String vkfilename= intent.getClipData().getItemAt(0).getText().toString();
                    vkfilename=MemeFileHelper.createFileHelper(this, MainActivity.uriFolder).createFileFromURL(vkfilename);
                    String vkrelativeFilepath =vkfilename;
                    insertToDB(vkrelativeFilepath);
                    break;
                //мем - картинка, gif или видео
                case FileClassifier.IMAGE: case FileClassifier.GIF :case FileClassifier.VIDEO:
                    // извлекаем uri одним из методов
                    Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    if (uri == null)
                        uri = intent.getData();
                    saveFromUri(uri);

                    break;


                default:Toast.makeText(this, "Не удалось обработать файл", Toast.LENGTH_SHORT);break;
            }

        }
       // memeListFragments.get(tabNum).changeFragment();
        //return newMeme.getMemeTab();
return tabNum;
    }

    void getPreferences() {
        dbNames.add(imagedb);
        dbNames.add(videodb);
        userData=getPreferences(MODE_PRIVATE);
        Set<String>usersbd= userData.getStringSet(dbKey,null);
        if(usersbd!=null) {
            for (String userbd : usersbd)
                if (!dbNames.contains(userbd))
                    dbNames.add(userbd);
                isDefaultSingEnabled=userData.getBoolean(SWITCH,false);
                defaultSign=userData.getString(SIGN,"");
        }
        userData.edit().clear().commit();



    }
    void setPreferences() {
        SharedPreferences.Editor editor = userData.edit();
        editor.putStringSet(dbKey,new HashSet<String>(dbNames));
        editor.putString(SIGN,defaultSign);
        editor.putBoolean(SWITCH,isDefaultSingEnabled);
        editor.apply();
    }
    //добавление файла в базу данных
    boolean insertToDB(String relativeFilepath) {
        try {

            addToMemesList(relativeFilepath);
            updateMemesList(tabNum);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void addToMemesList(String relativeFilepath){
        try {
        MemeObject qwe = new MemeObject(getApplicationContext(),relativeFilepath);
        } catch (Exception e) {
        e.printStackTrace();
        }
        //проверка наличия мема в списке
        int current=allMemesList.get(tabNum)
                .indexOf(new MemeObject(this,relativeFilepath));
       if(current>=0) return;
        //добавление мема в БД
        databases.get(FileClassifier.classifyByTab(relativeFilepath)).insert(relativeFilepath);
        //добавление мема в список
        allMemesList.get(tabNum).add(new MemeObject(this,relativeFilepath));
        //обновить фрагмент со списком
        memeListFragments.get(tabNum).changeFragment();
    }

    public void deleteFromMemesList(String relativeFilepath){

        //удаление из БД
        databases.get(tabNum).delete(relativeFilepath);
        //удаление файла
        MemeFileHelper.createFileHelper(this, MainActivity.uriFolder).deleteFile(relativeFilepath);
        //удаление из списка
        allMemesList.get(tabNum).remove(new MemeObject(this,relativeFilepath));
        //обновить фрагмент со списком
        memeListFragments.get(tabNum).changeFragment();
    }

    public void updateMeme(String relativeFilepath, String tag){
        //обновление в БД
        databases.get(tabNum).update(relativeFilepath,tag);
        //получение номера текущего мема в списке
        MemeObject currentMemeObject=new MemeObject(this,relativeFilepath);
        int current=allMemesList.get(tabNum).indexOf(currentMemeObject);
        //обновление данных в списке
        allMemesList.get(tabNum).get(current).memeRelativePath=relativeFilepath;
        allMemesList.get(tabNum).get(current).memeTag=tag;
        //обновить фрагмент со списком
        memeListFragments.get(tabNum).changeFragment();
    }
    void updateMemesList(int tab) {
        memesCategories.selectTab(memesCategories.getTabAt(tab));
        if(memeListFragments.get(tab).memesListAdapter==null)
        memeListFragments.get(tab).changeFragment();
        else
            memeListFragments.get(tab).memesListAdapter.add();
    }
    //вызов галереи для добавления мема
    public void addMeme(View v) {

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
                String filename = data.getDataString();
                deleteFromMemesList(filename);
            }
        //результат: подпись нужно изменить
        if (resultCode == MemeViewerActivity.CHANGE_CODE)
        {

            String filetag= data.getStringExtra(MemeViewerActivity.FILETAG_EXTRA);
            String filename = data.getStringExtra(MemeViewerActivity.FILENAME_EXTRA);
            tabNum=FileClassifier.classifyByTab(filename);
            updateMeme(filename,filetag);
        }

        //результат: файл нужно добавить
        if (requestCode == REQUEST_GALLERY)
            if (resultCode == RESULT_OK) {
                //получаем и сохраняем мем из uri
                //выбор вкладки согласно типу файла
                tabNum =getMemeFromIntent(data) ;

            }
      //  if (requestCode == REQUEST_DB)
            if (resultCode == REQUEST_DB) {
                saveFromUri(data.getData());
            }
       // if (requestCode == REQUEST_EXDB)
            if (resultCode == REQUEST_EXDB)
            { try {
                //список путей к экспортируемым файлам
                ArrayList<String> memepaths = new ArrayList<>();
                for (MemeDatabaseHelper thisdb : databases){
                   //добавление самой БД в список
                    memepaths.add(thisdb.getDbPath());
                    //добавление путей из БД в список
                    Cursor localcursor = thisdb.getCursor();
                    localcursor.moveToFirst();
                    for (int i = 0; i < localcursor.getCount(); i++) {
                        memepaths.add(localcursor.getString(1));
                        localcursor.moveToNext();
                    }
                }
                //сохранение архива
                new MemeFileHelper(this,uriFolder).zipPack(data.getData(),memepaths);
            }
            catch (Exception e){
                e.printStackTrace();
            }

            }

        if (requestCode ==REQUEST_PERSISTENT
                && resultCode == Activity.RESULT_OK) {
            if (data != null) {

                uriFolder=data.getData();

                final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION| Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(uriFolder, takeFlags);
                grantUriPermission(this.getPackageName(),uriFolder,takeFlags);
                memeFileHelper = MemeFileHelper.createFileHelper(this,uriFolder);
                init();

            }
        }

    }
    String saveFromUri(Uri uri) {
new BackgroundLoader().execute(uri);
        // imageDownloadThread(uri);
        //updateMemesList(tabNum);

return "";
    }

    public class BackgroundLoader extends AsyncTask<Uri,Integer,ArrayList<String>> {
        @Override
        protected void onPreExecute() {
            loadingProgress.setVisibility(ProgressBar.VISIBLE);

        }

        @Override
        protected ArrayList<String> doInBackground(Uri... uris) {
            ArrayList<String> filepath = new ArrayList<>();
            try {
                Uri uri=uris[0];
                String filename = "";
                InputStream inputStream;

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
                        String relativeFilepath =FileClassifier.getRelativePath(filename);
                        publishProgress(50);
                        memeFileHelper.writeToFile(inputStream, memeFileHelper.createFile(relativeFilepath));
                        filepath.add(relativeFilepath);
                        //insertToDB(relativeFilepath);

                    } else {

                        ArrayList<String> imported = (ArrayList<String>) new MemeFileHelper(getApplicationContext(),uriFolder).unzipPack(inputStream);
                        int y=imported.size();
                        for(String db:imported){
                            String dbName=db;

                            SQLiteDatabase importedDB = SQLiteDatabase.openOrCreateDatabase(db, null);
                            Cursor cursor = importedDB.rawQuery("SELECT * FROM memesTable", null);

                            if (cursor.moveToFirst()) {
                                do {
                                    String qwe= cursor.getString(1);
                                    if(qwe.contains(".")&&!qwe.contains("/"))
                                        qwe=FileClassifier.getRelativePath(qwe);
                                    if(dbName.contains(imagedb)){
                                        //addToMemesList(qwe);
                                        //insertToDB(qwe);
                                    filepath.add(qwe);
                                    }
                                    if(dbName.contains(videodb)){
                                        //addToMemesList(qwe);
                                        databases.get(1).insert(qwe, cursor.getString(2));
                                             }
                                } while (cursor.moveToNext());

                            }
                        }

                    }

                }

                catch (Exception e) {

                    e.printStackTrace();
                }

            return filepath;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
       loadingProgress.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<String> aVoid) {
            for(String memepath:aVoid) {
                insertToDB(memepath);
                memeListFragments.get(tabNum).changeFragment();
            }
            loadingProgress.setVisibility(ProgressBar.INVISIBLE);
        }
    }
    String relativeFilepath;
    Handler mHandler = new Handler();
    private void imageDownloadThread(Uri uri) {
        Thread thread = new Thread(new Runnable()
        {
            @Override public void run()
            {
                try {
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
                            relativeFilepath =FileClassifier.getRelativePath(filename);
                            memeFileHelper.writeToFile(inputStream, memeFileHelper.createFile(relativeFilepath));

                            mHandler.post(new Runnable() {
                                @Override public void run() {
                                    insertToDB(relativeFilepath);
                                    memeListFragments.get(tabNum).changeFragment();
                                }
                            });
                        } else {

                            ArrayList<String> imported = (ArrayList<String>) new MemeFileHelper(getApplicationContext(),uriFolder).unzipPack(inputStream);
                            int y=imported.size();
                            for(String db:imported){
                                String dbName=db;

                                SQLiteDatabase importedDB = SQLiteDatabase.openOrCreateDatabase(db, null);
                                Cursor cursor = importedDB.rawQuery("SELECT * FROM memesTable", null);

                                if (cursor.moveToFirst()) {
                                    do {
                                        relativeFilepath= cursor.getString(1);
                                        if(relativeFilepath.contains(".")&&!relativeFilepath.contains("/"))
                                            relativeFilepath=FileClassifier.getRelativePath(relativeFilepath);
                                        if(dbName.contains(imagedb)){
                                            //addToMemesList(qwe);

                                            //databases.get(0).insert(qwe, cursor.getString(2));
                                            mHandler.post(new Runnable() {
                                                @Override public void run() {

                                                    insertToDB(relativeFilepath);
                                                    memeListFragments.get(tabNum).changeFragment();
                                                }
                                            });
                                        }
                                        if(dbName.contains(videodb)){
                                            //addToMemesList(qwe);
                                            databases.get(1).insert(relativeFilepath, cursor.getString(2));
                                            mHandler.post(new Runnable() {
                                                @Override public void run() {
                                                    memeListFragments.get(tabNum).changeFragment();
                                                }
                                            });
                                        }
                                    } while (cursor.moveToNext());

                                }
                            }

                        }

                    }

                    catch (Exception e) {

                        e.printStackTrace();
                    }





                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

   public boolean checkPersistentUri(){

        ArrayList<UriPermission>persistedUriPermissions= (ArrayList<UriPermission>) this.getContentResolver().getPersistedUriPermissions();
        if(persistedUriPermissions.size()!=0){
            uriFolder=persistedUriPermissions.get(0).getUri();
            memeFileHelper = MemeFileHelper.createFileHelper(this,persistedUriPermissions.get(0).getUri());
            return true;
        }
        else
        {
            Intent intent1 = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent1.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent1.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(intent1, REQUEST_PERSISTENT);
            return false;
        }


    }
}