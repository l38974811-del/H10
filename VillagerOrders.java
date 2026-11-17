package com.example.villagerorders;

import com.example.villagerorders.network.ModNetwork;
import net.minecraftforge.fml.common.Mod;

@Mod(VillagerOrders.MOD_ID)
public class VillagerOrders {
    public static final String MOD_ID = "villagerorders";

    public VillagerOrders() {
        ModNetwork.register();
    }
}
