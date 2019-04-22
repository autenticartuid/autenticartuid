package com.autenticar.tuid;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.autenticar.tuid.facedetection.FaceDetectionProcessor;
import com.autenticar.tuid.model.Answer;
import com.autenticar.tuid.model.ChanceRequest;
import com.autenticar.tuid.model.ChanceResponse;
import com.autenticar.tuid.model.ConfirmationRequest;
import com.autenticar.tuid.model.ConfirmationResponse;
import com.autenticar.tuid.model.Document;
import com.autenticar.tuid.model.Persona;
import com.autenticar.tuid.model.Question;
import com.autenticar.tuid.model.RegisterRequest;
import com.autenticar.tuid.model.RegisterResponse;
import com.autenticar.tuid.model.ResponseBase;
import com.autenticar.tuid.model.UploadRequest;
import com.autenticar.tuid.model.ValidationRequest;
import com.autenticar.tuid.model.ValidationResponse;
import com.autenticar.tuid.services.TuidService;
import com.autenticar.tuid.services.utils.ServiceGenerator;
import com.autenticar.tuid.utils.HelperFunctions;

import java.sql.Date;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.text.TextUtils.isEmpty;

public class RegistrarScanUserActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {
    public static final String TAG = "tuid";
    public static final int REQUEST_GESTURES = 7003;
    public static final int REQUEST_SCAN = 7004;
    final int IMAGE_MAX_SIZE = 660;
    final Activity activity = this;
    private ImageView imgDNIDorso, imgDNIFrente, imgDNIFotoDNI;
    private ProgressBar progressBar;
    private ProgressDialog mProgressDialog;
    private Question question;
    public static boolean ban = false, send_value=false;
    public static CheckBox checkbox_terminos = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ban = false;
        setContentView(R.layout.activity_registrar_scan_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View hView = navigationView.getHeaderView(0);
        TextView nav_username = (TextView) hView.findViewById(R.id.nav_username);
        TextView nav_bien = (TextView) hView.findViewById(R.id.nav_bien);

        ImageView nav_ico_username = (ImageView) hView.findViewById(R.id.nav_ico_username);
        nav_username.setText(getIntent().getStringExtra(""));
        nav_bien.setTextSize(20);
        findViewById(R.id.txtTelefono).setEnabled(false);
        findViewById(R.id.txtTelfConfirmar).setEnabled(false);
        checkbox_terminos = (CheckBox) findViewById(R.id.checkbox_terminos);
        findViewById(R.id.btnIniciarScan).setVisibility(View.GONE);
        findViewById(R.id.layout_terminos).setVisibility(View.VISIBLE);
        checkbox_terminos.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    findViewById(R.id.btnIniciarScan).setVisibility(View.VISIBLE);
                }else{
                    findViewById(R.id.btnIniciarScan).setVisibility(View.GONE);
                }
            }
        });

        //this.setTitle(R.string.title_activity_registrar);

        findViewById(R.id.btnIniciarScan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RequestPermissions();
            }
        });

        findViewById(R.id.btnContinuarAGestos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SwitchStep(2);
            }
        });

        ((MainApp) getApplication()).InitPersonaToRegister();

        findViewById(R.id.btnCaptureGestures).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), LivePreviewActivity.class);
                intent.putExtra(LivePreviewActivity.GESTURE_TO_DETECT, FaceDetectionProcessor.DETECTAR_PESTANEO_CLOSED_EYES);
                startActivityForResult(intent, REQUEST_GESTURES);
            }
        });

        findViewById(R.id.btnConfirmar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm();
            }
        });

        findViewById(R.id.btnSendChance).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedOption = -1;
                selectedOption = ((RadioButton) findViewById(R.id.rdChance0)).isChecked() ? 0 : selectedOption;
                selectedOption = ((RadioButton) findViewById(R.id.rdChance1)).isChecked() ? 1 : selectedOption;
                selectedOption = ((RadioButton) findViewById(R.id.rdChance2)).isChecked() ? 2 : selectedOption;
                selectedOption = ((RadioButton) findViewById(R.id.rdChance3)).isChecked() ? 3 : selectedOption;
                if (selectedOption < 0) {
                    HelperFunctions.ShowAlert(activity, getString(R.string.alert_elige_respuesta));
                } else {
                    SendChanceAnswer(String.valueOf(selectedOption));
                }
            }
        });
        findViewById(R.id.checkbox_terminos_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HelperFunctions.showDialog(R.string.nav_titulo_termino_condiciones, R.string.nav_content_termino_condiciones, 1,view.getContext(), activity);
            }
        });


        findViewById(R.id.btnSendValidation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processValidation();
            }
        });

        findViewById(R.id.btnSendCodeValidation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendValidationCode();
            }
        });

        findViewById(R.id.optSendMail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((RadioButton) findViewById(R.id.optSendMail)).isChecked()) {
                    ((TextInputLayout) findViewById(R.id.txtTelefono)).getEditText().setText(null);
                    ((TextInputLayout) findViewById(R.id.txtTelfConfirmar)).getEditText().setText(null);
                    findViewById(R.id.txtTelefono).setEnabled(false);
                    findViewById(R.id.txtTelfConfirmar).setEnabled(false);
                    findViewById(R.id.txtEmail).setEnabled(true);
                    findViewById(R.id.txtEmailConfirmar).setEnabled(true);
                    ((RadioButton) findViewById(R.id.optSendSMS)).setChecked(false);
                }
            }
        });

        findViewById(R.id.optSendSMS).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((RadioButton) findViewById(R.id.optSendSMS)).isChecked()) {
                    ((TextInputLayout) findViewById(R.id.txtEmail)).getEditText().setText(null);
                    ((TextInputLayout) findViewById(R.id.txtEmailConfirmar)).getEditText().setText(null);
                    findViewById(R.id.txtEmail).setEnabled(false);
                    findViewById(R.id.txtEmailConfirmar).setEnabled(false);
                    findViewById(R.id.txtTelefono).setEnabled(true);
                    findViewById(R.id.txtTelfConfirmar).setEnabled(true);
                    ((RadioButton) findViewById(R.id.optSendMail)).setChecked(false);
                }
            }
        });

        imgDNIFrente = findViewById(R.id.imgDNIFrente);
        imgDNIDorso = findViewById(R.id.imgDNIDorso);
        imgDNIFotoDNI = findViewById(R.id.imgDNIFotoDNI);
        imgDNIFotoDNI.setVisibility(View.GONE);
        progressBar = findViewById(R.id.progressbar);
        ///debug
        SwitchStep(1);
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    private void IniciarScan() {
        ban=true;
        ((MainApp) getApplication()).InitPersonaToRegister();
        if (((MainApp) getApplication()).isDebugSession()) {
            Bitmap bitmap = HelperFunctions.getBitmapFromAsset("images/front.jpg", this);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
            imgDNIFrente.setImageBitmap(bitmap);

            bitmap = HelperFunctions.getBitmapFromAsset("images/back.jpg", this);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
            imgDNIDorso.setImageBitmap(bitmap);
        } else {
            Intent intent = new Intent(getBaseContext(), MultiTrackerActivity.class);
            startActivityForResult(intent, REQUEST_SCAN);
        }
    }

    private void SwitchStep(int paso) {
        switch (paso) {
            case 1: //SCAN
                findViewById(R.id.paso_reg1).setVisibility(View.VISIBLE);
                findViewById(R.id.layout_terminos).setVisibility(View.VISIBLE);
                findViewById(R.id.paso_reg2).setVisibility(View.GONE);
                findViewById(R.id.paso_chances).setVisibility(View.GONE);
                findViewById(R.id.paso_validacion).setVisibility(View.GONE);
                ban=false;
                break;

            case 2: //GESTOS
                findViewById(R.id.paso_reg1).setVisibility(View.GONE);
                findViewById(R.id.layout_terminos).setVisibility(View.GONE);
                findViewById(R.id.paso_reg2).setVisibility(View.VISIBLE);
                findViewById(R.id.paso_chances).setVisibility(View.GONE);
                findViewById(R.id.paso_validacion).setVisibility(View.GONE);
                ban=true;
                break;
            case 3: //chances
                findViewById(R.id.paso_reg1).setVisibility(View.GONE);
                findViewById(R.id.paso_reg2).setVisibility(View.GONE);
                findViewById(R.id.layout_terminos).setVisibility(View.GONE);
                findViewById(R.id.paso_chances).setVisibility(View.VISIBLE);
                findViewById(R.id.paso_validacion).setVisibility(View.GONE);
                ban=true;
                break;
            case 4: //validacion
                findViewById(R.id.paso_reg1).setVisibility(View.GONE);
                findViewById(R.id.paso_reg2).setVisibility(View.GONE);
                findViewById(R.id.layout_terminos).setVisibility(View.GONE);
                findViewById(R.id.paso_chances).setVisibility(View.GONE);
                findViewById(R.id.paso_validacion).setVisibility(View.VISIBLE);
                ban=true;
                break;
        }
    }

    private void sendValidationCode() {
        TextInputLayout txtCodigoValidaction = findViewById(R.id.txtCodigoValidacion);
        txtCodigoValidaction.setError(null);
        boolean cancel = false;
        View focusView = null;
        String codigo = txtCodigoValidaction.getEditText().getText().toString();
        if (isEmpty(codigo)) {
            txtCodigoValidaction.setError(getString(R.string.alert_ingresa_codigo_enviado));
            focusView = txtCodigoValidaction;
            cancel = true;
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            requestValidationCode(codigo, ((RadioButton) findViewById(R.id.optSendMail)).isChecked());
        }
    }

    private void requestValidationCode(String codigo, boolean isMail) {
        ConfirmationRequest request = new ConfirmationRequest();
        TuidService service = ServiceGenerator.createService(TuidService.class);
        request.userID = ((MainApp) getApplication()).GetUserSession().userID;
        if (isMail){
            request.MailCode = codigo;
        }else {
            request.SMSCode = codigo;
        }
        Call<ConfirmationResponse> callUser = service.confirmation(request);
        showProgressDialog(getString(R.string.alert_enviando_codigo_ver));
        callUser.enqueue(new Callback<ConfirmationResponse>() {
            @Override
            public void onResponse(Call<ConfirmationResponse> call, Response<ConfirmationResponse> response) {
                hideProgressDialog();
                ConfirmationResponse validationResponse = response.body();
                if (response.isSuccessful()) {
                    if (validationResponse.IsOk()) {
                        //go to login
                        HelperFunctions.CreateAlert(activity, getString(R.string.app_name), getString(R.string.registro_satisfactorio), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ((MainApp) getApplication()).SaveLogin();
                                finish();
                            }
                        }).show();
                    } else {
                        HelperFunctions.ShowAlert(activity, validationResponse.Message);
                    }
                } else {
                    HelperFunctions.ShowAlert(activity, response.message());
                }
            }
            @Override
            public void onFailure(Call<ConfirmationResponse> call, Throwable t) {
                hideProgressDialog();
            }
        });
    }

    private void processValidation() {
        TextInputLayout txtEMail = findViewById(R.id.txtEmail);
        TextInputLayout txtEmailConfirmar = findViewById(R.id.txtEmailConfirmar);
        TextInputLayout txtTelefono = findViewById(R.id.txtTelefono);
        TextInputLayout txtTelfConfirmar = findViewById(R.id.txtTelfConfirmar);
        txtEMail.setError(null);
        txtEmailConfirmar.setError(null);
        txtTelefono.setError(null);
        txtTelfConfirmar.setError(null);
        boolean cancel = false;
        View focusView = null;

        String email = txtEMail.getEditText().getText().toString();
        String emailConfirmar = txtEmailConfirmar.getEditText().getText().toString();
        String telefono = txtTelefono.getEditText().getText().toString();
        String telefonoConfirmar = txtTelfConfirmar.getEditText().getText().toString();

        // Check for a valid password, if the user entered one.
        if (isEmpty(email) && isEmpty(telefono)) {
            HelperFunctions.ShowAlert(activity, getString(R.string.datos_envio_vacios));
        } else {
            if (!isEmpty(email) && !HelperFunctions.isValidEmail(email)) {
                txtEMail.setError(getString(R.string.corre_invalido));
                focusView = txtEMail;
                cancel = true;
            }else if(!email.equals(emailConfirmar)){
                txtEmailConfirmar.setError(getString(R.string.validacion_conirmacion_email));
                focusView = txtEmailConfirmar;
                cancel = true;
            }
            if (!isEmpty(telefono) && (telefono.length() < 8)) {
                txtTelefono.setError(getString(R.string.telefono_invalido));
                focusView = txtTelefono;
                cancel = true;
            }else if(!telefono.equals(telefonoConfirmar)){
                txtTelfConfirmar.setError(getString(R.string.validacion_conirmacion_telefono));
                focusView = txtTelfConfirmar;
                cancel = true;
            }
            if (cancel) {
                // There was an error; don't attempt login and focus the first
                // form field with an error.
                focusView.requestFocus();
            } else {
                requestValidation(email, telefono);
            }
        }
    }

    private void requestValidation(String email, String telefono) {
        ValidationRequest request = new ValidationRequest();
        TuidService service = ServiceGenerator.createService(TuidService.class);

        request.userID = ((MainApp) getApplication()).GetUserSession().userID;
        if (!TextUtils.isEmpty(email))
            request.EMail = email;
        if (!TextUtils.isEmpty(telefono))
            request.CellPhone = telefono;

        Call<ValidationResponse> callUser = service.validation(request);
        showProgressDialog("Enviando validación...");
        callUser.enqueue(new Callback<ValidationResponse>() {
            @Override
            public void onResponse(Call<ValidationResponse> call, Response<ValidationResponse> response) {
                hideProgressDialog();
                ValidationResponse validationResponse = response.body();
                if (response.isSuccessful()) {
                    if (validationResponse.IsOk()) {
                        //go to validacion
                        HelperFunctions.ShowAlert(activity, getString(R.string.alert_recibiras_codigo_verificacion));
                    } else {
                        HelperFunctions.ShowAlert(activity, validationResponse.Message);
                    }
                } else {
                    HelperFunctions.ShowAlert(activity, response.message());
                }
            }
            @Override
            public void onFailure(Call<ValidationResponse> call, Throwable t) {
                hideProgressDialog();
            }
        });
    }

    private void confirm() {
        //Funcion anterior se cambia, para enviar toda la informacion completa.
        //upload(null);
        registrar();
    }

    private String obtener_img(int valor){
       try {
           Persona persona = ((MainApp) getApplication()).GetPersonaToRegister();
           Bitmap img_bit = null;
           if (((MainApp) getApplication()).isDebugSession()) { //cambio las imagenes a subir
               if(valor == 1){
                   img_bit = HelperFunctions.getBitmapFromAsset("images/dorso_dni.png", this);
               }else if(valor == 2){
                   img_bit = HelperFunctions.getBitmapFromAsset("images/frente_dni.png", this);
               }else if(valor == 3){
                   img_bit = HelperFunctions.getBitmapFromAsset("images/selfie.png", this);
               }else{
                   return "";
               }
           } else {
               if(valor == 1){
                   ImageView imageDorso =  imgDNIDorso;
                   BitmapDrawable drawableDorso = (BitmapDrawable) imageDorso.getDrawable();
                   img_bit = drawableDorso.getBitmap();
               }else if(valor == 2){
                   ImageView imageFrente =  imgDNIFrente;
                   BitmapDrawable drawableFrente = (BitmapDrawable) imageFrente.getDrawable();
                   img_bit = drawableFrente.getBitmap();
               }else if(valor == 3){
                   img_bit = ((MainApp) getApplication()).GetSelfie();
               }else{
                   return "";
               }
           }
           if(!img_bit.equals("")){
               return HelperFunctions.encodeTobase64(img_bit);
           }else{
               return "";
           }
       } catch (Exception ex) {
           HelperFunctions.ShowAlert(this, ex.getMessage());
           return "";
       }
   }

    private void registrar() {
        final RegisterRequest request = new RegisterRequest();
        TuidService service = ServiceGenerator.createService(TuidService.class);
        try {
            request.DeviceID = HelperFunctions.GetDeviceID(this);
            if (((MainApp) getApplication()).isDebugSession()) { //cambio las imagenes a subir
                request.DataDocument = new Document();
                request.DataDocument.address  = "JUAN BAUTISTA ALBERDI 4563 11 C - TOLOSA LA PLATA - BUENOS AIRES";
                request.DataDocument.birth    = Date.valueOf("1963-01-01");
                request.DataDocument.expires  = "2002-10-31";
                request.DataDocument.gender   = "F";
                request.DataDocument.lastname = "VILLAREAL";
                request.DataDocument.name     = "MARIA VICTORIA";
                request.DataDocument.number   = "99999999";
                request.DataDocument.tramit   = "000000105327000";
                request.DataDocument.type     = "DNI";
                request.DataDocument.front    = "";
                request.DataDocument.back     = "";
            } else {
                try {
                    request.DataDocument = ((MainApp) getApplication()).GetPersonaToRegister().GetRequest();
                    request.selfie = obtener_img(3);
                    request.DataDocument.front = obtener_img(2);
                    request.DataDocument.back = obtener_img(1);
                    Call<RegisterResponse> callUser = service.register(request);
                    showProgressDialog("Aguarde un momento, procesando el registro");
                    callUser.enqueue(new Callback<RegisterResponse>() {
                        @Override
                        public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                            hideProgressDialog();
                            RegisterResponse registerResponse = response.body();
                            if (response.isSuccessful()) {
                                if (registerResponse.IsOk()) {
                                    if (registerResponse.userID != null){
                                        (((MainApp) getApplication())).SetNewUserSession(registerResponse.userID, request.DataDocument.number, request.DataDocument.gender, request.DataDocument.name);
                                        SwitchStep(4);//ir a validacion
                                    }else{
                                        HelperFunctions.ShowAlert(activity, "Error en response data");
                                    }
                                } else if (registerResponse.IsChance()) {
                                    //go to chances
                                    (((MainApp) getApplication())).SetNewUserSession(registerResponse.userID, request.DataDocument.number, request.DataDocument.gender, request.DataDocument.name);
                                    question = registerResponse.Questions;
                                    ((TextView) findViewById(R.id.txtPreguntaChance)).setText(question.question);
                                    ((RadioButton) findViewById(R.id.rdChance0)).setText(question.chance0);
                                    ((RadioButton) findViewById(R.id.rdChance1)).setText(question.chance1);
                                    ((RadioButton) findViewById(R.id.rdChance2)).setText(question.chance2);
                                    ((RadioButton) findViewById(R.id.rdChance3)).setText(question.chance3);
                                    SwitchStep(3);//ir a chances
                                } else {
                                    HelperFunctions.ShowAlert(activity, registerResponse.Message);
                                }
                            } else {
                                HelperFunctions.ShowAlert(activity, response.message());
                            }
                        }
                        @Override
                        public void onFailure(Call<RegisterResponse> call, Throwable t) {
                            hideProgressDialog();
                        }
                    });

                } catch (Exception ex) {
                    HelperFunctions.ShowAlert(this, ex.getMessage());
                }
            }
        } catch (Exception ex) {
            HelperFunctions.ShowAlert(this, ex.getMessage());
        }
    }

    private void SendChanceAnswer(String id) {
        ChanceRequest request = new ChanceRequest();
        TuidService service = ServiceGenerator.createService(TuidService.class);
        request.DeviceID = HelperFunctions.GetDeviceID(this);
        request.userID = ((MainApp) getApplication()).GetUserSession().userID;
        Answer answer = new Answer(question.ID, id);
        request.Answers = new ArrayList<>();
        request.Answers.add(answer);

        Call<ChanceResponse> callUser = service.chance(request);
        showProgressDialog("Enviando respuesta...");

        callUser.enqueue(new Callback<ChanceResponse>() {
            @Override
            public void onResponse(Call<ChanceResponse> call, Response<ChanceResponse> response) {
                hideProgressDialog();
                ChanceResponse chanceResponse = response.body();
                if (response.isSuccessful()) {
                    if (chanceResponse.IsOk()) {
                        //go to validacion
                        SwitchStep(4);//ir a validacion
                    } else {
                        HelperFunctions.CreateAlert(activity, getString(R.string.imposible_validar_identidad), chanceResponse.Message, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                finish();
                            }
                        });
                    }
                } else {
                    HelperFunctions.ShowAlert(activity, response.message());
                }
            }
            @Override
            public void onFailure(Call<ChanceResponse> call, Throwable t) {
                hideProgressDialog();
            }
        });
    }

    private void upload(final Boolean isFront) {
        try {
            UploadRequest request = new UploadRequest();
            TuidService apiServiceUser = ServiceGenerator.createService(TuidService.class);
            Persona persona = ((MainApp) getApplication()).GetPersonaToRegister();
            Bitmap bitmap = null;
            if (((MainApp) getApplication()).isDebugSession()) { //cambio las imagenes a subir
                if (isFront != null) {
                    if (!isFront) {
                        bitmap = HelperFunctions.getBitmapFromAsset("images/dorso_dni.png", this);
                    } else {
                        bitmap = HelperFunctions.getBitmapFromAsset("images/frente_dni.png", this);
                    }
                } else {
                    bitmap = HelperFunctions.getBitmapFromAsset("images/selfie.png", this);
                    ;
                }
                request.Name = persona.FileNameToUpload(isFront, true);
            } else {
                if (isFront == null) {
                    bitmap = ((MainApp) getApplication()).GetSelfie();
                } else {
                    ImageView imageView = isFront ? imgDNIFrente : imgDNIDorso;
                    BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
                    bitmap = drawable.getBitmap();
                }
                request.Name = persona.FileNameToUpload(isFront, false);
            }

            request.FileEncode64 = HelperFunctions.encodeTobase64(bitmap);

            Call<ResponseBase> callUser = apiServiceUser.upload(request);
            showProgressDialog(getString(R.string.alert_verificando_info));

            callUser.enqueue(new Callback<ResponseBase>() {
                @Override
                public void onResponse(Call<ResponseBase> call, Response<ResponseBase> response) {
                    hideProgressDialog();
                    if (response.isSuccessful()) {
                        if (response.body().IsOk()) {
                            if (isFront == null) {
                                upload(true);
                            } else if (isFront) {
                                upload(false);
                            } else if (!isFront) {
                                registrar();
                            }
                        } else {
                            HelperFunctions.ShowAlert(activity, response.body().Message);
                        }
                    } else {
                        HelperFunctions.ShowAlert(activity, response.message());
                    }
                }
                @Override
                public void onFailure(Call<ResponseBase> call, Throwable t) {
                    hideProgressDialog();
                }
            });
        } catch (Exception ex) {
            HelperFunctions.ShowAlert(this, ex.getMessage());
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != RESULT_OK) {// && resultCode != CommonStatusCodes.SUCCESS) {
            return;
        }
        switch (requestCode) {
            case REQUEST_GESTURES:
                findViewById(R.id.btnCaptureGestures).setVisibility(View.GONE);
                findViewById(R.id.btnConfirmar).setVisibility(View.VISIBLE);
                HelperFunctions.CreateAlert(this, getString(R.string.alert_default_title), getString(R.string.alert_reconocimiento_ok)).show();
                break;
            case REQUEST_SCAN:
                Persona persona = ((MainApp) this.getApplication()).GetPersonaToRegister();
                imgDNIFrente.setImageBitmap(persona.dniFront);
                imgDNIDorso.setImageBitmap(persona.dniBack);
                imgDNIFotoDNI.setVisibility(View.VISIBLE);
                imgDNIFotoDNI.setImageBitmap(persona.bitmapFace);

                findViewById(R.id.btnContinuarAGestos).setVisibility(View.VISIBLE);
                findViewById(R.id.btnIniciarScan).setVisibility(View.GONE);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        boolean needPermission = false;
        if (grantResults.length == permissions.length
                ) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    needPermission = true;
                }
            }
        } else {
            needPermission = true;
        }
        if (needPermission) {
            HelperFunctions.CreateAlert(this, getString(R.string.alert_default_title), getString(R.string.es_necesario_otorgar_los_permisos_solicitados_para_continuar)).show();
        } else {
            IniciarScan();
        }
        return;
    }

    public void RequestPermissions() {

        String[] permisions = new String[]{Manifest.permission.CAMERA};
        if (!hasPermissions(this, permisions)) {
            ActivityCompat.requestPermissions(this, permisions, 1500);
        } else {
            IniciarScan();
        }
    }

    /*Funcionalidades del menu*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.nav_terminos_condiciones) {
            HelperFunctions.showDialog(R.string.nav_titulo_termino_condiciones, R.string.nav_content_termino_condiciones, 1,this, activity);
        } else if (id == R.id.nav_politicas) {
            HelperFunctions.showDialog(R.string.nav_titulo_politicas, R.string.nav_content_politicas, 2, this, activity);
        } else if (id == R.id.nav_empresa) {
            HelperFunctions.showDialog(R.string.nav_titulo_empresa, R.string.nav_content_empresa, 3, this, activity);
        } else if (id == R.id.nav_contacto) {
            HelperFunctions.showDialog(R.string.nav_titulo_contacto, R.string.nav_content_contacto, 4, this, activity);
        } else if (id == R.id.nav_salir){
            AlertDialog.Builder dialogo = new AlertDialog.Builder(this);
            dialogo.setTitle("Alerta");
            dialogo.setMessage("¿ Esta seguro que desea salir de la aplicación ?");
            dialogo.setCancelable(true);
            dialogo.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo, int id) {
                    dialogo.cancel();
                }
            });
            dialogo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo, int id) {
                    finish();
                }
            });
            //Cambio de color los botones del alert
            AlertDialog alert = dialogo.create();
            alert.show();
            Button btn_negativo = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
            btn_negativo.setTextColor(Color.BLACK);
            Button btn_positivo = alert.getButton(DialogInterface.BUTTON_POSITIVE);
            btn_positivo.setTextColor(Color.BLACK);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    /*Funcionalidades del menu fin */
    @Override
    public void onBackPressed() {
       if(!ban){
           super.onBackPressed();
       }else {
           AlertDialog.Builder dialogo = new AlertDialog.Builder(this);
           dialogo.setTitle(R.string.alert_titulo_alerta);
           dialogo.setMessage(R.string.alert_msn_cancelar_registro);
           dialogo.setCancelable(true);
           dialogo.setNegativeButton("No", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialogo, int id) {
                   dialogo.cancel();
               }
           });
           dialogo.setPositiveButton("Si", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialogo, int id) {
                  // SwitchStep(1);
                   finish();
               }
           });
           /*Cambio de color los botones del alert*/
           AlertDialog alert = dialogo.create();
           alert.show();
           Button btn_negativo = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
           btn_negativo.setTextColor(Color.BLACK);
           Button btn_positivo = alert.getButton(DialogInterface.BUTTON_POSITIVE);
           btn_positivo.setTextColor(Color.BLACK);
       }
    }
}
