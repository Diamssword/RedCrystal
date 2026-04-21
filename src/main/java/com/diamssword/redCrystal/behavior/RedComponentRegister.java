package com.diamssword.redCrystal.behavior;

import com.diamssword.redCrystal.behavior.base.RedCompBehavior;
import com.diamssword.redCrystal.behavior.base.RedCompBehaviorWithModel;
import com.diamssword.redCrystal.behavior.inputs.*;
import com.diamssword.redCrystal.behavior.modifiers.*;
import com.diamssword.redCrystal.behavior.outputs.*;
import com.diamssword.redCrystal.storage.assets.*;
import com.diamssword.redCrystal.storage.RedElement;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.function.function.TriFunction;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class RedComponentRegister {
	private final Map<String, TriFunction<String, RedElement, ? extends AbstractBehaviorAsset<?>, ? extends RedCompBehavior<? extends AbstractBehaviorAsset<?>>>> registry = new ConcurrentHashMap<>();
	private final Map<String, BuilderCodec<?>> settingsCodecRegistry = new ConcurrentHashMap<>();
	
	public void register(String id, TriFunction<String, RedElement, BehaviorAsset, RedCompBehavior<BehaviorAsset>> factory, BuilderCodec<?> settingsCodec) {
		var beh = new BehaviorAsset(id);
		register(id, factory, BehaviorAsset.class, BuilderCodec.builder(BehaviorAsset.class, () -> beh).build());
		settingsCodecRegistry.put(id, settingsCodec);
	}

	public void register(String id, TriFunction<String, RedElement, BehaviorAsset, RedCompBehavior<BehaviorAsset>> factory) {
		var beh = new BehaviorAsset(id);
		register(id, factory, BehaviorAsset.class, BuilderCodec.builder(BehaviorAsset.class, () -> beh).build());
	}

	public <T extends AbstractBehaviorAsset<?>> void register(String id, TriFunction<String, RedElement, T, RedCompBehavior<T>> factory, Class<T> clazz, BuilderCodec<T> codec) {
		registry.put(id, factory);
		AbstractBehaviorAsset.BEHAVIOR_CODEC.register(id, clazz, codec);
	}

	public <T extends AbstractBehaviorAsset<?>> void register(String id, TriFunction<String, RedElement, T, RedCompBehavior<T>> factory, Function<String, BuilderCodec<T>> codecBuilder) {
		registry.put(id, factory);
		var codec = codecBuilder.apply(id);
		AbstractBehaviorAsset.BEHAVIOR_CODEC.register(id, codec.getInnerClass(), codec);
	}

	public <T extends AbstractBehaviorAsset<?>> void register(String id, TriFunction<String, RedElement, T, RedCompBehavior<T>> factory, Function<String, BuilderCodec<T>> codecBuilder, BuilderCodec<?> settingsCodec) {
		registry.put(id, factory);
		var codec = codecBuilder.apply(id);
		AbstractBehaviorAsset.BEHAVIOR_CODEC.register(id, codec.getInnerClass(), codec);
		settingsCodecRegistry.put(id, settingsCodec);
	}

	@Nullable
	public BuilderCodec<?> getSettingsCodec(String id) {
		return settingsCodecRegistry.get(id);
	}

	@Nullable
	public <T extends AbstractBehaviorAsset<?>> RedCompBehavior<T> get(String id, RedElement parent, T asset) {
		var g = registry.get(id);
		if(g != null)
			return ((TriFunction<String, RedElement, T, RedCompBehavior<T>>) g).apply(id, parent, asset);
		return null;
	}
}
