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

package dev.vexor.radium.frapi.mixin.renderer.client;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

import dev.vexor.radium.frapi.impl.renderer.SpriteFinderImpl;

@Mixin(SpriteAtlasTexture.class)
public class SpriteAtlasTextureMixin implements SpriteFinderImpl.SpriteFinderAccess {
    @Shadow
    @Final
    public Map<String, Sprite> sprites;

    @Inject(method = "method_10315", at = @At("RETURN"))
    private void sodium$deleteSpriteFinder(CallbackInfo ci) {
    }

    @Override
    public SpriteFinderImpl fabric$spriteFinder() {
        Map<Identifier, Sprite> sprites = new HashMap<>();

        this.sprites.forEach((id, sprite) -> {
            sprites.put(new Identifier(id), sprite);
        });

        return new SpriteFinderImpl(
                sprites,
                (SpriteAtlasTexture) (Object) this
        );
    }
}
