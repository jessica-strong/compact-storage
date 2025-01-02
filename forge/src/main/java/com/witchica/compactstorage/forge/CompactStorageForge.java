package com.witchica.compactstorage.forge;

import com.mojang.logging.LogUtils;
import com.witchica.compactstorage.common.block.DrumBlock;
import com.witchica.compactstorage.common.block.entity.DrumBlockEntity;
import com.witchica.compactstorage.common.item.StorageUpgradeItem;
import com.witchica.compactstorage.common.screen.CompactChestScreenHandler;
import com.witchica.compactstorage.common.util.CompactStorageUtil;
import com.witchica.compactstorage.forge.block.ForgeCompactBarrelBlock;
import com.witchica.compactstorage.forge.block.ForgeCompactChestBlock;
import com.witchica.compactstorage.forge.block.ForgeDrumBlock;
import com.witchica.compactstorage.forge.block.entity.ForgeCompactBarrelBlockEntity;
import com.witchica.compactstorage.forge.block.entity.ForgeCompactChestBlockEntity;
import com.witchica.compactstorage.forge.block.entity.ForgeDrumBlockEntity;
import com.witchica.compactstorage.forge.item.ForgeBackpackItem;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

import static com.witchica.compactstorage.CompactStorageCommon.MOD_ID;

@Mod(MOD_ID)
public class CompactStorageForge {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MOD_ID);
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static ResourceLocation COMPACT_CHEST_GENERIC_IDENTIFIER = new ResourceLocation(MOD_ID, "compact_chest");
    public static ResourceLocation COMPACT_BARREL_GENERIC_IDENTIFIER = new ResourceLocation(MOD_ID, "compact_barrel");

    public static final RegistryObject<ForgeCompactChestBlock>[] COMPACT_CHEST_BLOCKS = new RegistryObject[16];
    public static final RegistryObject<ForgeCompactBarrelBlock>[] COMPACT_BARREL_BLOCKS = new RegistryObject[16];
    public static final RegistryObject<ForgeDrumBlock>[] DRUM_BLOCKS = new RegistryObject[CompactStorageUtil.DRUM_TYPES.length];
    public static final RegistryObject<ForgeBackpackItem>[] BACKPACK_ITEMS = new RegistryObject[16];

    public static final String COMPACT_CHEST_TRANSLATION_KEY = Util.makeDescriptionId("container", COMPACT_CHEST_GENERIC_IDENTIFIER);

    public static RegistryObject<BlockEntityType<ForgeCompactBarrelBlockEntity>> COMPACT_BARREL_ENTITY_TYPE =
            BLOCK_ENTITY_TYPES.register("compact_barrel", () -> BlockEntityType.Builder.of(ForgeCompactBarrelBlockEntity::new, Arrays.stream(COMPACT_BARREL_BLOCKS).map(RegistryObject::get).toArray(Block[]::new)).build(null));

    public static RegistryObject<BlockEntityType<ForgeCompactChestBlockEntity>> COMPACT_CHEST_ENTITY_TYPE =
            BLOCK_ENTITY_TYPES.register("compact_chest", () -> BlockEntityType.Builder.of(ForgeCompactChestBlockEntity::new, Arrays.stream(COMPACT_CHEST_BLOCKS).map(RegistryObject::get).toArray(Block[]::new)).build(null));

    public static RegistryObject<BlockEntityType<ForgeDrumBlockEntity>> DRUM_ENTITY_TYPE =
            BLOCK_ENTITY_TYPES.register("drum", () -> BlockEntityType.Builder.of(ForgeDrumBlockEntity::new, Arrays.stream(DRUM_BLOCKS).map(RegistryObject::get).toArray(Block[]::new)).build(null));

    public static final HashMap<DyeColor, RegistryObject<ForgeCompactChestBlock>> DYE_COLOR_TO_COMPACT_CHEST_MAP = new HashMap<DyeColor, RegistryObject<ForgeCompactChestBlock>>();
    public static final HashMap<DyeColor, RegistryObject<ForgeCompactBarrelBlock>> DYE_COLOR_TO_COMPACT_BARREL_MAP = new HashMap<DyeColor, RegistryObject<ForgeCompactBarrelBlock>>();
    public static final HashMap<DyeColor, RegistryObject<ForgeBackpackItem>> DYE_COLOR_TO_BACKPACK_MAP = new HashMap<DyeColor, RegistryObject<ForgeBackpackItem>>();

    public static final RegistryObject<StorageUpgradeItem> UPGRADE_ROW_ITEM = ITEMS.register("upgrade_row", () -> new StorageUpgradeItem(new Item.Properties()));
    public static final RegistryObject<StorageUpgradeItem> UPGRADE_COLUMN_ITEM = ITEMS.register("upgrade_column", () -> new StorageUpgradeItem(new Item.Properties()));

    public static final RegistryObject<MenuType<CompactChestScreenHandler>> COMPACT_CHEST_SCREEN_HANDLER = MENU_TYPES.register("compact_chest", () -> IForgeMenuType.create(CompactChestScreenHandler::new));
    public static final RegistryObject<CreativeModeTab> COMPACT_STORAGE_TAB = CREATIVE_MODE_TABS.register("compact_storage_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.compact_storage.general"))
            .icon(() -> new ItemStack(COMPACT_CHEST_BLOCKS[0].get(), 1))
            .displayItems((params, populator) -> {

                Arrays.stream(COMPACT_CHEST_BLOCKS).forEach(item -> populator.accept(item.get()));
                Arrays.stream(COMPACT_BARREL_BLOCKS).forEach(item-> populator.accept(item.get()));
                Arrays.stream(BACKPACK_ITEMS).forEach(item-> populator.accept(item.get()));
                Arrays.stream(DRUM_BLOCKS).forEach(item-> populator.accept(item.get()));

                populator.accept(UPGRADE_COLUMN_ITEM.get());
                populator.accept(UPGRADE_ROW_ITEM.get());
            }).build());

    static {
        for(int i = 0; i < 16; i++) {
            String dyeName = DyeColor.byId(i).getName().toLowerCase(Locale.ROOT);
            DyeColor color = DyeColor.byId(i);
            final int id = i;

            COMPACT_CHEST_BLOCKS[i] = BLOCKS.register("compact_chest_" + dyeName, () ->
                    new ForgeCompactChestBlock(BlockBehaviour.Properties.copy(Blocks.CHEST).noOcclusion().strength(2f, 5f))
            );

            ITEMS.register("compact_chest_" + dyeName, () ->
                    new BlockItem(COMPACT_CHEST_BLOCKS[id].get(), new Item.Properties())
            );

            DYE_COLOR_TO_COMPACT_CHEST_MAP.put(color, COMPACT_CHEST_BLOCKS[i]);



            BACKPACK_ITEMS[i] = ITEMS.register("backpack_" + dyeName, () ->
                    new ForgeBackpackItem(new Item.Properties().stacksTo(1)));
            DYE_COLOR_TO_BACKPACK_MAP.put(color, BACKPACK_ITEMS[i]);



            COMPACT_BARREL_BLOCKS[i] = BLOCKS.register("compact_barrel_" + color, () ->
                    new ForgeCompactBarrelBlock(BlockBehaviour.Properties.copy(Blocks.BARREL).strength(2f, 5f)));
            DYE_COLOR_TO_COMPACT_BARREL_MAP.put(color, COMPACT_BARREL_BLOCKS[i]);

            ITEMS.register("compact_barrel_" + dyeName, () ->
                    new BlockItem(COMPACT_BARREL_BLOCKS[id].get(), new Item.Properties())
            );
        }

        for(int i = 0; i < CompactStorageUtil.DRUM_TYPES.length; i++) {
            final int id = i;

            DRUM_BLOCKS[id] = BLOCKS.register( CompactStorageUtil.DRUM_TYPES[id] + "_drum", () ->
                    new ForgeDrumBlock(BlockBehaviour.Properties.copy(Blocks.BARREL).strength(2f, 2f)));

            ITEMS.register(CompactStorageUtil.DRUM_TYPES[i] + "_drum", () -> new BlockItem(DRUM_BLOCKS[id].get(), new Item.Properties()));
        }
    }
    public CompactStorageForge() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITY_TYPES.register(eventBus);
        MENU_TYPES.register(eventBus);
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
