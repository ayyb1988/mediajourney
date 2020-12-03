attribute vec4 aPosition;
attribute vec2 aCoordinate;
uniform mat4 uMatrix;
uniform mat4 uCoordinateMatrix;
varying vec2 vTextureCoordinate;

void main(){
    gl_Position = uMatrix*aPosition;
    vTextureCoordinate = (uCoordinateMatrix*vec4(aCoordinate,0.1,0.1)).xy;
}
