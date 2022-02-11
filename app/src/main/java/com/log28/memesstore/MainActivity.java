package com.log28.memesstore;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //объект списка
    //RecyclerView memesList;
    //объект БД
    MemeDatabaseHelper testdb;

    MemeDatabaseHelper imagedb;

    MemeDatabaseHelper videodb;
    //обработчик свайпов
    private GestureDetectorCompat swipeDetector;
    //номер вкладки
    int tabNum = 0;
    static final int IMAGE = 0;
    static final int VIDEO = 1;
    //слой для вкладок
    TabLayout memesCategories;
    //объект для работы с памятью
    FileHelper fileHelper;
    //адаптер для заполнения списка
   // MemesListAdapter memesListAdapter;

    MemeListFragment imageListFragment;
    MemeListFragment videoListFragment;
    private final int REQUEST_DB = 53;
    private final int REQUEST_GALLERY = 84;



    ViewPager2 pagerSlider;
    private FragmentStateAdapter pagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //проверка доступа к памяти
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

        }
        //обработчик свайпов
       // swipeDetector = new GestureDetectorCompat(this, new SwipeListener());
        //запрет поворота экрана (УДАЛИТЬ ПОЗДНЕЕ!)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        //объект с вкладками
        memesCategories = findViewById(R.id.categoriesLayout);
        //обработчик выбора вкладок
        memesCategories.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabNum = tab.getPosition();
                pagerSlider.setCurrentItem(tabNum);
                //setMemesList(tabNum);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        fileHelper = new FileHelper(this);
        //memesList = findViewById(R.id.memesList);
        //memesList.setLayoutManager(new GridLayoutManager(this, 2));


        //запрос интента при старте
        Intent intent = getIntent();
        //если интент существует и соответствует критерию получаем объект из интента
        if (intent.getAction() != "android.intent.action.MAIN")
            // if(intent!=null&&intent.getAction()=="android.intent.action.SEND")
            getMemeFromIntent(intent);

          //установка списка согласно выбранной вкладке
        //ВОЗМОЖНО ЗАМЕНИТЬ НА ФРАГМЕНТЫ


        imagedb=new MemeDatabaseHelper(this, "test", 1);
        videodb = new MemeDatabaseHelper(this, "video1", 1);
        imageListFragment=new MemeListFragment(imagedb);
        videoListFragment=new MemeListFragment(videodb);


        /*imageListFragment=new MemeListFragment(new MemeDatabaseHelper(this, "test", 1));
        videoListFragment=new MemeListFragment(new MemeDatabaseHelper(this, "video1", 1));
*/
     /*   FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
        ft.add(R.id.memeFramelayout,imageListFragment);
        ft.commit();
*/
        //setMemesList(tabNum);
        //установка обработчика свайпов
      /*  memesList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return swipeDetector.onTouchEvent(event);
            }
        });
*/



        pagerSlider=findViewById(R.id.pagerSlider);
        pagerAdapter=new ScreenSlidePagerAdapter(this, new ArrayList<MemeListFragment>(Arrays.asList(imageListFragment,videoListFragment)));
        pagerSlider.setAdapter(pagerAdapter);

    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {

        List<MemeListFragment> memeLists;
        public ScreenSlidePagerAdapter(FragmentActivity fa,List<MemeListFragment> memeLists) {
            super(fa);
            this.memeLists=memeLists;
        }

        @Override
        public long getItemId(int position) {
            memesCategories.selectTab(memesCategories.getTabAt(position));
            return super.getItemId(position);
        }

        @Override
        public Fragment createFragment(int position) {


            if(position==0)
            return imageListFragment;
            else
           return videoListFragment;
        }

        @Override
        public int getItemCount() {
            return memeLists.size();
        }
    }
    @Override
    protected void onStart() {
        super.onStart();

    }

    void getMemeFromIntent(Intent intent) {
        //Имя файла (не путь, только имя)
        String filename = "";
        //поток входных данных
        InputStream inputStream;
        //Если тип данных null
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
                fileHelper.createLocalFile(inputStream, filename);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            //Определение типа входных данных
            String receivedType = intent.getType();
            //Если получен текст, извлекаем имя видеофайла из ссылки
            if (receivedType.startsWith("text")) {
                String localFilename = intent.getClipData().getItemAt(0).getText().toString();
                if (localFilename.contains("&"))
                    localFilename = localFilename.substring(localFilename.indexOf("=") + 1, localFilename.lastIndexOf("&"));
                else if (localFilename.contains("="))
                    localFilename = localFilename.substring(localFilename.lastIndexOf("=") + 1);
                else localFilename = localFilename.substring(localFilename.lastIndexOf("/") + 1);
                filename = localFilename;
                //загружаем превью
                PreviewSaver previewSaver = new PreviewSaver(fileHelper);
                previewSaver.execute(new String[]{filename});
            }
//если получено изображение или видео
            else if (receivedType.startsWith("image") || receivedType.startsWith("video")) {

                Uri localUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (localUri == null)
                    localUri = intent.getData();


                Cursor returnCursor = getContentResolver().query(localUri, null, null, null, null);
                if (returnCursor != null) {
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    returnCursor.moveToFirst();
                    filename = returnCursor.getString(nameIndex);
                } else
                    filename = localUri.getPath().substring(localUri.getPath().lastIndexOf("/") + 1);

                try {
                    //получение потока входных данных
                    inputStream = getContentResolver().openInputStream(localUri);
                    //создание локального файла
                    fileHelper.createLocalFile(inputStream, filename);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            } else Toast.makeText(this, "Не удалось добавить файл", Toast.LENGTH_SHORT);
        }
        String DBname = "";
        //Определение типа данных для выбора БД
        switch (fileHelper.getType(filename)) {
            case FileHelper.VIDEO:
            case FileHelper.HTTPS:
                DBname = "video1";
                break;
            case FileHelper.IMAGE:
                DBname = "test";
                break;
        }
        //открытие и запись в БД
        MemeDatabaseHelper testdb = new MemeDatabaseHelper(this, DBname, 1);
        testdb.getWritableDatabase();
        testdb.insert(filename);
        testdb.close();

    }

    //заполнение списка данными из БД
    void setMemesList(int category) {
        FragmentTransaction ft=getSupportFragmentManager().beginTransaction();


        switch (category) {
            case IMAGE:
                //testdb = new MemeDatabaseHelper(this, "test", 1);
                // imageListFragment.setDB(testdb);
                imageListFragment.setDB(imagedb);

                ft.replace(R.id.memeFramelayout,imageListFragment);
                break;
            case VIDEO:
                //testdb = new MemeDatabaseHelper(this, "video1", 1);
                // imageListFragment.setDB(testdb);
               videoListFragment.setDB(videodb);

                ft.replace(R.id.memeFramelayout,videoListFragment);
                break;
        }
        ft.commit();
      /*  testdb.getWritableDatabase();
        memesListAdapter = new MemesListAdapter(this, testdb);
        memesList.setAdapter(memesListAdapter);
        memesListAdapter.notifyDataSetChanged();*/
//testdb.exportDB();

    }

    //вызов галерени для добавления мема
    public void addMeme(View v) {
//Вызываем стандартную галерею для выбора изображения с помощью Intent.ACTION_PICK:
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
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
    }

    //получение результата от дочерней активности
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //проверка кода запроса и кода результата
        //результат: файл нужно удалить
        if (requestCode == MemeViewerActivity.REQUEST_CODE)
            if (resultCode == MemeViewerActivity.DELETED_MEME_CODE) {
                //удаление файла
                new FileHelper(this).deleteFile(data.getDataString());
                //удаление записи из БД


                if (fileHelper.getType(data.getDataString()) == fileHelper.IMAGE) {
                    tabNum = 0;
                    imagedb.delete(data.getDataString());
                }
                else {
                    tabNum = 1;
                    videodb.delete(data.getDataString());
                }
                //testdb.delete(data.getDataString());
                //setMemesList(tabNum);
                memesCategories.selectTab(memesCategories.getTabAt(tabNum));
            }

        //результат: файл нужно добавить
        if (requestCode == REQUEST_GALLERY)
            if (resultCode == RESULT_OK) {
                //хитрое получение имени файла
                Uri uri = data.getData();
                Toast.makeText(this, uri.getPath(), Toast.LENGTH_LONG);
                Cursor returnCursor =
                        getContentResolver().query(uri, null, null, null, null);
                String fileName = null;
                try {
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    returnCursor.moveToFirst();
                    fileName = returnCursor.getString(nameIndex);
                } catch (Exception e) {
                    e.printStackTrace();

                }
                //получаем и сохраняем мем из uri
                getMemeFromIntent(data);
               // testdb.getWritableDatabase();
                //выбор вкладки согласно типу файла
                if (fileHelper.getType(fileName) == fileHelper.IMAGE)
                    tabNum = 0;
                else
                    tabNum = 1;
                setMemesList(tabNum);
                memesCategories.selectTab(memesCategories.getTabAt(tabNum));
            }

    }


}