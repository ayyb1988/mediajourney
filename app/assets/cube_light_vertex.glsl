//#version 300 es
precision mediump float;
attribute vec3 aPosition;
attribute vec2 aTexCoord;
attribute vec3 aNormal;

uniform mat4 uMatrix;
uniform mat4 uModelMatrix;
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

    vec3 fragPos = vec3(uModelMatrix * vec4(aPosition,1.0));

    // 环境光
    //强度
    float ambientStrength = 0.3;
    ambient = ambientStrength * lightColor;

    // 漫反射
    float diffuseStrength = 0.8;
    //顶点的单位法线
    vec3 unitNormal = normalize(vec3(uModelMatrix * vec4(aNormal, 1.0)));
    //从顶点到光源的单位向量
    vec3 lightDir = normalize(lightPos - fragPos);
    //上面来两个向量进行点乘
    float diff = max(dot(unitNormal, lightDir), 0.0);
    diffuse = diffuseStrength * diff * lightColor;
//
     //镜面反射
    float specularStrength = 0.9;
    //视角和顶点的单位向量
    vec3 viewDir = normalize(viewPos - fragPos);
    //调用reflect反射内置函数
    vec3 reflectDir = reflect(-lightDir, unitNormal);
    float spec = pow(max(dot(unitNormal, reflectDir), 0.0), 16.0);
    specular = specularStrength * spec * lightColor;

    v_texCoord = aTexCoord;
}