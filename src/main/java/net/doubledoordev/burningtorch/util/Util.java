package net.doubledoordev.burningtorch.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import net.doubledoordev.burningtorch.BurningTorch;
import net.doubledoordev.burningtorch.BurningTorchConfig;

public class Util
{
    public static final IntegerProperty DECAY = IntegerProperty.create("decay", 0, 5);

    public static boolean isSurroundingBlockFlammable(Level level, BlockPos pos)
    {
        for (Direction facing : Direction.values())
        {
            if (getCanBlockBurn(level, pos.offset(facing.getNormal())))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean getCanBlockBurn(Level level, BlockPos pos)
    {
        return (pos.getY() < 0 || pos.getY() >= 256 || level.isAreaLoaded(pos, 1)) && level.getBlockState(pos).getMaterial().isFlammable() && !level.isWaterAt(pos);
    }

    public static boolean holdingValidItem(Tag<Item> itemTag, Player player, InteractionHand interactionHand)
    {
        if (interactionHand == InteractionHand.MAIN_HAND)
            return itemTag.contains(player.getMainHandItem().getItem());
        else return itemTag.contains(player.getOffhandItem().getItem());
    }

    public static boolean extinguishBurningSource(Player player, Level level, BlockPos pos, InteractionHand interactionHand)
    {
        if (holdingValidItem(ItemTags.getAllTags().getTagOrEmpty(new ResourceLocation(BurningTorch.MODID, "extinguish_items")), player, interactionHand))
        {
            level.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.3F, 0.8F);
            level.setBlockAndUpdate(pos, level.getBlockState(pos).setValue(BlockStateProperties.LIT, false));
            return true;
        }
        return false;
    }

    public static boolean igniteBurningSource(Player player, Level level, BlockPos pos, InteractionHand interactionHand)
    {
        if (holdingValidItem(ItemTags.getAllTags().getTagOrEmpty(new ResourceLocation(BurningTorch.MODID, "relight_items")), player, interactionHand))
        {
            level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 0.3F, 0.8F);
            level.setBlockAndUpdate(pos, level.getBlockState(pos).setValue(BlockStateProperties.LIT, true));
            return true;
        }
        return false;
    }

    public static boolean trimBurningSource(Player player, Level level, BlockPos pos, BlockState state, InteractionHand interactionHand)
    {
        if (holdingValidItem(ItemTags.getAllTags().getTagOrEmpty(new ResourceLocation(BurningTorch.MODID, "cutting_items")), player, interactionHand))
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

    public static boolean refuelBurningSource(Player player, Level level, BlockPos pos, BlockState state, InteractionHand interactionHand)
    {
        for (String itemValue : BurningTorchConfig.GENERAL.extendingItems.get())
        {
            String[] splitTagFromValue = itemValue.split(",");

            Tag<Item> fuelTag = ItemTags.getAllTags().getTagOrEmpty(new ResourceLocation(splitTagFromValue[0]));
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
