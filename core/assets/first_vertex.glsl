attribute vec3 a_position;
uniform mat4 u_projTrans;
uniform mat4 u_worldTrans;

void main() {
    gl_Position = u_projTrans * u_worldTrans * vec4(a_position, 1.0);
}