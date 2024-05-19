package com.witchica.compactstorage.common.inventory;

import com.witchica.compactstorage.common.screen.CompactChestScreenHandler;
import io.netty.buffer.Unpooled;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import javax.xml.crypto.Data;

public class BackpackInventoryHandlerFactory implements MenuProvider {
    public InteractionHand hand;
    public ItemStack backpackStack;

    public BackpackInventoryHandlerFactory(Player player, InteractionHand hand) {
        this.hand = hand;
        this.backpackStack = player.getItemInHand(hand);
    }

    @Override
    public Component getDisplayName() {
        return backpackStack.has(DataComponents.CUSTOM_NAME) ? backpackStack.getHoverName() : Component.translatable("container.compact_storage.backpack");
    }

    public static BackpackInventory getBackpackInventory(Player player, InteractionHand hand) {
        ItemStack backpackStack = player.getItemInHand(hand);
        boolean isInOffhand = hand == InteractionHand.OFF_HAND;

        if(backpackStack.has(DataComponents.CUSTOM_DATA)) {
            CompoundTag backpackTag = backpackStack.get(DataComponents.CUSTOM_DATA).copyTag().getCompound("Backpack");
            return new BackpackInventory(backpackTag, player, isInOffhand, null);
        } else {
            return new BackpackInventory(new CompoundTag(), player, isInOffhand, null);
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        BackpackInventory backpackInventory = getBackpackInventory(player, hand);
        return new CompactChestScreenHandler(syncId, inv, writeToByteBuf(new FriendlyByteBuf(Unpooled.buffer())));
    }

    public FriendlyByteBuf writeToByteBuf(FriendlyByteBuf buf) {
        buf.writeInt(1);
        buf.writeInt(hand == InteractionHand.MAIN_HAND ? 0 : 1);
        return buf;
    }
}
