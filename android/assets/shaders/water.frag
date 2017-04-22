#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

uniform sampler2D u_texture;
uniform float u_time;
uniform vec3 u_light;

varying vec4 v_color;
varying vec2 v_texCoord;


void main() {
    vec2 sampleVec = v_texCoord;
    sampleVec.y -= u_time * .06;
    vec3 bump = normalize(2. * texture2D(u_texture, sampleVec).xyz - 1.);

    vec2 sampleVec2 = v_texCoord * 2.;
    sampleVec2.y -= u_time * .05;
    sampleVec2.x -= u_time * .01;
    vec3 bump2 = normalize(2. * texture2D(u_texture, sampleVec2).xyz -1.);

    vec2 sampleVec3 = v_texCoord * 4.;
    sampleVec3.y -= u_time * .05;
    sampleVec3.x += u_time * .01;
    vec3 bump3 = normalize(2. * texture2D(u_texture, sampleVec3).xyz -1.);

    vec3 totalwave = normalize(bump + bump2 + bump3);
    vec3 light = normalize(u_light);
    float lamberFactor= max (dot(light, totalwave), 0.0) ;

    float shininess = pow (max (dot (light, totalwave), 0.0), 2.0);

    gl_FragColor = lamberFactor * vec4( 0.5, 0.6, 1., .8);
}
