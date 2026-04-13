package com.diamssword.redCrystal.worldInteraction;

import com.hypixel.hytale.math.vector.Rotation3f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.BlockFace;

public class FacingUtil {
	/**
	 * Create a Rotation Vector matching the BlockFace
	 */
	public static Rotation3f facingToRotation(BlockFace face) {
		return degreeVectortoRadians(switch(face) {
			case None, North -> new Rotation3f(0, 0, 0);
			case Up -> new Rotation3f(90, 0, 0);
			case Down -> new Rotation3f(-90, 0, 0);
			case West -> new Rotation3f(0, 90, 0);
			case East -> new Rotation3f(0, -90, 0);
			case South -> new Rotation3f(0, 180, 0);
		});
	}

	public static BlockFace opposite(BlockFace face) {
		return switch(face) {
			case None, North -> BlockFace.South;
			case Up -> BlockFace.Down;
			case Down -> BlockFace.Up;
			case West -> BlockFace.East;
			case East -> BlockFace.West;
			case South -> BlockFace.None;
		};
	}

	/**
	 * Create a Rotation Vector matching the BlockFace and add a tilt on the horizontal plane
	 */
	public static Rotation3f facingToRotationWithTilt(BlockFace face, float tiltInDegree) {
		return degreeVectortoRadians(switch(face) {
			case None, North -> new Rotation3f(0, 0, tiltInDegree);
			case Up -> new Rotation3f(90, -tiltInDegree, 0);
			case Down -> new Rotation3f(-90, -tiltInDegree, 0);
			case West -> new Rotation3f(0, 90, tiltInDegree);
			case East -> new Rotation3f(0, -90, tiltInDegree);
			case South -> new Rotation3f(0, 180, tiltInDegree);
		});
	}

	/**
	 * Turn a degreeVector to a Radians Vector
	 */
	public static Rotation3f degreeVectortoRadians(Rotation3f degreeVec) {
		return new Rotation3f((float) Math.toRadians(degreeVec.x), (float) Math.toRadians(degreeVec.y), (float) Math.toRadians(degreeVec.z));
	}

	/**
	 * Create a vector matching the BlockFace with the given scale
	 *
	 * @param face
	 * @param scale
	 * @param addedX the horizontal Offset added to the vector
	 * @param addedY the vertical Offset added to the vector
	 */
	public static Vector3d facingToDir(BlockFace face, double scale, double addedX, double addedY) {
		return switch(face) {
			case None -> new Vector3d(0, 0, 0);
			case Up -> new Vector3d(-addedX, scale, addedY);
			case Down -> new Vector3d(addedX, -scale, addedY);
			case East -> new Vector3d(scale, addedY, -addedX);
			case West -> new Vector3d(-scale, addedY, addedX);
			case North -> new Vector3d(-addedX, addedY, -scale);
			case South -> new Vector3d(addedX, addedY, scale);
		};
	}

	public static boolean isNegative(BlockFace face) {
		return switch(face) {
			case None, Up, East, South -> false;
			default -> true;
		};
	}

	public static double extractAxis(BlockFace face, Vector3d vector) {
		return switch(face) {
			case Up, Down -> vector.y;
			case East, West -> vector.x;
			default -> vector.z;
		};
	}
}
