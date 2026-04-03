package com.diamssword.redCrystal.worldInteraction;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class FakeCommandSender implements CommandSender {
	@Override
	public String getDisplayName() {
		return "RedCrystal";
	}

	@Override
	public UUID getUuid() {
		return UUID.randomUUID();
	}

	@Override
	public boolean hasPermission(@NotNull String var1) {
		return true;
	}

	@Override
	public boolean hasPermission(@NotNull String var1, boolean var2) {
		return true;
	}

	@Override
	public void sendMessage(@NotNull Message var1) {

	}
}
