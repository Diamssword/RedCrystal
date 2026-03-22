package com.diamssword.redCrystal.storage;

import com.diamssword.redCrystal.redComponent.RedCompBehavior;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class StateLoader {
	@Nonnull
	public static BuilderCodec<StateLoader> CODEC = BuilderCodec.builder(StateLoader.class, StateLoader::new)
			//	.append(new KeyedCodec<>("StateIn", new ArrayCodec<>(Codec.SHORT, Short[]::new)), (a, b) -> a.input = b, (a) -> a.input)
			//	.add()
			.append(new KeyedCodec<>("StateOut", new ArrayCodec<>(Codec.SHORT, Short[]::new)), (a, b) -> a.output = b, (a) -> a.output)
			.add()
			.append(new KeyedCodec<>("StateInternal", new MapCodec<>(Codec.SHORT, HashMap::new, false)), (a, b) -> a.internalStates = b, (a) -> a.internalStates)
			.add()
			.build();
	private Short[] input;
	private Short[] output;
	private Map<String, Short> internalStates = new HashMap<>();

	public StateLoader() {

	}

	public StateLoader(short[] in, short[] out, Map<String, Short> internal) {
		output = box(out);
		internalStates = internal;
	}

	public void copyInput(short[] arr) {
		unbox(this.input, arr);
	}

	public void copyOutput(short[] arr) {
		unbox(output, arr);
	}

	public void copyInternal(Map<String, Short> map) {
		map.putAll(internalStates);
	}

	private static void unbox(Short[] from, short[] to) {
		if(from != null) {
			for(int i = 0; i < from.length; i++) {
				if(i < to.length)
					to[i] = from[i];
			}
		}
	}

	private static Short[] box(short[] primitive) {
		Short[] boxed = new Short[primitive.length];
		for(int i = 0; i < primitive.length; i++) {
			boxed[i] = primitive[i];
		}
		return boxed;
	}
}
