package net.minecraft.client.renderer;

import dev.vexor.radium.shaders.RVertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class CoreShaders {
    private static final List<ShaderProgram> PROGRAMS = new ArrayList<ShaderProgram>();
    public static final ShaderProgram BLIT_SCREEN = CoreShaders.register("blit_screen", RVertexFormats.BLIT_SCREEN);
    public static final ShaderProgram LIGHTMAP = CoreShaders.register("lightmap", RVertexFormats.BLIT_SCREEN);
    public static final ShaderProgram PARTICLE = CoreShaders.register("particle", VertexFormats.PARTICLE);
    public static final ShaderProgram POSITION = CoreShaders.register("position", VertexFormats.POSITION);
    public static final ShaderProgram POSITION_COLOR = CoreShaders.register("position_color", VertexFormats.POSITION_COLOR);
    public static final ShaderProgram POSITION_COLOR_LIGHTMAP = CoreShaders.register("position_color_lightmap", VertexFormats.POSITION_COLOR);
    public static final ShaderProgram POSITION_COLOR_TEX_LIGHTMAP = CoreShaders.register("position_color_tex_lightmap", VertexFormats.POSITION_TEXTURE_COLOR);
    public static final ShaderProgram POSITION_TEX = CoreShaders.register("position_tex", VertexFormats.POSITION_TEXTURE);
    public static final ShaderProgram POSITION_TEX_COLOR = CoreShaders.register("position_tex_color", VertexFormats.POSITION_TEXTURE_COLOR);
    public static final ShaderProgram RENDERTYPE_SOLID = CoreShaders.register("rendertype_solid", VertexFormats.BLOCK);
    public static final ShaderProgram RENDERTYPE_CUTOUT_MIPPED = CoreShaders.register("rendertype_cutout_mipped", VertexFormats.BLOCK);
    public static final ShaderProgram RENDERTYPE_CUTOUT = CoreShaders.register("rendertype_cutout", VertexFormats.BLOCK);
    public static final ShaderProgram RENDERTYPE_TRANSLUCENT = CoreShaders.register("rendertype_translucent", VertexFormats.BLOCK);
    public static final ShaderProgram RENDERTYPE_TRANSLUCENT_MOVING_BLOCK = CoreShaders.register("rendertype_translucent_moving_block", VertexFormats.BLOCK);
    public static final ShaderProgram RENDERTYPE_ARMOR_CUTOUT_NO_CULL = CoreShaders.register("rendertype_armor_cutout_no_cull", VertexFormats.ENTITY);
    public static final ShaderProgram RENDERTYPE_ARMOR_TRANSLUCENT = CoreShaders.register("rendertype_armor_translucent", VertexFormats.ENTITY);
    public static final ShaderProgram RENDERTYPE_ENTITY_SOLID = CoreShaders.register("rendertype_entity_solid", VertexFormats.ENTITY);
    public static final ShaderProgram RENDERTYPE_ENTITY_CUTOUT = CoreShaders.register("rendertype_entity_cutout", VertexFormats.ENTITY);
    public static final ShaderProgram RENDERTYPE_ENTITY_CUTOUT_NO_CULL = CoreShaders.register("rendertype_entity_cutout_no_cull", VertexFormats.ENTITY);
    public static final ShaderProgram RENDERTYPE_ENTITY_CUTOUT_NO_CULL_Z_OFFSET = CoreShaders.register("rendertype_entity_cutout_no_cull_z_offset", VertexFormats.ENTITY);
    public static final ShaderProgram RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL = CoreShaders.register("rendertype_item_entity_translucent_cull", VertexFormats.ENTITY);
    public static final ShaderProgram RENDERTYPE_ENTITY_TRANSLUCENT = CoreShaders.register("rendertype_entity_translucent", VertexFormats.ENTITY);
    public static final ShaderProgram RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE = CoreShaders.register("rendertype_entity_translucent_emissive", VertexFormats.ENTITY);
    public static final ShaderProgram RENDERTYPE_ENTITY_SMOOTH_CUTOUT = CoreShaders.register("rendertype_entity_smooth_cutout", VertexFormats.ENTITY);
    public static final ShaderProgram RENDERTYPE_BEACON_BEAM = CoreShaders.register("rendertype_beacon_beam", VertexFormats.BLOCK);
    public static final ShaderProgram RENDERTYPE_ENTITY_DECAL = CoreShaders.register("rendertype_entity_decal", VertexFormats.ENTITY);
    public static final ShaderProgram RENDERTYPE_ENTITY_NO_OUTLINE = CoreShaders.register("rendertype_entity_no_outline", VertexFormats.ENTITY);
    public static final ShaderProgram RENDERTYPE_ENTITY_SHADOW = CoreShaders.register("rendertype_entity_shadow", VertexFormats.ENTITY);
    public static final ShaderProgram RENDERTYPE_ENTITY_ALPHA = CoreShaders.register("rendertype_entity_alpha", VertexFormats.ENTITY);
    public static final ShaderProgram RENDERTYPE_EYES = CoreShaders.register("rendertype_eyes", VertexFormats.ENTITY);
    public static final ShaderProgram RENDERTYPE_ENERGY_SWIRL = CoreShaders.register("rendertype_energy_swirl", VertexFormats.ENTITY);
    public static final ShaderProgram RENDERTYPE_LEASH = CoreShaders.register("rendertype_leash", VertexFormats.POSITION_COLOR);
    public static final ShaderProgram RENDERTYPE_WATER_MASK = CoreShaders.register("rendertype_water_mask", VertexFormats.POSITION);
    public static final ShaderProgram RENDERTYPE_OUTLINE = CoreShaders.register("rendertype_outline", VertexFormats.POSITION_TEXTURE_COLOR);
    public static final ShaderProgram RENDERTYPE_ARMOR_ENTITY_GLINT = CoreShaders.register("rendertype_armor_entity_glint", VertexFormats.POSITION_TEXTURE);
    public static final ShaderProgram RENDERTYPE_GLINT_TRANSLUCENT = CoreShaders.register("rendertype_glint_translucent", VertexFormats.POSITION_TEXTURE);
    public static final ShaderProgram RENDERTYPE_GLINT = CoreShaders.register("rendertype_glint", VertexFormats.POSITION_TEXTURE);
    public static final ShaderProgram RENDERTYPE_ENTITY_GLINT = CoreShaders.register("rendertype_entity_glint", VertexFormats.POSITION_TEXTURE);
    public static final ShaderProgram RENDERTYPE_TEXT = CoreShaders.register("rendertype_text", VertexFormats.POSITION_TEXTURE_COLOR);
    public static final ShaderProgram RENDERTYPE_TEXT_BACKGROUND = CoreShaders.register("rendertype_text_background", VertexFormats.POSITION_COLOR);
    public static final ShaderProgram RENDERTYPE_TEXT_INTENSITY = CoreShaders.register("rendertype_text_intensity", VertexFormats.POSITION_TEXTURE_COLOR);
    public static final ShaderProgram RENDERTYPE_TEXT_SEE_THROUGH = CoreShaders.register("rendertype_text_see_through", VertexFormats.POSITION_TEXTURE_COLOR);
    public static final ShaderProgram RENDERTYPE_TEXT_BACKGROUND_SEE_THROUGH = CoreShaders.register("rendertype_text_background_see_through", VertexFormats.POSITION_COLOR);
    public static final ShaderProgram RENDERTYPE_TEXT_INTENSITY_SEE_THROUGH = CoreShaders.register("rendertype_text_intensity_see_through", VertexFormats.POSITION_TEXTURE_COLOR);
    public static final ShaderProgram RENDERTYPE_LIGHTNING = CoreShaders.register("rendertype_lightning", VertexFormats.POSITION_COLOR);
    public static final ShaderProgram RENDERTYPE_TRIPWIRE = CoreShaders.register("rendertype_tripwire", VertexFormats.BLOCK);
    public static final ShaderProgram RENDERTYPE_END_PORTAL = CoreShaders.register("rendertype_end_portal", VertexFormats.POSITION);
    public static final ShaderProgram RENDERTYPE_END_GATEWAY = CoreShaders.register("rendertype_end_gateway", VertexFormats.POSITION);
    public static final ShaderProgram RENDERTYPE_CLOUDS = CoreShaders.register("rendertype_clouds", VertexFormats.POSITION_COLOR);
    public static final ShaderProgram RENDERTYPE_LINES = CoreShaders.register("rendertype_lines", VertexFormats.POSITION_TEXTURE_COLOR_NORMAL);
    public static final ShaderProgram RENDERTYPE_CRUMBLING = CoreShaders.register("rendertype_crumbling", VertexFormats.BLOCK);
    public static final ShaderProgram RENDERTYPE_GUI = CoreShaders.register("rendertype_gui", VertexFormats.POSITION_COLOR);
    public static final ShaderProgram RENDERTYPE_GUI_OVERLAY = CoreShaders.register("rendertype_gui_overlay", VertexFormats.POSITION_COLOR);
    public static final ShaderProgram RENDERTYPE_GUI_TEXT_HIGHLIGHT = CoreShaders.register("rendertype_gui_text_highlight", VertexFormats.POSITION_COLOR);
    public static final ShaderProgram RENDERTYPE_GUI_GHOST_RECIPE_OVERLAY = CoreShaders.register("rendertype_gui_ghost_recipe_overlay", VertexFormats.POSITION_COLOR);
    public static final ShaderProgram RENDERTYPE_BREEZE_WIND = CoreShaders.register("rendertype_breeze_wind", VertexFormats.ENTITY);

    private static ShaderProgram register(String string, VertexFormat vertexFormat) {
        return CoreShaders.register(string, vertexFormat, ShaderDefines.EMPTY);
    }

    private static ShaderProgram register(String string, VertexFormat vertexFormat, ShaderDefines shaderDefines) {
        ShaderProgram shaderProgram = new ShaderProgram(new Identifier("core/" + string), vertexFormat, shaderDefines);
        PROGRAMS.add(shaderProgram);
        return shaderProgram;
    }

    public static List<ShaderProgram> getProgramsToPreload() {
        return PROGRAMS;
    }
}

