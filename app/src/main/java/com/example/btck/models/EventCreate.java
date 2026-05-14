package com.example.btck.models;

import com.google.gson.annotations.SerializedName;

public class EventCreate {
    @SerializedName("name")
    public String name;
    @SerializedName("description")
    public String description;

    public EventCreate(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
