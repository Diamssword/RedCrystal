package com.diamssword.redCrystal.storage.assets;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BehaviorAssetWithSettings extends AbstractBehaviorAsset<BehaviorAssetWithSettings> {


	public static BuilderCodec<BehaviorAssetWithSettings> AbsoluteCodec(String id) {
		return BaseCodec(id).append(new KeyedCodec<>("IsAbsolute", BuilderCodec.BOOLEAN), (a, b) -> a.params.put("IsAbsolute", b), a -> a.getBoolean("IsAbsolute"))
				.documentation("Transform an OR gate in a XOR gate").add()
				.build();
	}

	public static BuilderCodec<BehaviorAssetWithSettings> BinaryCodec(String id) {
		return BaseCodec(id).append(new KeyedCodec<>("IsBinary", BuilderCodec.BOOLEAN), (a, b) -> a.params.put("IsBinary", b), a -> a.getBoolean("IsBinary"))
				.documentation("Process the incoming signals as binary").add().build();
	}

	private static BuilderCodec.Builder<BehaviorAssetWithSettings> BaseCodec(String id) {
		return BuilderCodec.builder(BehaviorAssetWithSettings.class, () -> new BehaviorAssetWithSettings(id));
	}

	private Map<String, Object> params = new HashMap<>();

	public BehaviorAssetWithSettings(String id) {
		super(id);
	}


	public boolean getBoolean(String name) {
		try {
			var res = (Boolean) params.get(name);
			return res != null && res;
		} catch(Exception e) {
			return false;
		}
	}

	public Optional<String> getString(String name) {
		try {
			String res = (String) params.get(name);
			return Optional.ofNullable(res);
		} catch(Exception e) {
			return Optional.empty();
		}
	}

	public Optional<Integer> getInt(String name) {
		try {
			Integer res = (Integer) params.get(name);
			return Optional.ofNullable(res);
		} catch(Exception e) {
			return Optional.empty();
		}
	}

}