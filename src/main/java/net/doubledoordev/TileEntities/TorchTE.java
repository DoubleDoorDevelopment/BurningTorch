package net.doubledoordev.TileEntities;

import net.doubledoordev.Blocks.BurningTorchBase;
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

import static net.doubledoordev.BurningTorch.MOD_ID;

public class TorchTE extends TileEntity implements ITickable
{
    int decayLevel = 5;
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
        System.out.print("\nReading: "+ compound+"\n");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setInteger("decaylevel", decayLevel); //this.world.getBlockState(pos).getValue(BurningTorchBase.DECAY));
        System.out.print("\nWriting: "+ compound+"\n");
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
        if (this.world.getBlockState(pos).getBlock() == Block.getBlockFromName(MOD_ID + ":burningtorch"))
        {
            int decayLevelState = this.world.getBlockState(pos).getValue(BurningTorchBase.DECAY);

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

                        updateBlock();
                        rainTimer = 0;
                    }
                }

                // Timer is measuring in ticks! There are 20 ticks in a second!!!!
                if (decayTimer > 200)
                {
                    if (this.world.getBlockState(pos).getValue(BurningTorchBase.LIT) && decayLevel > 0)
                    {
                        /* this.world.setBlockState(pos, Block.getBlockFromName(MOD_ID + ":burningtorch").getDefaultState()
                                .withProperty(BurningTorchBase.LIT, this.world.getBlockState(pos).getValue(BurningTorchBase.LIT))
                                .withProperty(BurningTorchBase.DIRECTION, this.world.getBlockState(pos).getValue(BurningTorchBase.DIRECTION))
                                .withProperty(BurningTorchBase.DECAY, --decayLevelState));
                                */
                        this.decayLevel = decayLevel-1;


                        updateBlock();
                        decayTimer = 0;
                    }
                }
            }
            else
            {
                updateBlock();
                this.world.removeTileEntity(pos);
                this.world.setBlockToAir(pos);
                decayTimer = 0;
            }
        }
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
        super.onDataPacket(net, pkt);
        handleUpdateTag(pkt.getNbtCompound());
    }

    private void updateBlock()
    {
        this.world.markBlockRangeForRenderUpdate(pos, pos);
        this.world.notifyBlockUpdate(pos, this.world.getBlockState(pos), this.world.getBlockState(pos), 0);
        this.world.scheduleBlockUpdate(pos, this.getBlockType(),0,0);
        markDirty();
    }

}

