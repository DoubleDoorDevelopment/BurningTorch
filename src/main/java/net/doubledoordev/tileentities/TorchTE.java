package net.doubledoordev.tileentities;

import net.doubledoordev.ModConfig;
import net.doubledoordev.blocks.BlockBurningTorch;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class TorchTE extends TileEntity implements ITickable
{
    int decayLevel = ModConfig.startingDecayLevel;
    int rainTimer;
    int decayTimer;

    public TorchTE()
    {
        super();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        decayLevel = compound.getInteger("decaylevel");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setInteger("decaylevel", decayLevel);
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
        if (this.world.getBlockState(pos).getBlock() == Block.getBlockFromName("burningtorch:burningtorch"))
        {
            if (decayLevel > 0 && this.world.getBlockState(pos).getValue(BlockBurningTorch.LIT))
            {
                rainTimer++;
                decayTimer++;

                // Timer is measuring in ticks! There are 20 ticks in a second!!!!
                if (rainTimer > ModConfig.rainUpdateRate && ModConfig.shouldRainExtinguish)
                {
                    if (this.world.isRaining() && this.world.canBlockSeeSky(pos))
                    {
                        this.world.setBlockState(pos, world.getBlockState(pos)
                                .withProperty(BlockBurningTorch.LIT, false));
                        updateBlock();
                        rainTimer = 0;
                    }
                }

                // Timer is measuring in ticks! There are 20 ticks in a second!!!!
                if (decayTimer > ModConfig.decayRate)
                {
                    if (/*this.world.getBlockState(pos).getValue(BurningTorchBase.LIT) && */ decayLevel > 0)
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
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 3, this.getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
        updateBlock();
    }

    private void updateBlock()
    {
        this.world.markBlockRangeForRenderUpdate(pos, pos);
        this.world.notifyBlockUpdate(pos, this.world.getBlockState(pos), this.world.getBlockState(pos), 0);
        this.world.scheduleBlockUpdate(pos, this.getBlockType(),0,0);
        markDirty();
    }

}

