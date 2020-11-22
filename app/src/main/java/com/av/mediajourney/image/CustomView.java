package com.av.mediajourney.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.io.File;

public class CustomView extends View {
    private Paint paint;
    private Bitmap bitmap;

    public CustomView(Context context) {
        this(context,null,0);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "Pictures" /*+ File.separator + "kugou" */+ File.separator + "kg_1602121905506.jpg";

        bitmap = BitmapFactory.decodeFile(path);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(bitmap!=null&&!bitmap.isRecycled()) {
            canvas.drawBitmap(bitmap, 0, 0, paint);
        }
    }
}
