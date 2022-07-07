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
import java.util.concurrent.Executors;

import static android.content.Context.WINDOW_SERVICE;
import static com.log28.memesstore.MainActivity.mainMenu;

public class MemesListAdapter extends RecyclerView.Adapter<MemesListAdapter.ViewHolder> implements Filterable /* implements View.OnClickListener*/ {
   /* List<String> memesPaths;
    List<String> memesTags;
    List<String> filteredTags;*/
    Context context;
    MemeDatabaseHelper db;
    public boolean deletingMode=false;


    public List<MemeObject> memeObjects;
    public List<Integer> selected;
    //public List<MemeGroup> memeGroups;
    //List<MemeGroup> filteredGroups;

    List<String> filteredMemes;
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
  /*  memeGroups=new ArrayList<>();
    filteredGroups=new ArrayList<>()*/;
    memeObjects=new ArrayList<>();
    filteredMemes=new ArrayList<>();

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
            //memeGroups.add(new MemeGroup(currentFile,currentTag));
            //создание списка мемов
            memeObjects.add(new MemeObject(this,currentFile,currentTag));
            filteredMemes.add(currentTag);
            cursor.moveToNext();
        }
        //если файла нет, то он удаляется из БД
        else
            this.db.delete(currentFile);
    }
    //filteredGroups = memeGroups;
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
       /* holder.memeImageView.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.raw.logo));
        MyAsyncTask myAsyncTask=new MyAsyncTask(holder);
        myAsyncTask.executeOnExecutor( Executors.newSingleThreadExecutor(),filteredGroups.get(position).getName());
        holder.memeTag.setText(filteredGroups.get(position).getTag());
*/

        //РАСКОММЕНТИТЬ
        //String qwe =memeObjects.get(position).getName();
        if(filteredMemes.get(position)==memeObjects.get(position).getTag()){
            holder.memeImageView.setImageBitmap(memeObjects.get(position).getBitmap());
            holder.memeTag.setText(memeObjects.get(position).getTag());
           // redrawList();
        }




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
       intent.putExtra(MemeViewerActivity.FILENAME_EXTRA,memeObjects.get(position).getName());
        intent.putExtra(MemeViewerActivity.FILETAG_EXTRA,memeObjects.get(position).getTag());
           /*  intent.putExtra(MemeViewerActivity.FILENAME_EXTRA, memeGroups.get(position).getName());
            intent.putExtra(MemeViewerActivity.FILETAG_EXTRA, memeGroups.get(position).getTag());*/
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

                return true;
            }
        });


    }



    public void redrawList()
    {
        this.notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
       return filteredMemes.size();
        //  return filteredGroups.size();
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

                filteredMemes=new ArrayList<>();

                for(MemeObject memeObject:memeObjects){
                    if(charString.isEmpty()||memeObject.getTag().toLowerCase().contains(charString.toLowerCase()))
                        filteredMemes.add(memeObject.getTag());
                }


                //////////////////////////////////
                /*if (charString.isEmpty()) {
                    List<String> tempfilter=new ArrayList<>();
                    for(MemeObject memeObject:memeObjects)
                        tempfilter.add(memeObject.getTag());
                    filteredMemes=tempfilter;
                } else {
                    filteredGroups = new ArrayList<>();

                    for(MemeGroup currentMeme: memeGroups)
                        if (currentMeme.getTag().toLowerCase().contains(charString.toLowerCase())) {
                            filteredGroups.add(currentMeme);

                        }
                    }*/
                //filteredTags = memesTags;


                FilterResults filterResults = new FilterResults();
                //filterResults.values = filteredGroups;
                filterResults.values=filteredMemes;
                return filterResults;

            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                //filteredGroups = (ArrayList<MemeGroup>) filterResults.values;
                filteredMemes = (ArrayList<String>) filterResults.values;
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
