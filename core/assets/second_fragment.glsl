varying vec3 normal;

void main() {
    gl_FragColor = vec4(normal.x, normal.y, 0, 1);
}