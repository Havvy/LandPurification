package net.havvy.minecraft.landpurification;

import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;

import net.havvy.minecraft.landpurification.handlers.SpawnHandler;

@Mod(modid = LandPurification.MODID, version = LandPurification.VERSION)
public class LandPurification
{
    public static final String MODID = "landpurification";
    public static final String VERSION = "1.0";
    
    public final static int InitialPurity = 32;
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	MinecraftForge.EVENT_BUS.register(new SpawnHandler());
    }
}
