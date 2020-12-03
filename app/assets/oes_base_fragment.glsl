#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform int u_TypeIndex;
uniform vec3 u_Color;


varying vec2 vTextureCoordinate;
uniform samplerExternalOES uTexture;
void main() {

    vec4 color = texture2D(uTexture,vTextureCoordinate);

    if (u_TypeIndex == 0){
        gl_FragColor = color;
    } else if (u_TypeIndex == 1){
        float c = color.r * u_Color.r +
        color.g * u_Color.g +
        color.b * u_Color.b;
        gl_FragColor = vec4(c, c, c, 1.0f);
    } else {
        vec4 newColor = color + vec4(u_Color, 0.0f);
        gl_FragColor = newColor;
    }
}