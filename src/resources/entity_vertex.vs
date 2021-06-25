#version 330

layout (location =0) in vec3 position;
layout (location =1) in vec3 inColor;
layout (location =2) in vec2 texCoord;

out vec3 exColor;
out vec2 outTexCoord;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;
uniform float light;

void main()
{
    gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);

    //turn the entity red if over 15
    if ( light > 15.0 ){
        float newLight = light - 15;
        exColor = inColor * vec3(2,0.5,0.5) * pow(pow((newLight / 15.0),1.5),1.5);
    } else {
        exColor = inColor * pow(pow((light / 15.0),1.5),1.5);
    }
    outTexCoord = texCoord;
}