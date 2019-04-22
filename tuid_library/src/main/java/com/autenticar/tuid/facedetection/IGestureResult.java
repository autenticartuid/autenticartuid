package com.autenticar.tuid.facedetection;

import android.graphics.Bitmap;

import com.google.firebase.ml.vision.face.FirebaseVisionFace;

public interface IGestureResult {

    void GestureProgress(int valor);

    void GestureDetected(FirebaseVisionFace face, Bitmap bitmap, int gestureType);
}
