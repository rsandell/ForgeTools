package net.joinedminds.mc.forgetools.logic;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Sets;
import net.joinedminds.mc.forgetools.ForgeTools;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.INpc;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeChunkManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Description
 *
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
public final class Functions {

    public static int countDrops(ICommandSender sender, EntityPlayerMP player, HashMap<Chunk, Integer> items,
                                 boolean details, boolean kill, boolean killall, float radius, WorldDropsVisitor visitor) {

        int total = 0;

        // Iterate over loaded worlds
        for (WorldServer s : ForgeTools.server.worldServers) {
            boolean playerInWorld = (player != null) ? s.getWorldInfo().equals(player.worldObj.getWorldInfo()) : false;
            int worldItemCount = 0;
            int itemsDeleted = 0;

            // kill[all] are only relevant for the world the player is currently in
            if (!playerInWorld && (kill || killall))
                continue;

            // Count entities
            for (int id = 0; id < s.loadedEntityList.size(); ++id) {
                Object t = s.loadedEntityList.get(id);
                if (t instanceof EntityItem) {
                    EntityItem e = (EntityItem)t;

                    Chunk c = s.getChunkFromBlockCoords((int)Math.round(e.posX), (int)Math.round(e.posZ));
                    if (!c.isChunkLoaded)
                        continue;

                    ++worldItemCount;

                    if (((kill || killall) && sender != null && sender.getCommandSenderName().equals("Server")) ||    // Console is sending the command, so no player is needed
                            (playerInWorld && (killall || (kill && e.getDistanceToEntity(player) <= radius))))        // Player wants to kill items around them
                    {
                        e.setDead();
                        ++itemsDeleted;
                        continue;
                    }

                    if (items.get(c) == null) {
                        items.put(c, 1);
                    } else {
                        items.put(c, items.get(c) + 1);
                    }
                }
            }

            total += worldItemCount;
            visitor.visit(sender, player, playerInWorld, kill, killall, details, worldItemCount, itemsDeleted, s);

        }
        return total;
    }


    public static LagData countLag(Integer dim) {
        MinecraftServer server = ForgeTools.server;
        String dimName = dim.toString();
        try {
            dimName = server.worldServerForDimension(dim).provider.getDimensionName();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        long[] tickArray = server.worldTickTimes.get(dim);
        return countLag(dim, dimName, tickArray);
    }

    public static LagData countLag(Integer dim, String dimName, long[] tickArray) {
        double tickMS = Math.round(avgTick(tickArray) * 1.0E-5D) / 10d;
        double tickPct = (tickMS < 50) ? 100d : Math.round(50d / tickMS * 1000) / 10d;
        double tps = (tickMS < 50) ? 20d : Math.round((1000d / tickMS) * 10d) / 10d;

        return new LagData(tickMS, tickPct, tps, dim, dimName);
    }

    public static LagData[] countLag() {
        MinecraftServer server = ForgeTools.server;
        Hashtable<Integer, long[]> worldTickTimes = server.worldTickTimes;
        List<LagData> data = new ArrayList<LagData>(worldTickTimes.keySet().size());
        for (Integer i : worldTickTimes.keySet()) {
            data.add(countLag(i));
        }
        return data.toArray(new LagData[data.size()]);
    }

    protected static double avgTick(long[] serverTickArray) {
        long sum = 0L;
        long[] svTicks = serverTickArray;
        int size = serverTickArray.length;

        for (int i = 0; i < size; i++)
            sum += svTicks[i];

        return (double)sum / (double)size;
    }

    public static int countLoadedChunks(ICommandSender sender, EntityPlayerMP player,
                                        boolean details, LoadedChunksVisitor visitor) {
        MinecraftServer server = ForgeTools.server;
        int total = 0;
        for (WorldServer s : server.worldServers) {
            World tmp = ((World)s);
            ImmutableSetMultimap<ChunkCoordIntPair, ForgeChunkManager.Ticket> forcedChunks = tmp.getPersistentChunks();
            Set loadedChunks = new LinkedHashSet<ChunkCoordIntPair>();
            for (ChunkCoordIntPair c : forcedChunks.keys()) {
                for (ForgeChunkManager.Ticket t : forcedChunks.get(c)) {
                    loadedChunks = Sets.union(t.getChunkList(), loadedChunks);
                }
            }
            total += loadedChunks.size();

            boolean playerInWorld = (player != null) ? s.getWorldInfo().equals(player.worldObj.getWorldInfo()) : false;
            visitor.visit(sender, player, details, playerInWorld, s, loadedChunks.size());

        }
        return total;
    }

    public static MobData calcMobs(ICommandSender sender, EntityPlayerMP player, boolean details, boolean kill, MobType type, float radius, HashMap<Chunk, Integer> mobs, WorldMobsVisitor visitor) {
        MobData total = new MobData();

        for (WorldServer s : ForgeTools.server.worldServers) {
            MobData worldData = new MobData();
            boolean playerInWorld = (player != null) ? s.getWorldInfo().equals(player.worldObj.getWorldInfo()) : false;

            for (int id = 0; id < s.loadedEntityList.size(); id++) {
                Object m = s.loadedEntityList.get(id);
                Chunk c;

                if (m instanceof EntityLiving) {
                    c = s.getChunkFromChunkCoords(((EntityLiving)m).chunkCoordX, ((EntityLiving)m).chunkCoordY);
                    if (!c.isChunkLoaded)
                        continue;
                } else
                    continue;

                if (m instanceof EntityMob) {
                    if (kill && type.is(MobType.hostile) && (player == null || ((EntityLiving)m).getDistanceToEntity(player) <= radius)) {
                        ((EntityLiving)m).setDead();
                        worldData.incAmtRemoved();
                    } else
                        worldData.incAmtHos();
                } else if ((m instanceof IAnimals) && !(m instanceof INpc)) {
                    if (kill && type.is(MobType.passive) && (player == null || ((EntityLiving)m).getDistanceToEntity(player) <= radius)) {
                        ((EntityLiving)m).setDead();
                        worldData.incAmtRemoved();
                    } else
                        worldData.incAmtPas();
                } else if (m instanceof INpc) {
                    if (kill && (type.is(MobType.npc)) && (player == null || ((EntityLiving)m).getDistanceToEntity(player) <= radius)) {
                        ((EntityLiving)m).setDead();
                        worldData.incAmtRemoved();
                    } else
                        worldData.incAmtNPC();
                }

                if (!kill) {
                    if (mobs.get(c) == null) {
                        mobs.put(c, 1);
                    } else {
                        mobs.put(c, mobs.get(c) + 1);
                    }
                }
            }

            visitor.visit(sender, playerInWorld, worldData, s, details, kill, type);

            total.incTotal(worldData);
        }
        return total;
    }

}
