package com.diamssword.redCrystal;

import au.ellie.hyui.builders.HyUIPage;
import com.diamssword.redCrystal.menu.GlyphMenu;
import com.diamssword.redCrystal.redComponent.RedComponentRegister;
import com.diamssword.redCrystal.display.DisplayEntitySystem;
import com.diamssword.redCrystal.storage.PlayerSystems;
import com.diamssword.redCrystal.storage.RedElementState;
import com.diamssword.redCrystal.storage.RedElementSystems;
import com.diamssword.redCrystal.display.RedEntityHiddenComponent;
import com.diamssword.redCrystal.display.RedEntityLinkComponent;
import com.diamssword.redCrystal.interaction.*;
import com.diamssword.redCrystal.worldInteraction.FakeLivingEntity;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.List;

public class RedCrystalPlugin extends JavaPlugin {

	public static ComponentType<ChunkStore, RedElementState> RedElementComponent;
	public static ComponentType<EntityStore, RedEntityLinkComponent> RedLinkComponent;
	public static ComponentType<EntityStore, RedEntityHiddenComponent> RedEntityHiddenComponent;
	public static ComponentType<EntityStore, ToolSettings> RedToolSettingsComponent;

	public RedCrystalPlugin(@Nonnull JavaPluginInit init) {
		super(init);
	}

	@Override
	protected void setup() {
		RedComponentRegister.init();
		RedElementComponent = this.getChunkStoreRegistry().registerComponent(RedElementState.class, "RedCrystal_RedElementState", RedElementState.CODEC);
		this.getChunkStoreRegistry().registerSystem(new RedElementSystems.RedElementAddedSystem());
		this.getChunkStoreRegistry().registerSystem(new RedElementSystems.RedElementTickSystem());
		this.getChunkStoreRegistry().registerSystem(new RedElementSystems.RedElementDisplayTickSystem());
		RedToolSettingsComponent = this.getEntityStoreRegistry().registerComponent(ToolSettings.class, ToolSettings::new);
		RedLinkComponent = this.getEntityStoreRegistry().registerComponent(RedEntityLinkComponent.class, () -> {throw new UnsupportedOperationException();});
		RedEntityHiddenComponent = this.getEntityStoreRegistry().registerComponent(RedEntityHiddenComponent.class, () -> {throw new UnsupportedOperationException();});

		this.getEntityStoreRegistry().registerSystem(new PlayerSystems.ToolTicking());
		this.getEntityStoreRegistry().registerSystem(new DisplayEntitySystem());

		this.getEntityRegistry().registerEntity("RedCrystalFakeEntity", FakeLivingEntity.class, FakeLivingEntity::new, FakeLivingEntity.CODEC);

		Interaction.CODEC.register("RedCrystal_Wand_Interact", WandBlockInteraction.class, WandBlockInteraction.CODEC);
		Interaction.CODEC.register("RedCrystal_Wand_Reveal", WandRevealInteraction.class, WandRevealInteraction.CODEC);
		Interaction.CODEC.register("RedCrystal_Wand_Link", WandLinkInteraction.class, WandLinkInteraction.CODEC);
		Interaction.CODEC.register("RedCrystal_UseRedCrystalEntity", UseRedEntityInteraction.class, UseRedEntityInteraction.CODEC);
		Interaction.getAssetStore().loadAssets("Diamssword:RedCrystal", List.of(new UseRedEntityInteraction("*UseRedCrystalEntity")));
		RootInteraction.getAssetStore().loadAssets("Diamssword:RedCrystal", List.of(UseRedEntityInteraction.DEFAULT_ROOT));

		OpenCustomUIInteraction.registerCustomPageSupplier(this, HyUIPage.class, "RedCrystalGlyphMenu", (a, b, c, d) -> GlyphMenu.openMenu(c));
	}

}