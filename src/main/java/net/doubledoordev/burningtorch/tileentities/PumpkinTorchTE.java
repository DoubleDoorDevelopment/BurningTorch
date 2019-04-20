package net.doubledoordev.burningtorch.tileentities;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.doubledoordev.burningtorch.ModConfig;
import net.doubledoordev.burningtorch.blocks.BlockBurningPumpkin;

public class PumpkinTorchTE extends TileEntity implements ITickable
{
    int decayLevel = ModConfig.pumkinStartingDecayLevel;
    int decayTimer;

    public PumpkinTorchTE()
    {
        super();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        decayLevel = compound.getInteger("decaylevel");
        decayTimer = compound.getInteger("decayTimer");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setInteger("decaylevel", decayLevel);
        compound.setInteger("decayTimer", decayTimer);
        return compound;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return oldState.getBlock() != newSate.getBlock();
    }

    @Override
    public void update()
    {
        if (this.world.getBlockState(pos).getBlock() == Block.getBlockFromName("burningtorch:burningpumpkin"))
        {
            if (decayLevel > 0 && this.world.getBlockState(pos).getValue(BlockBurningPumpkin.LIT))
            {
                decayTimer++;

                // Timer is measuring in ticks! There are 20 ticks in a second!!!!
                if (decayTimer > ModConfig.decayRate)
                {
                    if (decayLevel > 0)
                    {
                        this.decayLevel = decayLevel - 1;
                        updateBlock();
                        decayTimer = 0;
                    }
                }
            }
            else if (decayLevel == 0)
            {
                this.world.removeTileEntity(pos);
                this.world.setBlockToAir(pos);
                decayTimer = 0;
            }
        }
        else
        {
            this.world.removeTileEntity(pos);
        }
    }

    public int getDecayLevel()
    {
        return this.decayLevel;
    }

    public void setDecayLevel(int decayLevel)
    {
        this.decayLevel = decayLevel;
        updateBlock();
    }

    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(this.pos, 3, this.getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
        readFromNBT(pkt.getNbtCompound());
        updateBlock();
    }

    private void updateBlock()
    {
        this.world.markBlockRangeForRenderUpdate(pos, pos);
        this.world.notifyBlockUpdate(pos, this.world.getBlockState(pos), this.world.getBlockState(pos), 0);
        this.world.scheduleBlockUpdate(pos, this.getBlockType(), 0, 0);
        markDirty();
    }
}
