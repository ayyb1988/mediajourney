//#version 120

//定义一个不经常更改的mat4矩阵
uniform mat4 u_Matrix;

attribute vec4 a_Position;
attribute vec4 a_Color;

varying vec4 v_Color;

void main() {
    v_Color = a_Color;
    //gl_Position的赋值采用矩阵和a_Position相乘
    gl_Position = u_Matrix * a_Position;
}
