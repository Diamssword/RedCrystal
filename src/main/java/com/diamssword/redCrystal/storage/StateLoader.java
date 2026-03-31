package com.diamssword.redCrystal.storage;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import org.bson.BsonDocument;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class StateLoader {
	@Nonnull
	public static BuilderCodec<StateLoader> CODEC = BuilderCodec.builder(StateLoader.class, StateLoader::new)
			//	.append(new KeyedCodec<>("StateIn", new ArrayCodec<>(Codec.SHORT, Short[]::new)), (a, b) -> a.input = b, (a) -> a.input)
			//	.add()
			.append(new KeyedCodec<>("RedElementBehaviorStateOut", new ArrayCodec<>(Codec.SHORT, Short[]::new)), (a, b) -> a.output = b, (a) -> a.output)
			.add()
			.append(new KeyedCodec<>("RedElementBehaviorStateIn", new ArrayCodec<>(Codec.SHORT, Short[]::new)), (a, b) -> a.input = b, (a) -> a.input)
			.add()
			.append(new KeyedCodec<>("RedElementBehaviorStateInternal", new MapCodec<>(Codec.SHORT, HashMap::new, false)), (a, b) -> a.internalStates = b, (a) -> a.internalStates)
			.add()
			.append(new KeyedCodec<>("RedElementBehaviorStateSettings", new BsonDocumentCodec()), (a, b) -> a.storedSettings = b, a -> a.storedSettings)
			.add()
			.build();
	private Short[] input = new Short[0];
	private Short[] output = new Short[0];
	private BsonDocument storedSettings = new BsonDocument();
	private Map<String, Short> internalStates = new HashMap<>();

	public StateLoader() {
	}

	public StateLoader(short inSize, short outSize) {
		output = new Short[outSize];
		Arrays.fill(output, (short) 0);
		input = new Short[inSize];
		Arrays.fill(input, (short) 0);
	}

	public void updateSize(short inSize, short outSize) {
		this.input = extend(input, inSize);
		this.output = extend(output, outSize);
	}

	public BsonDocument getStoredSettings() {
		return storedSettings;
	}

	public void setStoredSettings(BsonDocument doc) {
		this.storedSettings = doc;
	}

	private Short[] extend(Short[] input, short size) {
		var ne = Arrays.copyOf(input, size);
		for(int i = 0; i < ne.length; i++) {
			if(ne[i] == null)
				ne[i] = 0;
		}
		return ne;
	}

	public Map<String, Short> getInternalStates() {
		return internalStates;
	}

	public Short[] getInput() {
		return input;
	}

	public Short[] getOutput() {
		return output;
	}

}
