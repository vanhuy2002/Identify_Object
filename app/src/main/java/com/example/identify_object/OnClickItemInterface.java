package com.example.identify_object;

import com.example.identify_object.History.HistoryItem;

public interface OnClickItemInterface {
    boolean itemClick(HistoryItem model);
    void deleteItem(HistoryItem model);
}
