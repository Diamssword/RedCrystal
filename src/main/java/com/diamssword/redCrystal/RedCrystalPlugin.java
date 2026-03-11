package com.diamssword.redCrystal;

import au.ellie.hyui.builders.HyUIPage;
import com.diamssword.redCrystal.wand.GlyphMenu;
import com.diamssword.redCrystal.storage.Glyph;
import com.diamssword.redCrystal.redComponent.RedComponentRegister;
import com.diamssword.redCrystal.display.DisplayEntitySystem;
import com.diamssword.redCrystal.storage.PlayerSystems;
import com.diamssword.redCrystal.storage.RedElementState;
import com.diamssword.redCrystal.storage.RedElementSystems;
import com.diamssword.redCrystal.display.RedEntityHiddenComponent;
import com.diamssword.redCrystal.display.RedEntityLinkComponent;
import com.diamssword.redCrystal.interaction.*;
import com.diamssword.redCrystal.wand.LinkingState;
import com.diamssword.redCrystal.worldInteraction.FakeLivingEntity;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.asset.type.item.config.*;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class RedCrystalPlugin extends JavaPlugin {

	public static ComponentType<ChunkStore, RedElementState> RedElementComponent;
	public static ComponentType<EntityStore, RedEntityLinkComponent> RedLinkComponent;
	public static ComponentType<EntityStore, RedEntityHiddenComponent> RedEntityHiddenComponent;
	public static ComponentType<EntityStore, LinkingState> RedToolSettingsComponent;
	public static HytaleAssetStore<String, Glyph, DefaultAssetMap<String, Glyph>> GlyphAssets;


	public RedCrystalPlugin(@Nonnull JavaPluginInit init) {
		super(init);
	}

	@Override
	protected void setup() {
		var builder = HytaleAssetStore.builder(Glyph.class, new DefaultAssetMap<>());
		builder.setPath("RedCrystal/Glyphs").setCodec(Glyph.CODEC).setKeyFunction(Glyph::getId).loadsAfter(ResourceType.class);
		GlyphAssets = AssetRegistry.register(builder.build());
		RedComponentRegister.init();
		RedElementComponent = this.getChunkStoreRegistry().registerComponent(RedElementState.class, "RedCrystal_RedElementState", RedElementState.CODEC);
		this.getChunkStoreRegistry().registerSystem(new RedElementSystems.RedElementAddedSystem());
		this.getChunkStoreRegistry().registerSystem(new RedElementSystems.RedElementTickSystem());
		this.getChunkStoreRegistry().registerSystem(new RedElementSystems.RedElementDisplayTickSystem());
		RedToolSettingsComponent = this.getEntityStoreRegistry().registerComponent(LinkingState.class, LinkingState::new);
		RedLinkComponent = this.getEntityStoreRegistry().registerComponent(RedEntityLinkComponent.class, () -> {throw new UnsupportedOperationException();});
		RedEntityHiddenComponent = this.getEntityStoreRegistry().registerComponent(RedEntityHiddenComponent.class, () -> {throw new UnsupportedOperationException();});

		this.getEntityStoreRegistry().registerSystem(new PlayerSystems.ToolTicking());
		this.getEntityStoreRegistry().registerSystem(new DisplayEntitySystem());

		this.getEntityRegistry().registerEntity("RedCrystalFakeEntity", FakeLivingEntity.class, FakeLivingEntity::new, FakeLivingEntity.CODEC);

		Interaction.CODEC.register("RedCrystal_Wand_Interact", WandBlockInteraction.class, WandBlockInteraction.CODEC);
		Interaction.CODEC.register("RedCrystal_Wand_Interact_Entity", WandEntityInteraction.class, WandEntityInteraction.CODEC);
		Interaction.CODEC.register("RedCrystal_Wand_Reveal", WandRevealInteraction.class, WandRevealInteraction.CODEC);
		Interaction.CODEC.register("RedCrystal_UseRedCrystalEntity", UseRedEntityInteraction.class, UseRedEntityInteraction.CODEC);
		Interaction.getAssetStore().loadAssets("Diamssword:RedCrystal", List.of(new UseRedEntityInteraction("*UseRedCrystalEntity")));
		RootInteraction.getAssetStore().loadAssets("Diamssword:RedCrystal", List.of(UseRedEntityInteraction.DEFAULT_ROOT));

		OpenCustomUIInteraction.registerCustomPageSupplier(this, HyUIPage.class, "RedCrystalGlyphMenu", (a, b, c, d) -> GlyphMenu.openMenu(c));
		this.getEventRegistry().register(LoadedAssetsEvent.class, Glyph.class, this::onGlyphAssetChange);

	}

	private void onGlyphAssetChange(@Nonnull LoadedAssetsEvent<String, Glyph, DefaultAssetMap<String, Glyph>> event) {
		if(!event.isInitial()) {
			for(Map.Entry<String, Glyph> entry : event.getLoadedAssets().entrySet()) {

				Universe.get().getWorlds().forEach((k, w) -> {
					w.execute(() -> {
						w.getChunkStore().getStore().forEachChunk(RedElementState.getComponent(), (archetypeChunk, buffer) -> {
							for(int index = 0; index < archetypeChunk.size(); index++) {
								var comp = archetypeChunk.getComponent(index, RedElementState.getComponent());
								if(comp != null) {
									comp.getAllElements().forEach((f, el) -> {
										var asset = event.getLoadedAssets().get(el.getAsset().getId());
										if(asset != null) {
											System.out.println("reloaded " + el.getAsset().getId());
											el.setAsset(asset);
										}
									});
								}
							}
						});
					});
				});
			}
		}

	}
}