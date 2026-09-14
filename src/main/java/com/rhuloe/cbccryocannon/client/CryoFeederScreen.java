package com.rhuloe.cbccryocannon.client;

import com.rhuloe.cbccryocannon.content.CryoFeederMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public final class CryoFeederScreen extends AbstractContainerScreen<CryoFeederMenu> {
    private static final ResourceLocation SLOT = ResourceLocation.withDefaultNamespace("container/slot");
    private static final ResourceLocation CANISTER_SLOT = ResourceLocation.fromNamespaceAndPath(
        "cbc_cryo_cannon", "textures/gui/canister_slot.png");
    private static final int PANEL = 0xFFC6C6C6;
    private static final int LIGHT = 0xFFFFFFFF;
    private static final int MID = 0xFF8B8B8B;
    private static final int DARK = 0xFF373737;
    private static final int TEXT = 0x404040;

    public CryoFeederScreen(CryoFeederMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 176;
        imageHeight = 186;
        inventoryLabelX = 8;
        inventoryLabelY = 90;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = leftPos;
        int top = topPos;
        drawVanillaPanel(graphics, left, top, imageWidth, imageHeight);

        for (Slot slot : menu.slots) {
            graphics.blitSprite(SLOT, left + slot.x - 1, top + slot.y - 1, 18, 18);
        }

        if (!menu.getSlot(9).hasItem()) {
            graphics.blit(CANISTER_SLOT, left + 116, top + 38, 0, 0, 16, 16, 16, 16);
        }

        drawFuelGauge(graphics, left + 142, top + 18);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, TEXT, false);
        graphics.drawString(font, Component.translatable("container.cbc_cryo_cannon.shells"), 44, 79, TEXT,
            false);
        graphics.drawString(font, Component.translatable("container.cbc_cryo_cannon.fuel_slot"), 112, 25, TEXT,
            false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, TEXT, false);
        graphics.drawString(font, Component.translatable("container.cbc_cryo_cannon.fuel",
            menu.fuel(), menu.fuelCapacity()), 96, 79, TEXT, false);
    }

    private static void drawVanillaPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, DARK);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, LIGHT);
        graphics.fill(x + 3, y + 3, x + width - 1, y + height - 1, MID);
        graphics.fill(x + 3, y + 3, x + width - 3, y + height - 3, PANEL);
    }

    private void drawFuelGauge(GuiGraphics graphics, int x, int y) {
        int width = 18;
        int height = 58;
        graphics.fill(x, y, x + width, y + height, DARK);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, LIGHT);
        graphics.fill(x + 3, y + 3, x + width - 1, y + height - 1, MID);
        graphics.fill(x + 3, y + 3, x + width - 3, y + height - 3, 0xFF555555);

        int segmentCount = 14;
        int segmentHeight = 2;
        int segmentGap = 1;
        int segmentLeft = x + 5;
        int segmentRight = x + width - 5;
        int segmentBottom = y + height - 7;
        int filledSegments = Math.min(segmentCount,
            (menu.fuel() * segmentCount + menu.fuelCapacity() - 1) / menu.fuelCapacity());
        for (int segment = 0; segment < segmentCount; segment++) {
            int segmentY = segmentBottom - segmentHeight - segment * (segmentHeight + segmentGap);
            int color = segment < filledSegments ? 0xFF54C7E6 : 0xFF707070;
            graphics.fill(segmentLeft, segmentY, segmentRight, segmentY + segmentHeight, color);
        }
    }
}