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

    public Uri getImageResult() {
        return imageResult;
    }

    public void setImageResult(Uri imageResult) {
        this.imageResult = imageResult;
    }

    @ColumnInfo(name = "image_result")
    Uri imageResult;

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

}
