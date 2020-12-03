precision mediump float;

uniform sampler2D u_TextureUnit;
uniform int u_TypeIndex;
varying vec2 v_TextureCoordinates;
varying vec3 v_Color;

void main()
{
    vec4 color = texture2D(u_TextureUnit, v_TextureCoordinates);

    if (u_TypeIndex == 0){
        gl_FragColor = color;
    } else if (u_TypeIndex == 1){
        float c = color.r * v_Color.r +
        color.g * v_Color.g +
        color.b * v_Color.b;
        gl_FragColor = vec4(c, c, c, 1.0f);
    } else {
        vec4 newColor = color + vec4(v_Color, 0.0f);
        gl_FragColor = newColor;
    }


}