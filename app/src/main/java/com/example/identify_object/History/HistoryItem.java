package com.example.identify_object.History;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName = "create_item")

public class HistoryItem {
    @PrimaryKey(autoGenerate = true)
    int id;
    @ColumnInfo(name = "content_create")
    String name;
    @ColumnInfo(name = "result_create")
    String result;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
