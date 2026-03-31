package com.diamssword.redCrystal.gui;

import au.ellie.hyui.builders.*;
import au.ellie.hyui.events.MouseEventData;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.types.DefaultStyles;
import au.ellie.hyui.types.LayoutMode;
import com.diamssword.redCrystal.RedCrystalPlugin;
import com.diamssword.redCrystal.storage.Glyph;
import com.diamssword.redCrystal.wand.RedWandTool;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.codec.validation.validator.RangeValidator;
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
import java.util.concurrent.atomic.AtomicReference;


public class GlyphSettingsValidators {

	public static class FloatRangeValidator extends RangeValidator<Float> {
		public final float min;
		public final float max;
		public final float step;

		public FloatRangeValidator(float min, float max, float step) {
			super(min, max, true);
			this.min = min;
			this.max = max;
			this.step = step;
		}
	}
}