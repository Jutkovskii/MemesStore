package com.log28.memesstore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class MemeViewerActivity extends AppCompatActivity {

    //имя файла из БД
    String filename;
    //сообщение, добавляемое при отправке
    EditText memeSign;

    EditText currentMemeTag;

    String memeTag = "";

    String mimeType;
    Toolbar toolbar;
    Uri memeUri;

    boolean isImage=false;
    boolean isEdited=false;
    FragmentTransaction fragmentTransaction;

    ImageEditorFragment imageEditorFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        savedInstanceState = null;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meme_viewer);
        toolbar = findViewById(R.id.currentMemetoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("МЕМОХРАНИЛИЩЕ");
        Intent intent = getIntent();
        //получение имени файла
        MemeObject memeObject = intent.getParcelableExtra(MemeObject.memeObjectParcelTag);
        filename = memeObject.getMemeRelativePath();
        memeTag = memeObject.getTag();
        mimeType = memeObject.getMemeMimeType();
        memeUri = memeObject.getMemeUri();
        //выбор фрагмента в зависимости от типа
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.memeViewLayout,getFragment(filename, memeUri));
        fragmentTransaction.commit();

        memeSign = findViewById(R.id.memeSign);
        currentMemeTag = findViewById(R.id.currentMemeTag);
        currentMemeTag.setText(memeTag);
        currentMemeTag.setEnabled(false);
        currentMemeTag.setBackgroundColor(Color.argb(200,128,128,128));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.meme_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //РАСШИРИТЬ И ДОБАВИТЬ РЕДАКТИРОВАНИЕ ПОДПИСИ
        if (item.getItemId() == R.id.deleteMem) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setData(Uri.parse(filename));
            setResult(MemeUtils.DELETED_MEME_CODE, intent);
            finish();
        }
        if (item.getItemId() == R.id.editMem) {
            currentMemeTag.setEnabled(true);
            currentMemeTag.setBackgroundColor(Color.argb(255,255,255,255));
            if(isImage){
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                imageEditorFragment = new ImageEditorFragment(layoutIDs[4], filename);
                fragmentTransaction.replace(R.id.memeViewLayout,imageEditorFragment);
                fragmentTransaction.commit();
                isEdited=true;
            }

        }

        return false;//super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MemeUtils.FILENAME_EXTRA, filename);

        String tag = currentMemeTag.getText().toString();

        intent.putExtra(MemeUtils.FILETAG_EXTRA, tag);
        if (currentMemeTag.isEnabled())
            setResult(MemeUtils.CHANGE_CODE, intent);
        finish();
        super.onBackPressed();
    }

    public void onSendMeme(View view) {
        //создание интента отправки в дочерние активности на основании типа файла
        try {
            String extraMressage = "";

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(mimeType);
            if (memeUri == null) {

                extraMressage = "https://www.youtube.com/watch?v=" + filename + " ";
            } else
                if(isEdited) {
                    intent.putExtra(Intent.EXTRA_STREAM,  imageEditorFragment.save());
                }
                    else
                intent.putExtra(Intent.EXTRA_STREAM, memeUri);
            String finalMessage = extraMressage + memeSign.getText();
            if (MemeUtils.isDefaultSingEnabled)
                finalMessage = finalMessage + "\n\n" + MemeUtils.defaultSign;
            intent.putExtra(Intent.EXTRA_TEXT, finalMessage);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    static int layoutIDs[] = {R.layout.fragment_image, R.layout.fragment_gif, R.layout.fragment_video, R.layout.fragment_video_local,R.layout.fragment_image_editor};

    public Fragment getFragment(String relativeFilepath, Uri memeUri) {
        switch (FileClassifier.classfyByName(relativeFilepath)) {
            case FileClassifier.IMAGE:
                isImage=true;
                 return new ImageFragment(layoutIDs[0], relativeFilepath);

                //return new ImageEditorFragment(layoutIDs[0], relativeFilepath);
            case FileClassifier.GIF:
                return new GifFragment(layoutIDs[1], relativeFilepath);
            case FileClassifier.HTTPS:
                return new VideoFragment(layoutIDs[2], relativeFilepath);
            case FileClassifier.VIDEO:
                return new VideoLocalFragment(layoutIDs[3], relativeFilepath);
        }
        return null;
    }

}