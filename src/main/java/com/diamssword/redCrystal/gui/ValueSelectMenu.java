package com.diamssword.redCrystal.gui;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.function.consumer.TriConsumer;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;


public class ValueSelectMenu extends InteractiveCustomUIPage<UniversalDataBinding> {

	private UniversalEventBinder binder = new UniversalEventBinder();
	private final String uiFile;
	private TriConsumer<UICommandBuilder, UIEventBuilder, UniversalEventBinder> onBuild;

	protected ValueSelectMenu(@Nonnull PlayerRef playerRef, String uiFile) {
		super(playerRef, CustomPageLifetime.CanDismiss, UniversalDataBinding.CODEC);
		this.uiFile = uiFile;
	}

	public void onBuild(TriConsumer<UICommandBuilder, UIEventBuilder, UniversalEventBinder> callback) {
		this.onBuild = callback;
	}

	public void open() {
		if(playerRef.isValid() && playerRef.getReference().isValid()) {
			var player = playerRef.getReference().getStore().getComponent(playerRef.getReference(), Player.getComponentType());
			if(player != null)
				player.getPageManager().openCustomPage(playerRef.getReference(), playerRef.getReference().getStore(), this);
		}
	}

	public static void openRange(PlayerRef ref, int min, int max, int value, Consumer<Integer> onChange) {
		var page = new ValueSelectMenu(ref, "Pages/RedCrystal/RangeInput.ui");
		page.onBuild((builder, e, bind) -> {
			AtomicInteger valueIn = new AtomicInteger(value);
			Consumer<UICommandBuilder> update = (b) -> {
				var val = valueIn.get();
				b.set("#Text.Text", val + "");
				onChange.accept(val);
			};
			builder.set("#Text.Text", value + "");
			builder.set("#Slider.Min", min).set("#Slider.Max", max).set("#Slider.Value", value);
			bind.bindEvent("Slider", "#Slider.Value", (v, b) -> {
				valueIn.set(v);
				update.accept(b);
			}, Integer.class);
			bind.bindButton("BtM", (b) -> {
				valueIn.set(Math.min(valueIn.get() + 1, max));
				update.accept(b);
			});
			bind.bindButton("BtL", (b) -> {
				valueIn.set(Math.max(valueIn.get() - 1, min));
				update.accept(b);
			});

		});
		page.open();
	}

	@Override
	public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull UniversalDataBinding data) {
		var builder = new UICommandBuilder();
		binder.onReceived(builder, data);
		if(binder.getReloadFlag()) {
			var binder = new UIEventBuilder();
			build(ref, builder, binder, store);
			sendUpdate(builder, binder, true);
		} else
			sendUpdate(builder, false);
	}

	@Override
	public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {
		commandBuilder.append(uiFile);
		if(onBuild != null)
			onBuild.accept(commandBuilder, eventBuilder, binder);
		binder.setEventBuilder(eventBuilder, commandBuilder);
	}

}