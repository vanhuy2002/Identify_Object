package com.example.identify_object;

import android.widget.ImageView;

import com.example.identify_object.History.HistoryItem;

public interface OnClickItemInterface {
    boolean itemClick(HistoryItem model, ImageView img);
    void deleteItem(HistoryItem model);
}
