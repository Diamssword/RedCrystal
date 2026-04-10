package com.diamssword.redCrystal.gui;

import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class UniversalEventBinder {
	private final Map<String, BiConsumer<Object, UICommandBuilder>> map = new HashMap<>();
	private int index = 0;
	private final List<Consumer<UIEventBuilder>> delayedCalls = new ArrayList<>();
	private final List<Consumer<UICommandBuilder>> delayedStyle = new ArrayList<>();
	private boolean needFullReload = false;

	public void setEventBuilder(UIEventBuilder builder, UICommandBuilder commandBuilder) {
		delayedCalls.forEach(c -> c.accept(builder));
		delayedCalls.clear();
		delayedStyle.forEach(c -> c.accept(commandBuilder));
		delayedStyle.clear();
	}

	public void needFullReload() {
		this.needFullReload = true;
	}

	public boolean getReloadFlag() {
		var val = needFullReload;
		needFullReload = false;
		return val;
	}

	public void onBuilt(Consumer<UICommandBuilder> builder) {
		delayedStyle.add(builder);
	}

	public void setStyle(String id, String selector, String styleRef) {
		onBuilt((b) -> {
			b.set("#" + id + "." + selector, Value.ref("Common.ui", styleRef));
		});
	}

	public void setStyle(String id, String styleRef) {
		onBuilt((b) -> {
			b.set("#" + id + ".Style", Value.ref("Common.ui", styleRef));
		});
	}

	public <T> void bindEvent(String id, String value, BiConsumer<T, UICommandBuilder> callback, Class<T> clazz, CustomUIEventBindingType event) {
		map.put(id, (BiConsumer<Object, UICommandBuilder>) callback);
		delayedCalls.add((eventBuilder) -> {
			var key = UniversalDataBinding.KEY_VALUE_S;
			String type = "String";
			if(clazz == Double.class) {
				key = UniversalDataBinding.KEY_VALUE_D;
				type = "Double";
			} else if(clazz == Boolean.class) {
				key = UniversalDataBinding.KEY_VALUE_B;
				type = "Boolean";
			} else if(clazz == Integer.class) {
				key = UniversalDataBinding.KEY_VALUE_I;
				type = "Integer";
			}

			eventBuilder.addEventBinding(event, "#" + id, EventData.of(UniversalDataBinding.KEY_ID, id).append(UniversalDataBinding.KEY_TYPE, type).append(key, value), false);
		});
	}

	public <T> void bindEvent(String id, String value, BiConsumer<T, UICommandBuilder> callback, Class<T> clazz) {
		bindEvent(id, value, callback, clazz, CustomUIEventBindingType.ValueChanged);
	}

	public <T> String bindEvent(BiConsumer<T, UICommandBuilder> callback, Class<T> clazz) {
		var id = getCompId();
		bindEvent(id, "#" + id + ".Value", callback, clazz);
		return id;
	}

	public String bindEvent(BiConsumer<String, UICommandBuilder> callback) {
		var id = getCompId();
		bindEvent(id, "#" + id + ".Value", callback, String.class);
		return id;
	}

	public String bindEvent(String value, BiConsumer<String, UICommandBuilder> callback) {
		var id = getCompId();
		bindEvent(id, value, callback, String.class);
		return id;
	}

	public void bindButton(String id, Consumer<UICommandBuilder> callback) {
		map.put(id, (a, b) -> callback.accept(b));
		delayedCalls.add((eventBuilder) -> {
			eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#" + id, EventData.of(UniversalDataBinding.KEY_ID, id).append(UniversalDataBinding.KEY_TYPE, "Boolean"));
		});
	}

	public String bindButton(Consumer<UICommandBuilder> callback) {
		var id = getCompId();
		bindButton(id, callback);
		return id;
	}

	public String bindEventDouble(BiConsumer<Double, UICommandBuilder> callback) {
		return bindEvent(callback, Double.class);
	}

	public String bindEventInteger(BiConsumer<Integer, UICommandBuilder> callback) {
		return bindEvent(callback, Integer.class);
	}

	public String bindEventBool(BiConsumer<Boolean, UICommandBuilder> callback) {
		return bindEvent(callback, Boolean.class);
	}

	public String getCompId() {
		var str = "CompSettings" + index;
		index++;
		return str;
	}

	public void onReceived(UICommandBuilder builder, UniversalDataBinding data) {
		var call = map.get(data.elementId);
		if(call != null) {
			switch(data.type) {
				case "String" -> call.accept(data.stringValue, builder);
				case "Double" -> call.accept(data.doubleValue, builder);
				case "Boolean" -> call.accept(data.booleanValue, builder);
				case "Integer" -> call.accept(data.integerValue, builder);
			}

		}

	}
}