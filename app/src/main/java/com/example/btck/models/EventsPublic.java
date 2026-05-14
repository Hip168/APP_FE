package com.example.btck.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class EventsPublic {
    @SerializedName("data")
    public List<EventPublic> data;
    @SerializedName("count")
    public int count;
}
