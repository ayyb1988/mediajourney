
uniform samplerCube uTexture;
varying vec3 vPosition;

void main() {
    gl_FragColor = textureCube(uTexture,vPosition);
}
