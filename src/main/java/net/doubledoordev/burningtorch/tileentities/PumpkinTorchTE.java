package net.doubledoordev.burningtorch.tileentities;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

import net.doubledoordev.burningtorch.BurningTorchConfig;
import net.doubledoordev.burningtorch.blocks.BlockHolder;
import net.doubledoordev.burningtorch.blocks.BurningPumpkinBlock;

public class PumpkinTorchTE extends TileEntity implements ITickableTileEntity
{
    int decayLevel = BurningTorchConfig.GENERAL.pumpkinStartingDecayLevel.get();
    int decayTimer;

    private PumpkinTorchTE(TileEntityType<?> p_i49963_1_)
    {
        super(p_i49963_1_);
    }

    public PumpkinTorchTE()
    {
        this(TEHolder.pumpkintorchte);
    }

    @Override
    public void read(CompoundNBT compound)
    {
        super.read(compound);
        decayLevel = compound.getInt("decaylevel");
        decayTimer = compound.getInt("decayTimer");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
        compound.putInt("decaylevel", decayLevel);
        compound.putInt("decayTimer", decayTimer);
        return super.write(compound);
    }

    @Override
    @Nullable
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        return new SUpdateTileEntityPacket(this.pos, 3, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        return this.write(new CompoundNBT());
    }

    public int getDecayLevel()
    {
        return this.decayLevel;
    }

    public void setDecayLevel(int decayLevel)
    {
        this.decayLevel = decayLevel;
        this.world.setBlockState(pos, world.getBlockState(pos).with(BurningPumpkinBlock.DECAY, decayLevel));
    }

    @Override
    public void tick()
    {
        if (this.world.getBlockState(pos).getBlock() == BlockHolder.burningpumpkin.getBlock())
        {
            if (decayLevel > 0 && this.world.getBlockState(pos).get(BurningPumpkinBlock.LIT))
            {
                decayTimer++;

                // Timer is measuring in ticks! There are 20 ticks in a second!!!!
                if (decayTimer > BurningTorchConfig.GENERAL.decayRate.get())
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
                this.world.removeBlock(pos, false);
                decayTimer = 0;
            }
        }
        else
        {
            this.world.removeTileEntity(pos);
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
    {
        deserializeNBT(pkt.getNbtCompound());
        updateBlock();
    }

    private void updateBlock()
    {
        //this.world.markForRerender(pos);
        markDirty();
    }
}
