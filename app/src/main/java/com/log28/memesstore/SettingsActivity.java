package com.log28.memesstore;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity {

    Switch switchSign;
    Dialog devDialog;
    EditText defaultSign;
    String defaultString;
    boolean isSwitchEnabled;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        switchSign = findViewById(R.id.switchSign);
        defaultSign=findViewById(R.id.defaultSign);



    }

    @Override
    protected void onResume() {
        super.onResume();
        defaultSign.setText(MainActivity.defaultSign);
        switchSign.setChecked(MainActivity.isDefaultSingEnabled);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MainActivity.defaultSign=defaultSign.getText().toString();
        MainActivity.isDefaultSingEnabled=switchSign.isChecked();
    }

    public void onGetAppInfo(View v){
        devDialog = new Dialog(SettingsActivity.this);
        devDialog.setTitle("О программе");
        devDialog.setContentView(R.layout.dev_dialog);
        devDialog.show();
    }
    public void onUseDefaultSign(View v){

        if(switchSign.isChecked())
            MainActivity.defaultSign=defaultString;
        else
            MainActivity.defaultSign="";
        isSwitchEnabled=switchSign.isChecked();
    }

    public void onClickImport(View v){
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(chooseFile, MainActivity.REQUEST_DB);
    }
    public void onClickExport(View v){
        Intent chooseFile = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        chooseFile.setType("application/zip");
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(chooseFile, MainActivity.REQUEST_EXDB);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent intent = data;
        setResult(requestCode, intent);
        finish();
    }


}