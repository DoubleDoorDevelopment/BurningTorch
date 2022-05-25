package net.doubledoordev.burningtorch.items;

import java.util.Locale;
import java.util.function.Supplier;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import net.doubledoordev.burningtorch.BurningTorch;

public class ItemRegistry
{
    public static final DeferredRegister<Item> ITEMS_DEFERRED = DeferredRegister.create(ForgeRegistries.ITEMS, BurningTorch.MOD_ID);

    //Items
    public static final RegistryObject<Item> CHARRED_WOOD = register("charred_wood", () -> new Item(new Item.Properties()
            .tab(CreativeModeTab.TAB_MISC)));

    private static <T extends Item> RegistryObject<T> register(String name, Supplier<T> itemSupplier)
    {
        final String actualName = name.toLowerCase(Locale.ROOT);
        return ITEMS_DEFERRED.register(actualName, itemSupplier);
    }
}
