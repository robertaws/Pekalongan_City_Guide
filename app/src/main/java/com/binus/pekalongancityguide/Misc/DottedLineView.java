package com.binus.pekalongancityguide.Misc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class DottedLineView extends View {

    private Paint mPaint;

    public DottedLineView(Context context) {
        super(context);
        init();
    }

    public DottedLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DottedLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.GRAY);
        mPaint.setStrokeWidth(3);
        mPaint.setPathEffect(new DashPathEffect(new float[]{10, 20}, 0));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int x = getWidth() / 2;

        Path path = new Path();
        path.moveTo(x, 0);
        path.lineTo(x, getHeight());

        canvas.drawPath(path, mPaint);
    }
}
