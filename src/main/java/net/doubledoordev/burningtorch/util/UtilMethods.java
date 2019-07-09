package net.doubledoordev.burningtorch.util;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UtilMethods
{
    public static boolean isSurroundingBlockFlammable(World worldIn, BlockPos pos)
    {
        for (Direction facing : Direction.values())
        {
            if (getCanBlockBurn(worldIn, pos.offset(facing)))
            {
                return true;
            }
        }

        return false;
    }

    public static boolean getCanBlockBurn(World worldIn, BlockPos pos)
    {
        return (pos.getY() < 0 || pos.getY() >= 256 || worldIn.isAreaLoaded(pos, 1)) && worldIn.getBlockState(pos).getMaterial().isFlammable() && !worldIn.hasWater(pos);
    }
}
