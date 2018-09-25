package com.example.classifieds.Models;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


@IgnoreExtraProperties
public class HitsList {

    @SerializedName("hits")
    @Expose
    private List<PostSource> postSources;

    public List<PostSource> getPostSources() {
        return postSources;
    }

    public void setPostSources(List<PostSource> postSources) {
        this.postSources = postSources;
    }
}
