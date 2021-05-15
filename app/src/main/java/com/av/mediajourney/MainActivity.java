package com.av.mediajourney;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.av.mediajourney.audiorecord.AudioRecordActivity;
import com.av.mediajourney.audiotrack.AudioTrackStaticActivity;
import com.av.mediajourney.audiotrack.AudioTrackStreamActivity;
import com.av.mediajourney.bezier.BezierMainActivity;
import com.av.mediajourney.camera.CameraActivity;
import com.av.mediajourney.exoPlayer.ExoPlayerActivity;
import com.av.mediajourney.image.ImageActivity;
import com.av.mediajourney.mediaMuxer.MediaMuxerActivity;
import com.av.mediajourney.mediacodec.MediaCodecActivity;
import com.av.mediajourney.opengl.GlSurfaceActivity;
import com.av.mediajourney.opengl.gpuimage.GpuImageActivity;
import com.av.mediajourney.opengl.filter.FilterMainActivity;
import com.av.mediajourney.opengl.texture.GuangZhouTaTextureActivity;
import com.av.mediajourney.particles.ParticleActivity;
import com.av.mediajourney.skybox.CubeActivity;
import com.av.mediajourney.skybox.SkyBoxActivity;
import com.av.mediajourney.utils.PermissionCheckerUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv_audiotrackstream)
    TextView tvAudiotrackstream;
    @BindView(R.id.tv_image)
    TextView tvImage;
    @BindView(R.id.tv_audiorecord)
    TextView tvAudiorecord;
    @BindView(R.id.tv_audiotrackstaic)
    TextView tvAudiotrackstaic;
    @BindView(R.id.tv_camera)
    TextView tvCamera;
    @BindView(R.id.tv_muxer)
    TextView tvMuxer;
    @BindView(R.id.tv_mediacodec)
    TextView tvMediacodec;
    @BindView(R.id.tv_glsurfaceview)
    TextView tvGlsurfaceview;
    @BindView(R.id.tv_texture)
    TextView tvTexture;
    @BindView(R.id.tv_filter)
    TextView tvFilter;
    @BindView(R.id.tv_particles)
    TextView tvParticles;
    @BindView(R.id.tv_gpuimage)
    TextView tvGpuimage;
    @BindView(R.id.tv_bezier)
    TextView tvBezier;
    @BindView(R.id.tv_skybox)
    TextView tvSkybox;
    @BindView(R.id.tv_cube)
    TextView tvLight;
    @BindView(R.id.tv_exoplayer)
    TextView tvExoPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        PermissionCheckerUtil checker = new PermissionCheckerUtil(this);
        boolean isPermissionOK = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checker.checkPermission();
        if (!isPermissionOK) {
            Toast.makeText(this, "相机权限允许", Toast.LENGTH_SHORT).show();
            Log.d("MainActivity", "onCreate: 获取了权限 ");
        } else {
            Log.d("MainActivity", "onCreate: 无权限 ");
        }

    }

    @OnClick({R.id.tv_image, R.id.tv_audiorecord, R.id.tv_audiotrackstaic, R.id.tv_audiotrackstream,
            R.id.tv_camera, R.id.tv_muxer, R.id.tv_mediacodec, R.id.tv_glsurfaceview, R.id.tv_texture,
            R.id.tv_filter, R.id.tv_particles, R.id.tv_gpuimage, R.id.tv_bezier,R.id.tv_skybox,R.id.tv_cube,
            R.id.tv_exoplayer})
    public void onViewClicked(View view) {
        Class<?> targetClass = null;
        switch (view.getId()) {
            case R.id.tv_image:
                targetClass = ImageActivity.class;
                break;
            case R.id.tv_audiorecord:
                targetClass = AudioRecordActivity.class;
                break;
            case R.id.tv_audiotrackstaic:
                targetClass = AudioTrackStaticActivity.class;
                break;
            case R.id.tv_audiotrackstream:
                targetClass = AudioTrackStreamActivity.class;
                break;
            case R.id.tv_camera:
                targetClass = CameraActivity.class;
                break;
            case R.id.tv_muxer:
                targetClass = MediaMuxerActivity.class;
                break;
            case R.id.tv_mediacodec:
                targetClass = MediaCodecActivity.class;
                break;
            case R.id.tv_glsurfaceview:
//                targetClass = GuangZhouTaTextureActivity.class;
                targetClass = GlSurfaceActivity.class;
                break;
            case R.id.tv_texture:
                targetClass = GuangZhouTaTextureActivity.class;

//                targetClass = TextureActivity.class;
                break;
            case R.id.tv_filter:
                targetClass = FilterMainActivity.class;
                break;
            case R.id.tv_particles:
                targetClass = ParticleActivity.class;
//                targetClass = ParticlesActivity.class;
//                targetClass = ParticlesSkyBoxActivity.class;
//                targetClass = ParticlesHeightMapActivity.class;
                break;
            case R.id.tv_gpuimage:
                targetClass = GpuImageActivity.class;
                break;
            case R.id.tv_bezier:
//                targetClass = BezierActivity.class;
                targetClass = BezierMainActivity.class;
                break;
            case R.id.tv_skybox:
                targetClass = SkyBoxActivity.class;
                break;
            case R.id.tv_cube:
                targetClass = CubeActivity.class;
                break;
            case R.id.tv_exoplayer:
                targetClass = ExoPlayerActivity.class;
                break;

        }
        if (targetClass != null) {
            Intent intent = new Intent(MainActivity.this, targetClass);
            startActivity(intent);
        }





    }

}
