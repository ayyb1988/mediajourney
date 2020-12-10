package com.av.mediajourney.particles.objects;

import android.opengl.Matrix;

import com.av.mediajourney.particles.util.Geometry;

import java.util.Random;

public class ParticleShooter {

    //发射粒子的位置
    private final Geometry.Point position;
    //发射粒子的颜色
    private final int color;
    //发射粒子的方法
    private final Geometry.Vector direction;

    private float[] rotationMatrix = new float[16];
    private final Random random = new Random();
    final float angleVarianceInDegrees = 20f;

    public ParticleShooter(Geometry.Point position, int color, Geometry.Vector direction) {
        this.position = position;
        this.color = color;
        this.direction = direction;
    }

    /**
     * 调用粒子系统对象添加粒子
     *
     * @param particleSystem
     * @param currentTime
     * @param count
     */
    public void addParticles(ParticleSystem particleSystem, float currentTime, int count) {

        for (int i = 0; i < count; i++) {
            Matrix.setRotateEulerM(rotationMatrix, 0,
                    (random.nextFloat() - 0.5f) * angleVarianceInDegrees,
                    (random.nextFloat() - 0.5f) * angleVarianceInDegrees,
                    (random.nextFloat() - 0.5f) * angleVarianceInDegrees);

            float[] tmpDirectionFloat = new float[4];

            Matrix.multiplyMV(tmpDirectionFloat, 0,
                    rotationMatrix, 0,
                    new float[]{direction.x, direction.y, direction.z, 1f}, 0);

            Geometry.Vector newDirection = new Geometry.Vector(tmpDirectionFloat[0], tmpDirectionFloat[1], tmpDirectionFloat[2]);

            particleSystem.addParticle(position, color, newDirection, currentTime);

        }
    }
}
