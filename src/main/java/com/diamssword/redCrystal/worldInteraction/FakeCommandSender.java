package com.diamssword.redCrystal.worldInteraction;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;

import javax.annotation.Nonnull;
import java.util.UUID;

public class FakeCommandSender implements CommandSender {

	@Override
	public String getUsername() {
		return "RedCrystal";
	}

	@Override
	public UUID getUuid() {
		return UUID.randomUUID();
	}

	@Override
	public boolean hasPermission(@Nonnull String var1) {
		return true;
	}

	@Override
	public boolean hasPermission(@Nonnull String var1, boolean var2) {
		return true;
	}

	@Override
	public void sendMessage(@Nonnull Message var1) {

	}
}
