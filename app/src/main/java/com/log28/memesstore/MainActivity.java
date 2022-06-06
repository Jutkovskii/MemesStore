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
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    String searchMemeTag="";
    //объект БД
    MemeDatabaseHelper imagedb;
    MemeDatabaseHelper videodb;
    //номера вкладок
    private final int IMAGE_POS=0;
    private final int VIDEO_POS=1;
    //номер текущей вкладки
    int tabNum = IMAGE_POS;
    //слой для вкладок
    TabLayout memesCategories;
    //объект для работы с памятью
    FileHelper fileHelper;
    //фрагменты с отображением списков мемов
    MemeListFragment imageListFragment;
    MemeListFragment videoListFragment;
    //коды идентификации входящих Intent'ов
    private final int REQUEST_DB = 53;//импорт БД
    private final int REQUEST_GALLERY = 84;//добавление из галереи
    //флаг режима мультивыбора для удалени
   public static boolean deletingMode=false;
   //меню в ActionBar
    public static Menu mainMenu;

    ViewPager2 pagerSlider;
    private FragmentStateAdapter pagerAdapter;
    //создание тулбара для главного окна
    Toolbar toolbar;
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("OLOLOG","Активность с=Создание " );
        //проверка доступа к памяти
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

        }
        //запрос БД
        getBD();
        Log.d("OLOLOG","Активность Создание фрагментов " );
        //создание фрагментов с мемами
        imageListFragment = new MemeListFragment(imagedb);
        videoListFragment = new MemeListFragment(videodb);

        //запрет поворота экрана (УДАЛИТЬ ПОЗДНЕЕ!)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        //объект с вкладками
        memesCategories = findViewById(R.id.categoriesLayout);
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
        fileHelper = new FileHelper(this);


        //запрос интента при старте
        Intent intent = getIntent();
        //если интент существует и соответствует критерию получаем объект из интента
        if (intent.getAction() != "android.intent.action.MAIN")
            // if(intent!=null&&intent.getAction()=="android.intent.action.SEND")
            getMemeFromIntent(intent);
        //создание слайдера
        pagerSlider = findViewById(R.id.pagerSlider);
        pagerAdapter = new ScreenSlidePagerAdapter(this, new ArrayList<MemeListFragment>(Arrays.asList(imageListFragment, videoListFragment)));
        pagerSlider.setAdapter(pagerAdapter);

    toolbar=findViewById(R.id.mainToolbar);
    setSupportActionBar(toolbar);

    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {

        //список мемов
        List<MemeListFragment> memeLists;
        //текущая позиция
        int pos;
        public ScreenSlidePagerAdapter(FragmentActivity fa, List<MemeListFragment> memeLists) {
            super(fa);
            this.memeLists = memeLists;
        }

        @Override
        public long getItemId(int position) {
            //обновление при смене вкладки
            memesCategories.selectTab(memesCategories.getTabAt(position));

            switch (position){
                case IMAGE_POS:
                    imageListFragment.setFilter(searchMemeTag); break;
                case VIDEO_POS:
                    videoListFragment.setFilter(searchMemeTag); break;
                default:
                    imageListFragment.setFilter(searchMemeTag); break;
            }

            return super.getItemId(position);
        }

        @Override
        public Fragment createFragment(int position) {
            //обновление при создании
            pos=position;
            switch (position){
                case IMAGE_POS:
                    imageListFragment.setFilter(searchMemeTag);  return imageListFragment;
                case VIDEO_POS:
                    videoListFragment.setFilter(searchMemeTag); return videoListFragment;
                default:
                    imageListFragment.setFilter(searchMemeTag); return imageListFragment;
            }
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
                switch (tabNum){
                    case IMAGE_POS:
                        imageListFragment.memesListAdapter.getFilter().filter(newText);break;
                    case VIDEO_POS:
                        videoListFragment.memesListAdapter.getFilter().filter(newText); break;
                    default:
                        imageListFragment.memesListAdapter.getFilter().filter(newText);break;
                }

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
                if (tabNum == 0) {
                    currentFragment = imageListFragment;
                    currentDatabase = imagedb;
                } else {
                    currentFragment = videoListFragment;
                    currentDatabase = videodb;
                }
                //Collections.reverse(currentFragment.memesListAdapter.selected);
                currentFragment.memesListAdapter.selected.sort((a, b) -> b.compareTo(a));
                for (Integer pos :
                        currentFragment.memesListAdapter.selected) {
                    String toDelete = currentFragment.memesListAdapter.memeGroups.get(pos).getName();
                    new FileHelper(this).deleteFile(toDelete);
                    currentDatabase.delete(toDelete);
                    //MainActivity.menu1.removeItem(currentFragment.memesListAdapter.deleteItem.getItemId());
                    currentFragment.memesListAdapter.getDB();
                    //currentFragment.memesListAdapter.filteredGroups.remove( pos);
                    //currentFragment.memesListAdapter.notifyItemChanged(pos);

                    currentFragment.memesListAdapter.notifyItemRemoved(pos);


                }
                currentFragment.memesListAdapter.selected.clear();
                currentFragment.memesListAdapter.deletingMode = false;
                mainMenu.getItem(3).setVisible(false);
                mainMenu.getItem(2).setVisible(true);
                mainMenu.getItem(1).setVisible(true);
                currentFragment.memesListAdapter.notifyDataSetChanged();

            }
            //сбор всех мемов в zip файл
            if (item.getTitle().toString().matches("Экспорт")) {
                ArrayList<String> memepaths = new ArrayList<>();
                for (MemeDatabaseHelper thisdb : new MemeDatabaseHelper[]{imagedb, videodb}) {

                    memepaths.add(thisdb.getDbPath());
                    Cursor localcursor = thisdb.getCursor();
                    localcursor.moveToFirst();
                    for (int i = 0; i < localcursor.getCount(); i++) {
                        memepaths.add(FileHelper.getFullPath(localcursor.getString(1)));
                        localcursor.moveToNext();

                    }


                }
                new FileHelper(this).zipPack(memepaths);
            }
            //извлечение мемов из zip-файла
            if (item.getTitle().toString().matches("Импорт")) {
                selectDBforImport();
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
        //Имя файла (не путь, только имя)
        String filename = "";
        //поток входных данных
        InputStream inputStream;
        //Если интент получен из вызванной галереи, тип null
        if (intent.getType() == null) {
            //получение uri файла
            Uri uri = intent.getData();
            //запрос курсора из БД контента всея ОС
            Cursor returnCursor = getContentResolver().query(uri, null, null, null, null);
            //Определение стаолбца, содержащего имя файла
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            //определение имени файла
            filename = returnCursor.getString(nameIndex);
            try {
                //получение потока входных данных
                inputStream = getContentResolver().openInputStream(uri);
                //создание локального файла
                filename=  fileHelper.createLocalFile(inputStream, filename);

            } catch (Exception e) {

                Toast.makeText(this, "Не удалось обработать файл", Toast.LENGTH_LONG);
                e.printStackTrace();
            }
        }
        //Если интент получен из другого приложения
        else {
            //Определение типа входных данных
            String receivedType = intent.getType();
            //Если получен текст, извлекаем имя видеофайла из ссылки
            if (receivedType.startsWith("text")) {
                //Получение ссылки из интента
                String localFilename = intent.getClipData().getItemAt(0).getText().toString();
                //определение имени видеофайла по анализу ключевых символов в ссылке
                if (localFilename.contains("&"))
                    localFilename = localFilename.substring(localFilename.indexOf("=") + 1, localFilename.lastIndexOf("&"));
                else if (localFilename.contains("="))
                    localFilename = localFilename.substring(localFilename.lastIndexOf("=") + 1);
                else localFilename = localFilename.substring(localFilename.lastIndexOf("/") + 1);
                //фиксируем полученное имя
                filename = localFilename;
                //загружаем превью
                PreviewSaver previewSaver = new PreviewSaver(fileHelper);
                previewSaver.execute(new String[]{filename});
            }
            //если получено изображение или видео
            else if (receivedType.startsWith("image") || receivedType.startsWith("video")) {
                // извлекаем uri одним из методов
                Uri localUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (localUri == null)
                    localUri = intent.getData();
                //определяем имя файла
                Cursor returnCursor = getContentResolver().query(localUri, null, null, null, null);
                //Через курсор (для андроид 10 и выше)
                if (returnCursor != null) {
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    returnCursor.moveToFirst();
                    filename = returnCursor.getString(nameIndex);
                }
                //Через путь (для предыдущих версий)
                else
                    filename = localUri.getPath().substring(localUri.getPath().lastIndexOf("/") + 1);
                try {
                    //получение потока входных данных
                    inputStream = getContentResolver().openInputStream(localUri);
                    //создание локального файла
                  filename=  fileHelper.createLocalFile(inputStream, filename);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Не удалось создать локальный файл", Toast.LENGTH_SHORT);
                }

            }
            //если не сработал ни один метод
            else Toast.makeText(this, "Не удалось обработать файл", Toast.LENGTH_SHORT);
        }

        if (insertToDB(filename)) {
            if (fileHelper.getType(filename) == FileHelper.IMAGE) return FileHelper.IMAGE;
            else if (fileHelper.getType(filename) == FileHelper.GIF) return FileHelper.GIF;
            else return fileHelper.VIDEO;
        }
        else
            return -1;
    }

    //получение баз данных
    void getBD() {
        Log.d("OLOLOG","Активность Создание баз данных " );
        imagedb = new MemeDatabaseHelper(this, "imagedb", 1);
        videodb = new MemeDatabaseHelper(this, "videodb", 1);
    }

    //добавление файла в базу данных
    boolean insertToDB(String filename) {
        Log.d("OLOLOG","Активность Добавление в базы данных " );
        //получение баз данных, если они не были открыты (приложение стартовало по интенту)
        //getBD();
        try {
            switch (fileHelper.getType(filename)) {
                case FileHelper.VIDEO:
                case FileHelper.GIF:
                case FileHelper.HTTPS:
                    videodb.insert(filename);
                    break;
                case FileHelper.IMAGE:
                    imagedb.insert(filename);
                    break;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    //вызов галерени для добавления мема
    public void addMeme(View v) {
        Log.d("OLOLOG","Активность Добавление мема " );
//Вызываем стандартную галерею для выбора изображения с помощью Intent.ACTION_PICK:
        //Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.addCategory(Intent.CATEGORY_OPENABLE);
        //Тип получаемых объектов - image:
        if (tabNum == 0)
            photoPickerIntent.setType("image/*");
        else
            photoPickerIntent.setType("video/*");
        //Запускаем переход с ожиданием обратного результата в виде информации об изображении:
        startActivityForResult(photoPickerIntent, REQUEST_GALLERY);//посмотреть, как нынче надо
    }

    //НА БУДУЩЕЕ вызов импорта базы данных
    public void selectDBforImport() {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(chooseFile, REQUEST_DB);
       // imagedb.importDB();
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
                Log.d("OLOLOG","Активность удалить мем " );
                //удаление файла
                new FileHelper(this).deleteFile(data.getDataString());
                //удаление записи из БД
                if (fileHelper.getType(data.getDataString()) == fileHelper.IMAGE) {
                    tabNum = 0;
                    imagedb.delete(data.getDataString());
                } else {
                    tabNum = 1;
                    videodb.delete(data.getDataString());
                }

                memesCategories.selectTab(memesCategories.getTabAt(tabNum));
            }
        //результат: подпись нужно изменить
        if (resultCode == MemeViewerActivity.CHANGE_CODE)
            {

               String filetag= data.getStringExtra(MemeViewerActivity.FILETAG_EXTRA);
               String filename = data.getStringExtra(MemeViewerActivity.FILENAME_EXTRA);
                if (fileHelper.getType(filename) == fileHelper.IMAGE) {
                    tabNum = 0;
                    imagedb.update(filename,filetag);
                } else {
                    tabNum = 1;
                    videodb.update(filename,filetag);
                }
                memesCategories.selectTab(memesCategories.getTabAt(tabNum));
            }

        //результат: файл нужно добавить
        if (requestCode == REQUEST_GALLERY)
            if (resultCode == RESULT_OK) {
                Log.d("OLOLOG","Активность Получить мем из галереи " );
                //получаем и сохраняем мем из uri
                //выбор вкладки согласно типу файла
                if (getMemeFromIntent(data) == fileHelper.IMAGE)
                    tabNum = 0;
                else
                    tabNum = 1;
                //setMemesList(tabNum);
                memesCategories.selectTab(memesCategories.getTabAt(tabNum));
            }
     if (requestCode == REQUEST_DB){
         //Имя файла (не путь, только имя)
         String filename = "";
         //поток входных данных
         InputStream inputStream;
         //Если интент получен из вызванной галереи, тип null
         if (data.getType() == null) {
             //получение uri файла
             Uri uri = data.getData();
             //запрос курсора из БД контента всея ОС
             Cursor returnCursor = getContentResolver().query(uri, null, null, null, null);
             //Определение стаолбца, содержащего имя файла
             int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
             returnCursor.moveToFirst();
             //определение имени файла
             filename = returnCursor.getString(nameIndex);
             try {
                 //получение потока входных данных
                 inputStream = getContentResolver().openInputStream(uri);
                 //String localFile=new FileHelper(this).createLocalFile(inputStream,filename);
                 ArrayList<MemeGroup> imported =  new FileHelper(this).unzipPack( inputStream,FileHelper.getFullPath(filename));
                 for(MemeGroup thisGroup: imported){
                     if(FileHelper.getType(thisGroup.name)==FileHelper.IMAGE)
                         imagedb.insert(thisGroup.getName(),thisGroup.getTag());
                     if(FileHelper.getType(thisGroup.name)==FileHelper.VIDEO||FileHelper.getType(thisGroup.name)==FileHelper.GIF)
                         videodb.insert(thisGroup.getName(),thisGroup.getTag());

                 }

             } catch (Exception e) {

                 Toast.makeText(this, "Не удалось обработать файл", Toast.LENGTH_LONG);
                 e.printStackTrace();
             }
         }


     }
}
}