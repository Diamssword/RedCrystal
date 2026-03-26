package com.diamssword.redCrystal.behavior;

import com.diamssword.redCrystal.behavior.base.RedCompBehavior;
import com.diamssword.redCrystal.behavior.inputs.ButtonBehavior;
import com.diamssword.redCrystal.behavior.inputs.LeverBehavior;
import com.diamssword.redCrystal.behavior.inputs.VariatorBehavior;
import com.diamssword.redCrystal.behavior.modifiers.AndBehavior;
import com.diamssword.redCrystal.behavior.modifiers.NotBehavior;
import com.diamssword.redCrystal.behavior.modifiers.OrBehavior;
import com.diamssword.redCrystal.behavior.modifiers.ToggleBehavior;
import com.diamssword.redCrystal.behavior.outputs.FanBehavior;
import com.diamssword.redCrystal.behavior.outputs.InteractBehavior;
import com.diamssword.redCrystal.behavior.outputs.LightBehavior;
import com.diamssword.redCrystal.storage.assets.AbstractBehaviorAsset;
import com.diamssword.redCrystal.storage.assets.BehaviorAsset;
import com.diamssword.redCrystal.storage.assets.BehaviorAssetWithSettings;
import com.diamssword.redCrystal.storage.RedElement;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.function.function.TriFunction;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class RedComponentRegister {
	private static final Map<String, TriFunction<String, RedElement, ? extends AbstractBehaviorAsset<?>, ? extends RedCompBehavior<? extends AbstractBehaviorAsset<?>>>> registry = new ConcurrentHashMap<>();


	public static void init() {
		registry.clear();
		register("Button", ButtonBehavior::new);
		register("Interact", InteractBehavior::new);
		register("Toggle", ToggleBehavior::new);
		register("AND", AndBehavior::new);
		register("OR", OrBehavior::new, BehaviorAssetWithSettings::AbsoluteCodec);
		register("NOT", NotBehavior::new, BehaviorAssetWithSettings::BinaryCodec);
		register("Light", LightBehavior::new, BehaviorAssetWithSettings::LightCodec);
		register("Lever", LeverBehavior::new);
		register("Variator", VariatorBehavior::new, BehaviorAssetWithSettings::VariatorCodec);
		register("Fan", FanBehavior::new, BehaviorAssetWithSettings::DistanceCodec);


	}

	public static void register(String id, TriFunction<String, RedElement, BehaviorAsset, RedCompBehavior<BehaviorAsset>> factory) {
		var beh = new BehaviorAsset(id);
		register(id, factory, BehaviorAsset.class, BuilderCodec.builder(BehaviorAsset.class, () -> beh).build());
	}

	public static <T extends AbstractBehaviorAsset<?>> void register(String id, TriFunction<String, RedElement, T, RedCompBehavior<T>> factory, Class<T> clazz, BuilderCodec<T> codec) {
		registry.put(id, factory);
		AbstractBehaviorAsset.BEHAVIOR_CODEC.register(id, clazz, codec);
	}

	public static <T extends AbstractBehaviorAsset<?>> void register(String id, TriFunction<String, RedElement, T, RedCompBehavior<T>> factory, Function<String, BuilderCodec<T>> codecBuilder) {
		registry.put(id, factory);
		var codec = codecBuilder.apply(id);
		AbstractBehaviorAsset.BEHAVIOR_CODEC.register(id, codec.getInnerClass(), codec);
	}

	@Nullable
	public static <T extends AbstractBehaviorAsset<?>> RedCompBehavior<T> get(String id, RedElement parent, T asset) {
		var g = registry.get(id);
		if(g != null)
			return ((TriFunction<String, RedElement, T, RedCompBehavior<T>>) g).apply(id, parent, asset);
		return null;
	}
}
