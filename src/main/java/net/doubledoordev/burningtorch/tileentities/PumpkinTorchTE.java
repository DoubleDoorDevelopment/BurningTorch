package net.doubledoordev.burningtorch.tileentities;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
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
    public void load(BlockState state, CompoundNBT compound)
    {
        super.load(state, compound);
        decayLevel = compound.getInt("decaylevel");
        decayTimer = compound.getInt("decayTimer");
    }

    @Override
    public CompoundNBT save(CompoundNBT compound)
    {
        compound.putInt("decaylevel", decayLevel);
        compound.putInt("decayTimer", decayTimer);
        return super.save(compound);
    }

    @Override
    @Nullable
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        return new SUpdateTileEntityPacket(this.worldPosition, 3, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        return this.save(new CompoundNBT());
    }

    public int getDecayLevel()
    {
        return this.decayLevel;
    }

    public void setDecayLevel(int decayLevel)
    {
        this.decayLevel = decayLevel;
        this.level.setBlockAndUpdate(worldPosition, level.getBlockState(worldPosition).setValue(BurningPumpkinBlock.DECAY, decayLevel));
    }

    @Override
    public void tick()
    {
        if (this.level.getBlockState(worldPosition).getBlock() == BlockHolder.burningpumpkin.getBlock())
        {
            if (decayLevel > 0 && this.level.getBlockState(worldPosition).getValue(BurningPumpkinBlock.LIT))
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
                this.level.removeBlockEntity(worldPosition);
                this.level.removeBlock(worldPosition, false);
                decayTimer = 0;
            }
        }
        else
        {
            this.level.removeBlockEntity(worldPosition);
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
    {
        deserializeNBT(pkt.getTag());
        updateBlock();
    }

    private void updateBlock()
    {
        setChanged();
    }
}
