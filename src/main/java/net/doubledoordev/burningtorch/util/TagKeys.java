package net.doubledoordev.burningtorch.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import net.doubledoordev.burningtorch.BurningTorch;

public class TagKeys
{
    public static final TagKey<Item> EXTINGUISH_ITEMS = ItemTags.create(new ResourceLocation(BurningTorch.MOD_ID, "extinguish_items"));
    public static final TagKey<Item> RELIGHT_ITEMS = ItemTags.create(new ResourceLocation(BurningTorch.MOD_ID, "relight_items"));
    public static final TagKey<Item> CUTTING_ITEMS = ItemTags.create(new ResourceLocation(BurningTorch.MOD_ID, "cutting_items"));
}
