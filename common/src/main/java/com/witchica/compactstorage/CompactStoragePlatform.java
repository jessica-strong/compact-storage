package com.witchica.compactstorage;

import com.witchica.compactstorage.common.block.entity.CompactBarrelBlockEntity;
import com.witchica.compactstorage.common.block.entity.CompactChestBlockEntity;
import com.witchica.compactstorage.common.block.entity.DrumBlockEntity;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class CompactStoragePlatform {
    @ExpectPlatform
    public static BlockEntityType.BlockEntitySupplier<CompactChestBlockEntity> compactChestBlockEntitySupplier() {
        throw new AssertionError("Method not implemented on this platform.");
    }

    @ExpectPlatform
    public static BlockEntityType.BlockEntitySupplier<DrumBlockEntity> drumBlockEntitySupplier() {
        throw new AssertionError("Method not implemented on this platform.");
    }

    @ExpectPlatform
    public static BlockEntityType.BlockEntitySupplier<CompactBarrelBlockEntity> compactBarrelBlockEntitySupplier() {
        throw new AssertionError("Method not implemented on this platform.");
    }
}
