package com.diamssword.redCrystal.wand;

import com.diamssword.redCrystal.RedCrystalPlugin;
import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.storage.RedNode;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class LinkingState implements Component<EntityStore> {

	public static ComponentType<EntityStore, LinkingState> getComponentType() {
		return RedCrystalPlugin.RedToolSettingsComponent;
	}

	public static int MAX_LENGTH = 32;
	private static final int BASE_BEAM_COLOR = 0x71A44C;
	private static final int ERROR_BEAM_COLOR = 0x90571D;
	private static final int TOO_LONG_BEAM_COLOR = 0x782D22;
	private boolean isToolEquiped;
	private int blinkTime;
	private int color = BASE_BEAM_COLOR;
	public ConnectionInfo startedLink;

	public boolean tryToLink(Ref<EntityStore> player, RedElement element, short index, boolean output) {
		var pl = player.getStore().getComponent(player, Player.getComponentType());
		if(startedLink != null && startedLink.source.isValid()) {
			if(startedLink.source == element) {
				//	startedLink = null;
				return false;
			} else if(startedLink.output == output) {

				if(pl != null) {
					pl.sendMessage(Message.raw("Cant link 2 " + (output ? "Outputs" : "Inputs") + " together!"));
					color = ERROR_BEAM_COLOR;
					blinkTime = 20;
				}
				return false;
			} else {
				var newLink = new ConnectionInfo(element, output, index);
				var out = output ? element : startedLink.source;
				var in = output ? startedLink : newLink;
				var indexOut = output ? index : startedLink.index;
				var distance = out.getParent().getPosition().distanceTo(in.source.getParent().getPosition());
				if(distance <= MAX_LENGTH) {
					var pos = in.source.getParent().getPosition().clone().subtract(out.getParent().getPosition());
					out.setOutputNode(indexOut, new RedNode(in.source.getFace(), pos, in.index));
				} else if(pl != null) {
					pl.sendMessage(Message.raw("Link too long! Max length is 32 blocks"));
				}

				startedLink = null;
				return true;
			}
		} else {
			startedLink = new ConnectionInfo(element, output, index);
			return true;
		}
	}

	public int getColor() {
		return color;
	}

	public void handleBlink(Vector3d playerPos) {
		if(blinkTime > 0) {
			blinkTime--;
			if(blinkTime == 0) {
				color = BASE_BEAM_COLOR;
			}
		}
		if(startedLink != null && startedLink.source.isValid()) {
			var distance = startedLink.source.getParent().getPosition().toVector3d().distanceTo(playerPos);
			System.out.println(distance);
			if(distance > MAX_LENGTH) {
				color = TOO_LONG_BEAM_COLOR;
				blinkTime = 10;
			}
		}

	}

	public void tryCancelLink(RedElement element, short index, boolean output) {
		if(startedLink != null && startedLink.source == element && startedLink.index == index && startedLink.output == output) {
			startedLink = null;
		}
	}

	public boolean isToolEquiped() {
		return isToolEquiped;
	}

	public void setToolEquiped(boolean toolEquiped) {
		isToolEquiped = toolEquiped;
	}

	@NullableDecl
	@Override
	public Component<EntityStore> clone() {
		var s = new LinkingState();
		s.startedLink = this.startedLink;
		return s;
	}

	public class ConnectionInfo {
		public RedElement source;
		public boolean output;
		public short index;

		public ConnectionInfo(RedElement source, boolean output, short index) {
			this.source = source;
			this.output = output;
			this.index = index;
		}
	}

}
