package com.av.mediajourney.particles;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.av.mediajourney.R;
import com.av.mediajourney.opengl.texture.util.TextureHelper;
import com.av.mediajourney.particles.android.util.MatrixHelper;
import com.av.mediajourney.particles.objects.ParticleFireworksExplosion;
import com.av.mediajourney.particles.objects.ParticleShooter;
import com.av.mediajourney.particles.objects.ParticleSystem;
import com.av.mediajourney.particles.programs.ParticleShaderProgram;
import com.av.mediajourney.particles.util.Geometry;

import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

public class ParticlesRender implements GLSurfaceView.Renderer {

    private final Context mContext;

    private ParticleShaderProgram mProgram;
    private ParticleSystem mParticleSystem;
    private long mSystemStartTimeNS;
    private ParticleShooter mParticleShooter;
    private int mTextureId;
    private ParticleFireworksExplosion particleFireworksExplosion;

    public ParticlesRender(Context context) {
        this.mContext = context;
    }

    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] viewProjectionMatrix = new float[16];

    private final float[] hsv = {0f, 1f, 1f};
    private Random random;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0f,0f,0f,0f);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);

        mProgram = new ParticleShaderProgram(mContext);

        //定义粒子系统 最大包含1w个粒子，超过最大之后复用最前面的
        mParticleSystem = new ParticleSystem(10000);

        //粒子系统开始时间
        mSystemStartTimeNS = System.nanoTime();

        //定义粒子发射器
        mParticleShooter = new ParticleShooter(new Geometry.Point(0f, -0.9f, 0f),
                Color.rgb(255, 50, 5),
                new Geometry.Vector(0f, 0.8f, 0f));

        mTextureId = TextureHelper.loadTexture(mContext, R.drawable.particle_texture);

        particleFireworksExplosion = new ParticleFireworksExplosion();
        random = new Random();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0,0,width,height);

        Matrix.perspectiveM(projectionMatrix, 0,45, (float) width
                / (float) height, 1f, 10f);

        setIdentityM(viewMatrix, 0);
        translateM(viewMatrix, 0, 0f, -1.5f, -5f);
        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0,
                viewMatrix, 0);
    }


    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        //当前（相对）时间 单位秒
        float curTime = (System.nanoTime() - mSystemStartTimeNS)/1000000000f;

        //粒发射器添加粒子
//        mParticleShooter.addParticles(mParticleSystem,curTime,20);
//          int color = Color.rgb(255, 50, 5);



        //烟花爆炸粒子发生器 添加粒子
        particleFireworksExplosion.addExplosion(
                mParticleSystem,
                new Geometry.Point(
                        0,
                        1f ,
                        0),
//                color,
                curTime);

        //使用Program
        mProgram.useProgram();
        //设置Uniform变量
        mProgram.setUniforms(viewProjectionMatrix,curTime,mTextureId);
        //设置attribute变量
        mParticleSystem.bindData(mProgram);
        //开始绘制粒子
        mParticleSystem.draw();
    }
}
