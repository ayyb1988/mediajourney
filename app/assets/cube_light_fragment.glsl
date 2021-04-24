//#version 300 es
precision mediump float;
varying vec2 v_texCoord;
varying vec3 ambient;
varying vec3 diffuse;
varying vec3 specular;
uniform sampler2D uTexture;
void main()
{
    vec4 color = texture2D(uTexture, v_texCoord);
    vec3 finalColor = (ambient + diffuse +specular) * vec3(color);
    gl_FragColor = min(vec4(finalColor, color.a),vec4(1.0));
}