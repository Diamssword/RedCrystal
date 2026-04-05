package com.diamssword.redCrystal.gui;

import com.diamssword.redCrystal.storage.GlobalGlyphSettings;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.bson.BsonDocument;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class GlyphSettingsMenu extends InteractiveCustomUIPage<GlyphSettingsMenu.MenuEventData> {

	private final Consumer<GlobalGlyphSettings> setter;
	private final Supplier<GlobalGlyphSettings> getter;
	private GlyphSettingsMenuConverter<?> sepcific;
	private EventBinder binder = new EventBinder();
	private String ui = "Pages/RedCrystal/GlyphSettings.ui";
	private Runnable onClose;

	public GlyphSettingsMenu(@Nonnull PlayerRef ref, Supplier<GlobalGlyphSettings> settingsProvider, Consumer<GlobalGlyphSettings> settingsSetter) {
		super(ref, CustomPageLifetime.CanDismiss, GlyphSettingsMenu.MenuEventData.CODEC);
		this.setter = settingsSetter;
		this.getter = settingsProvider;

	}

	@Override
	public void onDismiss(@NotNull Ref<EntityStore> ref, @NotNull Store<EntityStore> store) {
		super.onDismiss(ref, store);
		if(onClose != null)
			onClose.run();
	}

	public GlyphSettingsMenu(PlayerRef ref) {
		super(ref, CustomPageLifetime.CanDismiss, GlyphSettingsMenu.MenuEventData.CODEC);
		this.setter = null;
		this.getter = null;

	}

	@Override
	public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {

		commandBuilder.append(ui);
		appendSettings("Main", commandBuilder, eventBuilder);
	}

	public <T> GlyphSettingsMenu withSpecific(String id, Supplier<BsonDocument> getter, Consumer<BsonDocument> setter, BuilderCodec<T> codec) {
		this.sepcific = new GlyphSettingsMenuConverter<T>(binder, id, playerRef, () -> codec.decode(getter.get(), new ExtraInfo()), (res) -> setter.accept(codec.encode(res, new ExtraInfo())), codec);
		return this;
	}

	public void openAsSubMenu(Runnable onClose) {
		ui = "Pages/RedCrystal/GlyphSettingsCentered.ui";
		Player playerComponent = (Player) playerRef.getReference().getStore().getComponent(playerRef.getReference(), Player.getComponentType());
		if(playerComponent != null)
			playerComponent.getPageManager().openCustomPage(playerRef.getReference(), playerRef.getReference().getStore(), this);
		this.onClose = onClose;
		/*prototype.onDismiss((page, bool) -> {
			onClose.run();
		});*/
	}

	public void appendSettings(String key, UICommandBuilder builder, UIEventBuilder eventBuilder) {
		if(setter != null) {
			var global = new GlyphSettingsMenuConverter<>(binder, "global", playerRef, getter, setter, GlobalGlyphSettings.CODEC);
			global.appendSettings(key, builder);
		}
		if(sepcific != null) {
			sepcific.appendSettings(key, builder);
		}
		binder.setEventBuilder(eventBuilder, builder);

	}


	@Override
	public void handleDataEvent(@NotNull Ref<EntityStore> ref, @NotNull Store<EntityStore> store, @NotNull MenuEventData data) {
		UICommandBuilder commandBuilder = new UICommandBuilder();
		if(data.elementId != null) {
			binder.onReceived(commandBuilder, data);
		}
		sendUpdate(commandBuilder, false);
	}

	public static class EventBinder {
		private Map<String, BiConsumer<Object, UICommandBuilder>> map = new HashMap<>();
		private int index = 0;
		private List<Consumer<UIEventBuilder>> delayedCalls = new ArrayList<>();
		private List<Consumer<UICommandBuilder>> delayedStyle = new ArrayList<>();

		public void setEventBuilder(UIEventBuilder builder, UICommandBuilder commandBuilder) {
			delayedCalls.forEach(c -> c.accept(builder));
			delayedCalls.clear();
			delayedStyle.forEach(c -> c.accept(commandBuilder));
			delayedStyle.clear();
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

		public <T> void bindEvent(String id, String value, BiConsumer<T, UICommandBuilder> callback, Class<T> clazz) {
			map.put(id, (BiConsumer<Object, UICommandBuilder>) callback);
			delayedCalls.add((eventBuilder) -> {
				var key = MenuEventData.KEY_VALUE_S;
				String type = "String";
				if(clazz == Double.class) {
					key = MenuEventData.KEY_VALUE_D;
					type = "Double";
				} else if(clazz == Boolean.class) {
					key = MenuEventData.KEY_VALUE_B;
					type = "Boolean";
				}
				eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#" + id, EventData.of(MenuEventData.KEY_ID, id).append(MenuEventData.KEY_TYPE, type).append(key, value), false);
			});
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

		public String bindButton(Consumer<UICommandBuilder> callback) {
			var id = getCompId();
			map.put(id, (a, b) -> callback.accept(b));
			delayedCalls.add((eventBuilder) -> {
				eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#" + id, EventData.of(MenuEventData.KEY_ID, id).append(MenuEventData.KEY_TYPE, "Boolean"));
			});
			return id;
		}

		public String bindEventDouble(BiConsumer<Double, UICommandBuilder> callback) {
			return bindEvent(callback, Double.class);
		}

		public String bindEventBool(BiConsumer<Boolean, UICommandBuilder> callback) {
			return bindEvent(callback, Boolean.class);
		}

		public String getCompId() {
			var str = "CompSettings" + index;
			index++;
			return str;
		}

		public void onReceived(UICommandBuilder builder, MenuEventData data) {
			var call = map.get(data.elementId);
			if(call != null) {
				switch(data.type) {
					case "String" -> call.accept(data.stringValue, builder);
					case "Double" -> call.accept(data.doubleValue, builder);
					case "Boolean" -> call.accept(data.booleanValue, builder);
				}

			}

		}
	}

	public static class MenuEventData implements EventDataWithGlyphSettings {
		static final String KEY_ID = "ElementId";
		static final String KEY_TYPE = "ValueType";
		static final String KEY_VALUE_S = "@ElementValueS";
		static final String KEY_VALUE_D = "@ElementValueD";
		static final String KEY_VALUE_B = "@ElementValueB";
		private static final BuilderCodec<GlyphSettingsMenu.MenuEventData> CODEC = appendFields(BuilderCodec.builder(
				GlyphSettingsMenu.MenuEventData.class, GlyphSettingsMenu.MenuEventData::new
		))
				.build();

		public static <T extends EventDataWithGlyphSettings> BuilderCodec.Builder<T> appendFields(BuilderCodec.Builder<T> codec) {
			return codec.append(new KeyedCodec<>(KEY_ID, Codec.STRING), (entry, s) -> entry.getSettings().elementId = s, entry -> entry.getSettings().elementId)
					.add()
					.append(new KeyedCodec<>(KEY_TYPE, Codec.STRING), (entry, s) -> entry.getSettings().type = s, entry -> entry.getSettings().type)
					.add()
					.append(new KeyedCodec<>(KEY_VALUE_S, Codec.STRING), (entry, s) -> entry.getSettings().stringValue = s, entry -> entry.getSettings().stringValue)
					.add()
					.append(new KeyedCodec<>(KEY_VALUE_D, Codec.DOUBLE), (entry, s) -> entry.getSettings().doubleValue = s, entry -> entry.getSettings().doubleValue)
					.add()
					.append(new KeyedCodec<>(KEY_VALUE_B, Codec.BOOLEAN), (entry, s) -> entry.getSettings().booleanValue = s, entry -> entry.getSettings().booleanValue)
					.add();
		}

		private String elementId;
		private String type;
		private String stringValue;
		private double doubleValue;
		private boolean booleanValue;

		public MenuEventData() {
		}

		@Override
		public MenuEventData getSettings() {
			return this;
		}
	}

	public interface EventDataWithGlyphSettings {
		MenuEventData getSettings();
	}
}