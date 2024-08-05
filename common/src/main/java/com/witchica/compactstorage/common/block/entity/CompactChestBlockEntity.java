package com.witchica.compactstorage.common.block.entity;

import com.witchica.compactstorage.CompactStorage;
import com.witchica.compactstorage.common.screen.CompactChestScreenHandler;
import com.witchica.compactstorage.common.util.CompactStorageInventoryImpl;

import com.witchica.compactstorage.common.util.CompactStorageUpgradeType;
import com.witchica.compactstorage.common.util.CompactStorageUtil;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.EnvironmentInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@EnvironmentInterface(
    itf = LidBlockEntity.class,
    value = EnvType.CLIENT
)
public class CompactChestBlockEntity extends RandomizableContainerBlockEntity implements MenuProvider, CompactStorageInventoryImpl, LidBlockEntity {
    private NonNullList<ItemStack> inventory;

    public int inventoryWidth = 9;
    public int inventoryHeight = 6;

    public int playersUsing = 0;
    public int playersUsingOld = 0;
    public boolean isOpen = false;

    public float lidOpenness = 0f;
    public float lastLidOpenness = 0f;
    private boolean retaining;

    public CompactChestBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(CompactStorage.COMPACT_CHEST_ENTITY_TYPE.get(), blockPos, blockState);
        this.inventory = NonNullList.withSize(inventoryWidth * inventoryHeight, ItemStack.EMPTY);
    }

    @Override
    public int getContainerSize() {
        return inventoryWidth * inventoryHeight;
    }

    @Override
    public float getOpenNess(float delta) {
        return Mth.lerp(delta, lastLidOpenness, lidOpenness);
    }


    protected NonNullList<ItemStack> getInvStackList() {
        return inventory;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return getInvStackList();
    }

    @Override
    protected void setItems(NonNullList<ItemStack> stackList) {
        inventory = stackList;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.chest");
    }

    @Override
    protected AbstractContainerMenu createMenu(int syncId, Inventory playerInventory) {
        return new CompactChestScreenHandler(syncId, playerInventory, writeToByteBuf());
    }

    @Override
    public int getInventoryWidth() {
        return inventoryWidth;
    }

    @Override
    public int getInventoryHeight() {
        return inventoryHeight;
    }

    @Override
    public void startOpen(Player player) {
        super.startOpen(player);

        if(!player.isSpectator()) {
            playersUsing += 1;
        }
    }

    @Override
    public void stopOpen(Player player) {
        super.stopOpen(player);

        if(!player.isSpectator()) {
            playersUsing -= 1;
        }
    }

    public void resizeInventory(boolean copy_contents) {
        NonNullList<ItemStack> newInventory = NonNullList.withSize(inventoryWidth * inventoryHeight, ItemStack.EMPTY);
        
        if(copy_contents) {
            NonNullList<ItemStack> list = this.inventory;

            for(int i = 0; i < list.size(); i++) {
                newInventory.set(i, list.get(i));
            }
        }

        this.inventory = newInventory;
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);

        this.inventoryWidth = nbt.contains("inventory_width") ? nbt.getInt("inventory_width") : 9;
        this.inventoryHeight = nbt.contains("inventory_height") ? nbt.getInt("inventory_height") : 3;
        this.retaining = nbt.contains("retaining") && nbt.getBoolean("retaining");

        this.inventory = NonNullList.withSize(inventoryWidth * inventoryHeight, ItemStack.EMPTY);
        CompactStorageUtil.readItemsFromTag(inventory, nbt, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        CompactStorageUtil.writeItemsToTag(inventory, nbt, registries);

        nbt.putInt("inventory_width", inventoryWidth);
        nbt.putInt("inventory_height", inventoryHeight);
        nbt.putBoolean("retaining", retaining);
    }

    @Override
    public void saveToItem(ItemStack stack, HolderLookup.Provider registries) {
        CompoundTag compoundTag = this.saveCustomOnly(registries);

        if(!retaining) {
            compoundTag.remove("Items");
        }
        this.removeComponentsFromTag(compoundTag);
        BlockItem.setBlockEntityData(stack, this.getType(), compoundTag);
        stack.applyComponents(this.collectComponents());
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);

        if(!retaining) {
            components.set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    public static void tick(Level world, BlockPos pos, BlockState state, CompactChestBlockEntity compactChestBlockEntity) {
        compactChestBlockEntity.lastLidOpenness = compactChestBlockEntity.lidOpenness;

        if(compactChestBlockEntity.playersUsing > 0 && compactChestBlockEntity.playersUsingOld == 0) {
            compactChestBlockEntity.isOpen = true;
            world.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 1f, 0.9f, true);
        }else if(compactChestBlockEntity.playersUsing == 0 && compactChestBlockEntity.playersUsingOld != 0) {
            compactChestBlockEntity.isOpen = false;
        }
        
        if (compactChestBlockEntity.playersUsing == 0 && compactChestBlockEntity.lidOpenness >= 0.6f && compactChestBlockEntity.lidOpenness <= 0.7f) {
            world.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 1f, 0.9f, true);
        }

        if(compactChestBlockEntity.isOpen && compactChestBlockEntity.lidOpenness < 0.5f) {
            compactChestBlockEntity.lidOpenness = Math.min(compactChestBlockEntity.lidOpenness += 0.25f, 1f);
        } else if(compactChestBlockEntity.isOpen && compactChestBlockEntity.lidOpenness < 1f) {
            compactChestBlockEntity.lidOpenness = Math.min(compactChestBlockEntity.lidOpenness += 0.1f, 1f);
        } else if(!compactChestBlockEntity.isOpen && compactChestBlockEntity.lidOpenness > 0.7f) {
            compactChestBlockEntity.lidOpenness = Math.max(compactChestBlockEntity.lidOpenness - 0.05f, 0f);
        } else if(!compactChestBlockEntity.isOpen && compactChestBlockEntity.lidOpenness > 0f) {
            compactChestBlockEntity.lidOpenness = Math.max(compactChestBlockEntity.lidOpenness - 0.15f, 0f);
        }

        compactChestBlockEntity.playersUsingOld = compactChestBlockEntity.playersUsing;
    }

    public FriendlyByteBuf writeToByteBuf() {
        FriendlyByteBuf packetByteBuf = new FriendlyByteBuf(Unpooled.buffer());
        packetByteBuf.writeInt(0);
        packetByteBuf.writeBlockPos(getBlockPos());

        return packetByteBuf;
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player playerEntity) {
        return new CompactChestScreenHandler(syncId, playerInventory, writeToByteBuf());
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    public boolean increaseSize(int x, int y) {
        if((inventoryWidth > 23 && x > 0) || (inventoryHeight > 11 && y > 0)) {
            return false;
        }

        inventoryWidth += x;
        inventoryHeight += y;

        resizeInventory(true);
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 1);

        return true;
    }

    @Override
    public void applyRetainingUpgrade() {
        this.retaining = true;
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 1);
    }

    @Override
    public boolean canUpgradeTypeBeApplied(CompactStorageUpgradeType upgradeType) {
        switch(upgradeType) {
            case RETAINING -> {
                return !this.retaining;
            }
            case WIDTH_INCREASE -> {
                return inventoryWidth < 21;
            }
            case HEIGHT_INCREASE -> {
                return inventoryHeight < 12;
            }
        }

        return false;
    }

    @Override
    public boolean hasUpgrade(CompactStorageUpgradeType upgradeTypes) {
        switch(upgradeTypes) {
            case RETAINING -> {
                return  retaining;
            }
            case WIDTH_INCREASE -> {
                return inventoryWidth > 9;
            }
            case HEIGHT_INCREASE -> {
                return inventoryHeight > 6;
            }
        }

        return false;
    }

    @Override
    public boolean applyUpgrade(CompactStorageUpgradeType upgradeType) {
        if(canUpgradeTypeBeApplied(upgradeType)) {
            switch(upgradeType) {
                case WIDTH_INCREASE -> {
                    increaseSize(1, 0);
                    return true;
                }
                case HEIGHT_INCREASE -> {
                    increaseSize(0, 1);
                    return true;
                }
                case RETAINING -> {
                    applyRetainingUpgrade();
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public NonNullList<ItemStack> getItemList() {
        return inventory;
    }
}