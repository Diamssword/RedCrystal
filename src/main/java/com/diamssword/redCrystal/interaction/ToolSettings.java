package com.diamssword.redCrystal.interaction;

import com.diamssword.redCrystal.RedCrystalPlugin;
import com.diamssword.redCrystal.storage.RedElement;
import com.diamssword.redCrystal.storage.RedNode;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class ToolSettings implements Component<EntityStore> {

	public static ComponentType<EntityStore, ToolSettings> getComponentType() {
		return RedCrystalPlugin.RedToolSettingsComponent;
	}

	public ConnectionInfo startedLink;

	public boolean tryToLink(Ref<EntityStore> player, RedElement element, short index, boolean output) {
		var pl = player.getStore().getComponent(player, Player.getComponentType());
		if(startedLink != null && startedLink.source.isValid()) {
			if(startedLink.source == element) {
				startedLink = null;
				return false;
			} else if(startedLink.output == output) {

				if(pl != null) {
					pl.sendMessage(Message.raw("Cant link 2 " + (output ? "Outputs" : "Inputs") + " together!"));
				}
				return false;
			} else {
				var newLink = new ConnectionInfo(element, output, index);
				var out = output ? element : startedLink.source;
				var in = output ? startedLink : newLink;
				var indexOut = output ? index : startedLink.index;
				var distance = out.getParent().getPosition().distanceTo(in.source.getParent().getPosition());
				if(distance <= 32) {
					var pos = in.source.getParent().getPosition().clone().subtract(out.getParent().getPosition());
					out.setOutputNode(indexOut, new RedNode(in.source.getFace(), pos, in.index));
					if(pl != null) {
						pl.sendMessage(Message.raw("Link created!"));
					}
				} else if(pl != null) {
					pl.sendMessage(Message.raw("Link too long! Max length is 32 blocks"));
				}

				startedLink = null;
				return true;
			}
		} else {
			startedLink = new ConnectionInfo(element, output, index);

			if(pl != null) {
				pl.sendMessage(Message.raw("Starting connection :" + index + " " + output));
			}
			return true;
		}
	}

	@NullableDecl
	@Override
	public Component<EntityStore> clone() {
		var s = new ToolSettings();
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
