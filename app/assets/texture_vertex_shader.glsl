uniform mat4 u_Matrix;

attribute vec4 a_Position;
attribute vec3 a_Color;
attribute vec2 a_TextureCoordinates;

varying vec2 v_TextureCoordinates;
varying vec3 v_Color;


void main()                    
{                            
    v_TextureCoordinates = a_TextureCoordinates;
    v_Color = a_Color;

    gl_Position = a_Position;
}          