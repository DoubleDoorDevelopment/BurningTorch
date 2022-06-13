package net.doubledoordev.burningtorch.blocks.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import net.doubledoordev.burningtorch.blocks.BlockRegistry;

public class BurningPumpkinBlockEntity extends BurningLightBlockEntity
{
    public BurningPumpkinBlockEntity(BlockPos pos, BlockState state)
    {
        super(BlockRegistry.BURNING_PUMPKIN_LIGHT_BLOCK_ENTITY, pos, state);
    }
}
