//#version 300 es
precision mediump float;
varying vec2 v_texCoord;
varying vec3 ambient;
varying vec3 diffuse;
varying vec3 specular;
uniform sampler2D uTexture;
void main()
{
//    vec4 objectColor = texture(uTexture, v_texCoord);
//    vec3 finalColor = (ambient + diffuse + specular) * vec3(objectColor);
    //gl_FragColor = vec4(finalColor, 1.0);
    gl_FragColor = texture2D(uTexture, v_texCoord);
}