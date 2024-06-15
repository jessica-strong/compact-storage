package com.witchica.compactstorage;

import com.mojang.logging.LogUtils;
import com.witchica.compactstorage.common.block.CompactBarrelBlock;
import com.witchica.compactstorage.common.block.CompactChestBlock;
import com.witchica.compactstorage.common.block.DrumBlock;
import com.witchica.compactstorage.common.block.entity.CompactBarrelBlockEntity;
import com.witchica.compactstorage.common.block.entity.CompactChestBlockEntity;
import com.witchica.compactstorage.common.block.entity.DrumBlockEntity;
import com.witchica.compactstorage.common.config.CompactStorageConfig;
import com.witchica.compactstorage.common.item.BackpackItem;
import com.witchica.compactstorage.common.item.StorageUpgradeItem;
import com.witchica.compactstorage.common.item.WrenchItem;
import com.witchica.compactstorage.common.screen.CompactChestScreenHandler;
import com.witchica.compactstorage.common.util.CompactStorageUpgradeType;
import com.witchica.compactstorage.common.util.CompactStorageUtil;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.api.EnvType;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CompactStorage {
    public static final String MOD_ID = "compact_storage";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(MOD_ID, Registries.BLOCK);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, Registries.ITEM);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(MOD_ID, Registries.BLOCK_ENTITY_TYPE);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(MOD_ID, Registries.MENU);
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static ResourceLocation COMPACT_CHEST_GENERIC_IDENTIFIER = ResourceLocation.fromNamespaceAndPath(MOD_ID, "compact_chest");
    public static ResourceLocation COMPACT_BARREL_GENERIC_IDENTIFIER = ResourceLocation.fromNamespaceAndPath(MOD_ID, "compact_barrel");

    public static final RegistrySupplier<CompactChestBlock>[] COMPACT_CHEST_BLOCKS = new RegistrySupplier[16];
    public static final RegistrySupplier<CompactChestBlock>[] COMPACT_CHEST_WOOD_BLOCKS = new RegistrySupplier[CompactStorageUtil.DRUM_TYPES.length];
    public static final RegistrySupplier<CompactBarrelBlock>[] COMPACT_BARREL_BLOCKS = new RegistrySupplier[16];
    public static final RegistrySupplier<CompactBarrelBlock>[] COMPACT_BARREL_WOOD_BLOCKS = new RegistrySupplier[CompactStorageUtil.DRUM_TYPES.length];
    public static final RegistrySupplier<DrumBlock>[] DRUM_BLOCKS = new RegistrySupplier[CompactStorageUtil.DRUM_TYPES.length];
    public static final RegistrySupplier<BackpackItem>[] BACKPACK_ITEMS = new RegistrySupplier[16];

    public static final String COMPACT_CHEST_TRANSLATION_KEY = Util.makeDescriptionId("container", COMPACT_CHEST_GENERIC_IDENTIFIER);

    public static RegistrySupplier<BlockEntityType<CompactBarrelBlockEntity>> COMPACT_BARREL_ENTITY_TYPE =
            BLOCK_ENTITY_TYPES.register("compact_barrel", () -> BlockEntityType.Builder.of(CompactStoragePlatform.compactBarrelBlockEntitySupplier(), getAllCompactBarrels()).build(null));

    public static RegistrySupplier<BlockEntityType<CompactChestBlockEntity>> COMPACT_CHEST_ENTITY_TYPE =
            BLOCK_ENTITY_TYPES.register("compact_chest", () -> BlockEntityType.Builder.of(CompactStoragePlatform.compactChestBlockEntitySupplier(), getAllCompactChests()).build(null));

    private static Block[] getAllCompactChests() {
        List<Block> blocks = new ArrayList<Block>();
        Arrays.stream(COMPACT_CHEST_BLOCKS).forEach(block -> blocks.add(block.get()));
        Arrays.stream(COMPACT_CHEST_WOOD_BLOCKS).forEach(block -> blocks.add(block.get()));

        return blocks.toArray(Block[]::new);
    }
    private static Block[] getAllCompactBarrels() {
        List<Block> blocks = new ArrayList<Block>();
        Arrays.stream(COMPACT_BARREL_BLOCKS).forEach(block -> blocks.add(block.get()));
        Arrays.stream(COMPACT_BARREL_WOOD_BLOCKS).forEach(block -> blocks.add(block.get()));

        return blocks.toArray(Block[]::new);
    }

    public static RegistrySupplier<BlockEntityType<DrumBlockEntity>> DRUM_ENTITY_TYPE =
            BLOCK_ENTITY_TYPES.register("drum", () -> BlockEntityType.Builder.of(CompactStoragePlatform.drumBlockEntitySupplier(), Arrays.stream(DRUM_BLOCKS).map(RegistrySupplier::get).toArray(Block[]::new)).build(null));

    public static final HashMap<DyeColor, RegistrySupplier<CompactChestBlock>> DYE_COLOR_TO_COMPACT_CHEST_MAP = new HashMap<DyeColor, RegistrySupplier<CompactChestBlock>>();
    public static final HashMap<DyeColor, RegistrySupplier<CompactBarrelBlock>> DYE_COLOR_TO_COMPACT_BARREL_MAP = new HashMap<DyeColor, RegistrySupplier<CompactBarrelBlock>>();
    public static final HashMap<DyeColor, RegistrySupplier<BackpackItem>> DYE_COLOR_TO_BACKPACK_MAP = new HashMap<DyeColor, RegistrySupplier<BackpackItem>>();

    public static final RegistrySupplier<StorageUpgradeItem> UPGRADE_ROW_ITEM = ITEMS.register("upgrade_row", () -> new StorageUpgradeItem(new Item.Properties(), CompactStorageUpgradeType.WIDTH_INCREASE));
    public static final RegistrySupplier<StorageUpgradeItem> UPGRADE_COLUMN_ITEM = ITEMS.register("upgrade_column", () -> new StorageUpgradeItem(new Item.Properties(), CompactStorageUpgradeType.HEIGHT_INCREASE));
    public static final RegistrySupplier<StorageUpgradeItem> UPGRADE_RETAINER_ITEM = ITEMS.register("upgrade_retainer", () -> new StorageUpgradeItem(new Item.Properties(), CompactStorageUpgradeType.RETAINING));

    public static RegistrySupplier<MenuType<CompactChestScreenHandler>> COMPACT_CHEST_SCREEN_HANDLER = MENU_TYPES.register("compact_chest", () -> MenuRegistry.ofExtended(CompactChestScreenHandler::new));

    //public static RegistrySupplier<Item> WRENCH_ITEM = ITEMS.register("wrench", () -> new WrenchItem(new Item.Properties().stacksTo(1)));
    public static final RegistrySupplier<CreativeModeTab> COMPACT_STORAGE_TAB = CREATIVE_MODE_TABS.register("compact_storage_tab", () -> CreativeTabRegistry.create(builder -> {
        builder.title(Component.translatable("itemGroup.compact_storage.general"))
                .icon(() -> new ItemStack(COMPACT_CHEST_BLOCKS[0].get(), 1))
                .displayItems((params, populator) -> {
                    populator.accept(UPGRADE_COLUMN_ITEM.get());
                    populator.accept(UPGRADE_ROW_ITEM.get());
                    populator.accept(UPGRADE_RETAINER_ITEM.get());
                    //populator.accept(WRENCH_ITEM.get());

                    Arrays.stream(COMPACT_CHEST_BLOCKS).forEach(item -> populator.accept(item.get()));
                    Arrays.stream(COMPACT_BARREL_BLOCKS).forEach(item-> populator.accept(item.get()));
                    Arrays.stream(BACKPACK_ITEMS).forEach(item-> populator.accept(item.get()));
                });
    }));

    public static final RegistrySupplier<CreativeModeTab> COMPACT_STORAGE_WOOD_TAB = CREATIVE_MODE_TABS.register("compact_storage_tab_wood", () -> CreativeTabRegistry.create(builder -> {
        builder.title(Component.translatable("itemGroup.compact_storage.wood"))
                .icon(() -> new ItemStack(COMPACT_CHEST_WOOD_BLOCKS[1].get(), 1))
                .displayItems((params, populator) -> {
                    Arrays.stream(COMPACT_CHEST_WOOD_BLOCKS).forEach(item -> populator.accept(item.get()));
                    Arrays.stream(COMPACT_BARREL_WOOD_BLOCKS).forEach(item -> populator.accept(item.get()));
                    Arrays.stream(DRUM_BLOCKS).forEach(item-> populator.accept(item.get()));
                });
    }));

    public static CompactStorageConfig CONFIG;

    static {
        for(int i = 0; i < 16; i++) {
            String dyeName = DyeColor.byId(i).getName().toLowerCase();
            DyeColor color = DyeColor.byId(i);
            final int id = i;

            COMPACT_CHEST_BLOCKS[i] = BLOCKS.register("compact_chest_" + dyeName, () ->
                    new CompactChestBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CHEST).noOcclusion().strength(2f, 5f)).setCanDye()
            );

            ITEMS.register("compact_chest_" + dyeName, () ->
                    new BlockItem(COMPACT_CHEST_BLOCKS[id].get(), new Item.Properties())
            );

            DYE_COLOR_TO_COMPACT_CHEST_MAP.put(color, COMPACT_CHEST_BLOCKS[i]);



            BACKPACK_ITEMS[i] = ITEMS.register("backpack_" + dyeName, () ->
                    new BackpackItem(new Item.Properties().stacksTo(1)));
            DYE_COLOR_TO_BACKPACK_MAP.put(color, BACKPACK_ITEMS[i]);



            COMPACT_BARREL_BLOCKS[i] = BLOCKS.register("compact_barrel_" + color, () ->
                    new CompactBarrelBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL).strength(2f, 5f)).setCanDye());
            DYE_COLOR_TO_COMPACT_BARREL_MAP.put(color, COMPACT_BARREL_BLOCKS[i]);

            ITEMS.register("compact_barrel_" + dyeName, () ->
                    new BlockItem(COMPACT_BARREL_BLOCKS[id].get(), new Item.Properties())
            );
        }

        for(int i = 0; i < CompactStorageUtil.DRUM_TYPES.length; i++) {
            final int id = i;

            DRUM_BLOCKS[id] = BLOCKS.register( CompactStorageUtil.DRUM_TYPES[id] + "_drum", () ->
                    new DrumBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL).strength(2f, 2f)));

            ITEMS.register(CompactStorageUtil.DRUM_TYPES[i] + "_drum", () -> new BlockItem(DRUM_BLOCKS[id].get(), new Item.Properties()));

            COMPACT_CHEST_WOOD_BLOCKS[id] = BLOCKS.register(CompactStorageUtil.DRUM_TYPES[i] + "_compact_chest", () -> new CompactChestBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CHEST)));
            ITEMS.register(CompactStorageUtil.DRUM_TYPES[i] + "_compact_chest", () -> new BlockItem(COMPACT_CHEST_WOOD_BLOCKS[id].get(), new Item.Properties()));

            COMPACT_BARREL_WOOD_BLOCKS[id] = BLOCKS.register(CompactStorageUtil.DRUM_TYPES[i] + "_compact_barrel", () -> new CompactBarrelBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL)));
            ITEMS.register(CompactStorageUtil.DRUM_TYPES[i] + "_compact_barrel", () -> new BlockItem(COMPACT_BARREL_WOOD_BLOCKS[id].get(), new Item.Properties()));
        }
    }
    public static void onInitialize() {
        if(Platform.getEnv() == EnvType.CLIENT) {
            ClientLifecycleEvent.CLIENT_SETUP.register(CompactStorageClient::clientSetupEvent);
        }

        //CONFIG = new CompactStorageConfig();

        BLOCKS.register();;
        ITEMS.register();
        BLOCK_ENTITY_TYPES.register();
        MENU_TYPES.register();
        CREATIVE_MODE_TABS.register();
    }

    public static Block getCompactBarrelFromDyeColor(DyeColor dye) {
        return DYE_COLOR_TO_COMPACT_BARREL_MAP.get(dye).get();
    }
    public static Block getCompactChestFromDyeColor(DyeColor dye) {
        return DYE_COLOR_TO_COMPACT_CHEST_MAP.get(dye).get();
    }

    public static Item getBackpackFromDyeColor(DyeColor dye) {
        return DYE_COLOR_TO_BACKPACK_MAP.get(dye).get();
    }
}
