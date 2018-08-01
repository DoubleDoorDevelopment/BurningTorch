package net.doubledoordev.TileEntities;

import net.doubledoordev.Blocks.BurningTorchBase;
import net.doubledoordev.ModConfig;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Map;

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
            if (decayLevel > 0 && this.world.getBlockState(pos).getValue(BurningTorchBase.LIT))
            {
                rainTimer++;
                decayTimer++;

                // Timer is measuring in ticks! There are 20 ticks in a second!!!!
                if (rainTimer > ModConfig.rainUpdateRate && ModConfig.shouldRainExtinguish)
                {
                    if (this.world.isRaining() && this.world.canBlockSeeSky(pos))
                    {
                        this.world.setBlockState(pos, world.getBlockState(pos)
                                .withProperty(BurningTorchBase.LIT, false));
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

    public NonNullList getDrops()
    {
        NonNullList<ItemStack> drops = NonNullList.create();
        switch (this.getDecayLevel())
        {
            case 5:
                for (Map.Entry<String, Integer> entry : ModConfig.drops.entrySet())
                {
                    Item item = Item.getByNameOrId(entry.getKey());
                    drops.add(new ItemStack(item, entry.getValue()));
                }
                return drops;
            case 4:
                for (Map.Entry<String, Integer> entry : ModConfig.drops.entrySet())
                {
                    Item item = Item.getByNameOrId(entry.getKey());
                    int quantity = entry.getValue();
                    if (entry.getValue() > 9)
                    {
                        quantity = entry.getValue() - 3;
                    }
                    drops.add(new ItemStack(item, quantity));
                }
                return drops;
            case 3:
                for (Map.Entry<String, Integer> entry : ModConfig.drops.entrySet())
                {
                    Item item = Item.getByNameOrId(entry.getKey());
                    int quantity = entry.getValue();
                    if (entry.getValue() > 6)
                    {
                        quantity = entry.getValue() - 2;
                    }
                    drops.add(new ItemStack(item, quantity));
                }
                return drops;
            case 2:
                for (Map.Entry<String, Integer> entry : ModConfig.drops.entrySet())
                {
                    Item item = Item.getByNameOrId(entry.getKey());
                    int quantity = entry.getValue();
                    if (entry.getValue() > 3)
                    {
                        quantity = entry.getValue() - 1;
                    }
                    drops.add(new ItemStack(item, quantity));
                }
                return drops;
            case 1:
                for (Map.Entry<String, Integer> entry : ModConfig.drops.entrySet())
                {
                    Item item = Item.getByNameOrId(entry.getKey());
                    int quantity = entry.getValue();
                    if (entry.getValue() > 1)
                    {
                        quantity = entry.getValue() - 1;
                    }
                    drops.add(new ItemStack(item, quantity));
                }
                return drops;
        }
        return drops;
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

