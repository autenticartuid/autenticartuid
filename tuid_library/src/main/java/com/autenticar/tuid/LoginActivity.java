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
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.autenticar.tuid.facedetection.FaceDetectionProcessor;
import com.autenticar.tuid.model.LoginRequest;
import com.autenticar.tuid.model.LoginResponse;
import com.autenticar.tuid.model.Session;
import com.autenticar.tuid.services.TuidService;
import com.autenticar.tuid.services.utils.ServiceGenerator;
import com.autenticar.tuid.utils.HelperFunctions;

import io.fabric.sdk.android.BuildConfig;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.autenticar.tuid.LivePreviewActivity.GESTURE_TO_DETECT;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
  public static final int REQUEST_LIVE_PREVIEW = 7002;
  private static final String TAG = "LoginActivity";
  private static final int RC_HANDLE_CAMERA_PERM = 2;
  private ProgressDialog mProgressDialog;
  public static final int REQUEST_GESTURES = 7003;
  public static boolean  send_value=false;
  final Activity activity = this;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    //Toast.makeText(getBaseContext(), getIntent().getData().toString(), Toast.LENGTH_LONG).show();

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    // ATTENTION: This was auto-generated to handle app links.
        /*Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();*/

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
    nav_bien.setTextSize(16);
    //nav_ico_username.setVisibility(View.INVISIBLE);

    Button btnRegistrarUser = findViewById(R.id.btnRegistrarUser);
    btnRegistrarUser.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        //Intent intent = new Intent(getBaseContext(), RegistrarUserActivity.class);
        Intent intent = new Intent(getBaseContext(), RegistrarScanUserActivity.class);
        startActivity(intent);
      }
    });

    final AppCompatActivity compatActivity = this;
    Button btnLoginDBiom = findViewById(R.id.btnLoginDBiom);
    btnLoginDBiom.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        int rc = ActivityCompat.checkSelfPermission(compatActivity, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
          takePicture();
        } else {
          requestCameraPermission();
        }
      }
    });
    TextView versionName = findViewById(R.id.txtVersionName);
    versionName.setText("Versión " + BuildConfig.VERSION_NAME);

  }

  protected void takePicture() {
    Intent intent = new Intent(getBaseContext(), LivePreviewActivity.class);
    intent.putExtra(GESTURE_TO_DETECT, FaceDetectionProcessor.DETECTAR_PESTANEO_CLOSED_EYES);
    startActivityForResult(intent, REQUEST_LIVE_PREVIEW);
  }

  /**
   * Handles the requesting of the camera permission.  This includes
   * showing a "Snackbar" message of why the permission is needed then
   * sending the request.
   */
  private void requestCameraPermission() {
    Log.w(TAG, "Camera permission is not granted. Requesting permission");

    final String[] permissions = new String[]{Manifest.permission.CAMERA};
    if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
            Manifest.permission.CAMERA)) {
      ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
      return;
    }

    final Activity thisActivity = this;

    View.OnClickListener listener = new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ActivityCompat.requestPermissions(thisActivity, permissions,
                RC_HANDLE_CAMERA_PERM);
      }
    };

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
  @Override
  public void onRequestPermissionsResult(int requestCode,
                                         @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    if (requestCode != RC_HANDLE_CAMERA_PERM) {
      Log.d(TAG, "Got unexpected permission result: " + requestCode);
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
      return;
    }

    if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      Log.d(TAG, "Camera permission granted - initialize the camera source");
      // We have permission, so create the camerasource
      return;
    }

    Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
            " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        finish();
      }
    };

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Multitracker sample")
            .setMessage("Requiere permiso de camara")
            .setPositiveButton(R.string.ok, listener)
            .show();
  }

  @Override
  protected void onResume() {
    Session session = ((MainApp) getApplication()).GetUserSession();
       /* if (session != null && session.description != null) {
            ((TextView) findViewById(R.id.txtCurrentUser)).setText(String.format(getString(R.string.no_sos_usuario), session.description));
        } else {
            findViewById(R.id.txtCurrentUser).setVisibility(View.GONE);
            //findViewById(R.id.btnLoginDBiom).setVisibility(View.GONE);
        }*/
    super.onResume();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

    if (resultCode != RESULT_OK)
      return;

    switch (requestCode) {
      case REQUEST_LIVE_PREVIEW:
        login();
    }

    super.onActivityResult(requestCode, resultCode, data);
  }

  private void login() {
    LoginRequest request = new LoginRequest();
    TuidService apiServiceUser = ServiceGenerator.createService(TuidService.class);

    Bitmap bitmap = null;
    if (((MainApp) getApplication()).isDebugSession()) { //cambio las imagenes a subir

      bitmap = HelperFunctions.getBitmapFromAsset("images/selfie.png", this);

    } else {
      bitmap = ((MainApp) getApplication()).GetSelfie();
      ;
    }

    request.FileEncode64 = HelperFunctions.encodeTobase64(bitmap);
    //Session session = ((MainApp) getApplication()).GetUserSession();
    //request.DNI = session.dni;
    //request.Sexo = session.gender;

    Call<LoginResponse> callUser = apiServiceUser.login(request);
    showProgressDialog(getString(R.string.alert_validando_identidad));
    final Activity activity = this;
    callUser.enqueue(new Callback<LoginResponse>() {
      @Override
      public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

        hideProgressDialog();
        if (response.isSuccessful()) {

          if (response.body().IsOk()) {
            LoginResponse loginResponse = response.body();
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            intent.putExtra("UserName",loginResponse.Name);
            startActivity(intent);
            finish();
          } else {
            HelperFunctions.ShowAlert(activity, response.body().Message);
          }
        } else {
          HelperFunctions.ShowAlert(activity, response.message());
        }
      }
      @Override
      public void onFailure(Call<LoginResponse> call, Throwable t) {
        hideProgressDialog();
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

}

