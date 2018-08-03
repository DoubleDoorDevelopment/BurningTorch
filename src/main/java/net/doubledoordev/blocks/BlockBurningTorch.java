package net.doubledoordev.blocks;

import net.doubledoordev.ModConfig;
import net.doubledoordev.tileentities.TorchTE;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Random;

import static net.doubledoordev.BurningTorch.MOD_ID;


public class BlockBurningTorch extends Block
{
    public static final PropertyDirection DIRECTION = PropertyDirection.create("direction");
    public static final PropertyBool LIT = PropertyBool.create("lit");
    public static final PropertyInteger DECAY = PropertyInteger.create("decay",0, 5);

    private static final AxisAlignedBB STANDING_AABB = new AxisAlignedBB(     0.4000000059604645D, 0.0D,                 0.4000000059604645D, 0.6000000238418579D,  0.8000000238418579D, 0.6000000238418579D);
    private static final AxisAlignedBB TORCH_NORTH_AABB = new AxisAlignedBB(  0.399999940395355D, 0.20000000298023224D, 0.599999988079071D,  0.599999761581421D,   1.000100011920929D,  1.0D);
    private static final AxisAlignedBB TORCH_SOUTH_AABB = new AxisAlignedBB(  0.3499999940395355D, 0.20000000298023224D, 0.0D,                0.6499999761581421D,  1.000100011920929D,  0.40000001192092896D);
    private static final AxisAlignedBB TORCH_WEST_AABB = new AxisAlignedBB(   0.599999988079071D,  0.20000000298023224D, 0.3499999940395355D, 1.0D,                 1.000100011920929D,  0.6499999761581421D);
    private static final AxisAlignedBB TORCH_EAST_AABB = new AxisAlignedBB(   0.0D,                0.20000000298023224D, 0.3499999940395355D, 0.40000001192092896D, 1.000100011920929D,  0.6499999761581421D);

    //TODO: Torches when destroyed need to drop special stuff correctly...
    public BlockBurningTorch(Material materialIn)
    {
        super(materialIn);
        setDefaultState(this.getDefaultState().withProperty(LIT, true).withProperty(DIRECTION, EnumFacing.UP).withProperty(DECAY, 5));
        setLightLevel(0.9375f);
        setCreativeTab(CreativeTabs.DECORATIONS);
        setUnlocalizedName("burningtorch");
        setRegistryName(MOD_ID, "burningtorch");
    }

    // Handles the relighting of torches and a bunch of other stuff.
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        TorchTE torchTE = (TorchTE) worldIn.getTileEntity(pos);

        for (String item : ModConfig.relightingItems)
        {
            if (playerIn.getHeldItemMainhand().getItem().getRegistryName().toString().equals(item) || playerIn.getHeldItemOffhand().getItem().getRegistryName().toString().equals(item) && !state.getValue(LIT))
            {
                worldIn.setBlockState(pos, worldIn.getBlockState(pos).withProperty(LIT, true));
                return true;
            }
        }

        for (String item : ModConfig.extinguishingingItems)
        {
            if (playerIn.getHeldItemMainhand().getItem().getRegistryName().toString().equals(item) || playerIn.getHeldItemOffhand().getItem().getRegistryName().toString().equals(item) && state.getValue(LIT))
            {
                worldIn.setBlockState(pos, worldIn.getBlockState(pos).withProperty(LIT, false));
                return true;
            }
        }

            for (Map.Entry<String, Integer> entry : ModConfig.extendingingItems.entrySet())
            {
                if (playerIn.getHeldItemMainhand().getItem().getRegistryName().toString().equals(entry.getKey()) /*|| playerIn.getHeldItemOffhand().getItem().getRegistryName().toString().equals(entry.getKey()) */ && torchTE.getDecayLevel() < 5)
                {
                    if (worldIn.getBlockState(pos).getBlock().getActualState(state, worldIn, pos).getValue(DECAY) + entry.getValue() > 5)
                    {
                        torchTE.setDecayLevel(5);
                        playerIn.getHeldItemMainhand().setCount(playerIn.getHeldItemMainhand().getCount() - 1);
                        return true;
                    }
                    else
                    {
                        torchTE.setDecayLevel(worldIn.getBlockState(pos).getBlock().getActualState(state, worldIn, pos).getValue(DECAY) + entry.getValue());
                        playerIn.getHeldItemMainhand().setCount(playerIn.getHeldItemMainhand().getCount() - 1);
                        return true;
                    }
                }
        }

        return false;
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    // Changes the lighting level based off the LIT blockstate property.
    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        int decayLevel;
        if (world.getTileEntity(pos) instanceof TorchTE)
        {
            TorchTE torchTE = (TorchTE) world.getTileEntity(pos);
            decayLevel = torchTE.getDecayLevel();
        }
        else
        {
            decayLevel = 5;
        }

        if (!state.getValue(LIT))
        {
            return ModConfig.lightLevelUnlitTorch;
        }
        switch (decayLevel)
        {
            case 5:
                return ModConfig.lightLevel5;
            case 4:
                return ModConfig.lightLevel4;
            case 3:
                return ModConfig.lightLevel3;
            case 2:
                return ModConfig.lightLevel2;
            case 1:
                return ModConfig.lightLevel1;
        }
        return 14;
    }

    // Set the render layer, CUTOUT is required or you get stupid looking torches...
    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }

    // Sets the collision box, as this is a torch we don't want people to get stuck on it NULL_AABB is used...
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos)
    {
        return NULL_AABB;
    }

    // Switch statement that handles the bounding boxes for each direction.
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        switch (state.getValue(DIRECTION))
        {
            case EAST:
                return TORCH_EAST_AABB;
            case WEST:
                return TORCH_WEST_AABB;
            case SOUTH:
                return TORCH_SOUTH_AABB;
            case NORTH:
                return TORCH_NORTH_AABB;
            default:
                return STANDING_AABB;
        }
    }

    // Does stuff, Check the super...
    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    // Does stuff, Check the super...
    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    // Does stuff, Check the super...
    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, DIRECTION, LIT, DECAY);
    }

    // Does stuff....
    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        if (this.canPlaceAt(worldIn, pos, facing))
        {
            return this.getDefaultState().withProperty(DIRECTION, facing);
        }
        else
        {
            for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL)
            {
                if (this.canPlaceAt(worldIn, pos, enumfacing))
                {
                    return this.getDefaultState().withProperty(DIRECTION, enumfacing);
                }
            }

            return this.getDefaultState();
        }
    }

    // Does stuff....
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        this.onNeighborChangeInternal(worldIn, pos, state);
    }

    // Does stuff....
    private boolean onNeighborChangeInternal(World worldIn, BlockPos pos, IBlockState state)
    {
        //if check drop is false
        if (!this.checkForDrop(worldIn, pos, state))
        {
            //no dropy
            return true;
        }
        else
        {
            EnumFacing enumfacing = state.getValue(DIRECTION);
            EnumFacing.Axis enumfacing$axis = enumfacing.getAxis();
            EnumFacing enumfacing1 = enumfacing.getOpposite();
            BlockPos blockpos = pos.offset(enumfacing1);
            boolean flag = false;

            // if axis is horizontal and faceshape is not soild.
            if (enumfacing$axis.isHorizontal() && worldIn.getBlockState(blockpos).getBlockFaceShape(worldIn, blockpos, enumfacing) != BlockFaceShape.SOLID)
            {
                flag = true;
            }
            //if axis is vertical and can't be placed?
            else if (enumfacing$axis.isVertical() && !this.canPlaceOn(worldIn, blockpos))
            {
                flag = true;
            }

            if (flag)
            {
                this.dropBlockAsItemWithChance(worldIn, pos, state,100,0);
                worldIn.setBlockToAir(pos);
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    // Handles the effects on the top of the torch.
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {
        TorchTE torchTE = (TorchTE) worldIn.getTileEntity(pos);
        boolean lit = stateIn.getValue(LIT);
        EnumFacing enumfacing = stateIn.getValue(DIRECTION);
        int decay = torchTE.getDecayLevel();

        double d0 = (double)pos.getX() + 0.5D;
        double d1 = (double)pos.getY() + 0.9D;
        double d2 = (double)pos.getZ() + 0.5D;

        switch (decay)
        {
            case 5:
                d1 = (double)pos.getY() + 0.9D;
                break;
            case 4:
                d1 = (double)pos.getY() + 0.85D;
                break;
            case 3:
                d1 = (double)pos.getY() + 0.75D;
                break;
            case 2:
                d1 = (double)pos.getY() + 0.62D;
                break;
            case 1:
                d1 = (double)pos.getY() + 0.44D;
                break;
            case 0:
                d1 = (double)pos.getY() + 0.35D;
                break;
        }

        if (lit)
        {
            if (enumfacing.getAxis().isHorizontal())
            {
                EnumFacing enumfacing1 = enumfacing.getOpposite();
                switch (decay)
                {
                    case 5:
                        worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + 0.2D * (double) enumfacing1.getFrontOffsetX(), d1 + 0.20D, d2 + 0.2D * (double) enumfacing1.getFrontOffsetZ(), 0.0D, 0.0D, 0.0D);
                        worldIn.spawnParticle(EnumParticleTypes.FLAME, d0 + 0.2D * (double) enumfacing1.getFrontOffsetX(), d1 + 0.20D, d2 + 0.2D * (double) enumfacing1.getFrontOffsetZ(), 0.0D, 0.0D, 0.0D);
                        break;
                    case 4:
                        worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + 0.22D * (double) enumfacing1.getFrontOffsetX(), d1 + 0.16D, d2 + 0.3D * (double) enumfacing1.getFrontOffsetZ(), 0.0D, 0.0D, 0.0D);
                        worldIn.spawnParticle(EnumParticleTypes.FLAME, d0 + 0.22D * (double) enumfacing1.getFrontOffsetX(), d1 + 0.16D, d2 + 0.3D * (double) enumfacing1.getFrontOffsetZ(), 0.0D, 0.0D, 0.0D);
                        break;
                    case 3:
                        worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + 0.24D * (double) enumfacing1.getFrontOffsetX(), d1 + 0.15D, d2 + 0.3D * (double) enumfacing1.getFrontOffsetZ(), 0.0D, 0.0D, 0.0D);
                        worldIn.spawnParticle(EnumParticleTypes.FLAME, d0 + 0.24D * (double) enumfacing1.getFrontOffsetX(), d1 + 0.15D, d2 + 0.3D * (double) enumfacing1.getFrontOffsetZ(), 0.0D, 0.0D, 0.0D);
                        break;
                    case 2:
                        worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + 0.3D * (double) enumfacing1.getFrontOffsetX(), d1 + 0.16D, d2 + 0.3D * (double) enumfacing1.getFrontOffsetZ(), 0.0D, 0.0D, 0.0D);
                        worldIn.spawnParticle(EnumParticleTypes.FLAME, d0 + 0.3D * (double) enumfacing1.getFrontOffsetX(), d1 + 0.16D, d2 + 0.3D * (double) enumfacing1.getFrontOffsetZ(), 0.0D, 0.0D, 0.0D);
                        break;
                    case 1:
                        worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + 0.35D * (double) enumfacing1.getFrontOffsetX(), d1 + 0.28D, d2 + 0.3D * (double) enumfacing1.getFrontOffsetZ(), 0.0D, 0.0D, 0.0D);
                        worldIn.spawnParticle(EnumParticleTypes.FLAME, d0 + 0.35D * (double) enumfacing1.getFrontOffsetX(), d1 + 0.28D, d2 + 0.3D * (double) enumfacing1.getFrontOffsetZ(), 0.0D, 0.0D, 0.0D);
                        break;
                    case 0:
                        worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + 0.1D * (double) enumfacing1.getFrontOffsetX(), d1 + 0.09D, d2 + 0.3D * (double) enumfacing1.getFrontOffsetZ(), 0.0D, 0.0D, 0.0D);
                        worldIn.spawnParticle(EnumParticleTypes.FLAME, d0 + 0.1D * (double) enumfacing1.getFrontOffsetX(), d1 + 0.09D, d2 + 0.3D * (double) enumfacing1.getFrontOffsetZ(), 0.0D, 0.0D, 0.0D);
                        break;
                }

            }
            else
            {
                worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                worldIn.spawnParticle(EnumParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    // Does stuff....
    private boolean checkForDrop(World worldIn, BlockPos pos, IBlockState state)
    {
        // If this block = this block and this can be placed here return true.
        if (state.getBlock() == this && this.canPlaceAt(worldIn, pos, state.getValue(DIRECTION)))
        {
            return true;
        }
        else
        {
            //if this block = this block
            if (worldIn.getBlockState(pos).getBlock() == this)
            {
                //drop and set to air.
                this.dropBlockAsItemWithChance(worldIn, pos, state,100,0);
                worldIn.setBlockToAir(pos);
            }

            return false;
        }
    }

    public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
    {
        for (EnumFacing enumfacing : DIRECTION.getAllowedValues())
        {
            if (this.canPlaceAt(worldIn, pos, enumfacing))
            {
                return true;
            }
        }

        return false;
    }

    // Stops torches from being placed on their self.
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face)
    {
        return BlockFaceShape.UNDEFINED;
    }

    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        this.checkForDrop(worldIn, pos, state);
    }

    // Does stuff....
    private boolean canPlaceAt(World worldIn, BlockPos pos, EnumFacing facing)
    {
        BlockPos blockpos = pos.offset(facing.getOpposite());
        IBlockState iblockstate = worldIn.getBlockState(blockpos);
        Block block = iblockstate.getBlock();
        BlockFaceShape blockfaceshape = iblockstate.getBlockFaceShape(worldIn, blockpos, facing);

        if (facing.equals(EnumFacing.UP) && this.canPlaceOn(worldIn, blockpos))
        {
            return true;
        }
        else if (facing != EnumFacing.UP && facing != EnumFacing.DOWN)
        {
            return !isExceptBlockForAttachWithPiston(block) && blockfaceshape == BlockFaceShape.SOLID;
        }
        else
        {
            return false;
        }
    }

    // Does stuff....
    private boolean canPlaceOn(World worldIn, BlockPos pos)
    {
        IBlockState state = worldIn.getBlockState(pos);
        return state.getBlock().canPlaceTorchOnTop(state, worldIn, pos);
    }

    // Part of attaching the TE to the block.
    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new TorchTE();
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
        TileEntity tileentity = worldIn instanceof ChunkCache ? ((ChunkCache)worldIn).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK) : worldIn.getTileEntity(pos);
        int decayLevel;
        if (tileentity instanceof TorchTE)
        {
            TorchTE torchTE = (TorchTE) tileentity;
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

