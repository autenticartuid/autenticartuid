package com.autenticar.tuid.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.autenticar.tuid.IPushSize;
import com.autenticar.tuid.R;
import com.autenticar.tuid.utils.FlipAnimation;

public class DocumentOverlay extends LinearLayout {
    public static final int OFFSET = 10;
    private Bitmap bitmap;
    private int cntr = 0;
    private boolean goingup = false;
    private Paint mLaserPaint = new Paint();

    private ImageView imageViewFront, imageViewBack;

    public DocumentOverlay(Context context) {
        super(context);
    }

    public DocumentOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public DocumentOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DocumentOverlay(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    IPushSize iPushSize;

    public void showBack(boolean hide) {
        imageViewBack = findViewById(R.id.imgCardfront);
        imageViewBack.setVisibility(hide ? GONE : View.VISIBLE);
    }

    public void init(boolean hide, IPushSize iPushSize) {
        imageViewFront = findViewById(R.id.imgCardfront);
        this.iPushSize = iPushSize;
        imageViewFront.setVisibility(hide ? GONE : View.VISIBLE);

    }
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (bitmap == null) {
            createWindowFrame();
        }
        canvas.drawBitmap(bitmap, 0, 0, null);
        setWillNotDraw(false);
    }

    public void flipDoc() {
        imageViewFront = findViewById(R.id.imgCardfront);
        imageViewFront.setVisibility(View.VISIBLE);
        imageViewBack = findViewById(R.id.imgCardBack);
        FlipAnimation flipAnimation = new FlipAnimation(imageViewFront, imageViewBack);
        if (imageViewFront.getVisibility() == View.GONE) {
            flipAnimation.reverse();
        } else {
            imageViewFront.startAnimation(flipAnimation);
        }
    }

    protected void createWindowFrame() {
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas osCanvas = new Canvas(bitmap);

        RectF outerRectangle = new RectF(0, 0, getWidth(), getHeight());

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(getResources().getColor(R.color.black_overlay));
        paint.setAlpha(99); // oscuro
        osCanvas.drawRect(outerRectangle, paint);

        paint.setColor(Color.TRANSPARENT);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));

        RectF innerRectangle = getRectDocument();

        if (iPushSize != null)
            iPushSize.SetSize(innerRectangle);

        int cornersRadius = 25;
        osCanvas.drawRoundRect(
                innerRectangle, // rect
                cornersRadius, // rx
                cornersRadius, // ry
                paint // Paint
        );

        paint.setStrokeWidth(10);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        // BORDER
        osCanvas.drawRoundRect(innerRectangle, cornersRadius, // rx
                cornersRadius, paint);

        mLaserPaint.setColor(Color.RED);
        mLaserPaint.setAlpha(99);
        mLaserPaint.setStrokeWidth(5);
        mLaserPaint.setStyle(Paint.Style.STROKE);
    }

    @NonNull
    public RectF getRectDocument() {
        int margin = 40;
        double relation = 1.55;

        int documentHeight;
        int screenWidth = getWidth();
        int screenHeight = getHeight();
        int marginTop;
        int marginLeft;
        if (screenHeight > screenWidth) // portrait
        {
            documentHeight = (int) (screenWidth / relation);
            marginTop = (screenHeight - documentHeight) / 2;
            marginLeft = margin;
        } else {
            documentHeight = (int) ((screenHeight - margin));
            int documentWidth = (int) (documentHeight * relation);
            marginTop = margin;
            marginLeft = (screenWidth - documentWidth) / 2;
        }


        return new RectF(marginLeft, marginTop, getWidth() - marginLeft, getHeight() - marginTop);
    }

    public void drawLaser(Canvas canvas, RectF rectF) {

        int limit = (int) (rectF.height() / 2);

        int middle = (int) (rectF.height() / 2 + rectF.top);
        middle = middle + cntr;
        if ((cntr < limit) && (goingup == false)) {
            canvas.drawRect(rectF.left + 2, middle - 1, rectF.right - 1, middle + 2, mLaserPaint);
            cntr = cntr + OFFSET;
        }

        if ((cntr >= limit) && (goingup == false)) goingup = true;

        if ((cntr > -limit) && (goingup == true)) {
            canvas.drawRect(rectF.left + 2, middle - 1, rectF.right - 1, middle + 2, mLaserPaint);
            cntr = cntr - OFFSET;
        }

        if ((cntr <= -limit) && (goingup == true)) goingup = false;
        int POINT_SIZE = 3;
        postInvalidateDelayed(5,
                (int) rectF.left - POINT_SIZE,
                (int) rectF.top - POINT_SIZE,
                (int) rectF.right + POINT_SIZE,
                (int) rectF.bottom + POINT_SIZE);
    }

    @Override
    public boolean isInEditMode() {
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        bitmap = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {


        RectF rect = getRectDocument();
        drawLaser(canvas, rect);

        super.onDraw(canvas);

    }

}