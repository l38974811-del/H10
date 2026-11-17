package com.example.villagerorders.command;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.npc.Villager;

import java.util.UUID;

public final class VillagerCommandData {
    private static final String COMMAND_KEY = "villagerorders.command";
    private static final String OWNER_KEY = "villagerorders.owner";

    private VillagerCommandData() {}

    public static VillagerCommand getCommand(Villager villager) {
        String saved = villager.getPersistentData().getString(COMMAND_KEY);
        if (saved.isEmpty()) {
            return VillagerCommand.NORMAL;
        }

        try {
            return VillagerCommand.valueOf(saved);
        } catch (IllegalArgumentException ignored) {
            return VillagerCommand.NORMAL;
        }
    }

    public static void setCommand(Villager villager, VillagerCommand command, UUID owner) {
        CompoundTag data = villager.getPersistentData();
        if (command == VillagerCommand.NORMAL) {
            clear(villager);
            return;
        }

        data.putString(COMMAND_KEY, command.name());
        data.remove(OWNER_KEY);
        if (command == VillagerCommand.FOLLOW && owner != null) {
            data.putUUID(OWNER_KEY, owner);
        }
    }

    public static UUID getOwner(Villager villager) {
        CompoundTag data = villager.getPersistentData();
        return data.hasUUID(OWNER_KEY) ? data.getUUID(OWNER_KEY) : null;
    }

    public static void clear(Villager villager) {
        CompoundTag data = villager.getPersistentData();
        data.remove(COMMAND_KEY);
        data.remove(OWNER_KEY);
    }
}
