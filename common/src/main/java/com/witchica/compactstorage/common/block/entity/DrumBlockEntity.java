package com.witchica.compactstorage.common.block.entity;

import com.witchica.compactstorage.CompactStorage;
import com.witchica.compactstorage.CompactStoragePlatform;
import com.witchica.compactstorage.common.block.CompactBarrelBlock;
import com.witchica.compactstorage.common.block.DrumBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DrumBlockEntity extends BlockEntity {
    public ItemStack clientItem = ItemStack.EMPTY;
    public int clientStackSize;
    public int clientStoredItems;
    private boolean retaining;

    public DrumBlockEntity(BlockPos pos, BlockState state) {
        super(CompactStorage.DRUM_ENTITY_TYPE.get(), pos, state);
    }

    public final SimpleContainer inventory = new SimpleContainer(64) {
        @Override
        public int getMaxStackSize() {
            if(getItem(0).isEmpty()) {
                return 64;
            }

            return getItem(0).getMaxStackSize();
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return getItem(0).isEmpty() || ItemStack.isSameItem(getItem(0), stack);
        }

        @Override
        public void setChanged() {
            super.setChanged();
            DrumBlockEntity.this.setChanged();
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            for(int i = getContainerSize()-1; i >=0; i--) {
                ItemStack stack = getItem(i);

                if(stack != null && !stack.isEmpty()) {
                    slot = i;
                    break;
                }
            }

            ItemStack stack =  super.removeItemNoUpdate(slot);
            setChanged();
            return stack;
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            for(int i = getContainerSize()-1; i >=0; i--) {
                ItemStack stack = getItem(i);

                if(stack != null && !stack.isEmpty()) {
                    slot = i;
                    break;
                }
            }

            ItemStack stack = super.removeItem(slot, amount);
            setChanged();
            return stack;
        }
    };

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, DrumBlockEntity blockEntity) {
        if(!level.isClientSide() && blockState.getValue(DrumBlock.RETAINING) != blockEntity.retaining) {
            level.setBlockAndUpdate(blockPos, blockState.setValue(DrumBlock.RETAINING, blockEntity.retaining));
        }
    }


    public boolean hasAnyItems() {
        return !inventory.getItem(0).isEmpty();
    }

    public Item getStoredType() {
        return hasAnyItems() ? inventory.getItem(0).getItem() : ItemStack.EMPTY.getItem();
    }

    public int getTotalItemCount() {
        return hasAnyItems() ? inventory.countItem(getStoredType()) : 0;
    }
    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        nbt.put("Inventory", inventory.createTag(registries));

        nbt.put("ClientItem", new ItemStack(getStoredType(), 1).saveOptional(registries));
        nbt.putInt("ClientStackSize", getStoredType().getDefaultMaxStackSize());
        nbt.putInt("ClientStoredItems", getTotalItemCount());

        nbt.putBoolean("Retaining", retaining);
        nbt.putInt("Version", 1);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        inventory.fromTag(nbt.getList("Inventory", Tag.TAG_COMPOUND), registries);

        this.clientItem = ItemStack.parseOptional(registries, nbt.getCompound("ClientItem"));
        this.clientStackSize = nbt.getInt("ClientStackSize");
        this.clientStoredItems = nbt.getInt("ClientStoredItems");
        this.retaining = nbt.getBoolean("Retaining");
    }

    @Override
    public void saveToItem(ItemStack stack, HolderLookup.Provider registries) {
        CompoundTag compoundTag = this.saveCustomOnly(registries);

        if(!retaining) {
            compoundTag.remove("Inventory");
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

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if(level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public String getTextToDisplay(boolean crouched) {

        if(crouched) {
            int stored = clientStoredItems;
            int maxStored = (clientStackSize == 0 ? 64 : clientStackSize) * 64;

            return "%d / %d".formatted(stored, maxStored);
        }

        if(clientStackSize == 0) {
            return "Empty";
        }

        int numStacks = clientStoredItems / clientStackSize;
        int leftover = clientStoredItems % clientStackSize;

        if(numStacks == 0 & leftover == 0) {
            return "Empty";
        } else if(numStacks == 0) {
            return "" + leftover;
        } else if(leftover == 0) {
            return clientStackSize + " x " + numStacks;
        } else {
            return (clientStackSize + " x " + numStacks + " + " + leftover);
        }
    }

    public boolean applyRetainingUpgrade() {
        if(!this.retaining) {
            this.retaining = true;
            setChanged();
            level.setBlock(getBlockPos(),getBlockState().setValue(DrumBlock.RETAINING, true), Block.UPDATE_CLIENTS);
            return true;
        }

        return false;
    }

    public boolean getRetaining() {
        return retaining;
    }
}
