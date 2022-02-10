package com.log28.memesstore;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MemeListFragment extends Fragment {
    //объект списка
    RecyclerView memesList;
    //объект БД
    MemeDatabaseHelper testdb=null;
    //адаптер для заполнения списка
    MemesListAdapter memesListAdapter;

    //объект для работы с памятью
    FileHelper fileHelper;

    static final int IMAGE = 0;
    static final int VIDEO = 1;

    public MemeListFragment() {

    }

    public MemeListFragment(MemeDatabaseHelper testdb) {
        this.testdb=testdb;


    }
public void setDB(MemeDatabaseHelper testdb) {
    this.testdb=testdb;

}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_meme_list, container, false);
    }
View view;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

this.view=view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(testdb!=null) {
            fileHelper = new FileHelper(view.getContext());
            memesList = view.findViewById(R.id.memesList);
            memesList.setLayoutManager(new GridLayoutManager(view.getContext(), 2));

            testdb.getWritableDatabase();
            memesListAdapter = new MemesListAdapter(view.getContext(), testdb);
            memesList.setAdapter(memesListAdapter);
            memesListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(testdb!=null)
            testdb.close();
    }
}