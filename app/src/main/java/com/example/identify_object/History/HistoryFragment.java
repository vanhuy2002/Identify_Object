package com.example.identify_object.History;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.identify_object.Adapter.HistoryAdapter;
import com.example.identify_object.Database.CreateDatabase;
import com.example.identify_object.Database.CreateItemDAO;
import com.example.identify_object.OnClickItemInterface;
import com.example.identify_object.R;
import com.example.identify_object.ResultActivity;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {
    RecyclerView recyclerView;
    HistoryAdapter adapter;
    List<HistoryItem> createList;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        adapter = new HistoryAdapter(getContext(), new OnClickItemInterface() {
            @Override
            public boolean itemClick(HistoryItem model) {
                Intent intent = new Intent(getContext(), ResultActivity.class);

                return true;
            }
        });

        createList = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        loadData();
        return view;
    }

    private void loadData() {
        createList = CreateDatabase.getInstance(getContext()).createItemDAO().getListItem();
        adapter.setData(createList);
    }

}
