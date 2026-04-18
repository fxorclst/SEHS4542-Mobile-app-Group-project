package com.group.groupProject.score.remote;

import com.group.groupProject.BuildConfig;
import com.group.groupProject.score.repository.ScoreboardRepository;
import com.group.groupProject.score.repository.ScoreboardRepositoryImpl;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class NetworkModule {

    private static final long TIMEOUT_SECONDS = 30L;
    private static final String DEFAULT_BASE_URL = "https://sehs.utkzml.easypanel.host/webhook/";

    private NetworkModule() {
    }

    public static ScoreboardApiService createApiService() {
        return createRetrofit().create(ScoreboardApiService.class);
    }

    public static ScoreboardRepository createRepository() {
        return new ScoreboardRepositoryImpl(createApiService());
    }

    private static Retrofit createRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(resolveBaseUrl())
                .client(createOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private static OkHttpClient createOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
    }

    private static String resolveBaseUrl() {
        String configuredBaseUrl = BuildConfig.SCOREBOARD_BASE_URL;
        String baseUrl = configuredBaseUrl == null || configuredBaseUrl.trim().isEmpty()
                ? DEFAULT_BASE_URL
                : configuredBaseUrl.trim();
        return baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }
}

