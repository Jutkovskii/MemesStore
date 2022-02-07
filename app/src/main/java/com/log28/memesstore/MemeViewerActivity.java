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
        if(path.endsWith(".jpg")||path.endsWith(".png")||path.endsWith(".webp")){
            imageFragment = new ImageFragment();
            imageFragment.setMemeImage(path);
            fragmentTransaction.add(R.id.memeViewLayout,imageFragment);
        }
        if(path.endsWith(".mp4")||path.endsWith(".3gp")) {
            videoLocalFragment = new VideoLocalFragment();
            videoLocalFragment.setMemeImage(path);
            fragmentTransaction.add(R.id.memeViewLayout, videoLocalFragment);
        }
        if(path.startsWith("http")){
            videoFragment = new VideoFragment();
            videoFragment.setMemeImage(path);
            fragmentTransaction.add(R.id.memeViewLayout, videoFragment);
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
        setResult(0,intent);
        finish();
        //Support.db.delete(path);

        return false;//super.onOptionsItemSelected(item);
    }

    public void onSendMeme(View view){

        try {
            if(path.startsWith("http")){
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                //intent.putExtra(Intent.EXTRA_TEXT,"Если ты это читаешь, значит у меня получилось");
                intent.putExtra(Intent.EXTRA_TEXT, path + "\n\n "+memeSign.getText() );
                startActivity(intent);
            }
                else
            {
                Uri memeUri = FileProvider.getUriForFile(MemeViewerActivity.this, "com.log28.memesstore", new File(path));
                //Uri memeUri = Uri.parse("file://"+path);

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, memeUri);
                //intent.putExtra(Intent.EXTRA_TEXT,"Если ты это читаешь, значит у меня получилось");
                intent.putExtra(Intent.EXTRA_TEXT, memeSign.getText());
                startActivity(intent);
            }

}
catch (Exception e){
    e.printStackTrace();
}



    }
}