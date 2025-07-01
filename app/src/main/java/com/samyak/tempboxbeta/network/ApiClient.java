package com.samyak.tempboxbeta.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static Retrofit retrofit = null;
    private static MailTmApiService apiService = null;
    
    public static synchronized MailTmApiService getApiService() {
        if (apiService == null) {
            retrofit = createRetrofit();
            apiService = retrofit.create(MailTmApiService.class);
        }
        return apiService;
    }
    
    private static Retrofit createRetrofit() {
        // Create HTTP logging interceptor
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        // Create OkHttpClient
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .build();
        
        // Create robust Gson configuration
        Gson gson = new GsonBuilder()
                .setLenient() // More forgiving JSON parsing
                .registerTypeAdapter(new TypeToken<List<String>>(){}.getType(), new FlexibleStringListDeserializer())
                .create();
        
        // Create Retrofit instance
        return new Retrofit.Builder()
                .baseUrl(MailTmApiService.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }
    
    // Custom deserializer to handle both arrays and objects for string lists
    private static class FlexibleStringListDeserializer implements JsonDeserializer<List<String>> {
        @Override
        public List<String> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json == null || json.isJsonNull()) {
                return new ArrayList<>();
            }
            
            if (json.isJsonArray()) {
                // Normal array case
                List<String> result = new ArrayList<>();
                for (JsonElement element : json.getAsJsonArray()) {
                    if (element.isJsonPrimitive()) {
                        result.add(element.getAsString());
                    } else {
                        result.add(element.toString());
                    }
                }
                return result;
            } else if (json.isJsonObject()) {
                // Object case - convert to single item list or return empty
                return Collections.singletonList(json.toString());
            } else if (json.isJsonPrimitive()) {
                // Single string case
                return Collections.singletonList(json.getAsString());
            }
            
            return new ArrayList<>();
        }
    }
    
    public static String formatAuthToken(String token) {
        return "Bearer " + token;
    }
} 