package com.log28.memesstore;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class LocalExceptionHandler implements Thread.UncaughtExceptionHandler {

    Thread.UncaughtExceptionHandler mainHandler;
    Context context;
    private static boolean exceptionGot=true;
    public  LocalExceptionHandler(Context context){
        mainHandler=Thread.getDefaultUncaughtExceptionHandler();
        this.context=context;
    }
    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
if(exceptionGot) {
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("plain/text");
    intent.putExtra(Intent.EXTRA_TEXT, e.getMessage());
    intent = Intent.createChooser(intent, "Отправить сообщение об ошибке");
    Log.d("GSOM",e.toString());
    context.startActivity(intent);
    exceptionGot=false;
}

if(mainHandler!=null){

            mainHandler.uncaughtException(t,e);


        }

    }
}
