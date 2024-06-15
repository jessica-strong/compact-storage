package com.witchica.compactstorage.common.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.witchica.compactstorage.common.screen.CompactChestScreenHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;

public class CompactChestScreen extends AbstractContainerScreen<CompactChestScreenHandler> {
    public static final ResourceLocation CHEST_SLOTS_TEXTURE = ResourceLocation.fromNamespaceAndPath("compact_storage", "textures/gui/chest_slots.png");
    public static final ResourceLocation CHEST_BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath("compact_storage", "textures/gui/chest.png");
    
    private CompactChestScreenHandler container;
    private Inventory playerInventory;

    public CompactChestScreen(CompactChestScreenHandler container, Inventory inventory, Component title) {
        super(container, inventory, title);
        this.container = container;
        this.playerInventory = inventory;

        this.imageWidth = 14 + container.inventoryWidth * 18;
        this.imageHeight = 114 + container.inventoryHeight * 18 + 7;
    }
    @Override
    protected void renderLabels(GuiGraphics context, int mouseX, int mouseY) {
        context.drawString(this.font, this.title, 8, 6, 4210752, false);
        context.drawString(font, this.playerInventory.getDisplayName(), 8, this.imageHeight-96-3, 4210752, false);
        
        this.renderTooltip(context,mouseX - leftPos, mouseY - topPos);
    }

    @Override
    protected void renderBg(GuiGraphics matrixStack, float delta, int mouseX, int mouseY) {
        //RenderSystem.disableLighting();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, CHEST_BACKGROUND_TEXTURE);

        //drawTexture(matrixStack, x, y, width, height, u, v, uWidth, vHeight, texWidth, texHeight);
        //corners

        matrixStack.blit(CHEST_BACKGROUND_TEXTURE, leftPos, topPos, 0, 0, 7, 7, 15, 15);
        matrixStack.blit(CHEST_BACKGROUND_TEXTURE, leftPos + imageWidth - 7, topPos, 8, 0, 7, 7, 15, 15);

        matrixStack.blit(CHEST_BACKGROUND_TEXTURE, leftPos, topPos + imageHeight - 7, 0, 8, 8, 7, 15, 15);
        matrixStack.blit(CHEST_BACKGROUND_TEXTURE, leftPos + imageWidth - 7, topPos + imageHeight - 7, 8, 8, 7, 7, 15, 15);

        //middle bit
        matrixStack.blit(CHEST_BACKGROUND_TEXTURE, leftPos + 7, topPos + 7, imageWidth - 14, imageHeight - 14, 7, 7, 1, 1, 15, 15);

        //left side
        matrixStack.blit(CHEST_BACKGROUND_TEXTURE, leftPos, topPos + 7, 7, imageHeight - 14, 0, 7, 7, 1, 15, 15);

        //right side
        matrixStack.blit(CHEST_BACKGROUND_TEXTURE, leftPos + imageWidth - 7, topPos + 7, 7, imageHeight - 14, 8, 7, 7, 1, 15, 15);

        //top
        matrixStack.blit(CHEST_BACKGROUND_TEXTURE, leftPos + 7, topPos, imageWidth - 14, 7, 7, 0, 1, 7, 15, 15);

        //bottom
        matrixStack.blit(CHEST_BACKGROUND_TEXTURE, leftPos + 7, topPos + imageHeight - 7, imageWidth - 14, 7, 7, 8, 1, 7, 15, 15);


        //chest slots
        matrixStack.blit(CHEST_SLOTS_TEXTURE, this.leftPos + 7, this.topPos + 17, 0, 0, 18 * container.inventoryWidth, 18 * container.inventoryHeight, 432, 216);
        //inv slots  
        matrixStack.blit(CHEST_SLOTS_TEXTURE, this.leftPos + (imageWidth / 2) - 9 * 9, this.topPos + (container.inventoryHeight * 18) + 18 + 17, 0, 0, 18 * 9, 18 * 3, 432, 216);
        //hotbar slots
        matrixStack.blit(CHEST_SLOTS_TEXTURE, this.leftPos + (imageWidth / 2) - 9 * 9, this.topPos + (container.inventoryHeight * 18) + 18 + 60 + 17, 0, 0, 18 * 9, 18 * 1, 432, 216);
    }

    @Override
    protected boolean checkHotbarKeyPressed(int keyCode, int scanCode) {
        if (this.minecraft.player.containerMenu.getCarried().isEmpty() && this.hoveredSlot != null) {
            for(int i = 0; i < 9; ++i) {
                if(i == this.minecraft.player.getInventory().selected && container.blockEntity == null) {
                    continue;
                }

                if (this.minecraft.options.keyHotbarSlots[i].matches(keyCode, scanCode)) {
                    this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, i, ClickType.SWAP);
                    return true;
                }
            }
         }
   
         return false;
    }
}
