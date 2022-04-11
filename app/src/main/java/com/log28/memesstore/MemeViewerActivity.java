package com.log28.memesstore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.io.File;

public class MemeViewerActivity extends AppCompatActivity {
    //параметр для извлечения данны хиз интента
    public static String FILENAME_EXTRA = "memeFilename";
    //код удаления
    public static int DELETED_MEME_CODE =55;
    //параметр для извлечения тегов
    public static String  FILETAG_EXTRA= "memeFiletag";
    //код запроса данный активности
    public static int REQUEST_CODE =104;

    public static int CHANGE_CODE =38;
    //имя файла из БД
    String filename;
    //сообщение, добавляемое при отправке
    EditText memeSign;

    EditText currentMemeTag;
    //фрагменты, выбираемые в зависимости от типа файла
    ImageFragment imageFragment;
    VideoLocalFragment videoLocalFragment;
    VideoFragment videoFragment;
    GifFragment gifFragment;
    String memeTag="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meme_viewer);
        Intent intent = getIntent();
        //получение имени файла
        filename = intent.getStringExtra(FILENAME_EXTRA);
        memeTag=intent.getStringExtra(FILETAG_EXTRA);
        //ЗАГЛУШКА!!!!
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        //выбор фрагмента в зависимости от типа
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        switch (new FileHelper(this).getType(filename)){
            case FileHelper.IMAGE:
                imageFragment = new ImageFragment();
                imageFragment.setMemeImage(filename);
                fragmentTransaction.add(R.id.memeViewLayout,imageFragment);
                break;
            case FileHelper.HTTPS:
                videoFragment = new VideoFragment();
                videoFragment.setMemeImage(filename);
                fragmentTransaction.add(R.id.memeViewLayout, videoFragment);
                break;
            case FileHelper.VIDEO:
                videoLocalFragment = new VideoLocalFragment();
                videoLocalFragment.setMemeImage(filename);
                fragmentTransaction.add(R.id.memeViewLayout, videoLocalFragment);
                break;
            case FileHelper.GIF:
                gifFragment = new GifFragment();
                gifFragment.setMemeImage(filename);
                fragmentTransaction.add(R.id.memeViewLayout, gifFragment);
                break;
        }

        fragmentTransaction.commit();
        memeSign=findViewById(R.id.memeSign);
        currentMemeTag=findViewById(R.id.currentMemeTag);
        currentMemeTag.setText(memeTag);
        currentMemeTag.setEnabled(false);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.meme_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //РАСШИРИТЬ И ДОБАВИТЬ РЕДАКТИРОВАНИЕ ПОДПИСИ
        if(item.getItemId()==R.id.deleteMem) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setData(Uri.parse(filename));
            setResult(DELETED_MEME_CODE, intent);
            finish();
        }
        if(item.getItemId()==R.id.editMem){
            currentMemeTag.setEnabled(true);
        }

            return false;//super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this,MainActivity.class);
        intent.putExtra(FILENAME_EXTRA,filename);

        String tag=currentMemeTag.getText().toString();
        intent.putExtra(FILETAG_EXTRA,tag);
        setResult(CHANGE_CODE,intent);
        finish();
        super.onBackPressed();
    }

    public void onSendMeme(View view){
        //создание интента отправки в дочерние активности на основании типа файла
        try {
            Uri memeUri =null;
            Intent intent=null;
            intent = new Intent(Intent.ACTION_SEND);
            memeUri= FileProvider.getUriForFile(MemeViewerActivity.this, "com.log28.memesstore", new File(new FileHelper(this).getFullPath(filename)));
                switch (new FileHelper(this).getType(filename)){
                    case FileHelper.IMAGE:
                        intent.setType("image/*");
                        intent.putExtra(Intent.EXTRA_STREAM, memeUri);
                        intent.putExtra(Intent.EXTRA_TEXT, memeSign.getText());
                        break;
                    case FileHelper.VIDEO:
                        intent.setType("video/*");
                        intent.putExtra(Intent.EXTRA_STREAM, memeUri);
                        intent.putExtra(Intent.EXTRA_TEXT, memeSign.getText());
                         break;
                    case FileHelper.HTTPS:
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, "https://www.youtube.com/watch?v="+ filename +" "+memeSign.getText());
                        break;
                    case FileHelper.GIF:
                        memeUri=Uri.fromFile(new File(new FileHelper(this).getFullPath(filename)));
                        intent.setType("image/gif");
                        intent.putExtra(Intent.EXTRA_STREAM, memeUri);
                        intent.putExtra(Intent.EXTRA_TEXT, memeSign.getText());

                }
            startActivity(intent);
}
catch (Exception e){
    e.printStackTrace();
}



    }
}