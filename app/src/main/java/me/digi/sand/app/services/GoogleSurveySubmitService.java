/*
 * Copyright (c) 2009-2018 digi.me Limited. All rights reserved.
 */

package me.digi.sand.app.services;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Path;

interface GoogleSurveySubmitService {

    @POST("{formId}/formResponse")
    @FormUrlEncoded
    Call<Void> submitSurvey(
            @SuppressWarnings("SameParameterValue") @Path("formId") String formId,
            @Field("entry.1002120629") String rating,
            @Field("entry.479032082") String reason
    );

}
