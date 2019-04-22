package com.autenticar.tuid.services;

import com.autenticar.tuid.model.ChanceRequest;
import com.autenticar.tuid.model.ChanceResponse;
import com.autenticar.tuid.model.ConfirmationRequest;
import com.autenticar.tuid.model.ConfirmationResponse;
import com.autenticar.tuid.model.ContactResponse;
import com.autenticar.tuid.model.LoginRequest;
import com.autenticar.tuid.model.LoginResponse;
import com.autenticar.tuid.model.RegisterRequest;
import com.autenticar.tuid.model.RegisterResponse;
import com.autenticar.tuid.model.ResponseBase;
import com.autenticar.tuid.model.UploadRequest;
import com.autenticar.tuid.model.ValidationRequest;
import com.autenticar.tuid.model.ValidationResponse;
import com.autenticar.tuid.model.ContactRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface TuidService {

    @POST("register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("chance")
    Call<ChanceResponse> chance(@Body ChanceRequest request);

    @POST("validation")
    Call<ValidationResponse> validation(@Body ValidationRequest request);

    @POST("confirmation")
    Call<ConfirmationResponse> confirmation(@Body ConfirmationRequest request);

    @POST("upload")
    Call<ResponseBase> upload(@Body UploadRequest request);

    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("comentary")
    Call<ContactResponse> comentary(@Body ContactRequest request);

}
