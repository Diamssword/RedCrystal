package com.diamssword.redCrystal.storage;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;

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
			.append(new KeyedCodec<>("RedElementBehaviorStateInternal", new MapCodec<>(Codec.SHORT, HashMap::new, false)), (a, b) -> a.internalStates = b, (a) -> a.internalStates)
			.add()
			.build();
	private Short[] input = new Short[0];
	private Short[] output = new Short[0];
	private Map<String, Short> internalStates = new HashMap<>();

	public StateLoader() {
	}

	public StateLoader(short inSize, short outSize) {
		System.out.println("creating full");
		output = new Short[outSize];
		Arrays.fill(output, (short) 0);
		input = new Short[inSize];
		Arrays.fill(input, (short) 0);
	}

	public void updateSize(short inSize, short outSize) {
		System.out.println("updating");
		System.out.println(output[0]);
		this.input = extend(input, inSize);
		this.output = extend(output, outSize);
	}

	private Short[] extend(Short[] input, short size) {
		var ne = Arrays.copyOf(input, size);
		for(int i = 0; i < ne.length; i++) {
			System.out.println(ne[i]);
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

	public StateLoader(short[] in, short[] out, Map<String, Short> internal) {
		output = box(out);
		internalStates = new HashMap<>(internal);
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
