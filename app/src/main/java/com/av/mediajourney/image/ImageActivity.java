package com.av.mediajourney.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.av.mediajourney.R;

import java.io.File;

public class ImageActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ImageView imageView;
    private SurfaceView surfaceView;
    private CustomView customView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_layout);

        imageView = findViewById(R.id.imageview);
        surfaceView = findViewById(R.id.surfaceview);
        customView = findViewById(R.id.customview);

        String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "Pictures"/* + File.separator + "kugou" */+ File.separator + "kg_1602121905506.jpg";
        Log.d("MainActivity", "onCreate: path: "+path);
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        imageView.setImageBitmap(bitmap);
        handSurfaceview();
//        PermissionHandler.requestCameraPermission(MainActivity.this, PermissionHandler.storagePermissions, "请求存储权限。请在【设置-应用-权限】中开启存储权限，以正常使用",
//                new Runnable() {
//                    @Override
//                    public void run() {
//                        handSurfaceview();
//                    }
//                }, new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.d("MainActivity", "onCreate: 无权限 ");
//                    }
//                });





    }

    private void handSurfaceview() {
        final String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "Pictures" /*+ File.separator + "kugou" */+ File.separator + "kg_1602121905506.jpg";
        Log.d("MainActivity", "onCreate: path: "+path);
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        imageView.setImageBitmap(bitmap);


        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if(holder == null){
                    return;
                }
                Paint paint = new Paint();
                paint.setAntiAlias(true);
//                                paint.setStyle(Paint.Style.STROKE);

                String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "Pictures" /*+ File.separator + "kugou"*/ + File.separator + "kg_1602121905506.jpg";

                Bitmap bitmap1 = BitmapFactory.decodeFile(path);

                Canvas canvas = null;
                try {
                    canvas = holder.lockCanvas();
                    canvas.drawBitmap(bitmap1,0,0,paint);
                }catch (Exception e){
                    e.printStackTrace();
                } finally {
                    if(canvas!=null){
                        holder.unlockCanvasAndPost(canvas);
                    }

                }

                Log.d(TAG, "surfaceCreated: ");




            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d(TAG, "surfaceChanged: format"+format+" w="+width+" h="+height);

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d(TAG, "surfaceDestroyed: ");

            }
        });
    }
}
