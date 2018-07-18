package net.doubledoordev.TileEntities;

import net.doubledoordev.Blocks.BurningTorchBase;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static net.doubledoordev.BurningTorch.MOD_ID;

public class TorchTE extends TileEntity implements ITickable
{
    int level = 5;
    int rainTimer = 0;
    int decayTimer = 0;

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        level = compound.getInteger("decaylevel");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("decaylevel", this.world.getBlockState(pos).getValue(BurningTorchBase.DECAY));
        return compound;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        if (this.world.getBlockState(pos).getBlock() != Block.getBlockFromName(MOD_ID + ":burningtorch"))
        {
            return true;
        }
        return false;
    }

    @Override
    public void update()
    {
        if (this.world.getBlockState(pos).getBlock() == Block.getBlockFromName(MOD_ID + ":burningtorch"))
        {
            int decayLevel = this.world.getBlockState(pos).getValue(BurningTorchBase.DECAY);

            if (decayLevel > 0)
            {
                rainTimer++;
                decayTimer++;

                // Timer is measuring in ticks! There are 20 ticks in a second!!!!
                if (rainTimer > 40)
                {
                    if (this.world.isRaining() && this.world.canBlockSeeSky(pos))
                    {
                        this.world.setBlockState(pos, Block.getBlockFromName(MOD_ID + ":burningtorch").getDefaultState()
                                .withProperty(BurningTorchBase.LIT, false)
                                .withProperty(BurningTorchBase.DIRECTION, this.world.getBlockState(pos).getValue(BurningTorchBase.DIRECTION))
                                .withProperty(BurningTorchBase.DECAY, this.world.getBlockState(pos).getValue(BurningTorchBase.DECAY)));

                        markDirty();
                        rainTimer = 0;
                    }
                }

                // Timer is measuring in ticks! There are 20 ticks in a second!!!!
                if (decayTimer > 200)
                {
                    if (this.world.getBlockState(pos).getValue(BurningTorchBase.LIT) && level > 0)
                    {

                        this.world.setBlockState(pos, Block.getBlockFromName(MOD_ID + ":burningtorch").getDefaultState()
                                .withProperty(BurningTorchBase.LIT, this.world.getBlockState(pos).getValue(BurningTorchBase.LIT))
                                .withProperty(BurningTorchBase.DIRECTION, this.world.getBlockState(pos).getValue(BurningTorchBase.DIRECTION))
                                .withProperty(BurningTorchBase.DECAY, --decayLevel));

                        markDirty();
                        decayTimer = 0;
                    }
                }
            }
            else
            {
                this.world.removeTileEntity(pos);
                this.world.setBlockToAir(pos);
                decayTimer = 0;
            }
        }
    }
}

