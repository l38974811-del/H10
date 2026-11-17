package com.example.villagerorders.event;

import com.example.villagerorders.VillagerOrders;
import com.example.villagerorders.command.VillagerCommand;
import com.example.villagerorders.command.VillagerCommandData;
import com.example.villagerorders.command.VillagerCommandGoal;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = VillagerOrders.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class VillagerCommandEvents {
    private VillagerCommandEvents() {}

    @SubscribeEvent
    public static void onVillagerInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (!(event.getTarget() instanceof Villager villager)) return;

        // Sneak + right-click is left untouched so the normal trading screen remains available.
        if (event.getEntity().isShiftKeyDown()) return;

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onVillagerJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof Villager villager) {
            villager.goalSelector.addGoal(-1, new VillagerCommandGoal(villager));
        }
    }

    @SubscribeEvent
    public static void onVillagerTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Villager villager) || villager.level().isClientSide()) return;

        VillagerCommand command = VillagerCommandData.getCommand(villager);
        if (command == VillagerCommand.STAY) {
            // Clear only the active walking intention. The villager's Brain and other memories remain intact.
            villager.getNavigation().stop();
            villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            Vec3 velocity = villager.getDeltaMovement();
            villager.setDeltaMovement(0.0D, velocity.y, 0.0D);
        } else if (command == VillagerCommand.FOLLOW && villager.tickCount % 10 == 0) {
            var ownerId = VillagerCommandData.getOwner(villager);
            if (ownerId == null) return;
            var owner = villager.level().getServer().getPlayerList().getPlayer(ownerId);
            if (owner != null && owner.isAlive() && villager.distanceToSqr(owner) > 6.25D) {
                villager.getNavigation().moveTo(owner, 1.05D);
            }
        }
    }
}
