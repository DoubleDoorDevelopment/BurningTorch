package net.doubledoordev.burningtorch.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import net.doubledoordev.burningtorch.blocks.blockentities.BurningLightBlockEntity;

public interface SimpleBurningBlock
{
    default void doLifeCycleTick(Level level, BlockPos pos, BlockState state, BurningLightBlockEntity burningLightBlockEntity)
    {
    }

    default BlockState getExpiredBlockStateReplacement()
    {
        return BlockRegistry.SOOT_MARK.get().defaultBlockState();
    }
}
