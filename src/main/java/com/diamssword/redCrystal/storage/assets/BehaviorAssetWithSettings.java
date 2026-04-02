package com.diamssword.redCrystal.storage.assets;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;

public class BehaviorAssetWithSettings {


	public static BuilderCodec<BehaviorAssetAbsolute> AbsoluteCodec(String id) {
		return BuilderCodec.builder(BehaviorAssetAbsolute.class, () -> new BehaviorAssetAbsolute(id)).append(new KeyedCodec<>("IsAbsolute", BuilderCodec.BOOLEAN), (a, b) -> a.isAbsolute = b, a -> a.isAbsolute)
				.documentation("Transform an OR gate in a XOR gate").add()
				.build();
	}

	public static BuilderCodec<BehaviorAssetBinary> BinaryCodec(String id) {
		return BuilderCodec.builder(BehaviorAssetBinary.class, () -> new BehaviorAssetBinary(id)).append(new KeyedCodec<>("IsBinary", BuilderCodec.BOOLEAN), (a, b) -> a.isBinary = b, a -> a.isBinary)
				.documentation("Process the incoming signals as binary").add().build();
	}

	public static BuilderCodec<BehaviorAssetSteps> VariatorCodec(String id) {
		return BuilderCodec.builder(BehaviorAssetSteps.class, () -> new BehaviorAssetSteps(id)).append(new KeyedCodec<>("Steps", BuilderCodec.SHORT), (a, b) -> a.steps = b, a -> a.steps)
				.documentation("The numbers of steps this variator has").add().build();
	}

	public static BuilderCodec<BehaviorAssetDistance> DistanceCodec(String id) {
		return BuilderCodec.builder(BehaviorAssetDistance.class, () -> new BehaviorAssetDistance(id)).append(new KeyedCodec<>("Distance", BuilderCodec.FLOAT), (a, b) -> a.distance = b, a -> a.distance)
				.documentation("The numbers of steps this variator has").add().build();
	}

	public static BuilderCodec<BehaviorAssetLight> LightCodec(String id) {
		return BuilderCodec.builder(BehaviorAssetLight.class, () -> new BehaviorAssetLight(id))
				.appendInherited(new KeyedCodec<>("Particles", ModelParticle.ARRAY_CODEC), (item, s) -> item.particles = s, item -> item.particles, (item, parent) -> item.particles = parent.particles
				)
				.add()
				.appendInherited(new KeyedCodec<>("Light", ProtocolCodecs.COLOR_LIGHT), (a, b) -> a.light = b, a -> a.light, (a, b) -> a.light = b.light)
				.add()
				.append(new KeyedCodec<>("IsRGB", BuilderCodec.BOOLEAN), (a, b) -> a.isRGB = b, a -> a.isRGB)
				.add()
				.build();
	}

	public static BuilderCodec<BehaviorAssetCalculus> CalculusCodec(String id) {
		return BuilderCodec.builder(BehaviorAssetCalculus.class, () -> new BehaviorAssetCalculus(id))
				.appendInherited(new KeyedCodec<>("Operation", new EnumCodec<>(BehaviorAssetCalculus.OperationType.class)), (item, s) -> item.operation = s, item -> item.operation, (item, parent) -> item.operation = parent.operation
				)
				.add()
				.build();
	}

	public static class BehaviorAssetLight extends AbstractBehaviorAsset<BehaviorAssetLight> {
		public ColorLight light = new ColorLight((byte) 0, (byte) 25, (byte) 25, (byte) 25);
		public boolean isRGB = false;
		public ModelParticle[] particles = new ModelParticle[0];

		public BehaviorAssetLight(String id) {
			super(id);
		}

	}

	public static class BehaviorAssetDistance extends AbstractBehaviorAsset<BehaviorAssetDistance> {

		public float distance = 8;

		public BehaviorAssetDistance(String id) {
			super(id);
		}

	}

	public static class BehaviorAssetSteps extends AbstractBehaviorAsset<BehaviorAssetSteps> {

		public short steps = 9;

		public BehaviorAssetSteps(String id) {
			super(id);
		}

	}

	public static class BehaviorAssetCalculus extends AbstractBehaviorAsset<BehaviorAssetCalculus> {

		public static enum OperationType {
			ADD,
			SUBTRACT,
			DIVIDE,
			MULTIPLY,
			MOD
		}

		public OperationType operation;

		public BehaviorAssetCalculus(String id) {
			super(id);
		}

	}

	public static class BehaviorAssetAbsolute extends AbstractBehaviorAsset<BehaviorAssetAbsolute> {

		public boolean isAbsolute;

		public BehaviorAssetAbsolute(String id) {
			super(id);
		}

	}

	public static class BehaviorAssetBinary extends AbstractBehaviorAsset<BehaviorAssetBinary> {

		public boolean isBinary;

		public BehaviorAssetBinary(String id) {
			super(id);
		}

	}
}