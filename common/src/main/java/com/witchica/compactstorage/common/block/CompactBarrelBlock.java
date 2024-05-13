package com.witchica.compactstorage.common.block;

import com.mojang.serialization.MapCodec;
import com.witchica.compactstorage.CompactStorage;
import com.witchica.compactstorage.CompactStoragePlatform;
import com.witchica.compactstorage.common.block.entity.CompactBarrelBlockEntity;
import com.witchica.compactstorage.common.item.StorageUpgradeItem;
import com.witchica.compactstorage.common.screen.CompactStorageMenuProvider;
import com.witchica.compactstorage.common.util.CompactStorageUtil;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;

public class CompactBarrelBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = DirectionProperty.create("facing");
    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    public static final BooleanProperty RETAINING = BooleanProperty.create("retaining");
    public static final MapCodec<CompactBarrelBlock> CODEC = simpleCodec(CompactBarrelBlock::new);
    private boolean canDye;


    public CompactBarrelBlock(BlockBehaviour.Properties settings) {
        super(settings);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(OPEN, false).setValue(RETAINING, false));
    }

    public CompactBarrelBlock setCanDye() {
        this.canDye = true;
        return this;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return getStateDefinition().any().setValue(FACING, ctx.getNearestLookingDirection().getOpposite()).setValue(OPEN, false).setValue(RETAINING, ctx.getItemInHand().hasTag() ? ctx.getItemInHand().getTag().getBoolean("retaining") : false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, RETAINING);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);

        if (itemStack.hasCustomHoverName()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);

            if (blockEntity instanceof CompactBarrelBlockEntity) {
                ((CompactBarrelBlockEntity) blockEntity).setCustomName(itemStack.getHoverName());
            }
        }

        if (!world.isClientSide && itemStack.hasTag()) {
            CompoundTag nbt = itemStack.getTag();
            BlockEntity blockEntity = world.getBlockEntity(pos);

            if (blockEntity instanceof CompactBarrelBlockEntity compactBarrelBlockEntity) {
                compactBarrelBlockEntity.load(nbt);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if(state.hasBlockEntity() && !(newState.getBlock() instanceof CompactBarrelBlock)) {
            level.removeBlockEntity(pos);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                              BlockHitResult hit) {
        if (!world.isClientSide) {
            if (!world.isClientSide) {
                BlockEntity blockEntity = world.getBlockEntity(pos);

                if(blockEntity instanceof CompactBarrelBlockEntity compactBarrelBlockEntity) {
                    Item heldItem = player.getItemInHand(hand).getItem();

                    if(heldItem instanceof StorageUpgradeItem storageUpgradeItem) {
                        if(compactBarrelBlockEntity.applyUpgrade(storageUpgradeItem.getUpgradeType())) {
                            player.getItemInHand(hand).shrink(1);
                            player.displayClientMessage(Component.translatable(storageUpgradeItem.getUpgradeType().upgradeSuccess).withStyle(ChatFormatting.GREEN), true);
                            player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1f, 1f);
                            return InteractionResult.CONSUME_PARTIAL;
                        } else {
                            player.playNotifySound(SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1f, 1f);
                            player.displayClientMessage(Component.translatable(storageUpgradeItem.getUpgradeType().upgradeFail).withStyle(ChatFormatting.RED), true);
                            return InteractionResult.FAIL;
                        }
                    } else if(canDye && heldItem instanceof DyeItem dyeItem) {
                        Block newBlock = CompactStorage.getCompactBarrelFromDyeColor(dyeItem.getDyeColor());

                        if(newBlock != this) {
                            world.setBlockAndUpdate(pos, newBlock.defaultBlockState().setValue(FACING, state.getValue(FACING)));
                            player.playNotifySound(SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 1f, 1f);
                            player.getItemInHand(hand).shrink(1);
                            return InteractionResult.CONSUME_PARTIAL;
                        }
                    }
                }

                openMenu(world, player, pos, state, hand);
            }
        }

        return InteractionResult.SUCCESS;
    }

    public void openMenu(Level level, Player player, BlockPos pos, BlockState state, InteractionHand hand) {
        MenuRegistry.openExtendedMenu((ServerPlayer) player, CompactStorageMenuProvider.ofBlock(pos, Component.translatable("container.compact_storage.compact_barrel")));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        CompactStorageUtil.dropContents(world, pos, state.getBlock(), player, registries);
        return super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    public void onExplosionHit(BlockState state, Level world, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> stackMerger) {
        CompactStorageUtil.dropContents(world, pos, this, null, registries);
        super.onExplosionHit(state, world, pos, explosion, stackMerger);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag options) {
        super.appendHoverText(stack, world, tooltip, options);
        CompactStorageUtil.appendTooltip(stack, world, tooltip, options, false);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(world.getBlockEntity(pos));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return CompactStoragePlatform.compactBarrelBlockEntitySupplier().create(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if(type == CompactStorage.COMPACT_BARREL_ENTITY_TYPE.get()) {
            return (world1, pos, state1, be) -> CompactBarrelBlockEntity.tick(world1, pos, state1, (CompactBarrelBlockEntity)  be);
        } else {
            return null;
        }
    }
}
