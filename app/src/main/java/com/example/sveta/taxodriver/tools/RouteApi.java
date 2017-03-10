package com.example.sveta.taxodriver.tools;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by bogdan on 10.03.17.
 */

public interface RouteApi {
    @GET("/maps/api/directions/json")
    Call<RouteResponse> getRoute(
            @Query(value = "origin", encoded = false) String position,
            @Query(value = "destination", encoded = false) String destination,
            @Query("sensor") boolean sensor,
            @Query("language") String language
    );
}
