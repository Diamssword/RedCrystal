package com.diamssword.redCrystal.behavior.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class RedTimers {
	private record Pair(int time, Runnable runnable) {}


	private int tick = 0;
	private final List<Pair> scheluded = new ArrayList<>();
	private final Queue<Pair> incoming = new ConcurrentLinkedQueue<>();
	private AtomicIntegerArray plannedOutputs;
	private Consumer<short[]> outFn;

	public RedTimers(short outputSize, Consumer<short[]> outputChangeFunction) {
		plannedOutputs = new AtomicIntegerArray(outputSize);
		this.outFn = outputChangeFunction;
	}

	public void setPlannedOutput(short index, short value) {
		plannedOutputs.set(index, value);
	}

	public void add(Runnable fn, int in) {
		incoming.add(new Pair(tick + in, fn));
	}

	public void tick() {
		tick++;
		Pair p;
		while((p = incoming.poll()) != null) {
			scheluded.add(p);
		}
		var it = scheluded.iterator();
		while(it.hasNext()) {
			var c = it.next();
			if(c.time <= tick) {
				c.runnable.run();
				it.remove();
			}
		}
		if(outFn != null) {
			short[] values = new short[plannedOutputs.length()];
			for(int i = 0; i < plannedOutputs.length(); i++) {
				values[i] = (short) plannedOutputs.get(i);
			}
			outFn.accept(values);
		}
	}
}
