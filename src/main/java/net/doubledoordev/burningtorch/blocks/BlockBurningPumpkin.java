package net.doubledoordev.burningtorch.blocks;

import java.util.Map;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.doubledoordev.burningtorch.ModConfig;
import net.doubledoordev.burningtorch.tileentities.PumpkinTorchTE;

import static net.doubledoordev.burningtorch.BurningTorch.MOD_ID;
import static net.doubledoordev.burningtorch.ModConfig.placeLitPumpkins;

public class BlockBurningPumpkin extends Block
{
    public static final PropertyInteger DECAY = PropertyInteger.create("decay", 0, 5);
    public static final PropertyBool LIT = PropertyBool.create("lit");
    public static final PropertyDirection DIRECTION = PropertyDirection.create("direction");

    public BlockBurningPumpkin(Material materialIn)
    {
        super(materialIn);
        if (placeLitPumpkins)
            setDefaultState(this.getDefaultState().withProperty(LIT, true).withProperty(DIRECTION, EnumFacing.UP).withProperty(DECAY, 5));
        else
            setDefaultState(this.getDefaultState().withProperty(LIT, false).withProperty(DIRECTION, EnumFacing.UP).withProperty(DECAY, 5));
        setLightLevel(0.9375f);
        setCreativeTab(CreativeTabs.DECORATIONS);
        if (placeLitPumpkins)
            setUnlocalizedName("burningpumpkin");
        else
            setUnlocalizedName("burningpumpkinextinguished");
        setRegistryName(MOD_ID, "burningpumpkin");
    }

    // Handles the relighting of torches and a bunch of other stuff.
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        PumpkinTorchTE pumpkinTE = (PumpkinTorchTE) worldIn.getTileEntity(pos);

        for (String item : ModConfig.relightingItems)
        {
            if (playerIn.getHeldItemMainhand().getItem().getRegistryName().toString().equals(item) || playerIn.getHeldItemOffhand().getItem().getRegistryName().toString().equals(item) && !state.getValue(LIT))
            {
                worldIn.setBlockState(pos, worldIn.getBlockState(pos).withProperty(LIT, true));
                worldIn.playSound(playerIn, pos, new SoundEvent(new ResourceLocation("item.flintandsteel.use")), SoundCategory.BLOCKS, 0.3F, 0.8F);
                return true;
            }
        }

        for (String item : ModConfig.extinguishingingItems)
        {
            if (playerIn.getHeldItemMainhand().getItem().getRegistryName().toString().equals(item) || playerIn.getHeldItemOffhand().getItem().getRegistryName().toString().equals(item) && state.getValue(LIT))
            {
                worldIn.setBlockState(pos, worldIn.getBlockState(pos).withProperty(LIT, false));
                worldIn.playSound(playerIn, pos, new SoundEvent(new ResourceLocation("block.redstone_torch.burnout")), SoundCategory.BLOCKS, 0.2F, 0.8F);
                return true;
            }
        }

        for (Map.Entry<String, Integer> entry : ModConfig.extendingingItems.entrySet())
        {
            if (playerIn.getHeldItemMainhand().getItem().getRegistryName().toString().equals(entry.getKey()) /*|| playerIn.getHeldItemOffhand().getItem().getRegistryName().toString().equals(entry.getKey()) */ && pumpkinTE.getDecayLevel() < 5)
            {
                if (worldIn.getBlockState(pos).getBlock().getActualState(state, worldIn, pos).getValue(DECAY) + entry.getValue() > 5)
                {
                    pumpkinTE.setDecayLevel(5);
                    playerIn.getHeldItemMainhand().setCount(playerIn.getHeldItemMainhand().getCount() - 1);
                    worldIn.playSound(playerIn, pos, new SoundEvent(new ResourceLocation("block.redstone_torch.burnout")), SoundCategory.BLOCKS, 0.2F, 0.8F);
                    return true;
                }
                else
                {
                    pumpkinTE.setDecayLevel(worldIn.getBlockState(pos).getBlock().getActualState(state, worldIn, pos).getValue(DECAY) + entry.getValue());
                    playerIn.getHeldItemMainhand().setCount(playerIn.getHeldItemMainhand().getCount() - 1);
                    worldIn.playSound(playerIn, pos, new SoundEvent(new ResourceLocation("block.redstone_torch.burnout")), SoundCategory.BLOCKS, 0.2F, 0.8F);
                    return true;
                }
            }
        }

        if (playerIn.getHeldItemMainhand().getItem() == Items.SHEARS)
        {
            worldIn.playSound(playerIn, pos, new SoundEvent(new ResourceLocation("entity.sheep.shear")), SoundCategory.BLOCKS, 0.2F, 0.8F);
            pumpkinTE.setDecayLevel(worldIn.getBlockState(pos).getBlock().getActualState(state, worldIn, pos).getValue(DECAY) - 1);
            return true;
        }

        return false;
    }

    @SideOnly(Side.CLIENT)
    public void initModel()
    {
        if (placeLitPumpkins)
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
        else
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName() + "_extinguished", "inventory"));
    }

    // Does stuff, Check the super...
    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, DIRECTION, LIT, DECAY);
    }

    // Changes the lighting level based off the LIT blockstate property.
    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        int decayLevel;
        if (world.getTileEntity(pos) instanceof PumpkinTorchTE)
        {
            PumpkinTorchTE torchTE = (PumpkinTorchTE) world.getTileEntity(pos);
            decayLevel = torchTE.getDecayLevel();
        }
        else
        {
            decayLevel = 5;
        }

        if (!state.getValue(LIT))
        {
            return ModConfig.pumpkinLightLevelUnlit;
        }
        switch (decayLevel)
        {
            case 5:
                return ModConfig.pumpkinLightLevel5;
            case 4:
                return ModConfig.pumpkinLightLevel4;
            case 3:
                return ModConfig.pumpkinLightLevel3;
            case 2:
                return ModConfig.pumpkinLightLevel2;
            case 1:
                return ModConfig.pumpkinLightLevel1;
        }
        return 14;
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand)
    {
        return this.getDefaultState().withProperty(DIRECTION, placer.getHorizontalFacing().getOpposite());
    }

    // Part of attaching the TE to the block.
    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new PumpkinTorchTE();
    }

    // Part of attaching the TE to the block.
    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        TileEntity tileentity = worldIn instanceof ChunkCache ? ((ChunkCache) worldIn).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK) : worldIn.getTileEntity(pos);
        int decayLevel;
        if (tileentity instanceof PumpkinTorchTE)
        {
            PumpkinTorchTE torchTE = (PumpkinTorchTE) tileentity;
            decayLevel = torchTE.getDecayLevel();
        }
        else
        {
            decayLevel = 0;
        }
        IBlockState blockState = state;

        if (worldIn.getBlockState(pos).getBlock() == this)
        {
            if (state.getValue(LIT))
            {
                switch (decayLevel)
                {
                    case 5:
                        switch (state.getValue(DIRECTION))
                        {
                            case NORTH:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, true).withProperty(DECAY, 5);
                                break;
                            case EAST:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.EAST).withProperty(LIT, true).withProperty(DECAY, 5);
                                break;
                            case WEST:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.WEST).withProperty(LIT, true).withProperty(DECAY, 5);
                                break;
                            case SOUTH:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.SOUTH).withProperty(LIT, true).withProperty(DECAY, 5);
                                break;
                            case UP:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.UP).withProperty(LIT, true).withProperty(DECAY, 5);
                                break;
                            case DOWN:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.DOWN).withProperty(LIT, true).withProperty(DECAY, 5);
                                break;
                            default:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, true).withProperty(DECAY, 5);
                                break;
                        }
                        break;

                    case 4:
                        switch (state.getValue(DIRECTION))
                        {
                            case NORTH:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, true).withProperty(DECAY, 4);
                                break;
                            case EAST:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.EAST).withProperty(LIT, true).withProperty(DECAY, 4);
                                break;
                            case WEST:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.WEST).withProperty(LIT, true).withProperty(DECAY, 4);
                                break;
                            case SOUTH:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.SOUTH).withProperty(LIT, true).withProperty(DECAY, 4);
                                break;
                            case UP:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.UP).withProperty(LIT, true).withProperty(DECAY, 4);
                                break;
                            case DOWN:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.DOWN).withProperty(LIT, true).withProperty(DECAY, 4);
                                break;
                            default:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, true).withProperty(DECAY, 4);
                                break;
                        }
                        break;

                    case 3:
                        switch (state.getValue(DIRECTION))
                        {
                            case NORTH:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, true).withProperty(DECAY, 3);
                                break;
                            case EAST:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.EAST).withProperty(LIT, true).withProperty(DECAY, 3);
                                break;
                            case WEST:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.WEST).withProperty(LIT, true).withProperty(DECAY, 3);
                                break;
                            case SOUTH:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.SOUTH).withProperty(LIT, true).withProperty(DECAY, 3);
                                break;
                            case UP:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.UP).withProperty(LIT, true).withProperty(DECAY, 3);
                                break;
                            case DOWN:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.DOWN).withProperty(LIT, true).withProperty(DECAY, 3);
                                break;
                            default:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, true).withProperty(DECAY, 3);
                                break;
                        }
                        break;

                    case 2:
                        switch (state.getValue(DIRECTION))
                        {
                            case NORTH:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, true).withProperty(DECAY, 2);
                                break;
                            case EAST:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.EAST).withProperty(LIT, true).withProperty(DECAY, 2);
                                break;
                            case WEST:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.WEST).withProperty(LIT, true).withProperty(DECAY, 2);
                                break;
                            case SOUTH:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.SOUTH).withProperty(LIT, true).withProperty(DECAY, 2);
                                break;
                            case UP:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.UP).withProperty(LIT, true).withProperty(DECAY, 2);
                                break;
                            case DOWN:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.DOWN).withProperty(LIT, true).withProperty(DECAY, 2);
                                break;
                            default:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, true).withProperty(DECAY, 2);
                                break;
                        }
                        break;

                    case 1:
                        switch (state.getValue(DIRECTION))
                        {
                            case NORTH:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, true).withProperty(DECAY, 1);
                                break;
                            case EAST:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.EAST).withProperty(LIT, true).withProperty(DECAY, 1);
                                break;
                            case WEST:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.WEST).withProperty(LIT, true).withProperty(DECAY, 1);
                                break;
                            case SOUTH:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.SOUTH).withProperty(LIT, true).withProperty(DECAY, 1);
                                break;
                            case UP:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.UP).withProperty(LIT, true).withProperty(DECAY, 1);
                                break;
                            case DOWN:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.DOWN).withProperty(LIT, true).withProperty(DECAY, 1);
                                break;
                            default:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, true).withProperty(DECAY, 1);
                                break;
                        }
                        break;

                    case 0:
                        switch (state.getValue(DIRECTION))
                        {
                            case NORTH:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, true).withProperty(DECAY, 0);
                                break;
                            case EAST:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.EAST).withProperty(LIT, true).withProperty(DECAY, 0);
                                break;
                            case WEST:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.WEST).withProperty(LIT, true).withProperty(DECAY, 0);
                                break;
                            case SOUTH:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.SOUTH).withProperty(LIT, true).withProperty(DECAY, 0);
                                break;
                            case UP:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.UP).withProperty(LIT, true).withProperty(DECAY, 0);
                                break;
                            case DOWN:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.DOWN).withProperty(LIT, true).withProperty(DECAY, 0);
                                break;
                            default:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, true).withProperty(DECAY, 0);
                                break;
                        }
                        break;
                }
            }
            else
                switch (decayLevel)
                {
                    case 5:
                        switch (state.getValue(DIRECTION))
                        {
                            case NORTH:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, false).withProperty(DECAY, 5);
                                break;
                            case EAST:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.EAST).withProperty(LIT, false).withProperty(DECAY, 5);
                                break;
                            case WEST:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.WEST).withProperty(LIT, false).withProperty(DECAY, 5);
                                break;
                            case SOUTH:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.SOUTH).withProperty(LIT, false).withProperty(DECAY, 5);
                                break;
                            case UP:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.UP).withProperty(LIT, false).withProperty(DECAY, 5);
                                break;
                            case DOWN:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.DOWN).withProperty(LIT, false).withProperty(DECAY, 5);
                                break;
                            default:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, false).withProperty(DECAY, 5);
                                break;
                        }
                        break;

                    case 4:
                        switch (state.getValue(DIRECTION))
                        {
                            case NORTH:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, false).withProperty(DECAY, 4);
                                break;
                            case EAST:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.EAST).withProperty(LIT, false).withProperty(DECAY, 4);
                                break;
                            case WEST:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.WEST).withProperty(LIT, false).withProperty(DECAY, 4);
                                break;
                            case SOUTH:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.SOUTH).withProperty(LIT, false).withProperty(DECAY, 4);
                                break;
                            case UP:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.UP).withProperty(LIT, false).withProperty(DECAY, 4);
                                break;
                            case DOWN:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.DOWN).withProperty(LIT, false).withProperty(DECAY, 4);
                                break;
                            default:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, false).withProperty(DECAY, 4);
                                break;
                        }
                        break;

                    case 3:
                        switch (state.getValue(DIRECTION))
                        {
                            case NORTH:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, false).withProperty(DECAY, 3);
                                break;
                            case EAST:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.EAST).withProperty(LIT, false).withProperty(DECAY, 3);
                                break;
                            case WEST:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.WEST).withProperty(LIT, false).withProperty(DECAY, 3);
                                break;
                            case SOUTH:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.SOUTH).withProperty(LIT, false).withProperty(DECAY, 3);
                                break;
                            case UP:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.UP).withProperty(LIT, false).withProperty(DECAY, 3);
                                break;
                            case DOWN:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.DOWN).withProperty(LIT, false).withProperty(DECAY, 3);
                                break;
                            default:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, false).withProperty(DECAY, 3);
                                break;
                        }
                        break;

                    case 2:
                        switch (state.getValue(DIRECTION))
                        {
                            case NORTH:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, false).withProperty(DECAY, 2);
                                break;
                            case EAST:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.EAST).withProperty(LIT, false).withProperty(DECAY, 2);
                                break;
                            case WEST:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.WEST).withProperty(LIT, false).withProperty(DECAY, 2);
                                break;
                            case SOUTH:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.SOUTH).withProperty(LIT, false).withProperty(DECAY, 2);
                                break;
                            case UP:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.UP).withProperty(LIT, false).withProperty(DECAY, 2);
                                break;
                            case DOWN:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.DOWN).withProperty(LIT, false).withProperty(DECAY, 2);
                                break;
                            default:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, false).withProperty(DECAY, 2);
                                break;
                        }
                        break;

                    case 1:
                        switch (state.getValue(DIRECTION))
                        {
                            case NORTH:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, false).withProperty(DECAY, 1);
                                break;
                            case EAST:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.EAST).withProperty(LIT, false).withProperty(DECAY, 1);
                                break;
                            case WEST:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.WEST).withProperty(LIT, false).withProperty(DECAY, 1);
                                break;
                            case SOUTH:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.SOUTH).withProperty(LIT, false).withProperty(DECAY, 1);
                                break;
                            case UP:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.UP).withProperty(LIT, false).withProperty(DECAY, 1);
                                break;
                            case DOWN:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.DOWN).withProperty(LIT, false).withProperty(DECAY, 1);
                                break;
                            default:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, false).withProperty(DECAY, 1);
                                break;
                        }
                        break;

                    case 0:
                        switch (state.getValue(DIRECTION))
                        {
                            case NORTH:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, false).withProperty(DECAY, 0);
                                break;
                            case EAST:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.EAST).withProperty(LIT, false).withProperty(DECAY, 0);
                                break;
                            case WEST:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.WEST).withProperty(LIT, false).withProperty(DECAY, 0);
                                break;
                            case SOUTH:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.SOUTH).withProperty(LIT, false).withProperty(DECAY, 0);
                                break;
                            case UP:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.UP).withProperty(LIT, false).withProperty(DECAY, 0);
                                break;
                            case DOWN:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.DOWN).withProperty(LIT, false).withProperty(DECAY, 0);
                                break;
                            default:
                                blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, false).withProperty(DECAY, 0);
                                break;
                        }
                        break;
                }
        }

        return blockState;

    }

    // Self explanatory.
    @Override
    public int getMetaFromState(IBlockState state)
    {
        int d = 0;

        if (state.getValue(LIT))
        {
            switch (state.getValue(DIRECTION))
            {
                case NORTH:
                    d = d | 1;
                    break;
                case EAST:
                    d = d | 2;
                    break;
                case WEST:
                    d = d | 3;
                    break;
                case SOUTH:
                    d = d | 4;
                    break;
                case UP:
                    d = d | 5;
                    break;
                case DOWN:
                    d = d | 6;
                    break;
                default:
                    d = d | 5;
                    break;
            }
        }
        else
            switch (state.getValue(DIRECTION))
            {
                case NORTH:
                    d = d | 7;
                    break;
                case EAST:
                    d = d | 8;
                    break;
                case WEST:
                    d = d | 9;
                    break;
                case SOUTH:
                    d = d | 10;
                    break;
                case UP:
                    d = d | 11;
                    break;
                case DOWN:
                    d = d | 12;
                    break;
                default:
                    d = d | 11;
                    break;
            }

        return d;
    }

    // Self explanatory.
    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        IBlockState blockState = this.getDefaultState();

        switch (meta)
        {
            case 1:
                blockState = blockState.withProperty(LIT, true).withProperty(DIRECTION, EnumFacing.NORTH);
                break;
            case 2:
                blockState = blockState.withProperty(LIT, true).withProperty(DIRECTION, EnumFacing.EAST);
                break;
            case 3:
                blockState = blockState.withProperty(LIT, true).withProperty(DIRECTION, EnumFacing.WEST);
                break;
            case 4:
                blockState = blockState.withProperty(LIT, true).withProperty(DIRECTION, EnumFacing.SOUTH);
                break;
            case 5:
                blockState = blockState.withProperty(LIT, true).withProperty(DIRECTION, EnumFacing.UP);
                break;
            case 6:
                blockState = blockState.withProperty(LIT, true).withProperty(DIRECTION, EnumFacing.DOWN);
                break;
            case 7:
                blockState = blockState.withProperty(LIT, false).withProperty(DIRECTION, EnumFacing.NORTH);
                break;
            case 8:
                blockState = blockState.withProperty(LIT, false).withProperty(DIRECTION, EnumFacing.EAST);
                break;
            case 9:
                blockState = blockState.withProperty(LIT, false).withProperty(DIRECTION, EnumFacing.WEST);
                break;
            case 10:
                blockState = blockState.withProperty(LIT, false).withProperty(DIRECTION, EnumFacing.SOUTH);
                break;
            case 11:
                blockState = blockState.withProperty(LIT, false).withProperty(DIRECTION, EnumFacing.UP);
                break;
            case 12:
                blockState = blockState.withProperty(LIT, false).withProperty(DIRECTION, EnumFacing.DOWN);
                break;
            default:
                blockState = blockState.withProperty(LIT, true).withProperty(DIRECTION, EnumFacing.UP);
                break;
        }

        return blockState;
    }

    @Override
    public void getDrops(net.minecraft.util.NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
        switch (getActualState(state, world, pos).getValue(DECAY))
        {
            case 5:
                for (Map.Entry<String, Integer> entry : ModConfig.drops.entrySet())
                {
                    Item item = Item.getByNameOrId(entry.getKey());
                    int quantity = entry.getValue();
                    if (entry.getValue() > 8)
                    {
                        quantity = entry.getValue() - 1;
                    }
                    drops.add(new ItemStack(item, quantity));
                }
                break;
            case 4:
                for (Map.Entry<String, Integer> entry : ModConfig.drops.entrySet())
                {
                    Item item = Item.getByNameOrId(entry.getKey());
                    int quantity = entry.getValue();
                    if (entry.getValue() > 8)
                    {
                        quantity = entry.getValue() - 3;
                    }
                    drops.add(new ItemStack(item, quantity));
                }
                break;
            case 3:
                for (Map.Entry<String, Integer> entry : ModConfig.drops.entrySet())
                {
                    Item item = Item.getByNameOrId(entry.getKey());
                    int quantity = entry.getValue();
                    if (entry.getValue() > 6)
                    {
                        quantity = entry.getValue() - 5;
                    }
                    drops.add(new ItemStack(item, quantity));
                }
                break;
            case 2:
                for (Map.Entry<String, Integer> entry : ModConfig.drops.entrySet())
                {
                    Item item = Item.getByNameOrId(entry.getKey());
                    int quantity = entry.getValue();
                    if (entry.getValue() > 3)
                    {
                        quantity = entry.getValue() - 6;
                    }
                    drops.add(new ItemStack(item, quantity));
                }
                break;
            case 1:
                for (Map.Entry<String, Integer> entry : ModConfig.drops.entrySet())
                {
                    Item item = Item.getByNameOrId(entry.getKey());
                    int quantity = entry.getValue();
                    if (entry.getValue() > 1)
                    {
                        quantity = entry.getValue() - 7;
                    }
                    drops.add(new ItemStack(item, quantity));
                }
                break;
        }
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
        if (willHarvest) return true; //If it will harvest, delay deletion of the block until after getDrops
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    /**
     * Spawns the block's drops in the world. By the time this is called the Block has possibly been set to air via
     * Block.removedByPlayer
     */
    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack tool)
    {
        super.harvestBlock(world, player, pos, state, te, tool);
        world.setBlockToAir(pos);
    }
}
