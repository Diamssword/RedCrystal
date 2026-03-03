package com.diamssword.redCrystal.redComponent;

import com.diamssword.redCrystal.storage.RedElement;
import com.hypixel.hytale.registry.Registry;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class RedComponentRegister {
	private static Map<String, BiFunction<String, RedElement, RedCompBehavior>> registry = new LinkedHashMap<>();

	public static void init() {
		registry.clear();
		registry.put("Button", ButtonBehavior::new);
		registry.put("Interact", InteractBehavior::new);
		registry.put("Toggle", ToggleBehavior::new);
		registry.put("Toggle1", ToggleBehavior::new);
		registry.put("Toggle2", ToggleBehavior::new);
		registry.put("Toggle3", ToggleBehavior::new);
		registry.put("Toggle4", ToggleBehavior::new);
		registry.put("Toggle5", ToggleBehavior::new);
		registry.put("Toggle6", ToggleBehavior::new);
		registry.put("Toggle7", ToggleBehavior::new);
		registry.put("Toggle8", ToggleBehavior::new);
		registry.put("Toggle9", ToggleBehavior::new);
		registry.put("Toggle91", ToggleBehavior::new);
		registry.put("Toggle92", ToggleBehavior::new);
		registry.put("Toggle93", ToggleBehavior::new);
	}

	@Nullable
	public static RedCompBehavior get(String id, RedElement parent) {
		var g = registry.get(id);
		if(g != null)
			return g.apply(id, parent);
		return null;
	}

	public static List<String> getAllIds() {
		return registry.keySet().stream().toList();
	}
}
