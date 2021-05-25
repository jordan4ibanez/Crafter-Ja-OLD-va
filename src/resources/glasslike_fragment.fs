#version 330

in  vec3 exColor;
in  vec2 outTexCoord;

out vec4 fragColor;

uniform sampler2D texture_sampler;

void main()
{

   //thank you Th3HolyMoose!
    vec4 p = texture( texture_sampler, outTexCoord);
    if (p.a == 0.0) {
        discard;
    }

    fragColor = p * vec4(exColor, 1.0);
}