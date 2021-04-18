precision mediump float;
attribute vec3 aPosition;
attribute vec2 aTexCoord;

uniform mat4 uMatrix;
varying vec2 v_texCoord;

void main()
{
    gl_Position = uMatrix * vec4(aPosition,1.0);
    v_texCoord = aTexCoord;
}