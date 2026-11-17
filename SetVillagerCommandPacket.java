package com.example.villagerorders.network;

import com.example.villagerorders.command.VillagerCommand;
import com.example.villagerorders.command.VillagerCommandData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SetVillagerCommandPacket {
    private final UUID villagerId;
    private final VillagerCommand command;

    public SetVillagerCommandPacket(UUID villagerId, VillagerCommand command) {
        this.villagerId = villagerId;
        this.command = command;
    }

    public static void encode(SetVillagerCommandPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.villagerId);
        buffer.writeEnum(packet.command);
    }

    public static SetVillagerCommandPacket decode(FriendlyByteBuf buffer) {
        return new SetVillagerCommandPacket(buffer.readUUID(), buffer.readEnum(VillagerCommand.class));
    }

    public static void handle(SetVillagerCommandPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            Entity entity = player.serverLevel().getEntity(packet.villagerId);
            if (!(entity instanceof Villager villager) || !villager.isAlive()) return;

            // Server-side validation: players can only command nearby villagers.
            if (player.distanceToSqr(villager) > 64.0D) return;

            if (packet.command == VillagerCommand.FOLLOW) {
                VillagerCommandData.setCommand(villager, VillagerCommand.FOLLOW, player.getUUID());
            } else {
                VillagerCommandData.setCommand(villager, packet.command, null);
            }
        });
        context.setPacketHandled(true);
    }
}
