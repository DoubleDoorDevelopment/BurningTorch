package net.doubledoordev.items;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static net.doubledoordev.BurningTorch.MOD_ID;

public class ItemCharredTorchRemains extends Item
{
    public ItemCharredTorchRemains()
    {
        setCreativeTab(CreativeTabs.MATERIALS);
        setUnlocalizedName("charredtorchremains");
        setRegistryName(MOD_ID, "charredtorchremains");
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
