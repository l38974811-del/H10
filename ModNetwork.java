package com.example.villagerorders.network;

import com.example.villagerorders.VillagerOrders;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(VillagerOrders.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static boolean registered = false;

    private ModNetwork() {}

    public static void register() {
        if (registered) return;
        CHANNEL.registerMessage(
                0,
                SetVillagerCommandPacket.class,
                SetVillagerCommandPacket::encode,
                SetVillagerCommandPacket::decode,
                SetVillagerCommandPacket::handle
        );
        registered = true;
    }
}
