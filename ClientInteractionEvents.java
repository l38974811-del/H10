package com.example.villagerorders.event;

import com.example.villagerorders.VillagerOrders;
import com.example.villagerorders.client.VillagerCommandScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;

@Mod.EventBusSubscriber(modid = VillagerOrders.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientInteractionEvents {
    private ClientInteractionEvents() {}

    @SubscribeEvent(receiveCanceled = true)
    public static void onVillagerInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (!event.getLevel().isClientSide()) return;
        if (event.getEntity().isShiftKeyDown()) return;
        if (event.getTarget() instanceof Villager villager) {
            Minecraft.getInstance().setScreen(new VillagerCommandScreen(villager));
        }
    }
}
