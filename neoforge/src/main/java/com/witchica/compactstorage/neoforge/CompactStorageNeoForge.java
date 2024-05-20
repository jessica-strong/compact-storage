package com.witchica.compactstorage.neoforge;

import com.witchica.compactstorage.CompactStorage;
import com.witchica.compactstorage.common.client.screen.CompactChestScreen;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.menu.forge.MenuRegistryImpl;
import net.minecraft.client.gui.screens.MenuScreens;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

@Mod(CompactStorage.MOD_ID)
public class CompactStorageNeoForge {

    public CompactStorageNeoForge(IEventBus eventBus) {
        CompactStorage.onInitialize();
        eventBus.register(this);
    }

    @SubscribeEvent
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CompactStorage.COMPACT_CHEST_ENTITY_TYPE.get(), (blockEntity, direction) -> {
            return new InvWrapper(blockEntity.getInventory());
        });
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CompactStorage.COMPACT_BARREL_ENTITY_TYPE.get(), (blockEntity, direction) -> {
            return new InvWrapper(blockEntity.getInventory());
        });
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CompactStorage.DRUM_ENTITY_TYPE.get(), (blockEntity, direction) -> {
            return new InvWrapper(blockEntity.inventory);
        });
    }

    @SubscribeEvent
    public void registerScreens(RegisterMenuScreensEvent event) {
        event.register(CompactStorage.COMPACT_CHEST_SCREEN_HANDLER.get(), CompactChestScreen::new);
    }
}
