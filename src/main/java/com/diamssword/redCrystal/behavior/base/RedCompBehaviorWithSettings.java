package com.diamssword.redCrystal.behavior.base;

import com.diamssword.redCrystal.behavior.RedComponentRegister;
import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.storage.assets.AbstractBehaviorAsset;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import org.bson.BsonDocument;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class RedCompBehaviorWithSettings<T extends AbstractBehaviorAsset<?>, J> extends RedCompBehavior<T> {
	private J settings;
	private Consumer<J> settingsChangeListener;

	public RedCompBehaviorWithSettings(String id, RedElement parent, T asset) {
		super(id, parent, asset);
	}

	public BuilderCodec<J> getSettingsCodec() {
		return (BuilderCodec<J>) RedComponentRegister.getSettingsCodec(getId());
	}

	public J getSettings() {
		if(settings == null)
			settings = this.getSettingsCodec().decode(this.getStateManager().getStoredSettings(), new ExtraInfo());
		return settings;
	}

	public boolean hideSettings() {
		return false;
	}

	public boolean canShowSetting(String key) {
		return true;
	}

	public void saveSettings() {
		this.getStateManager().setStoredSettings(this.getSettingsCodec().encode(settings, new ExtraInfo()));
	}

	public void setSettingsChangeListener(Consumer<J> function) {
		settingsChangeListener = function;
	}

	public void setStoredSettings(BsonDocument bson) {
		var old = settings;
		this.getStateManager().setStoredSettings(bson);
		settings = null;
		if(settingsChangeListener != null)
			settingsChangeListener.accept(old);

	}
}
