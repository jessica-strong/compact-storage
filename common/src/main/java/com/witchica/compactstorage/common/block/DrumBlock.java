package com.witchica.compactstorage.common.block;

import com.mojang.serialization.MapCodec;
import com.witchica.compactstorage.CompactStorage;
import com.witchica.compactstorage.CompactStoragePlatform;
import com.witchica.compactstorage.common.block.entity.DrumBlockEntity;
import com.witchica.compactstorage.common.item.StorageUpgradeItem;
import com.witchica.compactstorage.common.util.CompactStorageUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DrumBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = DirectionProperty.create("facing");
    public static final BooleanProperty RETAINING = BooleanProperty.create("retaining");
    public static final MapCodec<DrumBlock> CODEC = simpleCodec(DrumBlock::new);
    public DrumBlock(Properties settings) {
        super(settings);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(RETAINING, false));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        boolean retaining = false;
        return super.getStateForPlacement(ctx).setValue(FACING, ctx.getNearestLookingDirection().getOpposite())
                .setValue(RETAINING, retaining);
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return super.mirror(state, mirror).setValue(FACING, state.getValue(FACING).getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return super.rotate(state, rotation).setValue(FACING, state.getValue(FACING).getClockWise());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, RETAINING);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }


    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
        tooltip.add(Component.translatable("text.compact_storage.drum.tooltip_1").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("text.compact_storage.drum.tooltip_2").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));

        if(stack.has(DataComponents.CUSTOM_DATA)) {
            CustomData data = stack.get(DataComponents.CUSTOM_DATA);
            CompoundTag tag = data.copyTag();

            if(tag.contains("Retaining") && tag.getBoolean("Retaining")) {
                tooltip.add(Component.translatable("tooltip.compact_storage.retaining").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
            }
            if(tag.contains("TooltipItem")) {
                ItemStack stored = ItemStack.parseOptional(context.registries(), tag.getCompound("TooltipItem"));

                if(!stored.isEmpty()) {
                    int count = tag.getInt("TooltipCount");
                    tooltip.add(Component.translatable("tooltip.compact_storage.drum_contains", stored.getDisplayName().getString(), count).withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));

                }
            }
        }

        super.appendHoverText(stack, context, tooltip, tooltipFlag);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return CompactStoragePlatform.drumBlockEntitySupplier().create(pos, state);
    }

    public void extractItem(Level world, BlockPos pos, Player player) {
        DrumBlockEntity drumBlockEntity = (DrumBlockEntity) world.getBlockEntity(pos);
        SimpleContainer inventory = drumBlockEntity.inventory;

        ItemStack extracted = inventory.removeItemNoUpdate(0);

        if(!extracted.isEmpty()) {
            world.addFreshEntity(new ItemEntity(world, player.getBlockX(), player.getBlockY(), player.getBlockZ(), extracted));
            world.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 1f);
        }
    }

    public void insertItem(Level world, BlockPos pos, Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);

        DrumBlockEntity drum = (DrumBlockEntity) world.getBlockEntity(pos);
        SimpleContainer itemHandler = drum.inventory;

        boolean completed = false;

        if(itemInHand.isEmpty() && drum.hasAnyItems()) {
            Container playerInventory = player.getInventory();

            for(int i = 0; i < playerInventory.getContainerSize(); i++) {
                ItemStack itemStack = playerInventory.getItem(i);
                if(itemHandler.canPlaceItem(0, itemStack)) {
                    ItemStack returned = itemHandler.addItem(itemStack);

                    if(itemStack.getCount() != returned.getCount()) {
                        playerInventory.setItem(i, returned);
                        completed = true;
                        break;
                    }
                }
            }
        } else {
            ItemStack itemStack = player.getItemInHand(hand);

            if(itemHandler.canPlaceItem(0, itemStack)) {
                ItemStack returned = itemHandler.addItem(itemStack);

                if(itemStack.getCount() != returned.getCount()) {
                    player.setItemInHand(hand, returned);
                    completed = true;
                }
            }
        }

        if(completed) {
            world.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 1f);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide) {
            if(player.isShiftKeyDown()) {
                extractItem(level, pos, player);
            } else {
                if(player.getItemInHand(hand).getItem() == CompactStorage.UPGRADE_RETAINER_ITEM.get()) {
                    StorageUpgradeItem storageUpgradeItem = (StorageUpgradeItem) player.getItemInHand(hand).getItem();
                    if(level.getBlockEntity(pos) instanceof DrumBlockEntity drumBlockEntity) {
                        if(drumBlockEntity.applyRetainingUpgrade()) {
                            player.getItemInHand(hand).shrink(1);
                            player.displayClientMessage(Component.translatable(storageUpgradeItem.getUpgradeType().upgradeSuccess).withStyle(ChatFormatting.GREEN), true);
                            player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1f, 1f);
                            return ItemInteractionResult.CONSUME;
                        } else {
                            player.playNotifySound(SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1f, 1f);
                            player.displayClientMessage(Component.translatable(storageUpgradeItem.getUpgradeType().upgradeFail).withStyle(ChatFormatting.RED), true);
                            return ItemInteractionResult.FAIL;
                        }
                    }
                }

                insertItem(level, pos, player, hand);
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if(!world.isClientSide) {
            if(player.isShiftKeyDown()) {
               extractItem(world, pos, player);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void attack(BlockState state, Level world, BlockPos pos, Player player) {
        if(!world.isClientSide) {
            extractItem(world, pos, player);
        }

        super.attack(state, world, pos, player);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if(blockEntity instanceof DrumBlockEntity drumBlock) {
            int totalItemCount = drumBlock.getTotalItemCount();
            int stackSize = drumBlock.getStoredType().getDefaultMaxStackSize();
            int output = Mth.floor(((totalItemCount / (float) stackSize) / 64f) * 15f);
            return output;
        }

        return 0;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if(!level.isClientSide) {
            if(level.getBlockEntity(pos) instanceof DrumBlockEntity drumBlock) {
                if(stack.has(DataComponents.CUSTOM_DATA)) {
                    // TODO : RETAINING drumBlock.loadAdditional(stack.get(DataComponents.CUSTOM_DATA).copyTag());
                    drumBlock.setChanged();
                }
            }
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        CompactStorageUtil.dropContents(level, blockPos, blockState.getBlock(), player);
        return super.playerWillDestroy(level, blockPos, blockState, player);
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        CompactStorageUtil.dropContents(level, pos, level.getBlockState(pos).getBlock(), null);
        super.wasExploded(level, pos, explosion);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if(blockEntityType == CompactStorage.DRUM_ENTITY_TYPE.get()) {
            return (level1, blockPos, blockState, blockEntity) -> DrumBlockEntity.tick(level1, blockPos, blockState, (DrumBlockEntity) blockEntity);
        } else {
            return null;
        }
    }
}
