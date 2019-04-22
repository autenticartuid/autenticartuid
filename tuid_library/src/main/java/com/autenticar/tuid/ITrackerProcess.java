package com.autenticar.tuid;

import com.google.android.gms.vision.Detector;

public interface ITrackerProcess<T> {

    void ValueReaded(Detector.Detections<T> detectionResults);
}