package io.codetail.client.mover;

import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

public interface MoverService {

    public static final String ENDPOINT = "http://mover.uz";

    @GET("/")
    public Response home();

    @GET("/video/latest/")
    public Response home(@Query("page") int page);

    @GET("/video/{category}")
    public Response category(@Path("category") String category, @Query("page") int page);

    @GET("/search/")
    public Response search(@Query("val") String query);

    @GET("/search/")
    public Response search(@Query("val") String query, @Query("page") int page);

    @GET("/watch/{id}")
    public Response watch(@Path("id") String id);

    @GET("/channel/{name}")
    public Response channel(@Path("name") String name);

    @FormUrlEncoded @POST("/auth/ajax")
    @Headers("X-Requested-With: XMLHttpRequest")
    public Response signIn(@Field("username") String username, @Field("password") String password);

}
