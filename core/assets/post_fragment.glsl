
#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform mat4 u_projTrans;
uniform vec2 blurDir;

void main() {
        vec3 color = vec3(0);
        for(int i=0;i<60;i++){
            float offset = 0.1 * (i/60);
            vec2 blurCoords = v_texCoords + blurDir * (i/60.0);
            color += texture2D(u_texture, blurCoords).rgb / 60;
        }
        //float gray = (color.r + color.g + color.b) / 3.0;
        //vec3 grayscale = vec3(gray);

        gl_FragColor = vec4(color, 1.0);
}
