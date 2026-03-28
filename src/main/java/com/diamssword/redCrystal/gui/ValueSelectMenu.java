package com.diamssword.redCrystal.gui;

import au.ellie.hyui.builders.*;
import au.ellie.hyui.events.MouseEventData;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.types.DefaultStyles;
import au.ellie.hyui.types.LayoutMode;
import com.diamssword.redCrystal.RedCrystalPlugin;
import com.diamssword.redCrystal.storage.Glyph;
import com.diamssword.redCrystal.wand.RedWandTool;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


public class ValueSelectMenu {


	public static void openRange(PlayerRef ref, int min, int max, int value, Consumer<Integer> onChange) {


		var prototype = PageBuilder.pageForPlayer(ref).loadHtml("Pages/RedCrystal/RangeInput.html")
				.withLifetime(CustomPageLifetime.CanDismiss);
		AtomicInteger valueIn = new AtomicInteger(value);
		Consumer<UIContext> update = (ctx) -> {
			var val = valueIn.get();
			ctx.getById("label", LabelBuilder.class).ifPresent(l -> l.withText(val + ""));
			ctx.getById("input", SliderBuilder.class).ifPresent(l -> l.withValue(val));
			onChange.accept(val);
			ctx.updatePage(false);
		};
		prototype.addEventListener("input", CustomUIEventBindingType.ValueChanged, (b, ctx) -> {
			valueIn.set((Integer) b);
			update.accept(ctx);
		});
		prototype.getById("input", SliderBuilder.class).ifPresent(s -> s.withMin(min).withMax(max).withValue(value));
		prototype.getById("label", LabelBuilder.class).ifPresent(l -> l.withText(value + ""));
		prototype.addEventListener("btM", CustomUIEventBindingType.Activating, (b, ctx) -> {
			valueIn.set(Math.min(valueIn.get() + 1, max));
			update.accept(ctx);
		});
		prototype.addEventListener("btL", CustomUIEventBindingType.Activating, (b, ctx) -> {
			valueIn.set(Math.max(valueIn.get() - 1, min));
			update.accept(ctx);
		});
		prototype.open(ref, ref.getReference().getStore());
	}

}