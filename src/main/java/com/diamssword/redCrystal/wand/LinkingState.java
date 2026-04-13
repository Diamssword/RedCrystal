package com.diamssword.redCrystal.wand;

import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.storage.RedNode;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3i;

public class LinkingState {

	public static int MAX_LENGTH = 32;
	public static final int BASE_BEAM_COLOR = 0x71A44C;
	public static final int ERROR_BEAM_COLOR = 0x90571D;
	public static final int TOO_LONG_BEAM_COLOR = 0x782D22;
	private int blinkTime;
	private int color = BASE_BEAM_COLOR;
	public ConnectionInfo startedLink;

	public boolean tryToLink(Ref<EntityStore> player, RedElement element, short index, boolean output) {
		var plr = player.getStore().getComponent(player, PlayerRef.getComponentType());
		if(startedLink != null && startedLink.source.isValid()) {
			if(startedLink.source == element) {
				//	startedLink = null;
				return false;
			} else if(startedLink.output == output) {

				if(plr != null) {
					plr.sendMessage(Message.raw("Cant link 2 " + (output ? "Outputs" : "Inputs") + " together!"));
					color = ERROR_BEAM_COLOR;
					blinkTime = 20;
				}
				return false;
			} else {
				var newLink = new ConnectionInfo(element, output, index);
				var out = output ? element : startedLink.source;
				var in = output ? startedLink : newLink;
				var indexOut = output ? index : startedLink.index;
				var distance = out.getParent().getPosition().distance(in.source.getParent().getPosition());
				if(distance <= MAX_LENGTH) {
					var pos = new Vector3i(in.source.getParent().getPosition()).sub(out.getParent().getPosition());
					out.setOutputNode(indexOut, new RedNode(in.source.getFace(), pos, in.index));
				} else if(plr != null) {
					plr.sendMessage(Message.raw("Link too long! Max length is 32 blocks"));
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
			var distance = new Vector3d().set(startedLink.source.getParent().getPosition()).distance(playerPos);
			if(distance > MAX_LENGTH) {
				color = TOO_LONG_BEAM_COLOR;
				blinkTime = 10;
			}
		}

	}

	public boolean tryCancelLink(RedElement element, short index, boolean output) {
		if(startedLink != null && startedLink.source == element && startedLink.index == index && startedLink.output == output) {
			startedLink = null;
			return true;
		}
		return false;
	}


	public LinkingState clone() {
		var s = new LinkingState();
		s.startedLink = this.startedLink;
		return s;
	}

	public static class ConnectionInfo {
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
