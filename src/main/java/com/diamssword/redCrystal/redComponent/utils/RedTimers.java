package com.diamssword.redCrystal.redComponent.utils;

import java.util.ArrayList;
import java.util.List;

public class RedTimers {
	private record Pair(int time, Runnable runnable) {}

	private int tick = 0;
	private final List<Pair> scheluded = new ArrayList<>();

	public void add(Runnable fn, int in) {
		scheluded.add(new Pair(tick + in, fn));
	}

	public void tick() {
		tick++;
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
