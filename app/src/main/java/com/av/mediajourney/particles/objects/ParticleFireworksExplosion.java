/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
 ***/
package com.av.mediajourney.particles.objects;

import android.graphics.Color;
import android.opengl.Matrix;
import android.util.Log;

import com.av.mediajourney.particles.util.Geometry;

import java.util.Random;

import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.setRotateEulerM;


public class ParticleFireworksExplosion {

    private float[] mDirectionVector = {0f, 0f, 0.3f, 1f};
    private float[] mResultVector = new float[4];
    private final Random mRandom = new Random();
    private float[] mRotationMatrix = new float[16];

    private final int mPreAddParticleCount = 100;
    private final float[] hsv = {0f, 1f, 1f};


    public void addExplosion(ParticleSystem particleSystem, Geometry.Point position, float curTime) {


        //不是OnDrawFrame就添加烟花爆炸粒子，而是采用1/100的采样率 ，让粒子飞一会，从而产生烟花爆炸效果
        if (mRandom.nextFloat() < 1.0f / mPreAddParticleCount) {

            hsv[0] = mRandom.nextInt(360);
            int color = Color.HSVToColor(hsv);

            //同一时刻添加100*5个方向360随机的粒子
            for (int i = 0; i < mPreAddParticleCount *3 ; i++) {
                Matrix.setRotateEulerM(mRotationMatrix, 0, mRandom.nextFloat() * 360, mRandom.nextFloat() * 360, mRandom.nextFloat() * 360);
                Matrix.multiplyMV(mResultVector, 0, mRotationMatrix, 0, mDirectionVector, 0);
                particleSystem.addParticle(
                        position,
                        color,
                        new Geometry.Vector(mResultVector[0],
                                mResultVector[1]+0.3f,
                                mResultVector[2]),
                        curTime);
            }
        }
    }
}
