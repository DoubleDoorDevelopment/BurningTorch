package net.doubledoordev.burningtorch.util;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeConfigSpec;

public class Util
{
    public static final IntegerProperty DECAY = IntegerProperty.create("decay", 0, 5);
    public static final VoxelShape BUMP_INTO_BLOCK = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D);

    public static Vec3 blockPosToVec3(BlockPos pos)
    {
        return new Vec3(pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean isSurroundingBlockFlammable(Level level, BlockPos pos)
    {
        // Guard against replacing blocks that aren't burnable being removed due to them having something burnable around them.
        if (getCanBlockBurn(level, pos))
            for (Direction facing : Direction.values())
            {
                if (getCanBlockBurn(level, pos.offset(facing.getNormal())))
                {
                    return true;
                }
            }
        return false;
    }

    @SuppressWarnings("deprecation")
    public static boolean getCanBlockBurn(Level level, BlockPos pos)
    {
        return (level.isInWorldBounds(pos) || level.isAreaLoaded(pos, 1)) && level.getBlockState(pos).getMaterial().isFlammable() && !level.isWaterAt(pos);
    }

    public static boolean holdingValidItem(TagKey<Item> itemTag, Player player, InteractionHand interactionHand)
    {
        if (interactionHand == InteractionHand.MAIN_HAND)
            return player.getMainHandItem().is(itemTag);
        else return player.getOffhandItem().is(itemTag);
    }

    public static boolean extinguishBurningSource(Player player, Level level, BlockPos pos, InteractionHand interactionHand)
    {
        if (holdingValidItem(TagKeys.EXTINGUISH_ITEMS, player, interactionHand))
        {
            level.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.3F, 0.8F);
            level.setBlockAndUpdate(pos, level.getBlockState(pos).setValue(BlockStateProperties.LIT, false));
            return true;
        }
        return false;
    }

    public static boolean igniteBurningSource(Player player, Level level, BlockPos pos, InteractionHand interactionHand)
    {
        BlockState state = level.getBlockState(pos);

        // Check if the player is holding an item to light the object.
        if (holdingValidItem(TagKeys.RELIGHT_ITEMS, player, interactionHand))
        {
            // Handle water logged blocks separate as you can light them after they are waterlogged otherwise.
            if (state.hasProperty(BlockStateProperties.WATERLOGGED))
            {
                if (!state.getValue(BlockStateProperties.WATERLOGGED))
                {
                    level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 0.3F, 0.8F);
                    level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.LIT, true));
                }
                else return false;
            }

            // Handle everything else.
            level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 0.3F, 0.8F);
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.LIT, true));
            return true;
        }
        return false;
    }

    public static boolean trimBurningSource(Player player, Level level, BlockPos pos, BlockState state, InteractionHand interactionHand)
    {
        if (holdingValidItem(TagKeys.CUTTING_ITEMS, player, interactionHand))
        {
            // Need to do this on it's own so we can feed a reply if the object is too low.
            if (state.getValue(DECAY) > 1)
            {
                level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 0.2F, 0.8F);
                level.setBlockAndUpdate(pos, level.getBlockState(pos).setValue(DECAY, state.getValue(DECAY) - 1));
                return true;
            }
            else player.displayClientMessage(new TranslatableComponent("burningtorch.interact.shears.low"), true);
        }
        return false;
    }

    public static boolean refuelBurningSource(ForgeConfigSpec.ConfigValue<List<? extends String>> fuelTags, Player player, Level level, BlockPos pos, BlockState state, InteractionHand interactionHand)
    {
        for (String itemValue : fuelTags.get())
        {
            String[] splitTagFromValue = itemValue.split(",");

            TagKey<Item> fuelTag = ItemTags.create(new ResourceLocation(splitTagFromValue[0]));
            int fuelValue = Integer.parseInt(splitTagFromValue[1]);

            if (holdingValidItem(fuelTag, player, interactionHand) && state.getValue(DECAY) < 5)
            {
                if (level.getBlockState(pos).getValue(DECAY) + fuelValue > 5)
                {
                    level.playSound(null, pos, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.BLOCKS, 0.3F, 0.8F);
                    level.setBlockAndUpdate(pos, level.getBlockState(pos).setValue(DECAY, 5));
                }
                else
                {
                    level.setBlockAndUpdate(pos, level.getBlockState(pos).setValue(DECAY, state.getValue(DECAY) + fuelValue));
                }
                if (!player.isCreative())
                {
                    player.getMainHandItem().setCount(player.getMainHandItem().getCount() - 1);
                }
                return true;
            }
        }
        return false;
    }
}
