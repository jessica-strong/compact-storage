package com.witchica.compactstorage.fabric.block.entity;

import com.witchica.compactstorage.common.block.entity.CompactChestBlockEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

public class FabricCompactChestBlockEntity extends CompactChestBlockEntity {

    public InventoryStorage inventoryStorage;
    public FabricCompactChestBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
        this.inventoryStorage = InventoryStorage.of(this, null);
    }

    @Override
    public void resizeInventory(boolean copy_contents) {
        super.resizeInventory(copy_contents);
        this.inventoryStorage = InventoryStorage.of(this, null);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        this.inventoryStorage = InventoryStorage.of(this, null);
    }
}
