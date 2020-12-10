package com.av.mediajourney.particles.programs;

import android.content.Context;
import android.opengl.GLES20;

import com.av.mediajourney.opengl.ShaderHelper;

public class ParticleShaderProgram {

    private final String U_TIME ="u_Time";
    private final String U_TEXTURE_UNIT ="u_TextureUnit";

    private final String A_POSITION="a_Position";
    private final String A_COLOR="a_Color";
    private final String A_DIRECTION="a_Direction";
    private final String A_PATRICLE_START_TIME="a_PatricleStartTime";

    private final int program;

    private final int uTimeLocation;
    private final int uTextureUnit;

    private final int aPositionLocation;
    private final int aColorLocation;
    private final int aDirectionLocation;
    private final int aPatricleStartTimeLocation;



    public ParticleShaderProgram(Context context) {
        //生成program
        String vertexShaderCoder = ShaderHelper.loadAsset(context.getResources(), "particle_vertex_shader.glsl");
        String fragmentShaderCoder = ShaderHelper.loadAsset(context.getResources(), "particle_fragment_shader.glsl");
        this.program = ShaderHelper.loadProgram(vertexShaderCoder,fragmentShaderCoder);

        //获取uniform 和attribute的location

        uTimeLocation = GLES20.glGetUniformLocation(program,U_TIME);
        uTextureUnit = GLES20.glGetUniformLocation(program,U_TEXTURE_UNIT);

        aPositionLocation = GLES20.glGetAttribLocation(program,A_POSITION);
        aColorLocation = GLES20.glGetAttribLocation(program,A_COLOR);
        aDirectionLocation = GLES20.glGetAttribLocation(program,A_DIRECTION);
        aPatricleStartTimeLocation = GLES20.glGetAttribLocation(program,A_PATRICLE_START_TIME);
    }

    /**
     * 设置 始终如一的Uniform变量
     * @param curTime
     */
    public void setUniforms(float curTime, int textureId){
        GLES20.glUniform1f(uTimeLocation,curTime);

        //激活纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //绑定纹理id
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureId);
        //赋值
        GLES20.glUniform1i(uTextureUnit,0);

    }

    public int getProgram() {
        return program;
    }

    public int getaPositionLocation() {
        return aPositionLocation;
    }

    public int getaColorLocation() {
        return aColorLocation;
    }

    public int getaDirectionLocation() {
        return aDirectionLocation;
    }

    public int getaPatricleStartTimeLocation() {
        return aPatricleStartTimeLocation;
    }

    public void useProgram(){
        GLES20.glUseProgram(program);
    }
}
