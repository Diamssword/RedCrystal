package com.diamssword.redCrystal.wand;

import au.ellie.hyui.builders.*;
import au.ellie.hyui.events.MouseEventData;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.types.DefaultStyles;
import au.ellie.hyui.types.LayoutMode;
import com.diamssword.redCrystal.RedCrystalPlugin;
import com.diamssword.redCrystal.storage.Glyph;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nullable;
import javax.naming.directory.Attribute;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


public class GlyphMenu {

	private final AtomicReference<Integer> hovered = new AtomicReference<>();


	private final AtomicReference<String> selected = new AtomicReference<>("");
	private final AtomicReference<Glyph> selectedAsset = new AtomicReference<>();
	private final AtomicReference<String> search = new AtomicReference<>("");
	private PlayerRef playerRef;
	private RedWandTool toolSettings;
	private int slot;
	private final AtomicReference<Boolean> needFullRefresh = new AtomicReference<>();
	private final int contentSize = 4;
	private int linesSize = 0;

	public HyUIPage openMenu(PlayerRef ref) {
		this.playerRef = ref;
		var player = ref.getReference().getStore().getComponent(ref.getReference(), Player.getComponentType());
		ItemStack stack = player.getInventory().getActiveHotbarItem();
		if(stack != null) {
			toolSettings = RedWandTool.getForStack(stack);
			setSelected(toolSettings.getSelectedGlyph());
			slot = player.getInventory().getActiveHotbarSlot();
			var prototype = PageBuilder.detachedPage().loadHtml("Pages/RedCrystal/GlyphMenu.html")
					.withLifetime(CustomPageLifetime.CanDismiss);

			prototype.getById("GlyphContainer", GroupBuilder.class).ifPresent(this::createGlyphs);
			prototype.onBuild((a, b) -> {
				if(!b) {
					this.refreshGlyphs(a);
					setupSelectedPanel(a);
				}
			});
			prototype.getById("mainSettings", GroupBuilder.class).ifPresent(div -> {
				new GlyphSettingsMenu(ref, toolSettings::getMainSettings, s -> {
					toolSettings.setMainSettings(s);
					RedWandTool.updateToolStack(player, slot, toolSettings);
				}).appendSettings(div);
			});
			prototype.addEventListener("search", CustomUIEventBindingType.ValueChanged, (ob, ctx) -> {
				var val = ((String) ob).toLowerCase().trim();
				if(!search.get().equals(val)) {
					search.set(val);
					this.refreshGlyphs(ctx);
					ctx.getById("search", TextFieldBuilder.class).ifPresent(f -> f.withValue(search.get()));
					ctx.updatePage(false);
				}
			});
			return prototype.open(ref, ref.getReference().getStore());
		}
		return null;
	}

	private void createGlyphs(GroupBuilder parent) {
		var map = RedCrystalPlugin.GlyphAssets.getAssetMap().getAssetMap();
		var count = map.keySet().size();
		int lines = (int) Math.ceil(count / 4f);
		linesSize = lines;
		for(int i = 0; i < lines; i++) {
			var line = createGlyphLine(i);
			for(int j = 0; j < contentSize; j++) {
				line.addChild(createGlyph((i * contentSize) + j));
			}
			parent.addChild(line);
		}
	}

	private Queue<Glyph> getSortedAssets() {

		var map = RedCrystalPlugin.GlyphAssets.getAssetMap().getAssetMap();
		Queue<Glyph> assets = new PriorityQueue<>((a, b) -> {
			int cmp = a.compareTo(b);
			if(cmp != 0) return cmp;
			return translate(a.getTranslationProperties().getName()).compareTo(translate(b.getTranslationProperties().getName()));
		});
		var str = search.get();
		for(String id : map.keySet()) {
			var asset = map.get(id);
			var name = translate(asset.getTranslationProperties().getName());
			if(str.isBlank() || name.toLowerCase().contains(str)) {
				assets.add(asset);
			}
		}
		if(assets.isEmpty()) {
			for(String id : map.keySet()) {
				var asset = map.get(id);

				if(asset.getCategorie().toLowerCase().contains(str)) {
					assets.add(asset);
				}
			}
		}
		return assets;
	}

	private void refreshGlyphs(UIContext ctx) {

		Queue<Glyph> assets = getSortedAssets();

		for(int i = 0; i < linesSize; i++) {
			List<Glyph> ls = new ArrayList<>();
			for(int j = 0; j < contentSize; j++) {
				var asset = assets.poll();
				if(asset != null)
					ls.add(asset);
			}
			updateGlyphLine(i, ls, ctx);
		}
	}

	private void updateGlyphLine(int index, List<Glyph> assets, UIContext ctx) {
		ctx.getById("GlyphLine" + index, GroupBuilder.class).ifPresent(l -> {
			l.withVisible(!assets.isEmpty());
			if(!assets.isEmpty()) {
				for(int i = 0; i < contentSize; i++) {
					Glyph asset = null;
					if(i < assets.size())
						asset = assets.get(i);
					updateGlyph((index * contentSize) + i, asset, ctx);
				}
			}
		});
	}

	private void updateGlyph(int index, @Nullable Glyph asset, UIContext ctx) {
		ctx.getById("GlyphPanel" + index, GroupBuilder.class).ifPresent(l -> {
			l.withVisible(asset != null);
		});
		if(asset != null) {
			ctx.getById("GlyphImage" + index, ImageBuilder.class).ifPresent(l -> {
				l.withImage(parseUrl(asset.getIcon()));
			});
			ctx.getById("GlyphLabel" + index, LabelBuilder.class).ifPresent(l -> {
				l.withText(translate(asset.getTranslationProperties().getName()));
			});
			ctx.getById("GlyphButton" + index, CustomButtonBuilder.class).ifPresent(b -> {
				b.addEventListener(CustomUIEventBindingType.Activating, (_, ctx1) -> selectButton(asset.getId(), ctx1));
				b.addEventListenerWithContext(CustomUIEventBindingType.MouseEntered, MouseEventData.class, (_, ctx1) -> {
					hovered.set(index);
					setupInfoPanel(ctx1, asset);
				});
		/*		b.addEventListenerWithContext(CustomUIEventBindingType.MouseExited, MouseEventData.class, (_, ctx1) -> {
					if(index == hovered.get())
						setupInfoPanel(ctx1, selectedAsset.get());
				});*/
			});
		}
	}

	private GroupBuilder createGlyphLine(int index) {
		return GroupBuilder.group().withId("GlyphLine" + index).withLayoutMode(LayoutMode.Left).withAnchor(new HyUIAnchor().setLeft(0).setRight(0).setTop(0).setBottom(0).setWidth(540).setHeight(130));
	}

	private GroupBuilder createGlyph(int index) {
		var div = GroupBuilder.group().withAnchor(new HyUIAnchor().setWidth(130).setHeight(130)).withId("GlyphPanel" + index);
		var bt = CustomButtonBuilder.customButton().withAnchor(new HyUIAnchor().setWidth(120).setHeight(120)).withId("GlyphButton" + index);

		div.addChild(bt);
		var cont = GroupBuilder.group().withLayoutMode(LayoutMode.Top).withAnchor(new HyUIAnchor().setWidth(120).setHeight(120));
		cont.addChild(ImageBuilder.image().withAnchor(new HyUIAnchor().setWidth(90).setHeight(90)).withPadding(new HyUIPadding().setTop(5)).withId("GlyphImage" + index));
		cont.addChild(LabelBuilder.label().withStyle(new HyUIStyle().set("Alignment", "Center").setTextColor("#ffffff")).withId("GlyphLabel" + index).withText(index + ""));
		div.addChild(cont);
		return div;
	}


	private void selectButton(String id, UIContext ctx) {
		if(!id.equals(selected.get())) {
			toolSettings.setSelectedGlyph(id);
		/*	styleButton(button, true);
			ctx.getById("GlyphButton" + selectedID.get(), CustomButtonBuilder.class).ifPresent(b -> {
				styleButton(b, false);
			});
			selectedID.set(index);
*/
			ctx.getById("search", TextFieldBuilder.class).ifPresent(f -> f.withValue(search.get()));
			setSelected(id);
			setupSelectedPanel(ctx);
			ctx.updatePage(false);
			RedWandTool.updateToolStack(playerRef.getReference().getStore().getComponent(playerRef.getReference(), Player.getComponentType()), slot, toolSettings);
		}
	}

	private void setSelected(String id) {
		selected.set(id);
		selectedAsset.set(RedCrystalPlugin.GlyphAssets.getAssetMap().getAssetMap().get(id));
	}


	private static void styleButton(CustomButtonBuilder button, boolean selected) {
		if(selected) {
			button.withDefaultBackground(new HyUIPatchStyle().setTexturePath("Pages/RedCrystal/Selected_Glyph_Button1.png"));
			button.withHoveredBackground(new HyUIPatchStyle().setTexturePath("Pages/RedCrystal/Selected_Glyph_Button1.png"));
			button.withPressedBackground(new HyUIPatchStyle().setTexturePath("Pages/RedCrystal/Selected_Glyph_Button1.png"));
		} else {
			button.withDefaultBackground(DefaultStyles.primarySquareButtonDefaultBackground());
			button.withHoveredBackground(DefaultStyles.primarySquareButtonHoveredBackground());
			button.withPressedBackground(DefaultStyles.primarySquareButtonPressedBackground());
		}
	}

	private static String parseUrl(String filePath) {
		return filePath.replace("UI/Custom/", "");
	}

	private void setupInfoPanel(UIContext ctx, Glyph asset) {
		ctx.getById("hovered.image", ImageBuilder.class).ifPresent(im -> im.withImage(parseUrl(asset.getIcon())));
		ctx.getById("hovered.title", LabelBuilder.class).ifPresent(im -> im.withText(translate(asset.getTranslationProperties().getName())));
		ctx.getById("hovered.desc", LabelBuilder.class).ifPresent(im -> im.withText(translate(asset.getTranslationProperties().getDescription())));
		ctx.getById("hovered.type", LabelBuilder.class).ifPresent(im -> im.withText(translate(asset.getCategorie())));

		ctx.getById("hovered.io", LabelBuilder.class).ifPresent(im -> {
			var ioS = "Input";
			if(asset.getInputs() > 1)
				ioS += "s";
			ioS += ": " + asset.getInputs() + " | Output";
			if(asset.getOutputs() > 1)
				ioS += "s";
			ioS += ": " + asset.getOutputs();
			im.withText(ioS);
		});
		ctx.updatePage(false);
	}

	private void setupSelectedPanel(UIContext ctx) {
		if(selectedAsset.get() != null) {
			ctx.getById("selected.image", ImageBuilder.class).ifPresent(im -> im.withImage(parseUrl(selectedAsset.get().getIcon())));
			ctx.getById("selected.title", LabelBuilder.class).ifPresent(im -> im.withText(translate(selectedAsset.get().getTranslationProperties().getName())));
			ctx.updatePage(false);
		}
	}

	private String translate(String translateString) {
		var tr = I18nModule.get().getMessage(playerRef.getLanguage(), translateString);
		return tr != null ? tr : translateString;
	}

	private String translate(String glyph, String suffix) {
		var tr = I18nModule.get().getMessage(playerRef.getLanguage(), "server.RedCrystal.glyph." + glyph + "." + suffix);
		return tr != null ? tr : glyph;
	}
}