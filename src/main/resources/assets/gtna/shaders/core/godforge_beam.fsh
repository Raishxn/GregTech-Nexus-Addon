#version 150

// GT5-Unofficial (a3e1e112) tectech gorgeBeam.frag, LGPL-3.0.
uniform sampler2D Sampler0;
uniform vec3 CameraLocal;
uniform vec3 BeamColor;
uniform float Intensity;

in vec2 texCoord0;
in vec2 localPosition;
in float transparency;

out vec4 fragColor;

float luminanceTransform(vec3 color) {
    return 0.2126 * color.r + 0.7152 * color.g + 0.0722 * color.b;
}

void main() {
    float effect = dot(normalize(CameraLocal.xy), normalize(localPosition));
    float fade = pow(max((effect - .5) * 2.0, 0.0), Intensity);
    vec4 texColor = texture(Sampler0, texCoord0);
    float luminance = 1.0 - luminanceTransform(texColor.rgb);
    luminance = mix(luminance, 1.0, 1.0 - pow(fade, 6.0));
    fragColor = vec4(BeamColor, fade * transparency * luminance);
}
