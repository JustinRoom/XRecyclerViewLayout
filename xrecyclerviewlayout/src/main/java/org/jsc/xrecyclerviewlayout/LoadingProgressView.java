package org.jsc.xrecyclerviewlayout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by jsc on 2016/7/21.
 */
public class LoadingProgressView extends View{

    private final int[] colors = new int[]{
            Color.parseColor("#FF0000"),
            Color.parseColor("#FFFF00"),
            Color.parseColor("#008000"),
            Color.parseColor("#00FFFF"),
            Color.parseColor("#0000FF"),
            Color.parseColor("#800080")};
    private int DEFAULT_WIDTH = 0;

    private Paint mPaint;
    private int ballRadius = 0;

    public LoadingProgressView(Context context) {
        this(context, null);
    }

    public LoadingProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init(){
        DEFAULT_WIDTH = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40.0f, getContext().getResources().getDisplayMetrics());

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        if (width == 0)
            width = DEFAULT_WIDTH;
        int halfWidth = width / 2;
        int ballRadius = halfWidth / 5;

        float xCenter = width - ballRadius;
        float yCenter = halfWidth;

        for (int i = 0; i < 6; i ++){
            mPaint.setColor(colors[i]);

//            canvas.translate(halfWidth, halfWidth);
//            canvas.rotate(-60.0f);
//            canvas.translate(-halfWidth, -halfWidth);
            canvas.rotate(-60.0f, halfWidth, halfWidth);

            canvas.drawCircle(xCenter, yCenter, ballRadius, mPaint);
        }
    }
}
