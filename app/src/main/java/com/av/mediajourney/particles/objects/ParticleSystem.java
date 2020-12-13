package com.av.mediajourney.particles.objects;

import android.graphics.Color;
import android.opengl.GLES20;

import com.av.mediajourney.particles.data.VertexArray;
import com.av.mediajourney.particles.programs.ParticleShaderProgram;
import com.av.mediajourney.particles.util.Geometry;

public class ParticleSystem {
    //位置 xyz
    private final int POSITION_COMPONENT_COUNT = 3;
    //颜色 rgb
    private final int COLOR_COMPONENT_COUNT = 3;
    //运动矢量 xyz
    private final int VECTOR_COMPONENT_COUNT = 3;
    //开始时间
    private final int PARTICLE_START_TIME_COMPONENT_COUNT = 1;


    private final int TOTAL_COMPONENT_COUNT = POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT
            + VECTOR_COMPONENT_COUNT + PARTICLE_START_TIME_COMPONENT_COUNT;

    //步长
    private final int STRIDE = TOTAL_COMPONENT_COUNT * VertexArray.BYTES_PER_FLOAT;


    //粒子游标
    private int nextParticle;
    //粒子计数
    private int curParticleCount;
    //粒子数组
    private final float[] particles;
    //最大粒子数量
    private final int maxParticleCount;
    //VBO
    private final VertexArray vertexArray;

    public ParticleSystem(int maxParticleCount) {
        this.particles = new float[maxParticleCount * TOTAL_COMPONENT_COUNT];
        this.maxParticleCount = maxParticleCount;
        this.vertexArray = new VertexArray(particles);
    }

    /**
     * 添加粒子到FloatBuffer
     *
     * @param position        位置
     * @param color           颜色
     * @param direction       运动矢量
     * @param particStartTime 开始时间
     */
    public void addParticle(Geometry.Point position, int color, Geometry.Vector direction, float particStartTime) {
        final int particleOffset = nextParticle * TOTAL_COMPONENT_COUNT;
        int currentOffset = particleOffset;
        nextParticle++;
        if (curParticleCount < maxParticleCount) {
            curParticleCount++;
        }
        //重复使用，避免内存过大
        if (nextParticle == maxParticleCount) {
            nextParticle = 0;
        }
        //填充 位置坐标 xyz
        particles[currentOffset++] = position.x;
        particles[currentOffset++] = position.y;
        particles[currentOffset++] = position.z;

        //填充 颜色 rgb
        particles[currentOffset++] = Color.red(color) / 255f;
        particles[currentOffset++] = Color.green(color) / 255f;
        particles[currentOffset++] = Color.blue(color) / 255f;

        //填充 运动矢量
        particles[currentOffset++] = direction.x;
        particles[currentOffset++] = direction.y;
        particles[currentOffset++] = direction.z;

        //填充粒子开始时间
        particles[currentOffset++] = particStartTime;

        //把新增的粒子添加到顶点数组FloatBuffer中
        vertexArray.updateBuffer(particles, particleOffset, TOTAL_COMPONENT_COUNT);
    }

    public void bindData(ParticleShaderProgram program) {
        int dataOffset = 0;
        vertexArray.setVertexAttributePointer(dataOffset,
                program.getaPositionLocation(),
                POSITION_COMPONENT_COUNT, STRIDE);
        dataOffset +=POSITION_COMPONENT_COUNT;

        vertexArray.setVertexAttributePointer(dataOffset,
                program.getaColorLocation(),
                COLOR_COMPONENT_COUNT, STRIDE);
        dataOffset +=COLOR_COMPONENT_COUNT;

        vertexArray.setVertexAttributePointer(dataOffset,
                program.getaDirectionLocation(),
                VECTOR_COMPONENT_COUNT, STRIDE);
        dataOffset +=VECTOR_COMPONENT_COUNT;

        vertexArray.setVertexAttributePointer(dataOffset,
                program.getaPatricleStartTimeLocation(),
                PARTICLE_START_TIME_COMPONENT_COUNT, STRIDE);
    }

    public void draw() {
        GLES20.glDrawArrays(GLES20.GL_POINTS,0,curParticleCount);
    }
}
