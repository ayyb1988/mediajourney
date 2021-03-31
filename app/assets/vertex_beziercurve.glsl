//#version 120
attribute float a_tData;
uniform vec4 u_startEndData;
uniform vec4 u_ControlData;
uniform mat4 u_MVPMatrix;
uniform float u_offset;

vec2 bezierMix(vec2 p0, vec2 p1, vec2 p2, vec2 p3, float t)
{
    vec2 q0 = mix(p0, p1, t);
    vec2 q1 = mix(p1, p2, t);
    vec2 q2 = mix(p2, p3, t);

    vec2 r1 = mix(q0, q1, t);
    vec2 r2 = mix(q1, q2, t);

    return mix(r1, r2, t);
}

void main() {
    vec4 pos;
    pos.w=1.0;

    vec2 p0 = u_startEndData.xy;
    vec2 p3 = u_startEndData.zw;

    vec2 p1= u_ControlData.xy;
    vec2 p2= u_ControlData.zw;

    float t= a_tData;


    p0.y *= u_offset;
    p1.y *= u_offset;
    p2.y *= u_offset;
    p3.y *= u_offset;

    vec2 point = bezierMix(p0, p1, p2, p3, t);


    if (t<0.0)
    {
        pos.xy = vec2(0.0, 0.0);
    } else {
        pos.xy = point;
    }

    gl_PointSize = 4.0f;
    gl_Position = u_MVPMatrix * pos;

}
