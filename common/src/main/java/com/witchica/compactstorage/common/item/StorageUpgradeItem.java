package com.witchica.compactstorage.common.item;

import com.witchica.compactstorage.CompactStorage;
import com.witchica.compactstorage.CompactStoragePlatform;
import com.witchica.compactstorage.common.util.CompactStorageUpgradeType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class StorageUpgradeItem extends Item {
    private final CompactStorageUpgradeType upgradeType;

    public StorageUpgradeItem(Properties settings, CompactStorageUpgradeType upgradeType) {
        super(settings);
        this.upgradeType = upgradeType;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
        if (stack.getItem() == CompactStorage.UPGRADE_COLUMN_ITEM.get()) {
            tooltip.add(Component.translatable("tooltip.compact_storage.column_upgrade_descriptor").withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.translatable("tooltip.compact_storage.upgrade_backpack").withStyle(ChatFormatting.DARK_PURPLE));

        } else if (stack.getItem() == CompactStorage.UPGRADE_ROW_ITEM.get()) {
            tooltip.add(Component.translatable("tooltip.compact_storage.row_upgrade_descriptor").withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.translatable("tooltip.compact_storage.upgrade_backpack").withStyle(ChatFormatting.DARK_PURPLE));

        } else if (stack.getItem() == CompactStorage.UPGRADE_RETAINER_ITEM.get()) {
            tooltip.add(Component.translatable("tooltip.compact_storage.upgrade_retainer_description").withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.translatable("tooltip.compact_storage.new_item_description").withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC));
        }
        super.appendHoverText(stack, context, tooltip, tooltipFlag);
    }

    public CompactStorageUpgradeType getUpgradeType() {
        return upgradeType;
    }
}
