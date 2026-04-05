package com.diamssword.redCrystal.gui;

import com.diamssword.redCrystal.RedCrystalPlugin;
import com.diamssword.redCrystal.behavior.RedComponentRegister;
import com.diamssword.redCrystal.storage.Glyph;
import com.diamssword.redCrystal.wand.RedWandTool;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.bson.BsonDocument;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;

public class GlyphMenu extends InteractiveCustomUIPage<GlyphMenu.MenuEventData> {

	private final AtomicReference<String> selected = new AtomicReference<>("");
	private final AtomicReference<Glyph> selectedAsset = new AtomicReference<>();
	private final AtomicReference<String> search = new AtomicReference<>("");
	private RedWandTool toolSettings;
	private Player player;
	private int slot;
	private GlyphSettingsMenu settingsMenu;
	private final AtomicReference<Boolean> needFullRefresh = new AtomicReference<>();
	private final int contentSize = 4;
	private int linesSize = 0;

	public GlyphMenu(@Nonnull PlayerRef playerRef) {
		super(playerRef, CustomPageLifetime.CanDismiss, MenuEventData.CODEC);
	}

	@Override
	public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {
		commandBuilder.append("Pages/RedCrystal/GlyphMenu.ui");
		player = store.getComponent(ref, Player.getComponentType());
		ItemStack stack = InventoryComponent.getItemInHand(store, ref);
		if(stack != null) {
			toolSettings = RedWandTool.getForStack(stack);
			setSelected(toolSettings.getSelectedGlyph());
			var hotbar = playerRef.getReference().getStore().getComponent(playerRef.getReference(), InventoryComponent.Hotbar.getComponentType());
			slot = hotbar.getActiveSlot();
			createGlyphs(commandBuilder);
			boolean flag = false;
			if(selected.get() != null)
				flag = RedComponentRegister.getSettingsCodec(selected.get()) != null;
			commandBuilder.set("#GlyphSettingsBt.Visible", flag);

			eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#GlyphSettingsBt", EventData.of(MenuEventData.KEY_BUTTON, "Settings"));
			settingsMenu = new GlyphSettingsMenu(playerRef, toolSettings.getMainSettings()::clone, s -> {
				toolSettings.setMainSettings(s);
				RedWandTool.updateToolStack(player, slot, toolSettings);
			});
			settingsMenu.appendSettings("MainSettings", commandBuilder, eventBuilder);


			eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SearchInput", EventData.of(MenuEventData.KEY_SEARCH, "#SearchInput.Value"), false);
			refreshGlyphs(commandBuilder, eventBuilder);
			setupSelectedPanel(commandBuilder, eventBuilder);
		}
	}

	private void createGlyphs(UICommandBuilder builder) {
		var map = RedCrystalPlugin.GlyphAssets.getAssetMap().getAssetMap();
		var count = map.size();
		int lines = (int) Math.ceil(count / 4f);
		linesSize = lines;
		for(int i = 0; i < lines; i++) {
			StringBuilder line = new StringBuilder("Group #GlyphLine" + i + " {LayoutMode: Left; Anchor:(Right:0,Left: 0,Top:0,Bottom:0,Width: 540,Height:130);");
			for(int j = 0; j < contentSize; j++) {
				line.append(createGlyph((i * contentSize) + j));
			}
			line.append("}");
			builder.appendInline("#GlyphContainer", line.toString());
		}
	}

	private String createGlyph(int index) {
		return "Group #GlyphPanel" + index + " {" +
				"Anchor: (Width:130,Height:130);" +
				"Button #GlyphButton" + index + " {Anchor:(Width:120,Height:120);}" +
				"Group{LayoutMode:Top; Anchor:(Width:120,Height:120);" +
				"AssetImage #GlyphImage" + index + " {Anchor:(Width:90,Height:90);Padding:(Top:5);}" +
				"Label #GlyphLabel" + index + " { Style: (HorizontalAlignment:Center); Text:\"" + index + "\";}}}";
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

	private void refreshGlyphs(UICommandBuilder builder, UIEventBuilder event) {
		Queue<Glyph> assets = getSortedAssets();
		for(int i = 0; i < linesSize; i++) {
			List<Glyph> ls = new ArrayList<>();
			for(int j = 0; j < contentSize; j++) {
				var asset = assets.poll();
				if(asset != null)
					ls.add(asset);
			}
			updateGlyphLine(i, ls, builder, event);
		}
	}

	private void updateGlyphLine(int index, List<Glyph> assets, UICommandBuilder builder, UIEventBuilder event) {
		builder.set("#GlyphLine" + index + ".Visible", !assets.isEmpty());
		if(!assets.isEmpty()) {
			for(int i = 0; i < contentSize; i++) {
				Glyph asset = null;
				if(i < assets.size())
					asset = assets.get(i);
				updateGlyph((index * contentSize) + i, asset, builder, event);
			}
		}

	}

	private static String parseUrl(String filePath) {
		return filePath;
	}

	private void updateGlyph(int index, @Nullable Glyph asset, UICommandBuilder builder, UIEventBuilder event) {
		builder.set("#GlyphPanel" + index + ".Visible", asset != null);
		if(asset != null) {
			builder.set("#GlyphImage" + index + ".AssetPath", parseUrl(asset.getIcon()));
			builder.set("#GlyphLabel" + index + ".Text", translate(asset.getTranslationProperties().getName()));
			builder.set("#GlyphButton" + index + ".Style", Value.ref("Common.ui", "DefaultButtonStyle"));
			event.addEventBinding(CustomUIEventBindingType.Activating, "#GlyphButton" + index, EventData.of(MenuEventData.KEY_BUTTON, "Glyph").append(MenuEventData.KEY_ASSET, asset.getId()));
			event.addEventBinding(CustomUIEventBindingType.MouseEntered, "#GlyphButton" + index, EventData.of(MenuEventData.KEY_HOVERED, "true").append(MenuEventData.KEY_ASSET, asset.getId()), false);
		}
	}


	private void selectButton(String id, UICommandBuilder builder, UIEventBuilder event) {
		if(!id.equals(selected.get())) {
			toolSettings.setSelectedGlyph(id);
			setSelected(id);
			var codec = RedComponentRegister.getSettingsCodec(id);
			builder.set("#GlyphSettingsBt.Visible", codec != null);
			setupSelectedPanel(builder, event);
			RedWandTool.updateToolStack(playerRef.getReference().getStore().getComponent(playerRef.getReference(), Player.getComponentType()), slot, toolSettings);
		}
	}

	private void setupSelectedPanel(UICommandBuilder builder, UIEventBuilder event) {
		if(selectedAsset.get() != null) {
			builder.set("#SelectedImage.AssetPath", selectedAsset.get().getIcon());
			builder.set("#SelectedTitle.Text", translate(selectedAsset.get().getTranslationProperties().getName()));
		}
	}

	private void setSelected(String id) {
		selected.set(id);
		selectedAsset.set(RedCrystalPlugin.GlyphAssets.getAssetMap().getAssetMap().get(id));
	}

	private String translate(String translateString) {
		var tr = I18nModule.get().getMessage(playerRef.getLanguage(), translateString);
		return tr != null ? tr : translateString;
	}

	private void settingsClicked() {
		var select = selected.get();
		var codec = RedComponentRegister.getSettingsCodec(select);
		if(codec != null)
			new GlyphSettingsMenu(playerRef).withSpecific(select, () -> toolSettings.getGlyphSettings(select).orElse(new BsonDocument()), doc -> {
				toolSettings.setGlyphSettings(select, doc);
				RedWandTool.updateToolStack(player, slot, toolSettings);
			}, codec).openAsSubMenu(() -> {
				playerRef.getReference().getStore().getExternalData().getWorld().execute(() -> {
					var menu = new GlyphMenu(playerRef);
					Player playerComponent = playerRef.getReference().getStore().getComponent(playerRef.getReference(), Player.getComponentType());
					if(playerComponent != null)
						playerComponent.getPageManager().openCustomPage(playerRef.getReference(), playerRef.getReference().getStore(), menu);

				});
			});

	}

	private void setupInfoPanel(UICommandBuilder builder, UIEventBuilder event, Glyph asset) {
		builder.set("#HoveredImage.AssetPath", parseUrl(asset.getIcon()));
		builder.set("#HoveredTitle.Text", translate(asset.getTranslationProperties().getName()));
		builder.set("#HoveredDesc.Text", translate(asset.getTranslationProperties().getDescription()));
		builder.set("#HoveredType.Text", translate(asset.getCategorie()));
		var ioS = "Input";
		if(asset.getInputs() > 1)
			ioS += "s";
		ioS += ": " + asset.getInputs() + " | Output";
		if(asset.getOutputs() > 1)
			ioS += "s";
		ioS += ": " + asset.getOutputs();
		builder.set("#HoveredIO.Text", ioS);
		this.sendUpdate(builder, event, false);
	}

	public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull MenuEventData data) {
		UICommandBuilder commandBuilder = new UICommandBuilder();
		UIEventBuilder eventBuilder = new UIEventBuilder();

		if(data.clickedButton != null) {
			if(data.clickedButton.equals("Settings"))
				settingsClicked();
			else if(data.clickedButton.equals("Glyph")) {
				selectButton(data.assetId, commandBuilder, eventBuilder);
			}
		}
		if(data.hovered && data.assetId != null) {
			setupInfoPanel(commandBuilder, eventBuilder, RedCrystalPlugin.GlyphAssets.getAssetMap().getAsset(data.assetId));
		}
		if(data.search != null) {
			var val = data.search.toLowerCase().trim();
			if(!search.get().equals(val)) {
				search.set(val);
				refreshGlyphs(commandBuilder, eventBuilder);
			}
		}
		if(settingsMenu != null)
			settingsMenu.handleDataEvent(ref, store, data.getSettings());
		this.sendUpdate(commandBuilder, eventBuilder, false);
	}


	public static class MenuEventData implements EventDataWithGlyphSettings {
		static final String KEY_BUTTON = "ButtonClicked";
		static final String KEY_ASSET = "AssetId";
		static final String KEY_HOVERED = "Hovered";
		static final String KEY_SEARCH = "@SearchQuery";
		public static final BuilderCodec<MenuEventData> CODEC = UniversalDataBinding.appendFields(BuilderCodec.builder(
								MenuEventData.class, MenuEventData::new
						)
						.append(new KeyedCodec<>(KEY_BUTTON, Codec.STRING), (entry, s) -> entry.clickedButton = s, entry -> entry.clickedButton)
						.add()
						.append(new KeyedCodec<>(KEY_ASSET, Codec.STRING), (entry, s) -> entry.assetId = s, entry -> entry.assetId)
						.add()
						.append(new KeyedCodec<>(KEY_HOVERED, Codec.STRING), (entry, s) -> entry.hovered = Boolean.parseBoolean(s), entry -> entry.hovered + "")
						.add()
						.append(new KeyedCodec<>(KEY_SEARCH, Codec.STRING), (entry, s) -> entry.search = s, entry -> entry.search)
						.add())
				.build();
		private String clickedButton;
		private boolean hovered = false;
		private String assetId;
		private String search;
		private UniversalDataBinding settings = new UniversalDataBinding();

		public MenuEventData() {
		}

		@Override
		public UniversalDataBinding getSettings() {
			return settings;
		}
	}
}
