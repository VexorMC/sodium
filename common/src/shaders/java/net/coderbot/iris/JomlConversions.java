package net.coderbot.iris;

import net.coderbot.iris.vendored.joml.Vector3d;
import net.coderbot.iris.vendored.joml.Vector4f;
import net.minecraft.util.math.Vec3d;

public class JomlConversions {
	public static Vector3d fromVec3(Vec3d vec) {
		return new Vector3d(vec.x, vec.y, vec.z);
	}

	public static Vector4f toJoml(org.lwjgl.util.vector.Vector4f v) {
		return new Vector4f(v.x, v.y, v.z, v.w);
	}
}
