package com.diamssword.redCrystal.storage.assets;

import com.diamssword.redCrystal.redComponent.RedCompBehavior;
import com.diamssword.redCrystal.redComponent.RedComponentRegister;
import com.diamssword.redCrystal.storage.RedElement;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;

import javax.annotation.Nonnull;

public abstract class AbstractBehaviorAsset<T extends AbstractBehaviorAsset<T>> {

	@Nonnull
	public static CodecMapCodec<AbstractBehaviorAsset<?>> BEHAVIOR_CODEC = new CodecMapCodec<>("Type");
	public final String id;

	public AbstractBehaviorAsset(String id) {
		this.id = id;
	}

	public RedCompBehavior<T> createBehavior(RedElement parent) {
		return RedComponentRegister.get(id, parent, (T) this);
	}
}
