package com.diamssword.redCrystal.behavior.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RedTimers {
	private record Pair(int time, Runnable runnable) {}

	private int tick = 0;
	private final List<Pair> scheluded = new ArrayList<>();
	private final Queue<Pair> incoming = new ConcurrentLinkedQueue<>();

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
	}
}
