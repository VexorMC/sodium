package dev.vexor.radium.options.util;

import com.google.gson.*;
import net.minecraft.util.Identifier;

import java.lang.reflect.Type;

public class IdentifierSerializer implements JsonSerializer<Identifier>, JsonDeserializer<Identifier> {
    @Override
    public JsonElement serialize(Identifier src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("namespace", src.getNamespace());
        jsonObject.addProperty("path", src.getPath());
        return jsonObject;
    }

    @Override
    public Identifier deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return new Identifier(json.getAsJsonObject().get("namespace").getAsString(), json.getAsJsonObject().get("path").getAsString());
    }
}
