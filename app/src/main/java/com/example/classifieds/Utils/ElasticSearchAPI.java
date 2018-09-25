package com.example.classifieds.Utils;

import com.example.classifieds.Models.Hits;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Query;

public interface ElasticSearchAPI {

    @GET("_search/")
    Call<Hits> search(
            @HeaderMap Map<String, String> headers,
            @Query("default_operator") String operator,
            @Query("q") String query
    );

}
