package com.witchica.compactstorage.neoforge;

import com.witchica.compactstorage.common.block.entity.CompactBarrelBlockEntity;
import com.witchica.compactstorage.common.block.entity.CompactChestBlockEntity;
import com.witchica.compactstorage.common.block.entity.DrumBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class CompactStoragePlatformImpl {
    public static BlockEntityType.BlockEntitySupplier<CompactChestBlockEntity> compactChestBlockEntitySupplier() {
        return CompactChestBlockEntity::new;
    }

    public static BlockEntityType.BlockEntitySupplier<DrumBlockEntity> drumBlockEntitySupplier() {
        return DrumBlockEntity::new;
    }

    public static BlockEntityType.BlockEntitySupplier<CompactBarrelBlockEntity> compactBarrelBlockEntitySupplier() {
        return CompactBarrelBlockEntity::new;
    }
}
