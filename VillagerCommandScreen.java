package com.example.villagerorders.client;

import com.example.villagerorders.command.VillagerCommand;
import com.example.villagerorders.network.ModNetwork;
import com.example.villagerorders.network.SetVillagerCommandPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Locale;

public class VillagerCommandScreen extends Screen {
    private static final int PANEL_WIDTH = 360;
    private static final int PANEL_HEIGHT = 250;

    private final Villager villager;
    private String status = "Choose an order for this villager.";
    private int panelLeft;
    private int panelTop;

    public VillagerCommandScreen(Villager villager) {
        super(Component.literal("Villager Orders"));
        this.villager = villager;
    }

    @Override
    protected void init() {
        panelLeft = (this.width - PANEL_WIDTH) / 2;
        panelTop = (this.height - PANEL_HEIGHT) / 2;

        addRenderableWidget(Button.builder(Component.literal("Follow Me"), button -> sendOrder(VillagerCommand.FOLLOW,
                        "Order sent: Follow Me"))
                .bounds(panelLeft + 44, panelTop + 82, 118, 22).build());

        addRenderableWidget(Button.builder(Component.literal("Stay Here"), button -> sendOrder(VillagerCommand.STAY,
                        "Order sent: Stay Here"))
                .bounds(panelLeft + 44, panelTop + 118, 118, 22).build());

        addRenderableWidget(Button.builder(Component.literal("Hide From Monsters"), button -> sendOrder(VillagerCommand.HIDE,
                        "Order sent: Hide From Monsters"))
                .bounds(panelLeft + 44, panelTop + 154, 138, 22).build());

        addRenderableWidget(Button.builder(Component.literal("Resume Normal Behavior"), button -> sendOrder(
                        VillagerCommand.NORMAL, "Normal villager behavior restored"))
                .bounds(panelLeft + 18, panelTop + 205, 218, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Close"), button -> onClose())
                .bounds(panelLeft + 246, panelTop + 205, 96, 20).build());
    }

    private void sendOrder(VillagerCommand command, String newStatus) {
        ModNetwork.CHANNEL.sendToServer(new SetVillagerCommandPacket(villager.getUUID(), command));
        status = newStatus;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0x99000000);
        graphics.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + PANEL_HEIGHT, 0xF01B2430);
        graphics.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + 2, 0xFF70B7C8);
        graphics.fill(panelLeft, panelTop + PANEL_HEIGHT - 2, panelLeft + PANEL_WIDTH, panelTop + PANEL_HEIGHT, 0xFF70B7C8);
        graphics.fill(panelLeft, panelTop, panelLeft + 2, panelTop + PANEL_HEIGHT, 0xFF70B7C8);
        graphics.fill(panelLeft + PANEL_WIDTH - 2, panelTop, panelLeft + PANEL_WIDTH, panelTop + PANEL_HEIGHT, 0xFF70B7C8);

        graphics.drawCenteredString(font, Component.literal("VILLAGER ORDERS"), width / 2, panelTop + 12, 0xFFF2D38B);

        String villagerName = villager.getName().getString();
        String clippedName = font.plainSubstrByWidth(villagerName, PANEL_WIDTH - 40);
        graphics.drawCenteredString(font, Component.literal(clippedName), width / 2, panelTop + 34, 0xFFFFFFFF);

        float health = villager.getHealth();
        float maxHealth = villager.getMaxHealth();
        String healthText = String.format(Locale.ROOT, "Health: %.1f / %.1f", health, maxHealth);
        graphics.drawCenteredString(font, Component.literal(healthText), width / 2, panelTop + 49, 0xFFD5E2EA);

        int barLeft = panelLeft + 30;
        int barTop = panelTop + 64;
        int barWidth = PANEL_WIDTH - 60;
        graphics.fill(barLeft, barTop, barLeft + barWidth, barTop + 5, 0xFF3A4652);
        float healthRatio = maxHealth <= 0.0F ? 0.0F : Math.max(0.0F, Math.min(1.0F, health / maxHealth));
        graphics.fill(barLeft, barTop, barLeft + Math.round(barWidth * healthRatio), barTop + 5, 0xFF61C47A);

        drawOrderRow(graphics, Items.LEAD, panelTop + 84, "Walk to your position");
        drawOrderRow(graphics, Items.REDSTONE_BLOCK, panelTop + 120, "Remain in this place");
        drawOrderRow(graphics, Items.OAK_DOOR, panelTop + 156, "Find cover near monsters");

        graphics.drawCenteredString(font, Component.literal(status), width / 2, panelTop + 187, 0xFFB9D8E5);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawOrderRow(GuiGraphics graphics, net.minecraft.world.item.Item item, int rowTop, String description) {
        graphics.renderItem(new ItemStack(item), panelLeft + 21, rowTop + 3);
        graphics.drawString(font, Component.literal(description), panelLeft + 190, rowTop + 7, 0xFFE2E8F0, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
