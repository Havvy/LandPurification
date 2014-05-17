package net.havvy.minecraft.landpurification;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBlockTotem extends ItemBlock
{
    public static final String blockType[] = { "head", "body" };

    public ItemBlockTotem(int id)
    {
        super(id);
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    @Override
    public int getMetadata (int metadata)
    {
        return metadata;
    }

    public String getUnlocalizedName (ItemStack itemstack)
    {
        return "totem." + blockType[itemstack.getItemDamage()];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation (ItemStack stack, EntityPlayer player, List list, boolean booleanUnknown)
    {
    	list.add("Attracts monsters");
    }
}