package com.example.identify_object.History;

import android.net.Uri;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName = "create_item")

public class HistoryItem {
    @PrimaryKey(autoGenerate = true)
    int id;
    @ColumnInfo(name = "name_object")
    String name;
    @ColumnInfo(name = "image_result")
    String imageResult;

    public HistoryItem(String name, String imageResult) {
        this.name = name;
        this.imageResult = imageResult;
    }

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
    public String getImageResult() {
        return imageResult;
    }

    public void setImageResult(String imageResult) {
        this.imageResult = imageResult;
    }


}
