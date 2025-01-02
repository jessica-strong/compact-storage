package com.witchica.compactstorage.common.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.witchica.compactstorage.CompactStoragePlatform;
import com.witchica.compactstorage.common.block.CompactChestBlock;
import com.witchica.compactstorage.common.block.entity.CompactChestBlockEntity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class CompactChestBlockEntityRenderer implements BlockEntityRenderer<CompactChestBlockEntity> {
    private final ModelPart chestBase;

    private final ModelPart chestLid;
    private final ModelPart chestLock;

    public static final Map<Block, ResourceLocation> CHEST_TEXTURES = new HashMap<Block, ResourceLocation>();

    static {
        for(int i = 0; i < 16; i++) {
            CHEST_TEXTURES.put(CompactStoragePlatform.getCompactChestBlock(i), new ResourceLocation("compact_storage", String.format("textures/block/compact_chest_%s.png", DyeColor.byId(i).name().toLowerCase(Locale.ROOT))));
        }
    }
    
    public CompactChestBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        super();

        ModelPart modelPart = ctx.bakeLayer(ModelLayers.CHEST);
        this.chestBase = modelPart.getChild("bottom");
        this.chestLid = modelPart.getChild("lid");
        this.chestLock = modelPart.getChild("lock");
    }

    @Override
    public void render(CompactChestBlockEntity compactChestBlockEntity, float delta, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, int overlay) {
        Level world = compactChestBlockEntity.getLevel();
        boolean bl = world != null;


        BlockState blockState = bl ? compactChestBlockEntity.getBlockState() : CompactStoragePlatform.getCompactChestBlock(0).defaultBlockState().setValue(CompactChestBlock.FACING, Direction.SOUTH);
        Block block = blockState.getBlock();

        matrixStack.pushPose();

        float y_rotation = ((Direction) blockState.getValue(CompactChestBlock.FACING)).toYRot();

        matrixStack.translate(0.5D, 0.5D, 0.5D);
        matrixStack.mulPose(Axis.YP.rotationDegrees(-y_rotation));
        matrixStack.translate(-0.5D, -0.5D, -0.5D);

        float lid_openness = compactChestBlockEntity.getOpenNess(delta);
        
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderType.entitySolid(CHEST_TEXTURES.get(block)));

        chestLid.xRot = -(lid_openness * 1.5707964F);
        chestLock.xRot = chestLid.xRot;

        chestBase.render(matrixStack, vertexConsumer, light, overlay);
        chestLid.render(matrixStack, vertexConsumer, light, overlay);
        chestLock.render(matrixStack, vertexConsumer, light, overlay);

        matrixStack.popPose();
    }
    
}
