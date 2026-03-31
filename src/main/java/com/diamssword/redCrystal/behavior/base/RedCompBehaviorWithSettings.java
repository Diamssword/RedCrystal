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

public abstract class RedCompBehaviorWithSettings<T extends AbstractBehaviorAsset<?>, J> extends RedCompBehavior<T> {
	private J settings;

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


	public void saveSettings() {
		this.getStateManager().setStoredSettings(this.getSettingsCodec().encode(settings, new ExtraInfo()));
	}

	public void setStoredSettings(BsonDocument bson) {
		this.getStateManager().setStoredSettings(bson);
		settings = null;
	}
}
