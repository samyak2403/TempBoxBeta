package com.samyak.tempboxbeta.network;

import com.samyak.tempboxbeta.models.Account;
import com.samyak.tempboxbeta.models.ApiResponse;
import com.samyak.tempboxbeta.models.AuthToken;
import com.samyak.tempboxbeta.models.Domain;
import com.samyak.tempboxbeta.models.Message;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MailTmApiService {
    
    // Base URL constant
    String BASE_URL = "https://api.mail.tm/";
    
    // Domains endpoints
    @GET("domains")
    Call<ApiResponse<Domain>> getDomains(@Query("page") Integer page);
    
    @GET("domains/{id}")
    Call<Domain> getDomain(@Path("id") String id);
    
    // Authentication endpoints
    @POST("token")
    Call<AuthToken> getToken(@Body Account credentials);
    
    // Account endpoints
    @POST("accounts")
    Call<Account> createAccount(@Body Account account);
    
    @GET("accounts/{id}")
    Call<Account> getAccount(
        @Header("Authorization") String token,
        @Path("id") String id
    );
    
    @GET("me")
    Call<Account> getCurrentAccount(@Header("Authorization") String token);
    
    @DELETE("accounts/{id}")
    Call<Void> deleteAccount(
        @Header("Authorization") String token,
        @Path("id") String id
    );
    
    // Messages endpoints
    @GET("messages")
    Call<ApiResponse<Message>> getMessages(
        @Header("Authorization") String token,
        @Query("page") Integer page
    );
    
    @GET("messages/{id}")
    Call<Message> getMessage(
        @Header("Authorization") String token,
        @Path("id") String id
    );
    
    @DELETE("messages/{id}")
    Call<Void> deleteMessage(
        @Header("Authorization") String token,
        @Path("id") String id
    );
    
    @PATCH("messages/{id}")
    Call<Message> markMessageAsRead(
        @Header("Authorization") String token,
        @Path("id") String id
    );
} 