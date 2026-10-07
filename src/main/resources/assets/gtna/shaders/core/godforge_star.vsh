#version 150

// GT5-Unofficial (a3e1e112) tectech star.vert, LGPL-3.0.
in vec3 Position;
in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 texCoord0;

void main() {
    texCoord0 = UV0;
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
}
