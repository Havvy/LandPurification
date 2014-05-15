package net.havvy.minecraft.landpurification.handlers;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.IWorldGenerator;
import cpw.mods.fml.relauncher.Side;

import net.minecraftforge.event.entity.living.LivingSpawnEvent.CheckSpawn;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.ChunkDataEvent;

import net.havvy.minecraft.landpurification.LandPurification;

public class SpawnHandler implements IWorldGenerator
{
    public static int initialPurity;
    public static int[] dimensionBlacklist;
    private Map<DimChunk, Integer> mobCounts = new HashMap<DimChunk, Integer>();
    private Side side = FMLCommonHandler.instance().getEffectiveSide();

    public SpawnHandler()
    {
        Side side = FMLCommonHandler.instance().getEffectiveSide();
    }

    @SubscribeEvent
    public void onLivingSpawn (CheckSpawn event)
    {
        Result result = event.getResult();
        if (result != Result.DEFAULT)
            return;

        EntityLivingBase entity = event.entityLiving;

        // Only target monsters (and slimes).
        if (!(entity instanceof EntityMob || entity instanceof EntitySlime))
            return;

        // Ignore the end and the nether.
        int dimensionId = entity.worldObj.provider.dimensionId;

        if (dimensionIsBlacklisted(dimensionId))
            return;

        int chunkX = (int) entity.posX >> 4;
        int chunkZ = (int) entity.posZ >> 4;

        DimChunk dimChunk = new DimChunk(dimensionId, chunkX, chunkZ);

        // TEST: Only spawn in chunk 0, 0;
        // if (chunkX != 0 || chunkZ != 0) {
        // event.setResult(Result.DENY);
        // return;
        // }
        // END OF TEST

        if (mobCounts.containsKey(dimChunk))
        {
            if (mobCounts.get(dimChunk) == 0)
            {
                log(dimChunk, "Blocking spawn.");
                event.setResult(Result.DENY);
                return;
            }
        }

        NBTTagCompound spawnTag = new NBTTagCompound();
        spawnTag.setInteger("chunkX", chunkX);
        spawnTag.setInteger("chunkZ", chunkZ);
        spawnTag.setInteger("dimensionId", dimensionId);
        spawnTag.setBoolean("natural", true);

        NBTTagCompound entityTag = entity.getEntityData();
        entityTag.setTag("spawn", spawnTag);
    }

    @SubscribeEvent
    public void onLivingDeath (LivingDeathEvent event)
    {
        // Ignore non-monsters.
        EntityLivingBase entity = event.entityLiving;
        if (!(entity instanceof EntityMob))
            return;

        NBTTagCompound entityTag = entity.getEntityData();
        NBTTagCompound spawnTag = entityTag.getCompoundTag("spawn");

        // If entity doesn't have the necessary tag, ignore it.
        if (spawnTag == null)
            return;

        int dimensionId = spawnTag.getInteger("dimensionId");
        int chunkX = spawnTag.getInteger("chunkX");
        int chunkZ = spawnTag.getInteger("chunkZ");
        DimChunk dimChunk = new DimChunk(dimensionId, chunkX, chunkZ);
        log("Death: " + dimChunk);

        decrementMobCount(dimChunk);
    }

    /*
     * When the chunk loads, add mobCount to it if not already there.
     */
    @SubscribeEvent
    public void onChunkLoad (ChunkDataEvent.Load event)
    {
        int dimensionId = event.world.provider.dimensionId;
        if (dimensionIsBlacklisted(dimensionId))
            return;
        Chunk chunk = event.getChunk();
        DimChunk dimChunk = new DimChunk(dimensionId, chunk.xPosition,
                chunk.zPosition);

        NBTTagCompound tag = (NBTTagCompound) event.getData().getTag(
                "LandPurification");
        if (tag == null)
        {
            setInitialMobCount(dimChunk);
        } else
        {
            setMobCount(dimChunk, tag);
        }
    }

    @SubscribeEvent
    public void onChunkSave (ChunkDataEvent.Save event)
    {
        int dimensionId = event.world.provider.dimensionId;
        if (dimensionIsBlacklisted(dimensionId))
            return;
        Chunk chunk = event.getChunk();
        DimChunk dimChunk = new DimChunk(dimensionId, chunk.xPosition,
                chunk.zPosition);

        NBTTagCompound tag = new NBTTagCompound();

        if (mobCounts.containsKey(dimChunk))
        {
            tag.setInteger("mobCount", mobCounts.get(dimChunk));
        } else
        {
            tag.setInteger("mobCount", initialPurity);
        }

        event.getData().setTag("LandPurification", tag);
    }

    /*
     * Initialize the mob count for the chunk when it is generated.
     */
    @Override
    public void generate (Random random, int chunkX, int chunkZ, World world,
            IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
    {
        int dimensionId = world.provider.dimensionId;
        if (dimensionIsBlacklisted(dimensionId))
            return;
        DimChunk dimChunk = new DimChunk(dimensionId, chunkX, chunkZ);
        setInitialMobCount(dimChunk);
    }

    private void setInitialMobCount (DimChunk dimChunk)
    {
        mobCounts.put(dimChunk, initialPurity);
    }

    private void setMobCount (DimChunk dimChunk, NBTTagCompound tag)
    {
        int mobCount = tag.getInteger("mobCount");
        mobCounts.put(dimChunk, mobCount);
    }

    private void decrementMobCount (DimChunk dimChunk)
    {
        if (mobCounts.containsKey(dimChunk))
        {
            int mobCount = mobCounts.get(dimChunk);
            if (mobCount == 0)
            {
                return;
            } else
            {
                mobCounts.put(dimChunk, mobCount - 1);
            }
        }
    }

    private boolean dimensionIsBlacklisted (int dimensionId)
    {
        for (int ix = 0; ix < dimensionBlacklist.length; ix++)
        {
            if (dimensionBlacklist[ix] == dimensionId)
                return true;
        }

        return false;
    }

    private void log (String string)
    {
        if (side == Side.SERVER)
        {
            System.out.println("(S) " + string);
        } else if (side == Side.CLIENT)
        {
            System.out.println("(C) " + string);
        }
    }

    private void log (DimChunk dimChunk, String string)
    {
        log(dimChunk + ": " + string);
    }

    private static class DimChunk
    {
        public final int dimensionId;
        public final int chunkX;
        public final int chunkZ;

        public DimChunk(int dimensionId, int chunkX, int chunkZ)
        {
            this.dimensionId = dimensionId;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }

        @Override
        public int hashCode ()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + chunkX;
            result = prime * result + chunkZ;
            result = prime * result + dimensionId;
            return result;
        }

        @Override
        public boolean equals (Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final DimChunk other = (DimChunk) obj;
            if (chunkX != other.chunkX)
                return false;
            if (chunkZ != other.chunkZ)
                return false;
            if (dimensionId != other.dimensionId)
                return false;
            return true;
        }

        @Override
        public String toString ()
        {
            return "DimChunk(" + dimensionId + ", " + chunkX + ", " + chunkZ
                    + ")";
        }
    }
}
