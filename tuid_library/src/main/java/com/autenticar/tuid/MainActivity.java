package com.autenticar.tuid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.autenticar.tuid.utils.HelperFunctions;
import com.google.firebase.analytics.FirebaseAnalytics;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {
  final Activity activity = this;
  private FirebaseAnalytics mFirebaseAnalytics;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main_menu);
    mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    Bundle bundle = new Bundle();
    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "132435465768");
    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "yonathan");
    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();

    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);

    /*Donde se asigna el nombre de usuario en el menu*/
    View hView = navigationView.getHeaderView(0);
    TextView nav_username = (TextView) hView.findViewById(R.id.nav_username);
    nav_username.setText(getIntent().getStringExtra("UserName"));

       // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
       // setSupportActionBar(toolbar);
        String name = getIntent().getStringExtra("UserName");
        if (name != null) {
            ((TextView) findViewById(R.id.main_name)).setText(name);
        }else{
            ((TextView) findViewById(R.id.main_name)).setVisibility(View.GONE);
        }
  }
  @Override
  public void onBackPressed() {
    AlertDialog.Builder dialogo = new AlertDialog.Builder(this);
    dialogo.setTitle(R.string.alert_titulo_alerta);
    dialogo.setMessage(R.string.main_alert_back);
    dialogo.setCancelable(true);
    dialogo.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialogo, int id) {
        dialogo.cancel();
      }
    });
    dialogo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialogo, int id) {
        // SwitchStep(1);
        Intent intent=new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
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
    /*DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }*/
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
