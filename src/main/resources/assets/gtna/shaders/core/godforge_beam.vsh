#version 150

// GT5-Unofficial (a3e1e112) tectech gorgeBeam.vert, LGPL-3.0. The segment geometry is built on the CPU;
// Normal carries the local XY position used for the view-dependent fade and Color.a the segment transparency.
in vec3 Position;
in vec2 UV0;
in vec4 Color;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 texCoord0;
out vec2 localPosition;
out float transparency;

void main() {
    texCoord0 = UV0;
    localPosition = Normal.xy;
    transparency = Color.a;
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
}
