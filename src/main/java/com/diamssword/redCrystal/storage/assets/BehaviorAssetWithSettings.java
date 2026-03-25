package com.diamssword.redCrystal.storage.assets;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;

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

	public static BuilderCodec<BehaviorAssetParticle> ParticleCodec(String id) {
		return BuilderCodec.builder(BehaviorAssetParticle.class, () -> new BehaviorAssetParticle(id))
				.appendInherited(
						new KeyedCodec<>("Particles", ModelParticle.ARRAY_CODEC),
						(item, s) -> item.particles = s,
						item -> item.particles,
						(item, parent) -> item.particles = parent.particles
				).add().build();
	}

	public static class BehaviorAssetParticle extends AbstractBehaviorAsset<BehaviorAssetParticle> {

		public ModelParticle[] particles = new ModelParticle[0];

		public BehaviorAssetParticle(String id) {
			super(id);
		}

	}

	public static class BehaviorAssetSteps extends AbstractBehaviorAsset<BehaviorAssetBinary> {

		public short steps = 9;

		public BehaviorAssetSteps(String id) {
			super(id);
		}

	}

	public static class BehaviorAssetAbsolute extends AbstractBehaviorAsset<BehaviorAssetBinary> {

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