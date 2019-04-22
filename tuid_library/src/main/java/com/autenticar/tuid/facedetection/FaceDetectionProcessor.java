// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.autenticar.tuid.facedetection;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.autenticar.tuid.ui.vision.CameraImageGraphic;
import com.autenticar.tuid.ui.vision.FrameMetadata;
import com.autenticar.tuid.ui.vision.GraphicOverlay;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.IOException;
import java.util.List;

/**
 * Face Detector Demo.
 */
public class FaceDetectionProcessor extends VisionProcessorBase<List<FirebaseVisionFace>> {

    private static final String TAG = "FaceDetectionProcessor";

    private final FirebaseVisionFaceDetector detector;

    public static final int DETECTAR_SIN_GESTOS = 0;
    public static final int DETECTAR_SONRISA = 1;
    public static final int DETECTAR_OJOSCERRADOS_SONRIENDO = 2;
    public static final int DETECTAR_OJO_DIESTRO_CERRADO_SONRIENDO = 3;
    public static final int DETECTAR_OJO_IZQ_CERRADO_SONRIENDO = 4;
    public static final int DETECTAR_OJOSCERRADOS = 5;
    public static final int DETECTAR_PESTANEO_NEUTRAL = 6;
    public static final int DETECTAR_PESTANEO_CLOSED_EYES = 7;
    public static final int CANTIDAD_DETECCIONES_NECESARIAS = 6; // detecta 3 pestañeso cuenta 1 cuando abre y uno cuando cierra el ojo

    private int gestureToDetect = DETECTAR_SONRISA;
    private int contadorDetecciones = 0;
    private IGestureResult gestureResult;

    public FaceDetectionProcessor(int gestureToDetect, IGestureResult gestureResult) {
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .setContourMode(FirebaseVisionFaceDetectorOptions.NO_CONTOURS)
                        .setMinFaceSize(0.7f)
                        .build();
        this.gestureToDetect = gestureToDetect;
        this.gestureResult = gestureResult;
        detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
    }

    @Override
    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Face Detector: " + e);
        }
    }

    @Override
    protected Task<List<FirebaseVisionFace>> detectInImage(FirebaseVisionImage image) {
        return detector.detectInImage(image);
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull List<FirebaseVisionFace> faces,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        //graphicOverlay.clear();
        if (originalCameraImage != null) {
            CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay, originalCameraImage);
            graphicOverlay.add(imageGraphic);
        }
        for (int i = 0; i < faces.size(); ++i) {
            FirebaseVisionFace face = faces.get(i);
            /// controlar las probalitadades aca
            ProcesarGesto(face, originalCameraImage);
        /*int cameraFacing =
                frameMetadata != null ? frameMetadata.getCameraFacing() :
                        Camera.CameraInfo.CAMERA_FACING_BACK;
        FaceGraphic faceGraphic = new FaceGraphic(graphicOverlay, face, cameraFacing);
        graphicOverlay.add(faceGraphic);*/
        }

        graphicOverlay.postInvalidate();
    }

    private void ProcesarGesto(FirebaseVisionFace face, Bitmap bitmap) {
        switch (gestureToDetect) {
            case DETECTAR_SONRISA:
                if ((face.getSmilingProbability() > 0.8)
                        && face.getLeftEyeOpenProbability() > 0.5
                        && face.getRightEyeOpenProbability() > 0.5) {
                    contadorDetecciones += 1;
                } else {
                    contadorDetecciones = 0;
                }
                break;
            case DETECTAR_OJO_IZQ_CERRADO_SONRIENDO:
                if ((face.getLeftEyeOpenProbability() > 0.8)
                        && (face.getRightEyeOpenProbability() < 0.3)
                        && (face.getSmilingProbability() > 0.8)) {
                    contadorDetecciones += 1;
                } else {
                    contadorDetecciones = 0;
                }
                break;
            case DETECTAR_OJO_DIESTRO_CERRADO_SONRIENDO:
                if ((face.getLeftEyeOpenProbability() < 0.3)
                        && (face.getRightEyeOpenProbability() > 0.8)
                        && (face.getSmilingProbability() > 0.8)) {
                    contadorDetecciones += 1;
                } else {
                    contadorDetecciones = 0;
                }

                break;
            case DETECTAR_OJOSCERRADOS_SONRIENDO:
                if ((face.getLeftEyeOpenProbability() < 0.3)
                        && (face.getRightEyeOpenProbability() < 0.3)
                        && (face.getSmilingProbability() > 0.8)) {
                    contadorDetecciones += 1;
                } else {
                    contadorDetecciones = 0;
                }
                break;

            case DETECTAR_OJOSCERRADOS:
                if ((face.getLeftEyeOpenProbability() < 0.3)
                        && (face.getRightEyeOpenProbability() < 0.3)
                        && (face.getSmilingProbability() < 0.3)) {
                    contadorDetecciones += 1;
                } else {
                    contadorDetecciones = 0;
                    gestureResult.GestureProgress(contadorDetecciones);
                }
                break;
            case DETECTAR_SIN_GESTOS:
                if ((face.getLeftEyeOpenProbability() > 0.8)
                        && (face.getRightEyeOpenProbability() > 0.8)
                        && (face.getSmilingProbability() < 0.3)) {
                    contadorDetecciones += 1;
                } else {
                    contadorDetecciones = 0;
                    gestureResult.GestureProgress(contadorDetecciones);
                }
                break;

            /* manejo de pestaneo */
            case DETECTAR_PESTANEO_CLOSED_EYES:
                if ((face.getLeftEyeOpenProbability() > 0.8)
                        && (face.getRightEyeOpenProbability() > 0.8)
                        && (face.getSmilingProbability() < 0.3)) {
                    contadorDetecciones += 1;
                    gestureToDetect = DETECTAR_PESTANEO_NEUTRAL;
                }
                break;
            case DETECTAR_PESTANEO_NEUTRAL:
                if ((face.getLeftEyeOpenProbability() < 0.3)
                        && (face.getRightEyeOpenProbability() < 0.3)
                        && (face.getSmilingProbability() < 0.3)) {
                    contadorDetecciones += 1;
                    gestureToDetect = DETECTAR_PESTANEO_CLOSED_EYES;
                }
                break;
        }

        gestureResult.GestureProgress(contadorDetecciones);
        if (contadorDetecciones > CANTIDAD_DETECCIONES_NECESARIAS) {
            gestureResult.GestureDetected(face, bitmap, gestureToDetect);
        }
    }


    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Face detection failed " + e);
    }
}
