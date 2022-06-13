package net.doubledoordev.burningtorch.events;

import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.doubledoordev.burningtorch.blocks.BlockRegistry;
import net.doubledoordev.burningtorch.util.TagKeys;
import net.doubledoordev.burningtorch.util.Util;

public class PlaceLightInPumpkin
{
    @SubscribeEvent
    public static void interactEvent(PlayerInteractEvent.RightClickBlock event)
    {
        if (event.getSide().isServer())
        {
            Level level = event.getWorld();
            BlockPos targetPos = event.getPos();
            BlockState targetBlock = level.getBlockState(targetPos);
            Player player = event.getPlayer();
            InteractionHand handUsed = event.getHand();

            if (targetBlock.getBlock().equals(Blocks.CARVED_PUMPKIN) && Util.holdingValidItem(TagKeys.PUMPKIN_STUFFABLE, player, handUsed))
            {
                level.setBlockAndUpdate(targetPos, Objects.requireNonNull(BlockRegistry.BURNING_PUMPKIN.get().getStateForPlacement(new BlockPlaceContext(player, handUsed, player.getItemInHand(handUsed), event.getHitVec()))));
            }
        }
    }

}
