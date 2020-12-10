precision mediump float;

uniform sampler2D u_TextureUnit;

varying vec3 v_Color;
varying float v_ElapsedTime;

void main(){
    //粒子颜色随着颜色的推移变化
    //gl_FragColor = vec4(v_Color/v_ElapsedTime, 1.0);

    //通过内置函数texture2D和原来的fragcolor相乘
    gl_FragColor = vec4(v_Color/v_ElapsedTime, 1.0) * texture2D(u_TextureUnit, gl_PointCoord);

}