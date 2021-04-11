uniform mat4 uMatrix;
attribute vec3 aPosition;
varying vec3 vPosition;

void main() {
    vPosition = aPosition;
    gl_Position = uMatrix*vec4(aPosition, 1.0);

    gl_Position = gl_Position.xyww;
}
