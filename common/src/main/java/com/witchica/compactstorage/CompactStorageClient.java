package com.witchica.compactstorage;

import com.witchica.compactstorage.common.client.entity.CompactChestBlockEntityRenderer;
import com.witchica.compactstorage.common.client.entity.DrumBlockEntityRenderer;
import com.witchica.compactstorage.common.client.screen.CompactChestScreen;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class CompactStorageClient {
    public static void clientSetupEvent(Minecraft minecraft) {
        MenuRegistry.registerScreenFactory(CompactStorage.COMPACT_CHEST_SCREEN_HANDLER.get(), CompactChestScreen::new);
        BlockEntityRendererRegistry.register(CompactStorage.COMPACT_CHEST_ENTITY_TYPE.get(), CompactChestBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(CompactStorage.DRUM_ENTITY_TYPE.get(), DrumBlockEntityRenderer::new);


    }
}
