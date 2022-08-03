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

import java.util.ArrayList;

public class MemeListFragment extends Fragment {
    //объект списка
    RecyclerView memesList;
    //объект БД
    MemeDatabaseHelper testdb = null;
    //адаптер для заполнения списка
   public MemesListAdapter memesListAdapter;
    //объект для работы с памятью
    ArrayList<MemeObject> currentMemesList=new ArrayList<>();
    View view;
    public MemeListFragment(ArrayList<MemeObject> currentMemesList) {
        this.currentMemesList = currentMemesList;
    }
    public MemeListFragment(MemeDatabaseHelper testdb) {
        this.testdb = testdb;
   }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_meme_list, container, false);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        if (testdb != null||currentMemesList!=null) {
            memesList = view.findViewById(R.id.memesList);
            memesList.setLayoutManager(new GridLayoutManager(view.getContext(), 2));
        }
        changeFragment();

    }
String filterText="";


    public void changeFragment(){
       // memesListAdapter = new MemesListAdapter(view.getContext(), testdb);
        memesListAdapter = new MemesListAdapter(view.getContext(), currentMemesList);
        memesListAdapter.setFilter(filterText);
        memesList.setAdapter(memesListAdapter);
        memesListAdapter.notifyDataSetChanged();
    }

    public void add()
    {
        memesListAdapter.add();
    }
    public void setFilter(String newText){
        filterText=newText;
    }

}