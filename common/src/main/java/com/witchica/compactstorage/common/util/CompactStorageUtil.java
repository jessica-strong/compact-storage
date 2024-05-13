package com.witchica.compactstorage.common.util;

import com.witchica.compactstorage.common.block.entity.DrumBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;

public class CompactStorageUtil {

    public static final String[] DRUM_TYPES = new String[] {
         "acacia",
         "birch",
         "cherry",
         "crimson",
         "dark_oak",
         "jungle",
         "mangrove",
         "oak",
         "spruce",
         "warped",
         "bamboo"
    };

    public static void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag, boolean isBackpack) {
        int inventoryX = 9;
        int inventoryY = 6;

        CompoundTag compound = stack.get(DataComponents.CUSTOM_DATA).copyTag();

        if(isBackpack && compound != null) {
            compound = compound.getCompound("Backpack");
        }

        if(compound != null && compound.contains("inventory_width")) {
            inventoryX = compound.getInt("inventory_width");
            inventoryY = compound.getInt("inventory_height");
        }

        int slots = inventoryX * inventoryY;
        tooltip.add(Component.translatable("tooltip.compact_storage.storage_size", inventoryX, inventoryY, slots).withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));

        if(compound != null && compound.contains("retaining") && compound.getBoolean("retaining")) {
            tooltip.add(Component.translatable("tooltip.compact_storage.retaining").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        }

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(compound));
    }

    public static void dropContents(Level world, BlockPos pos, Block block, Player player, HolderLookup.Provider registries) {
        if(world.isClientSide) {
            return;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);

        if(blockEntity instanceof CompactStorageInventoryImpl inventory) {
            ItemStack chestStack = new ItemStack(block, 1);
            boolean retaining = inventory.hasUpgrade(CompactStorageUpgradeType.RETAINING);

            CompoundTag chestTag = new CompoundTag();
            chestTag.putInt("inventory_width", inventory.getInventoryWidth());
            chestTag.putInt("inventory_height", inventory.getInventoryHeight());
            chestTag.putBoolean("retaining", retaining);

            if(retaining) {
                writeItemsToTag(inventory.getItemList(), chestTag, registries);
            }

            chestStack.set(DataComponents.CUSTOM_DATA, CustomData.of(chestTag));


            if(inventory instanceof RandomizableContainerBlockEntity lootableContainerBlockEntity) {
                if(lootableContainerBlockEntity.hasCustomName()) {
                    chestStack.set(DataComponents.CUSTOM_NAME, lootableContainerBlockEntity.getCustomName());
                }
            }

            if(!retaining) {
                Containers.dropContents(world, pos, (Container) inventory);
            }

            if(player == null || !player.isCreative() || retaining) {
                Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), chestStack);
            }

            world.updateNeighbourForOutputSignal(pos, block);
        } else if (blockEntity instanceof DrumBlockEntity drumBlockEntity) {
            ItemStack drumStack = new ItemStack(block, 1);
            boolean retaining = drumBlockEntity.getRetaining();

            CompoundTag chestTag = new CompoundTag();
            chestTag.putBoolean("Retaining", retaining);

            if(retaining) {
                writeItemsToTag("Inventory", drumBlockEntity.inventory.getItems(), chestTag, registries);

                if(drumBlockEntity.hasAnyItems()) {
                    chestTag.put("TooltipItem", new ItemStack(drumBlockEntity.getStoredType(), 1).save(registries));
                    chestTag.putInt("TooltipCount", drumBlockEntity.getTotalItemCount());
                }
            }

            drumStack.set(DataComponents.CUSTOM_DATA, CustomData.of(chestTag));

            if(!retaining) {
                Containers.dropContents(world, pos, drumBlockEntity.inventory);
            }

            if(player == null || !player.isCreative() || retaining) {
                Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), drumStack);
            }

            world.updateNeighbourForOutputSignal(pos, block);
        }
    }


    public static void writeItemsToTag(NonNullList<ItemStack> inventory, CompoundTag tag, HolderLookup.Provider registries) {
        writeItemsToTag("Items", inventory, tag, registries);
    }

    public static void writeItemsToTag(String tagName, NonNullList<ItemStack> inventory, CompoundTag tag, HolderLookup.Provider lookupProvider) {
        ListTag listTag = new ListTag();

        for(int i = 0; i < inventory.size(); ++i) {
            ItemStack itemStack = (ItemStack)inventory.get(i);
            if (!itemStack.isEmpty()) {
                CompoundTag nbt = (CompoundTag) itemStack.save(lookupProvider);
                nbt.putInt("Slot", i);
                listTag.add(nbt);
            }
        }

        tag.put(tagName, listTag);
    }

    public static void readItemsFromTag(NonNullList<ItemStack> inventory, CompoundTag tag, HolderLookup.Provider lookupProvider) {
        ListTag listTag = tag.getList("Items", 10);

        for(int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag = listTag.getCompound(i);
            int j = compoundTag.getInt("Slot");
            if (j >= 0 && j < inventory.size()) {
                inventory.set(j, ItemStack.parseOptional(lookupProvider, compoundTag));
            }
        }
    }
}
