package net.doubledoordev.burningtorch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.*;


public class BurningTorchConfig
{
    public static final BurningTorchConfig.General GENERAL;
    static final ForgeConfigSpec spec;

    static
    {
        final Pair<BurningTorchConfig.General, ForgeConfigSpec> specPair = new Builder().configure(BurningTorchConfig.General::new);
        spec = specPair.getRight();
        GENERAL = specPair.getLeft();
    }

    public static class General
    {

        public static List<? extends String> torchExtendingTags()
        {
            return new ArrayList<>(Arrays.asList("burningtorch:fuel_value_5,5", "burningtorch:fuel_value_1,1"));
        }

        public static List<? extends String> pumpkinExtendingTags()
        {
            return new ArrayList<>(Arrays.asList("burningtorch:fuel_value_5,5", "burningtorch:fuel_value_1,1"));
        }

        // General stuff

        // Llama stuff
        public BooleanValue llamaSpitAtBlocks;
        public BooleanValue llamaSpitExtinguishesFirst;
        public IntValue llamaSpitVerticalSearchRange;
        public IntValue llamaSpitHorizontalSearchRange;
        public DoubleValue llamaDestroySpeedMultiplier;
        public IntValue llamaSpitAtBlockCoolDown;

        // Torch stuff
        public ConfigValue<List<? extends String>> torchExtendingTags;
        public BooleanValue torchBurnsEntities;
        public BooleanValue torchPlaceLit;
        public BooleanValue torchRainExtinguish;
        public BooleanValue torchBurnoutWarning;
        public BooleanValue torchMakesSootMark;
        public IntValue torchRainUpdateRate;
        public IntValue torchPercentToStartFire;
        public IntValue torchDelayBetweenFire;
        public IntValue torchDecayRate;
        public IntValue torchLightDecay5;
        public IntValue torchLightDecay4;
        public IntValue torchLightDecay3;
        public IntValue torchLightDecay2;
        public IntValue torchLightDecay1;
        public IntValue torchStartingDecayLevel;

        // Pumpkin stuff
        public ConfigValue<List<? extends String>> pumpkinExtendingTags;
        public BooleanValue pumpkinBurnsEntities;
        public BooleanValue pumpkinPlaceLit;
        public BooleanValue pumpkinRainExtinguish;
        public BooleanValue pumpkinBurnoutWarning;
        public BooleanValue pumpkinMakesSootMark;
        public BooleanValue pumpkinLeavesPumpkin;
        public IntValue pumpkinRainUpdateRate;
        public IntValue pumpkinPercentToStartFire;
        public IntValue pumpkinDelayBetweenFire;
        public IntValue pumpkinDecayRate;
        public IntValue pumpkinLightDecay5;
        public IntValue pumpkinLightDecay4;
        public IntValue pumpkinLightDecay3;
        public IntValue pumpkinLightDecay2;
        public IntValue pumpkinLightDecay1;
        public IntValue pumpkinStartingDecayLevel;

        General(Builder builder)
        {
            builder.comment("General configuration settings")
                    .push("General");

            builder.pop();

            builder.comment("Llama spit settings")
                    .push("Sir Spit's a lot");

            llamaSpitAtBlocks = builder
                    .comment("Allow llamas to spit at blocks in the #burningtorch:llama_spit_targets tag.")
                    .translation("burningTorch.config.llama.spitAtBlocks")
                    .define("llamaSpitAtBlocks", true);

            llamaSpitExtinguishesFirst = builder
                    .comment("Llama spit hitting a block with the \"LIT\" block state property will turn it off instead of breaking the block.")
                    .translation("burningTorch.config.llama.spitExtinguishesFirst")
                    .define("llamaSpitExtinguishesFirst", true);

            llamaSpitHorizontalSearchRange = builder
                    .comment("Horizontal range that llamas will target blocks from #burningtorch:llama_spit_targets tag to destroy/put out.")
                    .translation("burningTorch.config.llama.spitHorizontalSearchRange")
                    .defineInRange("llamaSpitHorizontalSearchRange", 6, 1, Integer.MAX_VALUE);

            llamaSpitVerticalSearchRange = builder
                    .comment("Vertical range llamas will target blocks from #burningtorch:llama_spit_targets tag to destroy/put out.")
                    .translation("burningTorch.config.llama.spitVerticalSearchRange")
                    .defineInRange("llamaSpitVerticalSearchRange", 3, 1, Integer.MAX_VALUE);

            llamaDestroySpeedMultiplier = builder
                    .comment("Mining speed of the block is multiplied by this number to get the spits required to break a block. Lower multiplier means faster breaking.")
                    .translation("burningTorch.config.llama.destroySpeedMultiplier")
                    .defineInRange("llamaDestroySpeedMultiplier", 10, 0, Double.MAX_VALUE);

            llamaSpitAtBlockCoolDown = builder
                    .comment("How quickly llamas can spit again, 20 ticks = 1 Second, 1200 ticks = 1 Minute, 72000 ticks = 1 Hour")
                    .translation("burningTorch.config.llama.spitAtBlockCoolDown")
                    .defineInRange("llamaSpitAtBlockCoolDown", 40, 1, Integer.MAX_VALUE);

            builder.pop();

            builder.comment("Torch settings")
                    .push("Torches");

            torchExtendingTags = builder
                    .comment("Item Tags that are valid fuel sources for torches to consume. Number is the value for every item in the list. Anything more than 5 is lost.")
                    .translation("burningTorch.config.torch.extendingTags")
                    .defineList("torchExtendingTags", BurningTorchConfig.General.torchExtendingTags(), p -> p instanceof String);

            torchStartingDecayLevel = builder
                    .comment("What decay level does a freshly placed torch start at?")
                    .translation("burningTorch.config.torch.startingDecayLevel")
                    .defineInRange("torchStartingDecayLevel", 5, 1, 5);

            torchDecayRate = builder
                    .comment("How many ticks between decay levels, there are 5 levels total. 20 ticks = 1 Second, 1200 ticks = 1 Minute, 72000 ticks = 1 Hour")
                    .translation("burningTorch.config.torch.decayRate")
                    .defineInRange("torchDecayRate", 18000, 1, Integer.MAX_VALUE);

            torchRainUpdateRate = builder
                    .comment("How quickly an unprotected torch will react to a storm in ticks, Lower values are faster. 20 ticks = 1 Second, 1200 ticks = 1 Minute, 72000 ticks = 1 Hour")
                    .translation("burningTorch.config.torch.rainUpdateRate")
                    .defineInRange("torchRainUpdateRate", 40, 1, Integer.MAX_VALUE);

            torchRainExtinguish = builder
                    .comment("Should rain extinguish unprotected torches?")
                    .translation("burningTorch.config.torch.rainExtinguish")
                    .define("torchRainExtinguish", true);

            torchBurnoutWarning = builder
                    .comment("Should torches produce lots of smoke before they burn out?")
                    .translation("burningTorch.config.torch.torchBurnoutWarning")
                    .define("torchBurnoutWarning", true);

            torchBurnsEntities = builder
                    .comment("Should lit torches burn entities when they walk over it?")
                    .translation("burningTorch.config.torch.burnEntities")
                    .define("torchBurnsEntities", true);

            torchPercentToStartFire = builder
                    .comment("% Chance to start a fire around a torch in a 3x3x3 space centered on the torch. 0 will disable fires being started around torches.")
                    .translation("burningTorch.config.torch.percentToStartFire")
                    .defineInRange("torchPercentToStartFire", 50, 0, 100);

            torchDelayBetweenFire = builder
                    .comment("Delay between a torch starting something around it on fire. 20 ticks = 1 Second, 1200 ticks = 1 Minute, 72000 ticks = 1 Hour")
                    .translation("burningTorch.config.torch.delayBetweenFire")
                    .defineInRange("torchDelayBetweenFire", 40, 1, Integer.MAX_VALUE);

            torchPlaceLit = builder
                    .comment("Should torches be placed lit?")
                    .translation("burningTorch.config.torch.placeState")
                    .define("torchPlaceLit", true);

            torchMakesSootMark = builder
                    .comment("Should torches when they burn out place soot on the ground/wall to mark where they where?")
                    .translation("burningTorch.config.torch.makesSootMark")
                    .define("torchMakesSootMark", true);

            torchLightDecay5 = builder
                    .comment("How much light does a full torch provide?")
                    .translation("burningTorch.config.torch.lightAtDecay5")
                    .defineInRange("torchLightAtDecay5", 15, 0, 16);

            torchLightDecay4 = builder
                    .comment("How much light does an almost full torch provide?")
                    .translation("burningTorch.config.torch.lightAtDecay4")
                    .defineInRange("torchLightAtDecay4", 15, 0, 16);

            torchLightDecay3 = builder
                    .comment("How much light does a half used torch provide?")
                    .translation("burningTorch.config.torch.lightAtDecay3")
                    .defineInRange("torchLightAtDecay3", 15, 0, 16);

            torchLightDecay2 = builder
                    .comment("How much light does an almost burnt out torch provide?")
                    .translation("burningTorch.config.torch.lightAtDecay2")
                    .defineInRange("torchLightAtDecay2", 15, 0, 16);

            torchLightDecay1 = builder
                    .comment("How much light does a burning out torch provide?")
                    .translation("burningTorch.config.torch.lightAtDecay1")
                    .defineInRange("torchLightAtDecay1", 15, 0, 16);

            builder.pop();


            builder.comment("Pumpkin settings")
                    .push("Pumpkin");

            pumpkinExtendingTags = builder
                    .comment("Item tags that are valid fuel sources for pumpkins to consume. Number is the value for every item in the list. Anything more than 5 is lost.")
                    .translation("burningTorch.config.pumpkin.extendingTags")
                    .defineList("pumpkinExtendingTags", BurningTorchConfig.General.pumpkinExtendingTags(), p -> p instanceof String);

            pumpkinStartingDecayLevel = builder
                    .comment("What decay level does a freshly placed pumpkin start at?")
                    .translation("burningTorch.config.pumpkin.startingDecayLevel")
                    .defineInRange("pumpkinStartingDecayLevel", 5, 1, 5);

            pumpkinDecayRate = builder
                    .comment("How many ticks between decay levels, there are 5 levels total. 20 ticks = 1 Second, 1200 ticks = 1 Minute, 72000 ticks = 1 Hour")
                    .translation("burningTorch.config.pumpkin.decayRate")
                    .defineInRange("pumpkinDecayRate", 18000, 1, Integer.MAX_VALUE);

            pumpkinRainUpdateRate = builder
                    .comment("How quickly an unprotected pumpkin will react to a storm in ticks, Lower values are faster. 20 ticks = 1 Second, 1200 ticks = 1 Minute, 72000 ticks = 1 Hour")
                    .translation("burningTorch.config.pumpkin.rainUpdateRate")
                    .defineInRange("pumpkinRainUpdateRate", 40, 1, Integer.MAX_VALUE);

            pumpkinRainExtinguish = builder
                    .comment("Should rain extinguish unprotected pumpkins?")
                    .translation("burningTorch.config.pumpkin.rainExtinguish")
                    .define("rainExtinguish", false);

            pumpkinBurnoutWarning = builder
                    .comment("Should pumpkins produce lots of smoke before they burn out?")
                    .translation("burningTorch.config.pumpkin.pumpkinBurnoutWarning")
                    .define("pumpkinBurnoutWarning", true);

            pumpkinBurnsEntities = builder
                    .comment("Should lit pumpkins burn entities when they touch it?")
                    .translation("burningTorch.config.pumpkin.burnEntities")
                    .define("pumpkinBurnsEntities", false);

            pumpkinPercentToStartFire = builder
                    .comment("% Chance to start a fire around a pumpkin in a 3x3x3 space centered on the pumpkin. 0 will disable fires being started around pumpkins.")
                    .translation("burningTorch.config.pumpkin.percentToStartFire")
                    .defineInRange("pumpkinPercentToStartFire", 0, 0, 100);

            pumpkinDelayBetweenFire = builder
                    .comment("Delay between a pumpkin starting something around it on fire. 20 ticks = 1 Second, 1200 ticks = 1 Minute, 72000 ticks = 1 Hour")
                    .translation("burningTorch.config.pumpkin.delayBetweenFire")
                    .defineInRange("pumpkinDelayBetweenFire", 40, 1, Integer.MAX_VALUE);

            pumpkinPlaceLit = builder
                    .comment("Should pumpkins be placed lit?")
                    .translation("burningTorch.config.pumpkin.placeState")
                    .define("pumpkinPlaceLit", true);

            pumpkinMakesSootMark = builder
                    .comment("Should pumpkins when they burn out place soot on the ground/wall to mark where they where? Carved pumpkins are favoured over soot placement.")
                    .translation("burningTorch.config.pumpkin.makesSootMark")
                    .define("pumpkinMakesSootMark", false);

            pumpkinLeavesPumpkin = builder
                    .comment("Should pumpkins when they burn out leave a carved pumpkin where it was that can be stuffed with a new item from the #burningtorch:pumpkin_stuffables tag.")
                    .translation("burningTorch.config.pumpkin.leavesPumpkin")
                    .define("pumpkinLeavesPumpkin", true);

            pumpkinLightDecay5 = builder
                    .comment("How much light does a full pumpkin provide?")
                    .translation("burningTorch.config.pumpkin.lightAtDecay5")
                    .defineInRange("pumpkinLightAtDecay5", 15, 0, 16);

            pumpkinLightDecay4 = builder
                    .comment("How much light does an almost full pumpkin provide?")
                    .translation("burningTorch.config.pumpkin.lightAtDecay4")
                    .defineInRange("pumpkinLightAtDecay4", 15, 0, 16);

            pumpkinLightDecay3 = builder
                    .comment("How much light does a half used pumpkin provide?")
                    .translation("burningTorch.config.pumpkin.lightAtDecay3")
                    .defineInRange("pumpkinLightAtDecay3", 15, 0, 16);

            pumpkinLightDecay2 = builder
                    .comment("How much light does an almost burnt out pumpkin provide?")
                    .translation("burningTorch.config.pumpkin.lightAtDecay2")
                    .defineInRange("pumpkinLightAtDecay2", 15, 0, 16);

            pumpkinLightDecay1 = builder
                    .comment("How much light does a burning out pumpkin provide?")
                    .translation("burningTorch.config.pumpkin.lightAtDecay1")
                    .defineInRange("pumpkinLightAtDecay1", 15, 0, 16);

            builder.pop();

            builder.comment("Candle settings")
                    .push("Candles");

            builder.pop();

            builder.comment("Campfire settings")
                    .push("Campfire");

            builder.pop();
        }
    }
}