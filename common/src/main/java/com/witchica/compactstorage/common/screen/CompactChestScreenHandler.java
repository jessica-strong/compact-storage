package com.witchica.compactstorage.common.screen;

import com.witchica.compactstorage.CompactStorage;
import com.witchica.compactstorage.common.inventory.BackpackInventory;

import com.witchica.compactstorage.common.inventory.BackpackInventoryHandlerFactory;
import com.witchica.compactstorage.common.util.CompactStorageInventoryImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CompactChestScreenHandler extends AbstractContainerMenu {
    private Container inventory;
    private Inventory playerInventory;

    public CompactStorageInventoryImpl blockEntity;

    public int inventoryWidth;
    public int inventoryHeight;

    private ItemStack backpack;
    private boolean isBackpackInOffhand;

    public CompactChestScreenHandler(int syncId, Inventory playerInventory, FriendlyByteBuf buf) {
        super(CompactStorage.COMPACT_CHEST_SCREEN_HANDLER.get(), syncId);
        int inventoryType = buf.readInt();

        this.playerInventory = playerInventory;

        if(inventoryType == 0) {
            BlockPos pos = buf.readBlockPos();
            CompactStorageInventoryImpl inv = (CompactStorageInventoryImpl) playerInventory.player.level().getBlockEntity(pos);
            this.inventory = (Container) inv;
            this.inventoryWidth = inv.getInventoryWidth();
            this.inventoryHeight = inv.getInventoryHeight();
            this.blockEntity = inv;
            this.backpack = null;
        } else {
            InteractionHand hand = buf.readInt() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            BackpackInventory backpackInventory = BackpackInventoryHandlerFactory.getBackpackInventory(playerInventory.player, hand);
            this.inventory = (Container) backpackInventory;
            this.inventoryWidth = backpackInventory.inventoryWidth;
            this.inventoryHeight = backpackInventory.inventoryHeight;
            this.backpack = playerInventory.player.getItemInHand(hand);
            this.isBackpackInOffhand = hand == InteractionHand.OFF_HAND;
            this.blockEntity = null;
        }

        checkContainerSize(inventory, inventoryWidth * inventoryHeight);
        inventory.startOpen(playerInventory.player);

        setupSlots(true);
    }

    protected CompactChestScreenHandler(@Nullable MenuType<?> type, int syncId) {
        super(type, syncId);
    }

    @Override
    public void removed(final Player player) {
        super.removed(player);
        inventory.stopOpen(player);
    }

    public void setupSlots(final boolean includeChestInventory) {
        // Creating Slots for GUI. A Slot is essentially a correspoding from inventory itemstacks to the GUI position.
        int i;
        int j;

        this.slots.clear();

        final int chestInvHeight = inventoryHeight * 18;

        // Chest Inventory
        for (i = 0; i < inventoryHeight; i++) {
            for (j = 0; j < inventoryWidth; j++) {
                final Slot slot = new Slot(inventory, i * inventoryWidth + j, 8 + j * 18, 18 + i * 18);
                this.addSlot(slot);
            }
        }

        // Player Inventory (27 storage + 9 hotbar)
        for (i = 0; i < 3; i++) {
            for (j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, i * 9 + j + 9, 8 + ((inventoryWidth * 18) / 2) - (9 * 9) + j * 18, 18 + i * 18 + chestInvHeight + 18));
            }
        }


        for (j = 0; j < 9; j++) {
            if(this.blockEntity == null && j==playerInventory.selected && !isBackpackInOffhand) {
                this.addSlot(new Slot(playerInventory, j, 8 + ((inventoryWidth * 18) / 2) - (9 * 9) + j * 18, 18 + chestInvHeight + 60 + 18) {
                    @Override
                    public boolean mayPickup(Player playerEntity) {
                        return false;
                    }

                });
            } else {
                this.addSlot(new Slot(playerInventory, j, 8 + ((inventoryWidth * 18) / 2) - (9 * 9) + j * 18, 18 + chestInvHeight + 60 + 18));
            }
        }
    }

    
    @Override
    public boolean stillValid(final Player player) {
        return this.inventory.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        final Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasItem()) {

            final ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();

            if (invSlot < this.inventory.getContainerSize()) {
                if (!this.moveItemStackTo(originalStack, this.inventory.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(originalStack, 0, this.inventory.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return newStack;
    }
}