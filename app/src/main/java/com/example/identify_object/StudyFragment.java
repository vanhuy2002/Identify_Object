package com.example.identify_object;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.identify_object.Adapter.HistoryAdapter;

import java.util.ArrayList;

public class StudyFragment extends Fragment {public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                                                      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_study, container, false);
    return view;
}
}
