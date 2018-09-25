package com.example.classifieds.Models;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@IgnoreExtraProperties
public class Hits {

    @SerializedName("hits")
    @Expose

    private HitsList hitsList;

    public HitsList getHitsList() {
        return hitsList;
    }

    public void setHitsList(HitsList hitsList) {
        this.hitsList = hitsList;
    }
}
