package com.example.gasmeterreader.api;

import com.example.gasmeterreader.entities.Read;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface WebServiceApi {
    @GET("/")
    Call<List<Read>> getReads();

    @POST("/{id}")
    Call<Void> updateRead(@Path("id") int id ,@Body Read read);
}
