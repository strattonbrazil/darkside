attribute vec3 a_position;
attribute vec3 a_normal;
uniform mat4 u_projTrans;
uniform mat4 u_worldTrans;

varying vec3 normal;

void main() {
    normal = (transpose(inverse(u_worldTrans)) * vec4(a_normal.xyz, 1.0)).xyz;
    gl_Position = u_projTrans * u_worldTrans * vec4(a_position, 1.0);
}