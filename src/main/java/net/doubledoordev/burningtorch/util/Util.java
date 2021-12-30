package net.doubledoordev.burningtorch.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

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
}
