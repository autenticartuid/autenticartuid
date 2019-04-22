/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.autenticar.tuid;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.autenticar.tuid.camera.CameraSourcePreview;
import com.autenticar.tuid.camera.DocumentOverlay;
import com.autenticar.tuid.camera.GraphicOverlay;
import com.autenticar.tuid.model.Persona;
import com.autenticar.tuid.utils.DNIArgentinaPDF417ParserHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiDetector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Activity for the multi-tracker app.  This app detects faces and barcodes with the rear facing
 * camera, and draws overlay graphics to indicate the position, size, and ID of each face and
 * barcode.
 */
public final class MultiTrackerActivity extends AppCompatActivity {
    private static final String TAG = "MultiTracker";

    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private CameraSource mCameraSource = null;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    private DocumentOverlay documentOverlay;
    public static pl.droidsonroids.gif.GifImageView mVideo = null;
    private MediaController mediaController;
    private boolean readingFront = false;
    private boolean takingFace;
    private boolean check_front=false;
    private boolean check_back=false;
    private int forma=0;
    private String fron_pdf_value="";
    private String fron_gcr_value="";
    private ProgressBar progressBar;
    final Activity activity = this;
    /**
     * Initializes the UI and creates the detector pipeline.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.scan_view);

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);
        mVideo = (pl.droidsonroids.gif.GifImageView) findViewById(R.id.videoView);
        readingFront = true;
        mediaController = new MediaController(this);
        progressBar = findViewById(R.id.progressbar);
        progressBar.setIndeterminate(false);
        progressBar.setProgress(0);
        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }

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
        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() {

        Context context = getApplicationContext();
        // A face detector is created to track faces.  An associated multi-processor instance
        // is set to receive the face detection results, track the faces, and maintain graphics for
        // each face on screen.  The factory is used by the multi-processor to create a separate
        // tracker instance for each face.
        final Persona persona = ((MainApp) getApplication()).GetPersonaToRegister();
        persona.setProgressBar(progressBar);
        persona.setActivity(activity);
        persona.setScan_guide(((TextView) findViewById(R.id.scan_guide)));
        //recibir los eventos de PDF
        ITrackerProcess<Face> iTrackerFace = new ITrackerProcess<Face>() {
            @Override
            public void ValueReaded(Detector.Detections<Face> detectionResults) {
                //solo capturo la imagen del frente
                if (!persona.IsFaceDetected() && persona.dniFront != null && readingFront && !takingFace) {

                    takingFace = true;
                    Face face = detectionResults.getDetectedItems().valueAt(0);
                    RectF rect = new RectF();
                    rect.set(face.getPosition().x - 30, face.getPosition().y - 30, face.getWidth() + 30, face.getHeight() + 30);
                    takingFace = true;
                    mPreview.takePicture(persona, 1, rect);
                }

            }
        };
        FaceDetector faceDetector = new FaceDetector.Builder(context).build();
        FaceTrackerFactory faceFactory = new FaceTrackerFactory(mGraphicOverlay, iTrackerFace);
        faceDetector.setProcessor(
                new MultiProcessor.Builder<>(faceFactory).build());

        ((TextView) findViewById(R.id.scan_guide)).setText("Por favor, enfoca el frente del DNI");

        // A barcode detector is created to track barcodes.  An associated multi-processor instance
        // is set to receive the barcode detection results, track the barcodes, and maintain
        // graphics for each barcode on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each barcode.
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context).build();

        final Activity activity = this;
        //recibir los eventos de PDF
        ITrackerProcess<Barcode> iTrackerBarcode = new ITrackerProcess<Barcode>() {
            @Override
            public void ValueReaded(Detector.Detections<Barcode> detectionResults) {
                if (!persona.IsBarcodeReaded() && !persona.IsOCRFrontReaded() && !check_front && detectionResults.getDetectedItems().size() > 0) {
                    DNIArgentinaPDF417ParserHelper.Parse(detectionResults.getDetectedItems().valueAt(0).rawValue, persona);
                    forma=1;
                }else if(!persona.IsPDFBackReaded() && persona.IsPDFFrontReaded()  && check_front && detectionResults.getDetectedItems().size() > 0){
                    DNIArgentinaPDF417ParserHelper.Parse(detectionResults.getDetectedItems().valueAt(0).rawValue, persona);
                }
            }
        };
        BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(mGraphicOverlay, iTrackerBarcode);
        barcodeDetector.setProcessor(
                new MultiProcessor.Builder<>(barcodeFactory).build());

        //recibir los eventos de OCR
        ITrackerProcess<TextBlock> iTrackerOCR = new ITrackerProcess<TextBlock>() {
            @Override
            public void ValueReaded(Detector.Detections<TextBlock> detectionResults) {

                if(detectionResults.getDetectedItems().size()>0) {
                    if (!persona.IsOCRFrontReaded() && readingFront && !check_front && !check_back) {
                        check_back=true;
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < detectionResults.getDetectedItems().size(); i++) {
                            stringBuilder.append(detectionResults.getDetectedItems().valueAt(i).getValue());
                        }
                        String valor = chararterSpece(stringBuilder.toString());
                        fron_gcr_value=valor;
                        persona.setMaxProgressBar(6);
                        persona.ProcessFrontOCRReaded(valor);
                        if (persona.IsOCRFrontReaded()) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mPreview.takePicture(persona, 2, null);
                                    forma=1;
                                    check_front=true;
                                }
                            });
                        }
                    } else if (!persona.IsPDFFrontReaded() && readingFront && !check_front && check_back) {
                        check_back=false;
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < detectionResults.getDetectedItems().size(); i++) {
                            stringBuilder.append(detectionResults.getDetectedItems().valueAt(i).getValue());
                        }
                        String valor = chararterSpece(stringBuilder.toString());
                        fron_pdf_value=valor;
                        persona.setMaxProgressBar(11);
                        persona.ProcessFrontPDFReaded(valor);
                        if (persona.IsPDFFrontReaded()) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mPreview.takePicture(persona, 2, null);
                                    forma=2;
                                    check_front=true;
                                }
                            });
                        }
                    } else if (persona.IsOCRFrontReaded() && !persona.IsOCRBackReaded() && !readingFront  && forma == 1) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < detectionResults.getDetectedItems().size(); i++) {
                            stringBuilder.append(detectionResults.getDetectedItems().valueAt(i).getValue());
                        }
                        String valor = chararterSpece(stringBuilder.toString());
                        persona.setMaxProgressBar(8);
                        persona.ProcessBackOCRReaded(valor);
                        if (persona.IsOCRBackReaded() && !valor.equals(fron_gcr_value)) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mPreview.takePicture(persona, 3, null);
                                    persona.iniProgressBar();
                                }
                            });
                        }
                    } else if (persona.IsPDFFrontReaded() && !persona.IsPDFBackReaded() && !readingFront &&  forma == 2) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < detectionResults.getDetectedItems().size(); i++) {
                            stringBuilder.append(detectionResults.getDetectedItems().valueAt(i).getValue());
                        }
                        String valor = chararterSpece(stringBuilder.toString());
                        persona.setMaxProgressBar(7);
                        persona.ProcessBackPDFReaded(fron_pdf_value, valor);
                        if (persona.IsPDFBackReaded() && !valor.equals(fron_pdf_value)) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mPreview.takePicture(persona, 3, null);
                                    persona.iniProgressBar();
                                }
                            });
                        }
                    } else if (persona.IsFilled()) {
                        setResult(RESULT_OK);
                        finish();
                    }
                    if ((persona.IsOCRFrontReaded() || persona.IsPDFFrontReaded()) && readingFront && persona.IsFaceDetected()) {
                        readingFront = false;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                FlipDNI(persona);
                            }
                        });
                    }
                }
            }
        };

        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        OCRTrackerFactory ocrTrackerFactory = new OCRTrackerFactory(mGraphicOverlay, iTrackerOCR);
        textRecognizer.setProcessor(new MultiProcessor.Builder<>(ocrTrackerFactory).build());

        // A multi-detector groups the two detectors together as one detector.  All images received
        // by this detector from the camera will be sent to each of the underlying detectors, which
        // will each do face and barcode detection, respectively.  The detection results from each
        // are then sent to associated tracker instances which maintain per-item graphics on the
        // screen.
        MultiDetector multiDetector = new MultiDetector.Builder()
                .add(faceDetector)
                .add(barcodeDetector)
                .add(textRecognizer)
                .build();

        if (!multiDetector.isOperational()) {
            // Note: The first time that an app using the barcode or face API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any barcodes
            // and/or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w(TAG, "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the barcode detector to detect small barcodes
        // at long distances.
        mCameraSource = new CameraSource.Builder(getApplicationContext(), multiDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024)
                .setRequestedFps(15.0f)
                .setAutoFocusEnabled(true)
                .build();

        /*mCameraSource =
                new CameraSource.Builder(getApplicationContext(),multiDetector)
                        .setFacing(CameraSource.CAMERA_FACING_BACK)
                        .setRequestedPreviewSize(1280, 1024)
                        .setRequestedFps(2.0f)
                        .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                        .setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null)
                        .setPersonaInfo(persona)
                        .setStatusChanged(this)
                        .build();*/
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }
    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
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
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }
        if (mCameraSource != null) {
            try {
                documentOverlay = findViewById(R.id.dniOverlay);
                mPreview.setCamera(getCameraInstance());
                mPreview.start(mCameraSource, mGraphicOverlay);
                final Handler handler = new Handler();
                documentOverlay.init(false, mPreview);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        documentOverlay.init(true, mPreview);
                    }
                }, 3000);

            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    private void FlipDNI(final Persona persona) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
               ((TextView) findViewById(R.id.scan_guide)).setText("Por favor, enfoca el dorso del DNI");
                persona.iniProgressBar();
                try {
                    GifDrawable gifDrawable = new GifDrawable(getResources(),R.drawable.dni_transparente);
                    mVideo.setImageDrawable(gifDrawable);
                    mVideo.setVisibility(VISIBLE);
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                synchronized (this) {
                                    wait(6000);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mVideo.setVisibility(GONE);
                                        }
                                    });
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        };
                    };
                    thread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 2000);
    }
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
                forma=0;
                readingFront=false;
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
    /**
     * Función que elimina acentos y caracteres especiales de
     * una cadena de texto.
     * @param input
     * @return cadena de texto limpia de acentos y caracteres especiales.
     */
    public static String chararterSpece(String input) {
        String original = "áàäéèëíìïóòöúùuñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜÑçÇ";
        String ascii = "aaaeeeiiiooouuunAAAEEEIIIOOOUUUNcC";
        String output = input;
        for (int i=0; i<original.length(); i++) {
            output = output.replace(original.charAt(i), ascii.charAt(i));
        }
        return output;
    }
}
