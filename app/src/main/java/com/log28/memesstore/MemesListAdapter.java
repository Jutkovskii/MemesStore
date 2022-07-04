package com.log28.memesstore;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.WINDOW_SERVICE;
import static com.log28.memesstore.MainActivity.mainMenu;

public class MemesListAdapter extends RecyclerView.Adapter<MemesListAdapter.ViewHolder> implements Filterable /* implements View.OnClickListener*/ {
   /* List<String> memesPaths;
    List<String> memesTags;
    List<String> filteredTags;*/
    Context context;
    MemeDatabaseHelper db;
    public boolean deletingMode=false;
MenuItem deleteItem;

    public List<Integer> selected;
    public List<MemeGroup> memeGroups;
    List<MemeGroup> filteredGroups;
   public MemesListAdapter(Context context,  MemeDatabaseHelper db){
       Log.d("OLOLOG","Адаптер созадние "+db.name );
       this.db=db;
       this.context = context;
       getDB();
   }
public void getDB(){
    Cursor cursor = db.getCursor();
    cursor.moveToFirst();
    //получаем из курсора список имён файлов
    // memesPaths = new ArrayList<>();
    // memesTags=new ArrayList<>();
    selected=new ArrayList<>();
    memeGroups=new ArrayList<>();
    filteredGroups=new ArrayList<>();


    int listSize=cursor.getCount();
    for(int i=0;i<listSize;i++){

        String currentFile=cursor.getString(1);
        String currentTag=cursor.getString(2);


        //еcли  имя файла не имеет расширения (ссылка на веб-ресурс)
        //или сам файл существует на диске, то добавляется в список
        if(!currentFile.contains(".")||new FileHelper(context).isExist(currentFile))
        {
            //memesPaths.add(currentFile);
            //memesTags.add(currentTag);
            memeGroups.add(new MemeGroup(currentFile,currentTag));

            cursor.moveToNext();
        }
        //если файла нет, то он удаляется из БД
        else
            this.db.delete(currentFile);
    }
    filteredGroups = memeGroups;
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
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        MyAsyncTask myAsyncTask=new MyAsyncTask(holder);
       // myAsyncTask.execute(filteredGroups.get(position).getName());
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onBindViewHolder(@NonNull MemesListAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {


        //устанавливаем битмап согласно имени файла
       /* holder.memeImageView.setImageBitmap(new FileHelper(context).getPreview(memesPaths.get(pos)));
        holder.memeTag.setText(memesTags.get(pos));*/
        if(deletingMode)
            holder.deleteCheck.setVisibility(CheckBox.VISIBLE);
        else
            holder.deleteCheck.setVisibility(CheckBox.INVISIBLE);
        if (selected.contains(position))
        holder.deleteCheck.setChecked(true);
        else
        holder.deleteCheck.setChecked(false);
       // holder.memeImageView.setImageBitmap(new FileHelper(context).getPreview(filteredGroups.get(position).getName()));
        holder.memeImageView.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.raw.logo));
        MyAsyncTask myAsyncTask=new MyAsyncTask(holder);
        myAsyncTask.execute(filteredGroups.get(position).getName());
        holder.memeTag.setText(filteredGroups.get(position).getTag());


        //установка обработчика кликов для вызова активности просмотра
holder.memeCardView.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        if(deletingMode)
        {
            holder.deleteCheck.setChecked(!holder.deleteCheck.isChecked());
            if(selected.contains(position))
                selected.remove(selected.indexOf(position));
            else
                selected.add(position);

            if(selected.isEmpty()) {
                deletingMode = false;
                MainActivity.deletingMode=false;
                //MainActivity.menu1.removeItem(deleteItem.getItemId());
                //menu1=MainActivity.mainMenu;
                mainMenu.getItem(3).setVisible(false);
                mainMenu.getItem(2).setVisible(true);
                mainMenu.getItem(1).setVisible(true);
                redrawList();
            }
        }
        else
        {
            Intent intent = new Intent(context, MemeViewerActivity.class);
        /*intent.putExtra(MemeViewerActivity.FILENAME_EXTRA,memesPaths.get(position));
        intent.putExtra(MemeViewerActivity.FILETAG_EXTRA,memesTags.get(position));*/
            intent.putExtra(MemeViewerActivity.FILENAME_EXTRA, memeGroups.get(position).getName());
            intent.putExtra(MemeViewerActivity.FILETAG_EXTRA, memeGroups.get(position).getTag());
            ((Activity) context).startActivityForResult(intent, MemeViewerActivity.REQUEST_CODE);
        }

    }
});
        //ЗДЕСТ БУДЕТ РЕЖИМ ВЫДЕЛЕНИЯ
        holder.memeCardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(!deletingMode)
                {holder.deleteCheck.setChecked(!holder.deleteCheck.isChecked());
                    if(selected.contains(position))
                        selected.remove(position);
                    else
                        selected.add(position);
                deletingMode=true;
                redrawList();
                MainActivity.deletingMode=true;
                  /*  deleteItem= MainActivity.menu1.add("Удалить");
                    deleteItem.setShowAsAction(1);*/
                    //MainActivity.menu1=MainActivity.deleteMenu;
                    try {
                        MenuItem qwe = mainMenu.getItem(3);
                        qwe.setVisible(true);
                        mainMenu.getItem(2).setVisible(false);
                        mainMenu.getItem(1).setVisible(false);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
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


    }






class MyAsyncTask extends AsyncTask<String,Void, Bitmap>{
    MemesListAdapter.ViewHolder holder;
    MyAsyncTask(MemesListAdapter.ViewHolder holder)
    {
        this.holder=holder;
    }
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected Bitmap doInBackground(String... strings) {
        return new FileHelper(context).getPreview(strings[0]);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        holder.memeImageView.setImageBitmap(bitmap);
    }
}




    public void redrawList()
    {
        this.notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
       // return memesPaths.size();
        return filteredGroups.size();
    }

    public void setFilter(String newText){
        getFilter().filter(newText);
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    filteredGroups = memeGroups;
                } else {
                    filteredGroups = new ArrayList<>();
                    for(MemeGroup currentMeme: memeGroups)
                        if (currentMeme.getTag().toLowerCase().contains(charString.toLowerCase())) {
                            filteredGroups.add(currentMeme);
                        }
                    }
                //filteredTags = memesTags;


                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredGroups;
                return filterResults;

            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredGroups = (ArrayList<MemeGroup>) filterResults.values;

                notifyDataSetChanged();
            }
        };
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView memeImageView;
        public CardView memeCardView;
        public TextView memeTag;
        public CheckBox deleteCheck;
        MemesListAdapter memesListAdapter;
         public ViewHolder(@NonNull View itemView) {
            super(itemView);
            memeCardView = itemView.findViewById(R.id.memeCardView);
            memeImageView = itemView.findViewById(R.id.memeImageView);
            memeTag=itemView.findViewById(R.id.memeTag);
            deleteCheck=itemView.findViewById(R.id.deleteCheck);
            deleteCheck.setVisibility(CheckBox.INVISIBLE);
            deleteCheck.setClickable(false);

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
