#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

uniform float u_test;
#ifdef HasDiffuseColor
uniform vec4 u_color;
#endif //HasDiffuseColor

void main() {
#ifdef HasDiffuseColor
    gl_FragColor.rgb = u_color.rgb * vec3(u_test);
#else
    gl_FragColor.rgb = vec3(u_test);
#endif //HasDiffuseColor
    gl_FragColor.rgba = vec4(1,0,0,1);
}