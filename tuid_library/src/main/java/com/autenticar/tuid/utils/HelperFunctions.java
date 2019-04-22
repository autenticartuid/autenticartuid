package com.autenticar.tuid.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.autenticar.tuid.R;
import com.autenticar.tuid.model.ContactRequest;
import com.autenticar.tuid.model.ContactResponse;
import com.autenticar.tuid.services.TuidService;
import com.autenticar.tuid.services.utils.ServiceGenerator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HelperFunctions {
    private static ProgressDialog mProgressDialog;
    public static AlertDialog CreateAlert(Activity context, String title, String message, View.OnClickListener onClickListener) {


        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        View view = context.getLayoutInflater().inflate(R.layout.custom_alert, null);
        dialog.setView(view);
        final AlertDialog alert = dialog.create();
        ((TextView) view.findViewById(R.id.dialog_text)).setText(message);
        Button ok = (Button) view.findViewById(R.id.ok_action);
        if (onClickListener == null) {
            onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //whatever you want
                    alert.dismiss();
                }
            };
        }
        ok.setOnClickListener(onClickListener);
        return alert;

    }

    public static AlertDialog CreateAlert(Activity context, String title, String message) {

        return CreateAlert(context, title, message, null);

    }


    public static void setHTMLFormmatedText(String formattedText, TextView view) {

        //String formattedText = "This <i>is</i> a <b>test</b> of <a href='http://foo.com'>html</a>";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            view.setText(Html.fromHtml(formattedText, Html.FROM_HTML_MODE_LEGACY));
        } else {
            view.setText(Html.fromHtml(formattedText));
        }
    }

    public static Bitmap getBitmapFromAsset(String strName, Context context) {
        AssetManager assetManager = context.getAssets();
        InputStream istr = null;
        try {
            istr = assetManager.open(strName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(istr);
        return bitmap;
    }

    public static String GetDeviceID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public static void ShowAlert(Activity context, String message) {
        CreateAlert(context, context.getString(R.string.app_name), message).show();
    }

    public static String encodeTobase64(Bitmap image) {
        Bitmap immagex = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        return imageEncoded;
    }

    public static void Vibrate(Context context) {
        Vibrator vibrator;
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500);
    }

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
    public final static void showDialog(int titulo, int msg, int item, final Context context, final Activity activity){
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        if(item==4){
            dialog.setContentView(R.layout.dialog_contact);
            TextView text_title_info_empresarialdialog = (TextView) dialog.findViewById(R.id.text_title_contact);
            text_title_info_empresarialdialog.setText(titulo);
            ImageView btn_cancel=(ImageView) dialog.findViewById(R.id.btn_cancel_contact);
            Button btn_send = (Button) dialog.findViewById(R.id.btn_send);
            final EditText text_content_info_dni_text =(EditText) dialog.findViewById(R.id.text_content_info_dni_text);
            final EditText text_content_info_email_text =(EditText) dialog.findViewById(R.id.text_content_info_email_text);
            final EditText text_content_info_asunto_text =(EditText) dialog.findViewById(R.id.text_content_info_asunto_text);
            final EditText text_content_info_msn_text =(EditText) dialog.findViewById(R.id.text_content_info_msn_text);
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            btn_send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ContactRequest request=new ContactRequest();
                    request.dni= String.valueOf(text_content_info_dni_text.getText());
                    request.email= String.valueOf(text_content_info_email_text.getText());
                    request.asunto= String.valueOf(text_content_info_asunto_text.getText());
                    request.mensaje= String.valueOf(text_content_info_msn_text.getText());
                    if(request.dni.trim().equals("")){
                        text_content_info_dni_text.requestFocus();
                        Toast.makeText(context,R.string.contact_validar_dni , Toast.LENGTH_LONG).show();
                    }else if(request.asunto.trim().equals("")){
                        text_content_info_asunto_text.requestFocus();
                        Toast.makeText(context,R.string.contact_validar_asunto , Toast.LENGTH_LONG).show();
                    }else if(request.email.length()<3){
                        text_content_info_email_text.requestFocus();
                        Toast.makeText(context,R.string.contact_validar_email , Toast.LENGTH_LONG).show();
                    }else if(request.mensaje.trim().equals("")){
                        text_content_info_msn_text.requestFocus();
                        Toast.makeText(context,R.string.contact_validar_mensaje, Toast.LENGTH_LONG).show();
                    }else{
                        send_contact(request,  dialog, activity, context);
                    }
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
    public static void send_contact(ContactRequest request, final Dialog dialog, final Activity activity, Context context){
        TuidService apiServiceContact = ServiceGenerator.createService(TuidService.class);
        Call<ContactResponse> callContact = apiServiceContact.comentary(request);
        showProgressDialog(String.valueOf(R.string.alert_verificando_info), context);
        callContact.enqueue(new Callback<ContactResponse>() {
            @Override
            public void onResponse(Call<ContactResponse> call, Response<ContactResponse> response) {
                hideProgressDialog();
                if (response.isSuccessful()) {
                    if (response.body().IsOk()) {
                        HelperFunctions.ShowAlert(activity, "Su mensaje se envio correctamente");
                        dialog.dismiss();
                    } else {
                        HelperFunctions.ShowAlert(activity, response.body().Message);
                    }
                } else {
                    HelperFunctions.ShowAlert(activity, response.message());
                }
            }
            @Override
            public void onFailure(Call<ContactResponse> call, Throwable t) {
                hideProgressDialog();
            }
        });
    }
    protected static void showProgressDialog(String message, Context context) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setMessage(message);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    protected static void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }
}
