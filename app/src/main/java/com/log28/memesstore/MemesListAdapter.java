package com.log28.memesstore;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.WINDOW_SERVICE;

public class MemesListAdapter extends RecyclerView.Adapter<MemesListAdapter.ViewHolder>/* implements View.OnClickListener*/ {
   public ViewHolder viewHolder;
    List<String> memesPaths;
    Context context;
    MemeDatabaseHelper db;
   public MemesListAdapter(Context context,  MemeDatabaseHelper db){
       this.db=db;
       this.context = context;
       Cursor cursor = db.getCursor();
       cursor.moveToFirst();
       memesPaths = new ArrayList<>();
       int listSize=cursor.getCount();
       for(int i=0;i<listSize;i++){
           String currentFile=cursor.getString(1);
           if(!currentFile.contains("."))
           {
               memesPaths.add(currentFile);
               cursor.moveToNext();
           }
           else
           if(new FileHelper(context).isExist(currentFile)) {
               memesPaths.add(currentFile);
               cursor.moveToNext();
           }
           else
              this.db.delete(currentFile);
       }
   }


    @NonNull
    @Override
    public MemesListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.meme_card_view, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        vh.memesListAdapter=this;
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MemesListAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

            holder.setMeme(memesPaths.get(position));

holder.memeCardView.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        String memePath = memesPaths.get(position);
        Intent intent = new Intent(context,MemeViewerActivity.class);
      /*  if(!memePath.contains("."))
            memePath="https://www.youtube.com/watch?v="+memePath;*/
        intent.putExtra("path",memePath);
        ((Activity)context).startActivityForResult(intent,2);
        //context.startActivity(intent);
    }
});

    }

    @Override
    public int getItemCount() {
        return memesPaths.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView memeImageView;
        public CardView memeCardView;
        public String memePath;
        MemesListAdapter memesListAdapter;
         public ViewHolder(@NonNull View itemView) {
            super(itemView);
            memeCardView = itemView.findViewById(R.id.memeCardView);
            memeImageView = itemView.findViewById(R.id.memeImageView);
           /* memeCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(view.getContext(),MemeViewerActivity.class);
                    if(!memePath.contains("."))
                        memePath="https://www.youtube.com/watch?v="+memePath;
                    intent.putExtra("path",memePath);
                    view.getContext().startActivity(intent);

                }
            });*/
            memeCardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {


                    // объект Builder для создания диалогового окна
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    // добавляем различные компоненты в диалоговое окно

                    builder.setMessage("Удалить мем?");
                    // устанавливаем кнопку, которая отвечает за позитивный ответ
                    builder.setPositiveButton("Да",
                            // устанавливаем слушатель
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    // по нажатию создаем всплывающее окно с типом нажатой конпки
                                    memesListAdapter.memesPaths.remove(memePath);
                                    memesListAdapter.db.delete(memePath);
                                    memesListAdapter.notifyDataSetChanged();
                                }
                            });
                    // объект Builder создал диалоговое окно и оно готово появиться на экране
                    // вызываем этот метод, чтобы показать AlertDialog на экране пользователя
                    builder.show();







                   /* memesListAdapter.memesPaths.remove(memePath);
                    memesListAdapter.db.delete(memePath);
                    memesListAdapter.notifyDataSetChanged();*/
                    return true;
                }
            });

            LinearLayout linearLayout = itemView.findViewById(R.id.linearLayout);
            ViewGroup.LayoutParams params = linearLayout.getLayoutParams();
            Display display = ((WindowManager) itemView.getContext().getSystemService(WINDOW_SERVICE))
                    .getDefaultDisplay();
            params.width = display.getWidth() / 2;
            params.height = params.width;// display.getHeight()/2;
            linearLayout.setLayoutParams(params);

        }

        void setMeme(String memePath) {
            this.memePath = memePath;

            Bitmap bmp = null;
            //ВОЗМОЖНО УДАЛИТЬ
            if (memePath.endsWith(".mp4")) {
                bmp = ThumbnailUtils.createVideoThumbnail(memePath,
                        MediaStore.Video.Thumbnails.MINI_KIND);
                memeImageView.setImageBitmap(bmp);
            }
            if (!memePath.contains(".")) {
                String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/MemesStore2/Previews/";
                bmp = BitmapFactory.decodeFile(rootDir + memePath + ".jpg");
                if (bmp == null)
                    memesListAdapter.db.delete(memePath);
                memeImageView.setImageBitmap(bmp);

            }
            if (memePath.endsWith(".jpg") || memePath.endsWith(".png")) {
                bmp = BitmapFactory.decodeFile(memePath);
            }


 FileHelper fileHelper=new FileHelper(memesListAdapter.context);

                bmp=fileHelper.getPreview(memePath);
                memeImageView.setImageBitmap(bmp);

        }

    }

}
