precision mediump float;
varying vec2 v_texCoord;
uniform sampler2D uTexture;
void main()
{
    gl_FragColor = texture2D(uTexture, v_texCoord);
}