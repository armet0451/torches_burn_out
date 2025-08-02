package armet.torch_burnout;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;



// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(SimpleTorchBurnout.MODID)
public class SimpleTorchBurnout {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "torch_burnout";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "torch_burnout" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "torch_burnout" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "torch_burnout" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);




    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
       // registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }


    // Creates a new Block with the id "torch_burnout:example_block", combining the namespace and path
    public static final BlockBehaviour.Properties TORCH_BEHAVIOR = BlockBehaviour.Properties.ofFullCopy(Blocks.TORCH).randomTicks();

    public static final DeferredBlock<Block> LIT_TORCH =
            registerBlock("lit_torch", () -> new LitTorch(TORCH_BEHAVIOR, ParticleTypes.FLAME, 14));

    public static final DeferredItem<Item> LIT_TORCH_ITEM = ITEMS.register("lit_torch", () -> new BlockItem(LIT_TORCH.get(), new Item.Properties()));



    public static final DeferredBlock<Block> LIT_SOUL_TORCH =
            registerBlock("lit_soul_torch", () -> new LitTorch(TORCH_BEHAVIOR, ParticleTypes.SOUL_FIRE_FLAME, 10));

    public static final DeferredItem<Item> LIT_SOUL_TORCH_ITEM = ITEMS.register("lit_soul_torch", () -> new BlockItem(LIT_SOUL_TORCH.get(), new Item.Properties()));


    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public SimpleTorchBurnout(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::addCreative);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (SimpleTorchBurnout) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);



        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {








    }

    // Add the example block item to the building blocks tab

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {

            event.getParentEntries().forEach(stack -> {
                        Item itemStack = stack.getItem();
                        if (itemStack.equals(Items.TORCH) || itemStack.equals(Items.SOUL_TORCH)) {
                            event.remove(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                        }
                    });


            event.insertFirst(LIT_SOUL_TORCH_ITEM.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertFirst(LIT_TORCH_ITEM.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
