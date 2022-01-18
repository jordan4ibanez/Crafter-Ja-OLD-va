#version 440

in  vec3 exColor;
in  vec2 outTexCoord;

out vec4 fragColor;

uniform sampler2D texture_sampler;


void main()
{
    //export the processed matrix to a pixel
    vec4 p = texture( texture_sampler, outTexCoord);

    if (p.a == 0.0) {
        discard;
    }

    fragColor = p * vec4(exColor, 1.0);
}