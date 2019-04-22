package com.autenticar.tuid;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

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
import com.autenticar.tuid.utils.DNIArgentinaPDF417ParserHelper;
import com.autenticar.tuid.utils.HelperFunctions;
import com.autenticar.tuid.utils.cropimage.CropImage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.text.TextUtils.isEmpty;

public class RegistrarUserActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String TAG = "tuid";
    public static final int REQUEST_FRENTE_DNI_CODE = 6000;
    public static final int REQUEST_PIC_FRENTE_DNI_CODE = 6001;
    public static final int REQUEST_DORSO_DNI_CODE = 6002;
    public static final int REQUEST_PIC_DORSO_DNI_CODE = 6003;
    public static final int REQUEST_LIVE_PREVIEW = 7003;
    private static final Uri CONTENT_URI = Uri.parse("content://com.callmarket.tuidmpv/");
    private static final String TEMP_PHOTO_FILE_NAME = "tuid_photo_capture.jpg";
    private File mFileTemp;
    private ImageView imgDNIDorso, imgDNIFrente;
    final int IMAGE_MAX_SIZE = 660;
    private ProgressBar progressBar;
    private int prueba_Vida_Index = 0;
    final Activity activity = this;

    private int pruebas_Vida[] = {-1, -1, -1}; //agregar otro cero para otra prueba de vida
    private ProgressDialog mProgressDialog;

    private Question question;

    protected void startCropImage(boolean withoutAspect, int aspectX, int aspectY, int requestCode) {

        Intent intent = new Intent(this, CropImage.class);
        intent.putExtra(CropImage.IMAGE_PATH, mFileTemp.getPath());
        intent.putExtra(CropImage.SCALE, false);

        if (!withoutAspect) {
            intent.putExtra(CropImage.ASPECT_X, aspectX);
            intent.putExtra(CropImage.ASPECT_Y, aspectY);
        } else {
            intent.putExtra(CropImage.ASPECT_X, 0);
            intent.putExtra(CropImage.ASPECT_Y, 0);
        }

        startActivityForResult(intent, requestCode);
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


    private void ReflejarResultadoPrueba() {
        CheckBox checkBox = null;
        switch (prueba_Vida_Index) {
            case 0:
                checkBox = findViewById(R.id.chkGesto1);
                break;
            case 1:
                checkBox = findViewById(R.id.chkGesto2);
                break;
            case 2:
                checkBox = findViewById(R.id.chkGesto3);
                break;

        }

        checkBox.setChecked(true);

    }

    protected void takePicture(int requestCode) {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            Uri mImageCaptureUri = null;
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mImageCaptureUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", mFileTemp);
                } else {
                    mImageCaptureUri = Uri.fromFile(mFileTemp);
                }
            } else {
                mImageCaptureUri = CONTENT_URI;
            }
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            intent.putExtra("return-data", true);
            startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {

            Log.d(TAG, "cannot take picture", e);
        }
    }

    private void ContinueTakingPicture(int requestCode) {
        if (((MainApp) getApplication()).isDebugSession()) {
            if (requestCode == REQUEST_PIC_FRENTE_DNI_CODE) {
                Bitmap bitmap = HelperFunctions.getBitmapFromAsset("images/front.jpg", this);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(0);
                Datos datos = new Datos();
                datos.isFront = true;
                datos.bitmap = bitmap;
                imgDNIFrente.setImageBitmap(datos.bitmap);
                new ProcessInfo().execute(datos);
            } else {
                Bitmap bitmap = HelperFunctions.getBitmapFromAsset("images/back.jpg", this);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(0);
                Datos datos = new Datos();
                datos.isFront = false;
                datos.bitmap = bitmap;
                imgDNIDorso.setImageBitmap(datos.bitmap);
                new ProcessInfo().execute(datos);
            }
        } else {
            takePicture(requestCode);
        }
    }

    public void RequestPermissions(int requestCode) {

        String[] permisions = new String[]{android.Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        if (!hasPermissions(this, permisions)) {
            ActivityCompat.requestPermissions(this, permisions, requestCode);
        } else {
            ContinueTakingPicture(requestCode);
        }
    }

    private void SwitchStep(int paso) {
        switch (paso) {
            case 1:
                findViewById(R.id.paso_reg1).setVisibility(View.VISIBLE);
                findViewById(R.id.paso_reg2).setVisibility(View.GONE);
                findViewById(R.id.paso_reg3).setVisibility(View.GONE);
                findViewById(R.id.paso_chances).setVisibility(View.GONE);
                findViewById(R.id.paso_validacion).setVisibility(View.GONE);
                break;

            case 2:
                findViewById(R.id.paso_reg1).setVisibility(View.GONE);
                findViewById(R.id.paso_reg2).setVisibility(View.VISIBLE);
                findViewById(R.id.paso_reg3).setVisibility(View.GONE);
                findViewById(R.id.paso_chances).setVisibility(View.GONE);
                findViewById(R.id.paso_validacion).setVisibility(View.GONE);
                break;
            case 3:
                findViewById(R.id.paso_reg1).setVisibility(View.GONE);
                findViewById(R.id.paso_reg2).setVisibility(View.GONE);
                findViewById(R.id.paso_reg3).setVisibility(View.VISIBLE);
                findViewById(R.id.paso_chances).setVisibility(View.GONE);
                findViewById(R.id.paso_validacion).setVisibility(View.GONE);
                break;
            case 4: //chances
                findViewById(R.id.paso_reg1).setVisibility(View.GONE);
                findViewById(R.id.paso_reg2).setVisibility(View.GONE);
                findViewById(R.id.paso_reg3).setVisibility(View.GONE);
                findViewById(R.id.paso_chances).setVisibility(View.VISIBLE);
                findViewById(R.id.paso_validacion).setVisibility(View.GONE);
                break;
            case 5: //validacion
                findViewById(R.id.paso_reg1).setVisibility(View.GONE);
                findViewById(R.id.paso_reg2).setVisibility(View.GONE);
                findViewById(R.id.paso_reg3).setVisibility(View.GONE);
                findViewById(R.id.paso_chances).setVisibility(View.GONE);
                findViewById(R.id.paso_validacion).setVisibility(View.VISIBLE);
                break;
        }
    }

    //region MANEJO DE PERMISOS

    public void OpenCropActivity(int requestCode) {
        startCropImage(false, 5, 3, requestCode);
    }




    class Datos {
        Bitmap bitmap;
        boolean isFront = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PIC_FRENTE_DNI_CODE:
            case REQUEST_PIC_DORSO_DNI_CODE:
                boolean needPermission = false;
                if (grantResults.length == permissions.length
                ) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                            needPermission = true;
                        }
                    }

                } else
                    needPermission = true;

                if (needPermission) {
                    HelperFunctions.CreateAlert(this, getString(R.string.alert_default_title), getString(R.string.es_necesario_otorgar_los_permisos_solicitados_para_continuar)).show();
                } else {
                    ContinueTakingPicture(requestCode);
                }
                return;

            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        setContentView(R.layout.activity_registrar_user);
        this.setTitle(R.string.title_activity_registrar);

        findViewById(R.id.btnCapturarFrente).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RequestPermissions(REQUEST_PIC_FRENTE_DNI_CODE);
            }
        });

        findViewById(R.id.btnCapturarReverso).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RequestPermissions(REQUEST_PIC_DORSO_DNI_CODE);
            }
        });

        findViewById(R.id.btnCaptureGestures).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getBaseContext(), LivePreviewActivity.class);
                intent.putExtra(LivePreviewActivity.GESTURE_TO_DETECT, pruebas_Vida[prueba_Vida_Index]);
                startActivityForResult(intent, REQUEST_LIVE_PREVIEW);

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
                    findViewById(R.id.txtTelefono).setEnabled(false);
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
                    ((RadioButton) findViewById(R.id.optSendMail)).setChecked(false);
                }
            }
        });

        imgDNIFrente = findViewById(R.id.imgDNIFrente);
        imgDNIDorso = findViewById(R.id.imgDNIDorso);

        //busco la ruta de la imagen
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mFileTemp = new File(Environment.getExternalStorageDirectory(), TEMP_PHOTO_FILE_NAME);
        } else {
            mFileTemp = new File(this.getFilesDir(), TEMP_PHOTO_FILE_NAME);
        }

        progressBar = findViewById(R.id.progressbar);


        ///debug
        SwitchStep(1);
        //((MainApp) getApplication()).SetNewUserSession(158);


        int counter = pruebas_Vida.length;
        pruebas_Vida[0] = 0; // siempre sin gesto primero
        boolean exists = false;
        while (counter > 1) {
            int rand = (int) (Math.random() * 5) + 1;

            exists = false;

            for (int valor = 1; valor < pruebas_Vida.length; valor++
            ) {
                if (pruebas_Vida[valor] == rand) {
                    exists = true;
                }
            }
            if (!exists) {
                counter -= 1;
                pruebas_Vida[counter] = rand;
            }
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
        if (isMail)
            request.MailCode = codigo;
        else
            request.SMSCode = codigo;

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
        TextInputLayout txtEMailConfirmacion = findViewById(R.id.txtEmailConfirmar);
        TextInputLayout txtTelefono = findViewById(R.id.txtTelefono);
        txtEMail.setError(null);
        txtEMailConfirmacion.setError(null);
        txtTelefono.setError(null);
        boolean cancel = false;
        View focusView = null;

        String email = txtEMail.getEditText().getText().toString();
        String emailConfirmacion = txtEMailConfirmacion.getEditText().getText().toString();
        String telefono = txtTelefono.getEditText().getText().toString();

        // Check for a valid password, if the user entered one.
        if (isEmpty(email) && isEmpty(telefono)) {
            HelperFunctions.ShowAlert(activity, getString(R.string.datos_envio_vacios));
        } else {
            if (!isEmpty(email) && !HelperFunctions.isValidEmail(email)) {
                txtEMail.setError(getString(R.string.corre_invalido));
                focusView = txtEMail;
                cancel = true;
            }else if(!email.equals(emailConfirmacion)){
                txtEMail.setError(getString(R.string.validacion_conirmacion_email));
                focusView = txtEMailConfirmacion;
                cancel = true;
            }
            if (!isEmpty(telefono) && (telefono.length() < 8)) {
                txtTelefono.setError(getString(R.string.telefono_invalido));
                focusView = txtTelefono;
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
        upload(null);
    }

    private void registrar() {

        final RegisterRequest request = new RegisterRequest();
        TuidService service = ServiceGenerator.createService(TuidService.class);

        try {
            request.DeviceID = HelperFunctions.GetDeviceID(this);

            if (((MainApp) getApplication()).isDebugSession()) { //cambio las imagenes a subir
                request.DataDocument = new Document();
                request.DataDocument.address = "JUAN BAUTISTA ALBERDI 4563 11 C - TOLOSA LA PLATA - BUENOS AIRES";
                request.DataDocument.birth = Date.valueOf("1963-01-01");
                request.DataDocument.expires = "2002-10-31";
                request.DataDocument.gender = "F";
                request.DataDocument.lastname = "VILLAREAL";
                request.DataDocument.name = "MARIA VICTORIA";
                request.DataDocument.number = "99999999";
                request.DataDocument.tramit = "000000105327000";
                request.DataDocument.type = "DNI";
            } else {
                try {
                    request.DataDocument = ((MainApp) getApplication()).GetPersonaToRegister().GetRequest();

                    Call<RegisterResponse> callUser = service.register(request);
                    showProgressDialog("Aguarde un momento.");

                    callUser.enqueue(new Callback<RegisterResponse>() {
                        @Override
                        public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {

                            hideProgressDialog();
                            RegisterResponse registerResponse = response.body();
                            if (response.isSuccessful()) {

                                if (registerResponse.IsOk()) {
                                    //go to validacion
                                    (((MainApp) getApplication())).SetNewUserSession(registerResponse.userID, request.DataDocument.number, request.DataDocument.gender, request.DataDocument.name);
                                    SwitchStep(5);//ir a validacion
                                } else if (registerResponse.IsChance()) {
                                    //go to chances
                                    (((MainApp) getApplication())).SetNewUserSession(registerResponse.userID, request.DataDocument.number, request.DataDocument.gender, request.DataDocument.name);
                                    question = registerResponse.Questions;
                                    ((TextView) findViewById(R.id.txtPreguntaChance)).setText(question.question);
                                    ((RadioButton) findViewById(R.id.rdChance0)).setText(question.chance0);
                                    ((RadioButton) findViewById(R.id.rdChance1)).setText(question.chance1);
                                    ((RadioButton) findViewById(R.id.rdChance2)).setText(question.chance2);
                                    ((RadioButton) findViewById(R.id.rdChance3)).setText(question.chance3);
                                    SwitchStep(4);//ir a chances

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
                        SwitchStep(5);//ir a validacion
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

        boolean isFront = false;
        ImageView imgView = null;
        switch (requestCode) {
            case REQUEST_LIVE_PREVIEW:

                ReflejarResultadoPrueba();

                prueba_Vida_Index += 1;

                if (prueba_Vida_Index == pruebas_Vida.length) {
                    findViewById(R.id.btnCaptureGestures).setVisibility(View.GONE);
                    findViewById(R.id.btnConfirmar).setVisibility(View.VISIBLE);
                    HelperFunctions.CreateAlert(this, getString(R.string.alert_default_title), getString(R.string.alert_reconocimiento_ok)).show();
                } else {
                    ((Button) findViewById(R.id.btnCaptureGestures)).setText(R.string.action_continuar);
                }

                break;
            case REQUEST_PIC_FRENTE_DNI_CODE:
                OpenCropActivity(REQUEST_FRENTE_DNI_CODE);
                break;
            case REQUEST_FRENTE_DNI_CODE:
                imgView = imgDNIFrente;
                isFront = true;
                break;
            case REQUEST_PIC_DORSO_DNI_CODE:
                OpenCropActivity(REQUEST_DORSO_DNI_CODE);
                break;
            case REQUEST_DORSO_DNI_CODE:
                imgView = imgDNIDorso;
                isFront = false;
                break;
        }

        if (imgView != null) {

            Bitmap bitmap;

            String path = data.getStringExtra(CropImage.SAVE_PATH);
            if (path == null) {

                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
            Datos datos = new Datos();
            datos.isFront = isFront;
            datos.bitmap = BitmapFactory.decodeFile(path); //original
            imgView.setImageBitmap(getBitmap(path)); //comprimido
            new ProcessInfo().execute(datos);

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    //entrada  / progreso / retorno
    class ProcessInfo extends AsyncTask<Datos, Integer, Double> {
        Datos datos;
        Persona persona;
        Bitmap bitmap;
        double processResult = 0;

        @Override
        protected Double doInBackground(Datos... params) {

            persona = ((MainApp) getApplication()).GetPersonaToRegister();

            datos = params[0];
            bitmap = datos.bitmap;


            if (datos.isFront) {
                FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

                FirebaseVisionBarcodeDetectorOptions options =
                        new FirebaseVisionBarcodeDetectorOptions.Builder()
                                .setBarcodeFormats(
                                        FirebaseVisionBarcode.FORMAT_PDF417
                                )
                                .build();

                FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                        .getVisionBarcodeDetector(options);

                detector.detectInImage(image)
                        .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                            @Override
                            public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                                // Task completed successfully

                                if (barcodes.size() > 0) {
                                    DNIArgentinaPDF417ParserHelper.Parse(barcodes.get(0).getRawValue(), persona);

                                    TextRecognizer textRecognizer = new TextRecognizer.Builder(getBaseContext()).build();

                                    if (!textRecognizer.isOperational()) {

                                        Toast.makeText(getBaseContext(), R.string.falla_inicializar_ocr, Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.INVISIBLE);
                                    } else {

                                        StringBuilder stringBuilder = new StringBuilder();
                                        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                                        SparseArray<TextBlock> item = textRecognizer.detect(frame);

                                        for (int i = 0; i < item.size(); i++) {
                                            stringBuilder.append(item.valueAt(i).getValue());
                                        }
                                        processResult = persona.ProcessFrontOCRReaded(stringBuilder.toString());
                                        progressBar.setVisibility(View.INVISIBLE);
                                        if (datos.isFront && processResult > 0.75d) {
                                            SwitchStep(2);
                                        } else {

                                            Toast.makeText(getBaseContext(), R.string.error_leer_informacion_texto_no_se_pudo, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                } else {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(getBaseContext(), R.string.error_leer_codigo_qr, Toast.LENGTH_LONG).show();
                                    ;
                                }
                                progressBar.setVisibility(View.INVISIBLE);

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(getBaseContext(), R.string.error_leer_informacion_texto, Toast.LENGTH_LONG).show();
                                ;
                            }
                        });
            } else {

                TextRecognizer textRecognizer = new TextRecognizer.Builder(getBaseContext()).build();

                if (!textRecognizer.isOperational()) {
                    Toast.makeText(getBaseContext(), R.string.falla_inicializar_ocr, Toast.LENGTH_LONG).show();
                } else {
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> item = textRecognizer.detect(frame);
                    StringBuilder stringBuilder = new StringBuilder();

                    for (int i = 0; i < item.size(); i++) {
                        stringBuilder.append(item.valueAt(i).getValue());
                    }
                    processResult = persona.ProcessBackOCRReaded(stringBuilder.toString());
                    // HelperFunctions.setHTMLFormmatedText(persona.toHTML(), (TextView) findViewById(R.id.lblBackResult));
                }

            }

            return processResult;

        }

        @Override
        protected void onPostExecute(Double result) {


            if (!datos.isFront && result > 0.5d) {
                progressBar.setVisibility(View.INVISIBLE);
                SwitchStep(3);
            } else if (!datos.isFront) {
                Toast.makeText(getBaseContext(), R.string.error_leer_informacion_texto_no_se_pudo_d, Toast.LENGTH_LONG).show();
            }

        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //progressBar.setProgress(values[0]);
        }
    }

    private Bitmap getBitmap(String path) {

        Uri uri = Uri.fromFile(new File(path));
        InputStream in = null;

        ContentResolver mContentResolver = getContentResolver();
        try {
            in = mContentResolver.openInputStream(uri);

            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(in, null, o);
            in.close();

            int scale = 1;
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = (int) Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            in = mContentResolver.openInputStream(uri);
            Bitmap b = BitmapFactory.decodeStream(in, null, o2);
            in.close();

            return b;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "file " + path + " not found");
        } catch (IOException e) {
            Log.e(TAG, "file " + path + " not found");
        }
        return null;
    }
    /*Funcionalidades del menu*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        System.out.println("iten entro");
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

    public void showDialog(int titulo, int msg, int item){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        if(item==4){
            dialog.setContentView(R.layout.dialog_contact);
            TextView text_title_info_empresarialdialog = (TextView) dialog.findViewById(R.id.text_title_contact);
            text_title_info_empresarialdialog.setText(titulo);
            ImageView btn_cancel=(ImageView) dialog.findViewById(R.id.btn_cancel_contact);
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
        }else{
            dialog.setContentView(R.layout.dialog_info_empresarial);
            TextView text_title_info_empresarialdialog = (TextView) dialog.findViewById(R.id.text_title_info_empresarial);
            TextView text_content_info_empresarial = (TextView) dialog.findViewById(R.id.text_content_info_empresarial);
            text_title_info_empresarialdialog.setText(titulo);
            text_content_info_empresarial.setText(msg);
            ImageView btn_cancel=(ImageView) dialog.findViewById(R.id.btn_cancel);
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            text_content_info_empresarial.setText(msg);
        }
        dialog.show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.nav_terminos_condiciones) {

            showDialog(R.string.nav_titulo_termino_condiciones, R.string.nav_content_termino_condiciones, 1);
        } else if (id == R.id.nav_politicas) {
            showDialog(R.string.nav_titulo_politicas, R.string.nav_content_politicas, 2);
        } else if (id == R.id.nav_empresa) {
            showDialog(R.string.nav_titulo_empresa, R.string.nav_content_empresa, 3);
        } else if (id == R.id.nav_contacto) {
            showDialog(R.string.nav_titulo_contacto, R.string.nav_content_contacto, 4);
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
                Intent intent = new Intent(getApplicationContext(), RegistrarScanUserActivity.class);
                startActivity(intent);
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
