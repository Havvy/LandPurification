package net.havvy.minecraft.landpurification;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.creativetab.CreativeTabs;

import net.minecraftforge.common.MinecraftForge;

import java.util.List;

public class BlockTotem extends Block
{
	public static int id;
	
	public BlockTotem ()
	{
		super(id, Material.piston);
		
        this.setHardness(5F)
        .setStepSound(Block.soundGravelFootstep)
        .setUnlocalizedName("blockTotem")
        .setCreativeTab(CreativeTabs.tabBlock);
		
		MinecraftForge.setBlockHarvestLevel(this, "pickaxe", 1);
	}
	
	@Override
	public int damageDropped (int metadata)
	{
		return metadata;
	}
	
	public void getSubBlocks(int unknown, CreativeTabs tab, List subItems)
	{		
		subItems.add(new ItemStack(this, 1, 0));
		subItems.add(new ItemStack(this, 1, 1));
	}
}
