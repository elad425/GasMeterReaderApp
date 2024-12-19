package com.example.gasmeterreader.api;

import static com.example.gasmeterreader.utils.EntityUtils.assignReadsToBuildings;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.gasmeterreader.database.BuildingDao;
import com.example.gasmeterreader.R;
import com.example.gasmeterreader.entities.Read;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Api {
    private final BuildingDao dao;
    Retrofit retrofit;
    WebServiceApi webServiceApi;

    public Api(BuildingDao dao, Context context) {
        this.dao = dao;
        retrofit = new Retrofit.Builder().baseUrl(context.getString(R.string.baseUrl))
                .addConverterFactory(GsonConverterFactory.create()).build();
        webServiceApi = retrofit.create(WebServiceApi.class);
    }

    public void getReads() {
        Call<List<Read>> call = webServiceApi.getReads();
        call.enqueue(new Callback<List<Read>>() {
            @Override
            public void onResponse(@NonNull Call<List<Read>> call, @NonNull Response<List<Read>> response) {
                assert response.body() != null;
                dao.insertList(assignReadsToBuildings(response.body()));
            }
            @Override
            public void onFailure(@NonNull Call<List<Read>> call, @NonNull Throwable t) {
            }
        });
    }

    public void updateRead(int readId, Read updatedread) {
        Call<Void> call = webServiceApi.updateRead(readId,updatedread);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
            }
        });
    }

}
