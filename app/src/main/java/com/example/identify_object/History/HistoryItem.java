package com.example.identify_object.History;

public class HistoryItem {
    String id;
    String name;

    public HistoryItem(String name) {
        id = String.valueOf(System.currentTimeMillis());
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
