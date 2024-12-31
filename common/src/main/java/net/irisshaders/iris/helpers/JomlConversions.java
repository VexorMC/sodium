package net.irisshaders.iris.helpers;

import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

public class JomlConversions {
	public static Vector3d fromVec3(Vec3d vec) {
		return new Vector3d(vec.x, vec.y, vec.z);
	}
}
