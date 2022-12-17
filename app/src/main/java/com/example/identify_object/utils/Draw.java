package com.example.identify_object.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

public class Draw extends View {

    Paint boundaryPaint;
    Paint textPaint;
    String text;
    Rect rect;


    public Draw(Context context, Rect rect, String text) {
        super(context);
        this.rect = rect;
        this.text = text;
        init();
    }


    private void init(){
        boundaryPaint = new Paint();
        boundaryPaint.setColor(Color.RED);
        boundaryPaint.setStrokeWidth(10f);
        boundaryPaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint();
        textPaint.setColor(Color.BLUE);
        textPaint.setTextSize(50f);
        textPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawText(text,(float)rect.centerX(),(float)rect.centerY(),textPaint);
        canvas.drawRect((float)rect.left, (float)rect.top, (float)rect.right, (float)rect.bottom, boundaryPaint);
    }
}
