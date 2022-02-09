package com.log28.memesstore;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.WINDOW_SERVICE;

public class MemesListAdapter extends RecyclerView.Adapter<MemesListAdapter.ViewHolder>/* implements View.OnClickListener*/ {
    List<String> memesPaths;
    Context context;
    MemeDatabaseHelper db;
   public MemesListAdapter(Context context,  MemeDatabaseHelper db){
       this.db=db;
       this.context = context;
       Cursor cursor = db.getCursor();
       cursor.moveToFirst();
       //получаем из курсора список имён файлов
       memesPaths = new ArrayList<>();
       int listSize=cursor.getCount();
       for(int i=0;i<listSize;i++){
           String currentFile=cursor.getString(1);
           //еcли  имя файла не имеет расширения (ссылка на веб-ресурс)
           //или сам файл существует на диске, то добавляется в список
           if(!currentFile.contains(".")||new FileHelper(context).isExist(currentFile))
           {
               memesPaths.add(currentFile);
               cursor.moveToNext();
           }
           //если файла нет, то он удаляется из БД
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

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onBindViewHolder(@NonNull MemesListAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        //устанавливаем битмап согласно имени файла
        holder.memeImageView.setImageBitmap(new FileHelper(context).getPreview(memesPaths.get(position)));
        //установка обработчика кликов для вызова активности просмотра
holder.memeCardView.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        Intent intent = new Intent(context,MemeViewerActivity.class);
        intent.putExtra(MemeViewerActivity.FILENAME_EXTRA,memesPaths.get(position));
        ((Activity)context).startActivityForResult(intent,MemeViewerActivity.REQUEST_CODE);
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
        MemesListAdapter memesListAdapter;
         public ViewHolder(@NonNull View itemView) {
            super(itemView);
            memeCardView = itemView.findViewById(R.id.memeCardView);
            memeImageView = itemView.findViewById(R.id.memeImageView);
            //ЗДЕСТ БУДЕТ РЕЖИМ ВЫДЕЛЕНИЯ
            memeCardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {


                    // объект Builder для создания диалогового окна
                  /*  AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    // добавляем различные компоненты в диалоговое окно

                    builder.setMessage("Удалить мем?");
                    // устанавливаем кнопку, которая отвечает за позитивный ответ
                    builder.setPositiveButton("Да",
                            // устанавливаем слушатель
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    // по нажатию создаем всплывающее окно с типом нажатой конпки
                                    memesListAdapter.memesPaths.remove(filename);
                                    memesListAdapter.db.delete(filename);
                                    memesListAdapter.notifyDataSetChanged();
                                }
                            });
                    // объект Builder создал диалоговое окно и оно готово появиться на экране
                    // вызываем этот метод, чтобы показать AlertDialog на экране пользователя
                    builder.show();
*/
                    return true;
                }
            });
            //установка размеров каждого элемента согласно размерам экрана
            LinearLayout linearLayout = itemView.findViewById(R.id.linearLayout);
            ViewGroup.LayoutParams params = linearLayout.getLayoutParams();
            Display display = ((WindowManager) itemView.getContext().getSystemService(WINDOW_SERVICE))
                    .getDefaultDisplay();
            params.width = display.getWidth() / 2;
            params.height = params.width;
            linearLayout.setLayoutParams(params);

        }



    }

}
