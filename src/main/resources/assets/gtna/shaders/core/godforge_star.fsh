#version 150

// GT5-Unofficial (a3e1e112) tectech star.frag, LGPL-3.0: keeps the texture luma and takes the
// chroma of the star color in YIQ space, then applies the star gamma.
uniform sampler2D Sampler0;
uniform vec4 StarColor;
uniform float Gamma;

in vec2 texCoord0;

out vec4 fragColor;

vec3 toYIQ(vec3 rgb) {
    return mat3(0.299, 1.0, 0.40462981, 0.587, -0.46081557, -1.0, 0.114, -0.53918443, 0.59537019) * rgb;
}

vec3 toRGB(vec3 yiq) {
    return mat3(1.0, 1.0, 1.0, 0.5696804, -0.1620848, -0.6590654, 0.3235513, -0.3381869, 0.8901581) * yiq;
}

void main() {
    vec3 tex = texture(Sampler0, texCoord0).rgb;
    vec3 original = toYIQ(tex);
    vec4 color;
    if (length(original.xy) < .01) {
        color = vec4(tex, 1.0);
    } else {
        vec3 targetYIQ = toYIQ(StarColor.rgb);
        vec3 finalrgb = toRGB(vec3(original.x, targetYIQ.yz));
        finalrgb = pow(max(finalrgb, vec3(0.0)), vec3(1.0 / Gamma));
        color = vec4(finalrgb, StarColor.a);
    }
    if (color.a < 0.1) discard;
    fragColor = color;
}
