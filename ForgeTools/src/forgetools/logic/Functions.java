package forgetools.logic;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Sets;
import forgetools.ForgeTools;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;
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

                    if (( (kill || killall) && sender != null && sender.getCommandSenderName().equals("Server")) ||    // Console is sending the command, so no player is needed
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

    public static interface WorldDropsVisitor {
        void visit(ICommandSender sender, EntityPlayerMP player, boolean playerInWorld, boolean kill, boolean killall,
                   boolean details, int worldItemCount, int itemsDeleted, WorldServer s);
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

    public static class LagData {
        public final double tickMS;
        public final double tickPct;
        public final double tps;
        public final Integer dim;
        public final String dimName;

        public LagData(double tickMS, double tickPct, double tps, int dim, String dimName) {
            this.tickMS = tickMS;
            this.tickPct = tickPct;
            this.tps = tps;
            this.dim = dim;
            this.dimName = dimName;
        }
    }

    public static int countLoadedChunks(ICommandSender sender, EntityPlayerMP player, 
                                        boolean details, LoadedChunksVisitor visitor) {
        MinecraftServer server = ForgeTools.server;
        int total = 0;
        for(WorldServer s : server.worldServers)
        {
            World tmp = ((World) s);
            ImmutableSetMultimap<ChunkCoordIntPair, ForgeChunkManager.Ticket> forcedChunks = tmp.getPersistentChunks();
            Set loadedChunks = new LinkedHashSet<ChunkCoordIntPair>();
            for(ChunkCoordIntPair c : forcedChunks.keys())
            {
                for(ForgeChunkManager.Ticket t : forcedChunks.get(c))
                {
                    loadedChunks = Sets.union(t.getChunkList(), loadedChunks);
                }
            }
            total += loadedChunks.size();

            boolean playerInWorld = (player != null) ? s.getWorldInfo().equals(player.worldObj.getWorldInfo()) : false;
            visitor.visit(sender, player, details, playerInWorld, s, loadedChunks.size());

        }
        return total;
    }
    
    public static interface LoadedChunksVisitor {
        void visit(ICommandSender sender, EntityPlayerMP player, boolean details, boolean playerInWorld, WorldServer s, int chunkSize);
    }
}
