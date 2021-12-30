package net.doubledoordev.burningtorch.blocks;

import java.util.Locale;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import net.doubledoordev.burningtorch.BurningTorch;
import net.doubledoordev.burningtorch.blocks.blockentities.BurningLightBlockEntity;

public class BlockRegistry
{
    public static final DeferredRegister<Block> BLOCK_DEFERRED = DeferredRegister.create(ForgeRegistries.BLOCKS, BurningTorch.MODID);
    public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITY_DEFERRED = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, BurningTorch.MODID);

    // Blocks
    public static final RegistryObject<Block> BURNING_TORCH = register("torch", () -> new TorchBlock(
            BlockBehaviour.Properties.of(Material.DECORATION)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.WOOD)
                    .randomTicks()
                    .isSuffocating(BlockRegistry::never)
    ));
    public static final RegistryObject<Block> BURNING_PUMPKIN = register("pumpkin", () -> new PumpkinBlock(
            BlockBehaviour.Properties.of(Material.VEGETABLE, MaterialColor.COLOR_ORANGE)
                    .strength(1.0f)
                    .sound(SoundType.WOOD)
                    .randomTicks()
    ));

    // Block Entities
    public static final RegistryObject<BlockEntityType<BurningLightBlockEntity>> BURNING_LIGHT_BLOCK_ENTITY = register("burning_light_block_entity", BurningLightBlockEntity::new, BURNING_TORCH);


    //Internals for registry
    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String name, BlockEntityType.BlockEntitySupplier<T> factory, Supplier<? extends Block> block)
    {
        return TILE_ENTITY_DEFERRED.register(name, () -> BlockEntityType.Builder.of(factory, block.get()).build(null));
    }

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier)
    {
        final String actualName = name.toLowerCase(Locale.ROOT);
        return BLOCK_DEFERRED.register(actualName, blockSupplier);
    }

    private static boolean never(BlockState state, BlockGetter world, BlockPos pos)
    {
        return false;
    }

    private static boolean always(BlockState state, BlockGetter world, BlockPos pos)
    {
        return true;
    }

}
