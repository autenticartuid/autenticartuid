package com.autenticar.tuid;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.autenticar.tuid.model.Persona;
import com.autenticar.tuid.model.Session;
import com.autenticar.tuid.services.utils.ServiceGenerator;
import com.autenticar.tuid.services.utils.UserExclusionStrategy;
import com.autenticar.tuid.utils.FontsOverride;
import com.google.firebase.FirebaseApp;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainApp extends Application {

    private static Session userSession;
    Bitmap selfie;

    public void SetSelfie(Bitmap bm) {
        selfie = bm;
    }

    public Bitmap GetSelfie() {
        return selfie;
    }
    private Persona personaToRegister;

    public Persona GetPersonaToRegister() {
        return personaToRegister;
    }

    public Session GetUserSession() {
        return userSession;
    }

    public void SetNewUserSession(int userID, String dni, String gender, String description) {
        userSession = Session.build(userID, dni, gender, description);
    }


    public void InitPersonaToRegister() {
        personaToRegister = new Persona();
    }

    @Override
    public void onCreate() {
        super.onCreate();


        //sobre escribo los fonts
        FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/MuseoSans-500.otf");
        FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/MuseoSans-500.otf");
        FontsOverride.setDefaultFont(this, "SERIF", "fonts/MuseoSans-500.otf");
        FontsOverride.setDefaultFont(this, "SANS_SERIF", "fonts/MuseoSans-500.otf");

        //     PrintConfig.initDefault(getAssets(), "fonts/MaterialIcons-Regular.ttf");

        Gson gson = new GsonBuilder()
                //.setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .setDateFormat("yyyy-MM-dd")
                .setExclusionStrategies(new UserExclusionStrategy())
                .create();
        //instancion la conexion
        String stringConnection;

        stringConnection = getURLServicios();

        ServiceGenerator.builder = new Retrofit.Builder()
                .baseUrl(stringConnection)
                //  .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson));
        ServiceGenerator.context = getBaseContext();

        InitPersonaToRegister();
        FirebaseApp.initializeApp(this);
        LoadLoginInfo();
    }

    public String getURLServicios() {
        return getString(R.string.urlConnection);
    }

    public boolean isDebugSession() {
        return false; // (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
    }

    public void SaveLogin() {
        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("dni", this.GetUserSession().dni);
        editor.putString("gender", this.GetUserSession().gender);
        editor.putString("description", this.GetUserSession().description);
        editor.putInt("userId", this.GetUserSession().userID);
        editor.commit();
    }

    private boolean LoadLoginInfo() {
        Context context = getApplicationContext();
        ;
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        int userId = sharedPref.getInt("userId", 0);
        String dni = sharedPref.getString("dni", null);
        String gender = sharedPref.getString("gender", null);
        String description = sharedPref.getString("description", null);
        if (dni != null && gender != null && userId > 0) {
            this.SetNewUserSession(userId, dni, gender, description);
            return true;
        } else {
            return false;
        }

    }

}
