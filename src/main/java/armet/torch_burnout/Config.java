package armet.torch_burnout;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue BURNOUT_CHANCE = BUILDER
            .comment("Chance for a lit torch to burn out each random tick. Higher value = torches burn out faster")
            .defineInRange("burnoutChance", 0.03, 0, 1.0);

    public static final ModConfigSpec.BooleanValue BURNED_TORCHES = BUILDER
            .comment("Whether torches leave behind a 'burned-out' variant or simply disappear.")
            .define("burnedTorches", true);

    public static final ModConfigSpec.BooleanValue CAN_RELIGHT = BUILDER
            .comment("Whether burnt-out torches can be reignited with Flint and Steel")
            .define("canRelight", true);



    public static final ModConfigSpec.BooleanValue PLACE_LIT = BUILDER
            .comment("Whether torches should automatically be lit when placed, like in vanilla")
            .define("placeLit", true);






    static final ModConfigSpec SPEC = BUILDER.build();


}
