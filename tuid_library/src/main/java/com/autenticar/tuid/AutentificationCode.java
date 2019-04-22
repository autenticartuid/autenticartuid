package com.autenticar.tuid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.autenticar.tuid.model.ConfirmationRequest;
import com.autenticar.tuid.model.ConfirmationResponse;
import com.autenticar.tuid.services.TuidService;
import com.autenticar.tuid.services.utils.ServiceGenerator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AutentificationCode extends AppCompatActivity {
    private static final String TAG="AutentificationCode";
    final Activity activity = this;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_autentification_code);
        Intent intent = getIntent();
        Uri data = intent.getData();
        if(data!=null){
            if(!data.getQuery().equals("")){
                if(!data.getQueryParameter("MailCode").equals("") && data.getQueryParameter("userID").length()>0){
                    requestValidationCode(data.getQueryParameter("MailCode"), Integer.parseInt(data.getQueryParameter("userID")));
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                synchronized (this) {
                                    wait(3000);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent_login = new Intent(getApplicationContext(), LoginActivity.class);
                                            startActivity(intent_login);
                                        }
                                    });
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        };
                    };
                    thread.start();
                }else{
                    Log.w(TAG, "getDynamicLink:Paramet null");
                    Intent intent_login = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent_login);
                }
            }else{
                Log.w(TAG, "getDynamicLink:Query null");
                Intent intent_login = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent_login);
            }
        }else{
            Log.w(TAG, "getDynamicLink:Data null");
            Intent intent_login = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent_login);
        }
    }
    private void requestValidationCode(String codigo, int userID) {
        ConfirmationRequest request = new ConfirmationRequest();
        TuidService service = ServiceGenerator.createService(TuidService.class);
        request.userID = userID;
        request.MailCode = codigo;
        Call<ConfirmationResponse> callUser = service.confirmation(request);
        getSupportActionBar().hide();
        final TextView text_loding= (TextView) findViewById(R.id.text_loding);
        text_loding.setText(getString(R.string.alert_enviando_codigo_ver));
        callUser.enqueue(new Callback<ConfirmationResponse>() {
            @Override
            public void onResponse(Call<ConfirmationResponse> call, Response<ConfirmationResponse> response) {
                //hideProgressDialog();
                ConfirmationResponse validationResponse = response.body();
                if (response.isSuccessful()) {
                    if (validationResponse.IsOk()) {
                        ((MainApp) getApplication()).SaveLogin();
                        text_loding.setText("El usuario fue validado correctamente!");
                    } else {
                        Log.w(TAG, "requestValidationCode:IsOk false ");
                        text_loding.setText(validationResponse.Message);
                    }
                } else {
                    text_loding.setText( response.message());
                    Log.w(TAG, "requestValidationCode:isSuccessful false ");
                }
            }
            @Override
            public void onFailure(Call<ConfirmationResponse> call, Throwable t) {
                Log.w(TAG, "requestValidationCode:onFailure ");
                text_loding.setText("El usuario fue validado anteriormente!");
            }
        });
    }
    protected void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(message);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    protected void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }
}
