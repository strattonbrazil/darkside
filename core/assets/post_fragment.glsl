
#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D u_texture2;
uniform vec2 blurDir;

vec4 sampleColor(in vec2 screenCoords)
{
    vec4 attrs = texture2D(u_texture2, screenCoords);
    vec3 normal;
    normal.xy = attrs.xy; // unpack normal from buffer
    normal.z = sqrt(1-dot(normal.xy, normal.xy));

    vec3 lightDir = normalize(vec3(1,1,0.5));
    vec3 diffuse = texture2D(u_texture, screenCoords).rgb;

    vec3 color = diffuse * dot(lightDir, normal);

    return vec4(color, 1);
}

void main() {
    // accumulate color for motion blur
    vec3 color = vec3(0);
    for(int i = 0; i < 60; i++) {
        float offset = 0.1 * (i/60);
        vec2 blurCoords = v_texCoords + blurDir * (i/60.0);
        color += sampleColor(blurCoords).rgb / 60;
    }

    float part = 0.999f;

    gl_FragColor = vec4(color, 1.0) * part + texture2D(u_texture2, v_texCoords) * (1-part);
}
