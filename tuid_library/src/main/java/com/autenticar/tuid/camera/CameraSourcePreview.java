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
package com.autenticar.tuid.camera;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.autenticar.tuid.IPushSize;
import com.autenticar.tuid.model.Persona;
import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.CameraSource;

import java.io.IOException;
import java.util.List;

public class CameraSourcePreview extends ViewGroup implements IPushSize {
    private static final String TAG = "CameraSourcePreview";

    private Context mContext;
    private SurfaceView mSurfaceView;
    private boolean mStartRequested;
    private boolean mSurfaceAvailable;
    private CameraSource mCameraSource;

    private RectF docSize;
    private GraphicOverlay mOverlay;
    private List<Camera.Size> mSupportedPreviewSizes;

    public CameraSourcePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mStartRequested = false;
        mSurfaceAvailable = false;


        mSurfaceView = new SurfaceView(context);
        mSurfaceView.getHolder().addCallback(new SurfaceCallback());

        addView(mSurfaceView);
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix,
                true);
    }

    private Bitmap rotateImage(Bitmap bm, int i) {
        Matrix matrix = new Matrix();
        switch (i) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bm;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bm;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
            bm.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    //1 face 2 front dni  3 back dni
    public void takePicture(final Persona person, final int destin, final RectF size) {
        mCameraSource.takePicture(null, new
                CameraSource.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] bytes) {
            /*
            int orientation = Exif.getOrientation(bytes);
            Bitmap bitmap = null;

            double factorW = 1;
            double factorH = 1;
            switch (orientation) {
                case 90:
                    bitmap = rotateImage(bitmapOrigen, ExifInterface.ORIENTATION_ROTATE_90); // invierto los lados
                    factorW = ((double) bitmap.getWidth()) / ((double) mCameraSource.getPreviewSize().getHeight());
                    factorH = ((double) bitmap.getHeight()) / ((double) mCameraSource.getPreviewSize().getWidth());
                    break;
                case 180:
                    bitmap = rotateImage(bitmapOrigen, ExifInterface.ORIENTATION_ROTATE_180);
                    factorW = ((double) bitmap.getWidth()) / ((double) mCameraSource.getPreviewSize().getWidth());
                    factorH = ((double) bitmap.getHeight()) / ((double) mCameraSource.getPreviewSize().getHeight());
                    break;
                case 270:
                    bitmap = rotateImage(bitmapOrigen, ExifInterface.ORIENTATION_ROTATE_270); // invierto los lados
                    factorW = ((double) bitmap.getWidth()) / ((double) mCameraSource.getPreviewSize().getHeight());
                    factorH = ((double) bitmap.getHeight()) / ((double) mCameraSource.getPreviewSize().getWidth());
                    break;
                default:
                    bitmap = bitmapOrigen;
                    factorW = ((double) bitmapOrigen.getWidth()) / ((double) mCameraSource.getPreviewSize().getWidth());
                    factorH = ((double) bitmapOrigen.getHeight()) / ((double) mCameraSource.getPreviewSize().getHeight());
                    break;
            }
*/
            Bitmap bitmapOrigen = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            //achico la imagen
            Bitmap croppedImage = scaleBitmap(bitmapOrigen);
            bitmapOrigen.recycle();
            boolean rotated = false;
            if (getOrientation(croppedImage) == 90) {
                croppedImage = rotateImage(croppedImage, ExifInterface.ORIENTATION_ROTATE_90); // invierto los lados
            }
            Bitmap bitmap = croppedImage; // rotateImage(croppedImage, ExifInterface.ORIENTATION_ROTATE_90); // invierto los lados

            //la llevo al tamano de la pantalla
            double factorW = 1;
            double factorH = 1;

            if (rotated) {
                factorW = ((double) bitmap.getWidth()) / ((double) mCameraSource.getPreviewSize().getWidth());
                factorH = ((double) bitmap.getHeight()) / ((double) mCameraSource.getPreviewSize().getHeight());
            } else {
                factorW = ((double) bitmap.getWidth()) / ((double) mCameraSource.getPreviewSize().getHeight());
                factorH = ((double) bitmap.getHeight()) / ((double) mCameraSource.getPreviewSize().getWidth());
            }
            int left;
            int top;
            int width;
            int height;

            if (size != null) {
                left = (int) (size.left * factorW);
                top = (int) (size.top * factorH);
                width = (int) (size.right * factorW);
                height = (int) (size.bottom * factorH);
            } else {
                //si la imagen es mas chica que el documento tomo la imagen de base
                left = (int) (docSize.left * factorW);
                top = (int) (docSize.top * factorH);
                width = (int) ((docSize.right - docSize.left) * factorW);
                if (width > bitmap.getWidth()) {
                    //left = left - (width - bitmap.getWidth()); //me corro a la izquierda
                    width = bitmap.getWidth();
                }

                height = (int) ((docSize.bottom - docSize.top) * factorH);
                if (height > bitmap.getHeight()) {
                    //top = top- (height-bitmap.getHeight()); //subo
                    height = bitmap.getHeight(); //subo
                }
            }
            try {
                croppedImage = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            } catch (Exception e) {
                throw e;
            }
            if (croppedImage == null) {
                return;
            }

            Rect r = new Rect();
            r.left = left;
            r.top = top;
            r.right = left + width;
            r.bottom = top + height;

            Canvas canvas = new Canvas(croppedImage);
            Rect dstRect = new Rect(0, 0, width, height);
            canvas.drawBitmap(bitmap, r, dstRect, null);

            switch (destin) {
                case 1:
                    //right y bottom son width y height
                    person.SetFaceDetected(croppedImage);
                    break;
                case 2:
                    person.dniFront = croppedImage;
                    break;
                case 3:
                    person.dniBack = croppedImage;
                    break;
            }
        }
        });
    }

    private int getOrientation(Bitmap bitmap) {
        if (bitmap.getWidth() > bitmap.getHeight()) {
            return 90;
        }
        return 0;
    }

    private Bitmap scaleBitmap(Bitmap bm) {
        int width = bm.getWidth();
        int height = bm.getHeight();

        Log.v("Pictures", "Width and height are " + width + "--" + height);

        if (width > height) {
            // landscape
            float ratio = (float) width / 660;
            width = 660;
            height = (int) (height / ratio);
        } else if (height > width) {
            // portrait
            float ratio = (float) height / 660;
            height = 660;
            width = (int) (width / ratio);
        } else {
            // square
            height = 660;
            width = 660;
        }

        Log.v("Pictures", "after scaling Width and height are " + width + "--" + height);

        bm = Bitmap.createScaledBitmap(bm, width, height, true);
        return bm;
    }

    public void start(CameraSource cameraSource) throws IOException {
        if (cameraSource == null) {
            stop();
        }

        mCameraSource = cameraSource;

        if (mCameraSource != null) {
            mStartRequested = true;
            startIfReady();
        }
    }

    public void start(CameraSource cameraSource, GraphicOverlay overlay) throws IOException {
        mOverlay = overlay;

        start(cameraSource);
    }

    public void stop() {
        if (mCameraSource != null) {
            mCameraSource.stop();
        }
    }

    public void release() {
        if (mCameraSource != null) {
            mCameraSource.release();
            mCameraSource = null;
        }
    }

    public void setCamera(Camera mCamera) {
        mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();

    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.height / size.width;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;

            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        return optimalSize;
    }

    private void startIfReady() throws IOException {
        if (mStartRequested && mSurfaceAvailable) {
            mCameraSource.start(mSurfaceView.getHolder());
            if (mOverlay != null) {
                Size size = mCameraSource.getPreviewSize();
                int min = Math.min(size.getWidth(), size.getHeight());
                int max = Math.max(size.getWidth(), size.getHeight());
                if (isPortraitMode()) {
                    // Swap width and height sizes when in portrait, since it will be rotated by
                    // 90 degrees
                    mOverlay.setCameraInfo(min, max, mCameraSource.getCameraFacing());
                } else {
                    mOverlay.setCameraInfo(max, min, mCameraSource.getCameraFacing());
                }
                mOverlay.clear();
            }
            mStartRequested = false;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = 320;
        int height = 240;

        if (mCameraSource != null) {

            Size size = mCameraSource.getPreviewSize();
            if (size != null && mSupportedPreviewSizes != null) {

                Camera.Size size2 = getOptimalPreviewSize(mSupportedPreviewSizes, size.getWidth(), size.getHeight());
                if (size != null) {
                    width = size2.width;
                    height = size2.height;

                }
            } else {
                size = mCameraSource.getPreviewSize();
                if (size != null) {
                    width = size.getWidth();
                    height = size.getHeight();
                }
            }


        }

        // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
        if (isPortraitMode()) {
            int tmp = width;
            width = height;
            height = tmp;
        }

        final int layoutWidth = right - left;
        final int layoutHeight = bottom - top;

        // Computes height and width for potentially doing fit width.
        int childWidth = layoutWidth;
        int childHeight = (int) (((float) layoutWidth / (float) width) * height);

        // If height is too tall using fit width, does fit height instead.
        if (childHeight > layoutHeight) {
            childHeight = layoutHeight;
            childWidth = (int) (((float) layoutHeight / (float) height) * width);
        }

        for (int i = 0; i < getChildCount(); ++i) {
            getChildAt(i).layout(0, 0, childWidth, childHeight); //childHeight
        }

        try {
            startIfReady();
        } catch (IOException e) {
            Log.e(TAG, "Could not start camera source.", e);
        }
    }

    private boolean isPortraitMode() {
        int orientation = mContext.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return false;
        }
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return true;
        }

        Log.d(TAG, "isPortraitMode returning false by default");
        return false;
    }

    @Override
    public void SetSize(RectF size) {
        docSize = size;
    }

    private class SurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder surface) {
            mSurfaceAvailable = true;
            try {
                startIfReady();
            } catch (IOException e) {
                Log.e(TAG, "Could not start camera source.", e);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surface) {
            mSurfaceAvailable = false;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }
    }


}
