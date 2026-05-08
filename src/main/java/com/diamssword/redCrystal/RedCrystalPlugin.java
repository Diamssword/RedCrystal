package com.diamssword.redCrystal;

import com.diamssword.redCrystal.behavior.base.RedCompBehaviorWithModel;
import com.diamssword.redCrystal.behavior.inputs.*;
import com.diamssword.redCrystal.behavior.modifiers.*;
import com.diamssword.redCrystal.behavior.outputs.*;
import com.diamssword.redCrystal.gui.GlyphMenu;
import com.diamssword.redCrystal.storage.*;
import com.diamssword.redCrystal.behavior.RedComponentRegister;
import com.diamssword.redCrystal.storage.assets.BehaviorAssetWithSettings;
import com.diamssword.redCrystal.storage.assets.BehaviorAssetWithSwitchModels;
import com.diamssword.redCrystal.systems.DisplayEntitySystem;
import com.diamssword.redCrystal.display.RedEntityHiddenComponent;
import com.diamssword.redCrystal.display.RedEntityLinkComponent;
import com.diamssword.redCrystal.interaction.*;
import com.diamssword.redCrystal.systems.PlayerSystems;
import com.diamssword.redCrystal.systems.RedElementSystems;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
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

public class RedCrystalPlugin extends JavaPlugin {

	public static ComponentType<ChunkStore, RedElementState> RedElementComponent;
	public static ComponentType<EntityStore, RedEntityLinkComponent> RedLinkComponent;
	public static ComponentType<EntityStore, RedEntityHiddenComponent> RedEntityHiddenComponent;
	public static ComponentType<EntityStore, PlayerDatas> RedToolSettingsComponent;
	public static HytaleAssetStore<String, Glyph, DefaultAssetMap<String, Glyph>> GlyphAssets;
	public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
	private static RedComponentRegister behaviorRegister;

	public RedCrystalPlugin(@Nonnull JavaPluginInit init) {
		super(init);
	}

	public static RedComponentRegister getBehaviorRegister() {
		return behaviorRegister;
	}

	@Override
	protected void setup() {
		behaviorRegister = new RedComponentRegister();
		var builder = HytaleAssetStore.builder(Glyph.class, new DefaultAssetMap<>());
		builder.setPath("RedCrystal/Glyphs").setCodec(Glyph.CODEC).loadsAfter(Item.class).setKeyFunction(Glyph::getId).loadsAfter(ResourceType.class);
		initBehaviors(behaviorRegister);
		if(GlyphAssets == null)
			GlyphAssets = AssetRegistry.register(builder.build());
		RedElementComponent = this.getChunkStoreRegistry().registerComponent(RedElementState.class, "RedCrystal_RedElementState", RedElementState.CODEC);
		RedToolSettingsComponent = this.getEntityStoreRegistry().registerComponent(PlayerDatas.class, PlayerDatas::new);
		RedLinkComponent = this.getEntityStoreRegistry().registerComponent(RedEntityLinkComponent.class, () -> {throw new UnsupportedOperationException();});
		RedEntityHiddenComponent = this.getEntityStoreRegistry().registerComponent(RedEntityHiddenComponent.class, () -> {throw new UnsupportedOperationException();});

		Interaction.CODEC.register("RedCrystal_Wand_Interact", WandBlockInteraction.class, WandBlockInteraction.CODEC);
		Interaction.CODEC.register("RedCrystal_Wand_Interact_Entity", WandEntityInteraction.class, WandEntityInteraction.CODEC);
		Interaction.CODEC.register("RedCrystal_Wand_Reveal", WandRevealInteraction.class, WandRevealInteraction.CODEC);
		Interaction.CODEC.register("RedCrystal_UseRedCrystalEntity", UseRedEntityInteraction.class, UseRedEntityInteraction.CODEC);
		Interaction.getAssetStore().loadAssets("Diamssword:RedCrystal", List.of(new UseRedEntityInteraction("*UseRedCrystalEntity")));
		RootInteraction.getAssetStore().loadAssets("Diamssword:RedCrystal", List.of(UseRedEntityInteraction.DEFAULT_ROOT));
		OpenCustomUIInteraction.registerCustomPageSupplier(this, GlyphMenu.class, "RedCrystalGlyphMenu", (a, b, c, d) -> new GlyphMenu(c));
		this.getEventRegistry().register(LoadedAssetsEvent.class, Glyph.class, this::onGlyphAssetChange);

		this.getChunkStoreRegistry().registerSystem(new RedElementSystems.RedElementAddedSystem());
		this.getChunkStoreRegistry().registerSystem(new RedElementSystems.RedElementTickSystem());
		this.getChunkStoreRegistry().registerSystem(new RedElementSystems.RedElementDisplayTickSystem());
		this.getEntityStoreRegistry().registerSystem(new PlayerSystems.ToolTicking());
		this.getEntityStoreRegistry().registerSystem(new PlayerSystems.InventoryTicking());
		this.getEntityStoreRegistry().registerSystem(new DisplayEntitySystem());
	}

	private static void initBehaviors(RedComponentRegister register) {
		register.register("Button", ButtonBehavior::new, BehaviorAssetWithSwitchModels::getCODEC, ButtonBehavior.CODEC);
		register.register("Keypad", KeypadBehavior::new, BehaviorAssetWithSwitchModels::getCODEC, KeypadBehavior.CODEC);
		register.register("Interact", InteractBehavior::new);
		register.register("Toggle", ToggleBehavior::new);
		register.register("AND", AndBehavior::new);
		register.register("OR", OrBehavior::new, BehaviorAssetWithSettings::AbsoluteCodec);
		register.register("NOT", NotBehavior::new, BehaviorAssetWithSettings::BinaryCodec);
		register.register("Light", LightBehavior::new, BehaviorAssetWithSettings::LightCodec, LightBehavior.CODEC);
		register.register("Lever", LeverBehavior::new, BehaviorAssetWithSwitchModels::getCODEC, RedCompBehaviorWithModel.CODEC);
		register.register("PressurePlate", PressurePlateBehavior::new, BehaviorAssetWithSwitchModels::getCODEC, PressurePlateBehavior.CODEC);
		register.register("Variator", VariatorBehavior::new, BehaviorAssetWithSettings::VariatorCodec, RedCompBehaviorWithModel.CODEC);
		register.register("LaserDetector", LaserDetectorBehavior::new, LaserDetectorBehavior.CODEC);
		register.register("Fan", FanBehavior::new, BehaviorAssetWithSettings::DistanceCodec);
		register.register("Calculus", CalculusBehavior::new, BehaviorAssetWithSettings::CalculusCodec);
		register.register("Comparator", ComparatorBehavior::new, ComparatorBehavior.CODEC);
		register.register("BitwiseOperator", BitwiseOperatorBehavior::new, BitwiseOperatorBehavior.CODEC);
		register.register("PreciseInput", PreciseInput::new, BehaviorAssetWithSwitchModels::getCODEC, RedCompBehaviorWithModel.CODEC);
		register.register("NumberDisplay", NumberDisplayBehavior::new);
		register.register("Piston", PistonBehavior::new, PistonBehavior.CODEC);
		register.register("Delayer", DelayBehavior::new, DelayBehavior.CODEC);


	}

	private void onGlyphAssetChange(@Nonnull LoadedAssetsEvent<String, Glyph, DefaultAssetMap<String, Glyph>> event) {
		if(!event.isInitial()) {
			var keys = event.getLoadedAssets().keySet();
			Universe.get().getWorlds().forEach((k, w) -> {
				w.execute(() -> {
					w.getChunkStore().getStore().forEachChunk(RedElementState.getComponent(), (archetypeChunk, buffer) -> {
						for(int index = 0; index < archetypeChunk.size(); index++) {
							var comp = archetypeChunk.getComponent(index, RedElementState.getComponent());
							if(comp != null) {
								comp.getAllElements().forEach((f, el) -> {
									if(keys.contains(el.getAsset().getId())) {
										var asset = event.getLoadedAssets().get(el.getAsset().getId());
										if(asset != null) {
											el.setAsset(asset);
										}
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