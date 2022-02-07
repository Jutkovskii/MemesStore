package com.log28.memesstore;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    //объект списка
    RecyclerView memesList;
    //объект БД
    MemeDatabaseHelper testdb;
    //обработчик свайпов
    private GestureDetectorCompat swipeDetector;
    //номер вкладки
    int tabNum=0;
    static final int IMAGE=0;
    static final  int VIDEO=1;
    //слой для вкладок
    TabLayout memesCategories;
    //объект для работы с памятью
    FileHelper fileHelper;
    //адаптер для заполнения списка
    MemesListAdapter memesListAdapter;
    //НЕ ПОМНЮ ЧТО
    private final int Pick_image = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //проверка доступа к памяти
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

        }
        //обработчик свайпов
        swipeDetector=new GestureDetectorCompat(this, new SwipeListener());
        //запрет поворота экрана (УДАЛИТЬ ПОЗДНЕЕ!)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        //объект с вкладками
        memesCategories = findViewById(R.id.categoriesLayout);
        //обработчик выбора вкладок
        memesCategories.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        tabNum =tab.getPosition();
        setMemesList(tabNum);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
});

        fileHelper = new FileHelper(this);
        memesList = findViewById(R.id.memesList);
        memesList.setLayoutManager(new GridLayoutManager(this,2));
    }

    @Override
    protected void onStart() {
        super.onStart();
        //запрос интента при старте
        Intent intent = getIntent();
        //если интент существует и соответствует критерию получаем объект из интента
        if(intent!=null&&intent.getAction()=="android.intent.action.SEND")
            getMemeFromIntent(intent);
        //установка списка согласно выбранной вкладке
        //ВОЗМОЖНО ЗАМЕНИТЬ НА ФРАГМЕНТЫ
        setMemesList(tabNum);
        //установка обработчика свайпов
        memesList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return swipeDetector.onTouchEvent(event);
            }
        });
    }

    void getMemeFromIntent(Intent intent) {
    //Имя файла (не путь, только имя)
    String filename = "";
    //поток входных данных
    InputStream inputStream;
    //Если тип данных null
    if (intent.getType() == null)
//if(true)
    {
        //получение uri файла
        Uri uri = intent.getData();
        //запрос курсора из БД контента всея ОС
        Cursor returnCursor =
                getContentResolver().query(uri, null, null, null, null);
        //Определение стаолбца, содержащего имя файла
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        //определение имени файла
        filename = returnCursor.getString(nameIndex);
        try {
            //получение потока входных данных
            inputStream= getContentResolver().openInputStream(uri);
            //создание локального файла
            fileHelper.createLocalFile(inputStream, filename);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    else{
       //Определение типа входных данных
        String receivedType = intent.getType();
        //Если получен текст, извлекаем имя видеофайла из ссылки
if(receivedType.startsWith("text")){
    String localFilename = intent.getClipData().getItemAt(0).getText().toString();
    if (localFilename.contains("&"))
        localFilename = localFilename.substring(localFilename.indexOf("=") + 1, localFilename.lastIndexOf("&"));
    else if (localFilename.contains("="))
        localFilename = localFilename.substring(localFilename.lastIndexOf("=") + 1);
    else localFilename = localFilename.substring(localFilename.lastIndexOf("/") + 1);
    filename=localFilename;
    //загружаем превью
    PreviewSaver previewSaver = new PreviewSaver(fileHelper);
    previewSaver.execute(new String[]{filename});
}
//если получено изображение или видео
if(receivedType.startsWith("image")||receivedType.startsWith("video")){

    Uri localUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
    if( localUri!=null)
    {

        Cursor returnCursor =getContentResolver().query(localUri, null, null, null, null);
        if (returnCursor!=null)
        {int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        filename = returnCursor.getString(nameIndex);}
else
            filename  = localUri.getPath().substring(localUri.getPath().lastIndexOf("/")+1);
        //получение потока входных данных
        try {
            inputStream= getContentResolver().openInputStream(localUri);
            fileHelper.createLocalFile(inputStream, filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //создание локального файла

    }
    else{//А СЮДА ВООБЩЕ ЗАХОДИТ?!!!!!!!
        localUri = intent.getData();
        Cursor returnCursor =getContentResolver().query(localUri, null, null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        filename = returnCursor.getString(nameIndex);
        try {
            inputStream= getContentResolver().openInputStream(localUri);
            fileHelper.createLocalFile(inputStream, filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

        }

}
    String DBname="";
    //Определение типа данных для выбора БД
    switch(fileHelper.getType(filename)){
        case FileHelper.VIDEO: case FileHelper.HTTPS: DBname="video1"; break;
        case FileHelper.IMAGE: DBname="test"; break;
        }
    //открытие и запись в БД
        MemeDatabaseHelper testdb = new MemeDatabaseHelper(this, DBname, 1);
        testdb.getWritableDatabase();
        testdb.insert(filename);//fileHelper.getFileLocation(filename));
        testdb.close();

}
//заполнение списка данными из БД
void setMemesList(int category){

        switch (category){
            case IMAGE:
                testdb= new MemeDatabaseHelper(this,"test",1);

                break;
            case VIDEO:
                testdb= new MemeDatabaseHelper(this,"video1",1);
                break;
        }

    testdb.getWritableDatabase();
    memesListAdapter = new MemesListAdapter(this,testdb);
    memesList.setAdapter(memesListAdapter);
    memesListAdapter.notifyDataSetChanged();

}
//вызов галерени для добавления мема
    public void addMeme(View v){
//Вызываем стандартную галерею для выбора изображения с помощью Intent.ACTION_PICK:
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        //Тип получаемых объектов - image:
        photoPickerIntent.setType("image/*");
        //Запускаем переход с ожиданием обратного результата в виде информации об изображении:
        startActivityForResult(photoPickerIntent, Pick_image);//посмотреть, как нынче надо
    }

    //получение результата от дочерней активности
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==15)
           testdb.delete(data.getDataString());
        if(resultCode==RESULT_OK)
        {
            Uri uri=data.getData();
            String type=uri.getPath();
            String action=uri.getLastPathSegment();
            Toast.makeText(this,uri.getPath(),Toast.LENGTH_LONG);
            Cursor returnCursor =
                    getContentResolver().query(uri, null, null, null, null);
            try {
                String path = uri.getPath();
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();
               String fileName = returnCursor.getString(nameIndex);

                InputStream imageStream = getContentResolver().openInputStream(uri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                int y=selectedImage.getHeight();
                y++;

            } catch (FileNotFoundException e) {
                e.printStackTrace();

            }
            getMemeFromIntent(data);

            testdb.getWritableDatabase();


        }
        memesListAdapter = new MemesListAdapter(this,testdb);
        memesList.setAdapter(memesListAdapter);
        memesListAdapter.notifyDataSetChanged();
    }


//обработчик свайпов
private  class SwipeListener extends GestureDetector.SimpleOnGestureListener{
int DIST=10;
    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if(Math.abs(velocityX)>DIST&&Math.abs(velocityX)>Math.abs(velocityY)*2) {
            int setCategory=-1;
            if (velocityX > 0)
            setCategory=IMAGE;
            else
                setCategory=VIDEO;
            setMemesList(setCategory);
            memesCategories.selectTab(memesCategories.getTabAt(setCategory));
        }
        return false;//super.onFling(e1, e2, velocityX, velocityY);
    }
}




}