#version 330 core

#ifndef MAX_TEXTURE_LOD_BIAS
#error "MAX_TEXTURE_LOD_BIAS constant not specified"
#endif

#import <radium:include/fog.glsl>
#import <radium:include/chunk_material.glsl>

in vec4 v_Color; // The interpolated vertex color
in vec2 v_TexCoord; // The interpolated block texture coordinates
in float v_FragDistance; // The fragment's distance from the camera (cylindrical and spherical)
in float fadeFactor;

flat in uint v_Material;

uniform sampler2D u_BlockTex; // The block texture

uniform vec4 u_FogColor; // The color of the shader fog
uniform float u_FogStart; // The starting position of the shader fog
uniform float u_FogEnd; // The ending position of the shader fog

out vec4 fragColor; // The output fragment for the color framebuffer

void main() {
float lodBias = _material_use_mips(v_Material) ? 0.0 : float(-MAX_TEXTURE_LOD_BIAS);

// Apply per-vertex color
vec4 color = texture(u_BlockTex, v_TexCoord, lodBias);
color *= v_Color;

color *= fadeFactor;

#ifdef USE_FRAGMENT_DISCARD
if (color.a < _material_alpha_cutoff(v_Material)) {
discard;
}
#endif

fragColor = _linearFog(color, v_FragDistance, u_FogColor, u_FogStart, u_FogEnd);
}