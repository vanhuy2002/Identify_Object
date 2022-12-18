package com.example.identify_object.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.identify_object.History.HistoryItem;

@Database(entities = {HistoryItem.class}, version = 2)
public abstract class CreateDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "create.db";
    private static CreateDatabase instance;

    public static synchronized CreateDatabase getInstance(Context context) {
        if(instance == null)
            instance = Room.databaseBuilder(context.getApplicationContext(), CreateDatabase.class, DATABASE_NAME)
                    .allowMainThreadQueries().build();
        return instance;
    }

    public abstract CreateItemDAO createItemDAO();
}
