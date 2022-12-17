package com.example.identify_object.Database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.identify_object.History.HistoryItem;

import java.util.List;

@Dao
public interface CreateItemDAO {

    @Insert
    void insertItem(HistoryItem historyCreateItem);

    @Query("SELECT * FROM create_item ORDER BY id DESC")
    List<HistoryItem> getListItem();
}
