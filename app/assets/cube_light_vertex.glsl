//#version 300 es
precision mediump float;
attribute vec3 aPosition;
attribute vec2 aTexCoord;
attribute vec3 a_normal;

uniform mat4 uMatrix;
uniform mat4 u_ModelMatrix;
uniform vec3 lightPos;
uniform vec3 lightColor;
uniform vec3 viewPos;
varying vec2 v_texCoord;
varying vec3 ambient;
varying vec3 diffuse;
varying vec3 specular;
void main()
{
    gl_Position = uMatrix * vec4(aPosition,1.0);
//    gl_Position = vec4(aPosition,1.0);

    vec3 fragPos = vec3(u_ModelMatrix * vec4(aPosition,1.0));
//    vec3 fragPos = vec3(aPosition);

    // Ambient
    float ambientStrength = 0.1;
    ambient = ambientStrength * lightColor;

    // Diffuse
    float diffuseStrength = 0.5;
    vec3 unitNormal = normalize(vec3(u_ModelMatrix * vec4(a_normal, 1.0)));
    vec3 lightDir = normalize(lightPos - fragPos);
    float diff = max(dot(unitNormal, lightDir), 0.0);
    diffuse = diffuseStrength * diff * lightColor;

    // Specular
    float specularStrength = 0.9;
    vec3 viewDir = normalize(viewPos - fragPos);
    vec3 reflectDir = reflect(-lightDir, unitNormal);
    float spec = pow(max(dot(unitNormal, reflectDir), 0.0), 16.0);
    specular = specularStrength * spec * lightColor;
    v_texCoord = aTexCoord;
}