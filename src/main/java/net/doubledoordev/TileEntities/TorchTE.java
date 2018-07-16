package net.doubledoordev.TileEntities;

import net.doubledoordev.Blocks.BurningTorchBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

public class TorchTE extends TileEntity implements ITickable
{
    int decaylevel = 5;
    boolean permanent = false;
    int timer = 0;

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        decaylevel = compound.getInteger("decaylevel");
        permanent = compound.getBoolean("permanent");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("decaylevel", decaylevel);
        compound.setBoolean("permanent", permanent);
        return compound;
    }

    @Override
    public void update()
    {
        timer++;

        if (timer > 40)
        {

            BurningTorchBase.setState(this.world, this.pos);
            markDirty();
        }

    }


}
