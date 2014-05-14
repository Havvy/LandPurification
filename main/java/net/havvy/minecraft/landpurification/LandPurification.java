package net.havvy.minecraft.landpurification;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

import net.havvy.minecraft.landpurification.handlers.SpawnHandler;

@Mod(modid = LandPurification.MODID, version = LandPurification.VERSION)
public class LandPurification
{
    public static final String MODID = "landpurification";
    public static final String VERSION = "1.0";
    
    protected static int initialPurity;
    
    @EventHandler
    public void preinit(FMLPreInitializationEvent event)
    {
    	Configuration config = new Configuration(event.getSuggestedConfigurationFile());

    	config.load();

    	SpawnHandler.initialPurity = config.get(Configuration.CATEGORY_GENERAL, "initialPurity", 32).getInt();
    	SpawnHandler.dimensionBlacklist = config.get(Configuration.CATEGORY_GENERAL, "dimensionBlacklist", new int[] { -1, 1 }).getIntList();
    		
    	config.save();
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	SpawnHandler spawnHandler = new SpawnHandler();
    	MinecraftForge.EVENT_BUS.register(spawnHandler);
    	GameRegistry.registerWorldGenerator(spawnHandler, Integer.MAX_VALUE);
    }
}
