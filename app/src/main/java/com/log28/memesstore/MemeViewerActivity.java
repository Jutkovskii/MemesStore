package com.log28.memesstore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;

public class MemeViewerActivity extends AppCompatActivity {

    String path;
    EditText memeSign;
    ImageFragment imageFragment;
    VideoLocalFragment videoLocalFragment;
    VideoFragment videoFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meme_viewer);
        Intent intent = getIntent();
        path = intent.getStringExtra("path");

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);


        //Вот сюда можно FILEHELPER
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        switch (new FileHelper(this).getType(path)){
            case FileHelper.IMAGE:
                imageFragment = new ImageFragment();
                imageFragment.setMemeImage(path);
                fragmentTransaction.add(R.id.memeViewLayout,imageFragment);
                break;
            case FileHelper.HTTPS:
                videoFragment = new VideoFragment();
                videoFragment.setMemeImage(path);
                fragmentTransaction.add(R.id.memeViewLayout, videoFragment);
                break;

            case FileHelper.VIDEO:
                videoLocalFragment = new VideoLocalFragment();
                videoLocalFragment.setMemeImage(path);
                fragmentTransaction.add(R.id.memeViewLayout, videoLocalFragment);
                break;
        }

        fragmentTransaction.commit();


        memeSign=findViewById(R.id.memeSign);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.meme_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        Intent intent = new Intent(this,MainActivity.class);
        intent.setData(Uri.parse(path));
        setResult(15,intent);
        finish();
        //Support.db.delete(path);

        return false;//super.onOptionsItemSelected(item);
    }

    public void onSendMeme(View view){

        try {
/*
            if(path.startsWith("http")){
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                //intent.putExtra(Intent.EXTRA_TEXT,"Если ты это читаешь, значит у меня получилось");
                intent.putExtra(Intent.EXTRA_TEXT, new FileHelper(this).getFullPath(path) + "\n\n "+memeSign.getText() );
                startActivity(intent);
            }
                else
            {
                Uri memeUri = FileProvider.getUriForFile(MemeViewerActivity.this, "com.log28.memesstore", new File(new FileHelper(this).getFullPath(path)));
                //Uri memeUri = Uri.parse("file://"+path);

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, memeUri);
                //intent.putExtra(Intent.EXTRA_TEXT,"Если ты это читаешь, значит у меня получилось");
                intent.putExtra(Intent.EXTRA_TEXT, memeSign.getText());
                startActivity(intent);
            }
*/
            Uri memeUri =null;
            Intent intent=null;
                switch (new FileHelper(this).getType(path)){
                    case FileHelper.IMAGE:
                         memeUri= FileProvider.getUriForFile(MemeViewerActivity.this, "com.log28.memesstore", new File(new FileHelper(this).getFullPath(path)));
                        //Uri memeUri = Uri.parse("file://"+path);

                         intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("image/*");
                        intent.putExtra(Intent.EXTRA_STREAM, memeUri);
                        //intent.putExtra(Intent.EXTRA_TEXT,"Если ты это читаешь, значит у меня получилось");
                        intent.putExtra(Intent.EXTRA_TEXT, memeSign.getText());
                        startActivity(intent);
                        break;
                    case FileHelper.VIDEO:
                        memeUri = FileProvider.getUriForFile(MemeViewerActivity.this, "com.log28.memesstore", new File(new FileHelper(this).getFullPath(path)));
                        //Uri memeUri = Uri.parse("file://"+path);

                        intent= new Intent(Intent.ACTION_SEND);
                        intent.setType("video/*");
                        intent.putExtra(Intent.EXTRA_STREAM, memeUri);
                        //intent.putExtra(Intent.EXTRA_TEXT,"Если ты это читаешь, значит у меня получилось");
                        intent.putExtra(Intent.EXTRA_TEXT, memeSign.getText());
                        startActivity(intent);
                        break;
                    case FileHelper.HTTPS:
                         intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        //intent.putExtra(Intent.EXTRA_TEXT,"Если ты это читаешь, значит у меня получилось");
                        intent.putExtra(Intent.EXTRA_TEXT, "https://www.youtube.com/watch?v="+path +" "+memeSign.getText());
                        startActivity(intent);
                        break;
                }

}
catch (Exception e){
    e.printStackTrace();
}



    }
}