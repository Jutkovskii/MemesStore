package com.log28.memesstore;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MemeListFragment extends Fragment {
    //объект списка
    RecyclerView memesList;
    //объект БД
    MemeDatabaseHelper testdb = null;
    //адаптер для заполнения списка
    MemesListAdapter memesListAdapter;
    //объект для работы с памятью


    View view;


    public MemeListFragment() {
    }



    public MemeListFragment(MemeDatabaseHelper testdb) {
        this.testdb = testdb;

        Log.d("OLOLOG","Фрагмент Конструктор для "+testdb.name );

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("OLOLOG","Фрагмент Созадние для "+ testdb.name );

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("OLOLOG","Фрагмент Создать view для  "+ testdb.name  );
        // Inflate the layout for this fragment
        this.view = inflater.inflate(R.layout.fragment_meme_list, container, false);

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        Log.d("OLOLOG","Фрагмент Создан view  для "+ testdb.name  );
        if (testdb != null) {
            Log.d("OLOLOG","База данных "+ testdb.name );
            // fileHelper = new FileHelper(view.getContext());
            memesList = view.findViewById(R.id.memesList);
            memesList.setLayoutManager(new GridLayoutManager(view.getContext(), 2));

        }
    }
String filterText="";
    public void setFilter(String newText){
        filterText=newText;
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.d("OLOLOG","Фрагмент Восстановить "+ testdb.name  );

     /*   if (testdb != null) {
            Log.d("OLOLOG","База данных "+ testdb.name );
           // fileHelper = new FileHelper(view.getContext());
            memesList = view.findViewById(R.id.memesList);
            memesList.setLayoutManager(new GridLayoutManager(view.getContext(), 2));

            //testdb.getWritableDatabase();
            memesListAdapter = new MemesListAdapter(view.getContext(), testdb);
            memesList.setAdapter(memesListAdapter);
            memesListAdapter.notifyDataSetChanged();
        }*/
        memesListAdapter = new MemesListAdapter(view.getContext(), testdb);
        memesListAdapter.setFilter(filterText);
        memesList.setAdapter(memesListAdapter);
        memesListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("OLOLOG","Фрагмент Остановить "+ testdb.name  );
        /*if (testdb != null)
            testdb.close();*/
    }



    @Override
    public void onStart() {
        super.onStart();
        Log.d("OLOLOG","Фрагмент Запустить "+ testdb.name  );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("OLOLOG","Фрагмент Уничтожить "+ testdb.name  );
    }


}