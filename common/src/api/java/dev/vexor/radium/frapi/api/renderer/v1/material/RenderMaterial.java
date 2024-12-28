/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.vexor.radium.frapi.api.renderer.v1.material;

import net.minecraft.block.Block;
import net.minecraft.util.Identifier;

public interface RenderMaterial extends MaterialView {
	/**
	 * This will be identical to the material that would be obtained by calling {@link MaterialFinder#find()} on a new,
	 * unaltered, {@link MaterialFinder} instance. It is defined here for clarity and convenience.
	 *
	 * <p>Quads using this material use {@link Block#getRenderLayerType()} of the associated block to
	 * determine texture blending, honor block color index, are non-emissive, and apply both diffuse and ambient
	 * occlusion shading to vertex colors.
	 *
	 * <p>All standard, non-fluid baked models are rendered using this material.
	 */
	Identifier STANDARD_ID = new Identifier("fabric", "standard");
}
