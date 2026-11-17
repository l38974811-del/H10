package com.example.villagerorders.command;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.EnumSet;

/**
 * A temporary goal. It does not replace the villager's normal AI goals or Brain.
 * It only takes control while a player-issued order is active.
 */
public class VillagerCommandGoal extends Goal {
    private final Villager villager;
    private int repathDelay;

    public VillagerCommandGoal(Villager villager) {
        this.villager = villager;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return switch (VillagerCommandData.getCommand(villager)) {
            case FOLLOW -> getOwner() != null && getOwner().isAlive();
            case STAY -> true;
            case HIDE -> findNearestMonster() != null;
            case NORMAL -> false;
        };
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        repathDelay = 0;
    }

    @Override
    public void stop() {
        villager.getNavigation().stop();
    }

    @Override
    public void tick() {
        VillagerCommand command = VillagerCommandData.getCommand(villager);

        switch (command) {
            case FOLLOW -> followOwner();
            case STAY -> {
                villager.getNavigation().stop();
                villager.getLookControl().setLookAt(villager.getX(), villager.getEyeY(), villager.getZ());
            }
            case HIDE -> hideFromNearbyMonster();
            case NORMAL -> villager.getNavigation().stop();
        }
    }

    private ServerPlayer getOwner() {
        if (!(villager.level() instanceof ServerLevel serverLevel)) return null;
        var ownerId = VillagerCommandData.getOwner(villager);
        return ownerId == null ? null : serverLevel.getServer().getPlayerList().getPlayer(ownerId);
    }

    private void followOwner() {
        ServerPlayer owner = getOwner();
        if (owner == null || !owner.isAlive()) {
            villager.getNavigation().stop();
            return;
        }

        double distanceSqr = villager.distanceToSqr(owner);
        if (distanceSqr <= 6.25D) {
            villager.getNavigation().stop();
            villager.getLookControl().setLookAt(owner, 30.0F, 30.0F);
            return;
        }

        if (repathDelay-- <= 0 || villager.getNavigation().isDone()) {
            villager.getNavigation().moveTo(owner, 1.05D);
            repathDelay = 10;
        }
    }

    private void hideFromNearbyMonster() {
        Monster threat = findNearestMonster();
        if (threat == null) {
            villager.getNavigation().stop();
            return;
        }

        if (repathDelay-- > 0 && !villager.getNavigation().isDone()) return;
        repathDelay = 20;

        BlockPos shelter = findCoveredSafePosition(threat);
        if (shelter != null) {
            villager.getNavigation().moveTo(
                    shelter.getX() + 0.5D,
                    shelter.getY(),
                    shelter.getZ() + 0.5D,
                    1.15D
            );
            return;
        }

        // If no covered spot is reachable nearby, move away from the closest threat.
        Vec3 away = DefaultRandomPos.getPosAway(villager, 12, 7, threat.position());
        if (away != null) {
            villager.getNavigation().moveTo(away.x, away.y, away.z, 1.2D);
        }
    }

    private Monster findNearestMonster() {
        Level level = villager.level();
        AABB searchArea = villager.getBoundingBox().inflate(14.0D);
        return level.getEntitiesOfClass(Monster.class, searchArea, Monster::isAlive)
                .stream()
                .min(Comparator.comparingDouble(villager::distanceToSqr))
                .orElse(null);
    }

    /** Finds a nearby two-block-high place with a roof and some distance from the monster. */
    private BlockPos findCoveredSafePosition(Monster threat) {
        Level level = villager.level();
        BlockPos origin = villager.blockPosition();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;

        for (int dx = -10; dx <= 10; dx++) {
            for (int dz = -10; dz <= 10; dz++) {
                for (int dy = -2; dy <= 2; dy++) {
                    BlockPos feet = origin.offset(dx, dy, dz);
                    BlockPos floor = feet.below();
                    BlockPos head = feet.above();
                    BlockPos roof = feet.above(2);

                    BlockState floorState = level.getBlockState(floor);
                    BlockState feetState = level.getBlockState(feet);
                    BlockState headState = level.getBlockState(head);
                    BlockState roofState = level.getBlockState(roof);

                    if (!floorState.isSolidRender(level, floor)) continue;
                    if (!feetState.getCollisionShape(level, feet).isEmpty()) continue;
                    if (!headState.getCollisionShape(level, head).isEmpty()) continue;
                    if (roofState.isAir() || roofState.getCollisionShape(level, roof).isEmpty()) continue;

                    Vec3 position = Vec3.atBottomCenterOf(feet);
                    if (position.distanceToSqr(threat.position()) < 64.0D) continue;

                    double distance = position.distanceToSqr(villager.position());
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        best = feet.immutable();
                    }
                }
            }
        }

        return best;
    }
}
