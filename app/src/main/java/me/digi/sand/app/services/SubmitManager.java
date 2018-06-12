/*
 * Copyright (c) 2009-2018 digi.me Limited. All rights reserved.
 */

package me.digi.sand.app.services;

import android.support.annotation.NonNull;

import java.util.concurrent.ConcurrentHashMap;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SubmitManager {

    private static final String FORM_ID = "1FAIpQLSef68QCsnYXpgKncHdxqTY5YnrUGVUweWnaSWIP8238Q6bs7w";
    private static volatile SubmitManager ourInstance;
    private final Retrofit retrofit;
    private GoogleSurveySubmitService service;
    private final ConcurrentHashMap<String, FormSubmitListener> listeners;

    private SubmitManager() {
        retrofit = new Retrofit.Builder()
                .client(new OkHttpClient.Builder().build())
                .baseUrl("https://docs.google.com/forms/d/e/")
                .build();
        listeners = new ConcurrentHashMap<>();
    }

    public static SubmitManager getInstance() {
        if (ourInstance == null) {
            synchronized (SubmitManager.class) {
                ourInstance = new SubmitManager();
            }
        }
        return ourInstance;
    }

    public void addListener(FormSubmitListener listener) {
        listeners.put(listener.getClass().getSimpleName(), listener);
    }

    @SuppressWarnings("unused")
    public void removeListener(Object listener) {
        listeners.remove(listener.getClass().getSimpleName());
    }

    public boolean submitForm(float rating, String reason) {
        if (reason.isEmpty()) {
            return false;
        }
        int rate = Math.round(rating);
        if (rate > 5) {
            rate = 5;
        } else if (rate < 0) {
            rate = 0;
        }
        if (service == null) {
            service = retrofit.create(GoogleSurveySubmitService.class);
        }
        service.submitSurvey(FORM_ID, Integer.toString(rate), reason).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                for (FormSubmitListener listener: listeners.values()) {
                    listener.onFormSubmittedSuccessfully();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                for (FormSubmitListener listener: listeners.values()) {
                    listener.onFormSubmitFailed(t.getMessage());
                }
            }
        });


        return true;
    }

    public interface FormSubmitListener {
        void onFormSubmittedSuccessfully();
        void onFormSubmitFailed(String error);
    }
}
