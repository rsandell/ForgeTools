package forgetools.commands;

import cpw.mods.fml.common.FMLCommonHandler;
import forgetools.ForgeTools;
import forgetools.logic.Functions;
import forgetools.logic.WorldDropsVisitor;
import forgetools.util.ItemChunkRef;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

import java.util.Date;
import java.util.HashMap;

/**
 * Counts items on the ground and reports counts and locations
 *
 * @author rlcrock
 */
public class DropsCommand extends ForgeToolsGenericCommand {
    private Date lastCheck = new Date();    // Last time the chunk list was updated
    private HashMap<Chunk, Integer> items = new HashMap<Chunk, Integer>();    // Chunk list
    private int total;    // Total number of items in all worlds

    public DropsCommand(String cmds) {
        super(cmds); // Set cmd name and aliases
    }

    public String getCommandUsage(ICommandSender par1ICommandSender) {
        return "/" + cmdName + " [ [detail [force] ] | [kill [radius] ] | killall]";
    }

    public void processCommand(ICommandSender sender, String[] args) {
        // If we are on the server, return
        // TODO: Add ability to run commands in limited scope from console
        if (!FMLCommonHandler.instance().getEffectiveSide().isServer()) return;

        EntityPlayerMP player = null;
        if (!sender.getCommandSenderName().equals("Server"))
            player = getCommandSenderAsPlayer(sender);    // Player object of player who sent command

        MinecraftServer server = ForgeTools.server;

        // Set argument defaults
        boolean details = false, kill = false, killall = false, force = false;
        float radius = ForgeTools.killRadius;

        // Parse args
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("detail") || args[0].equalsIgnoreCase("d")) {
                details = true;
                if (args.length == 2 && (args[1].equalsIgnoreCase("force") || args[1].equalsIgnoreCase("f")))
                    force = true;
                else if (args.length != 1)
                    throw new WrongUsageException(getCommandUsage(sender));
            } else if (args[0].equalsIgnoreCase("kill") || args[0].equalsIgnoreCase("k")) {
                kill = true;
                if (args.length == 2) {
                    try {
                        radius = Float.parseFloat(args[1]);
                    } catch (Exception e) {
                        throw new WrongUsageException("Invalid radius.");
                    }
                }
            } else if (args[0].equals("killall"))
                killall = true;
            else throw new WrongUsageException(getCommandUsage(sender));
        }

        // Check to see if we need to rebuild the map.
        // This is done on a kill[all], when forced, or if it's been more than 10s (default) since the last rebuild
        if (lastCheck.getTime() < new Date().getTime() - (ForgeTools.timeout * 1000) || force || kill || killall) {
            // Clear the old counts and set the new check time
            items.clear();
            lastCheck = new Date();
            total = Functions.countDrops(sender, player, items, details, kill, killall, radius, new WorldDropsVisitor() {
                @Override
                public void visit(ICommandSender sender, EntityPlayerMP player, boolean playerInWorld,
                                  boolean kill, boolean killall, boolean details, int worldItemCount, int itemsDeleted,
                                  WorldServer s) {
                    // Send results
                    String prefix = (playerInWorld) ? "\u00a72" : "";
                    if (sender != null && (kill || killall))
                        sender.sendChatToPlayer(ChatMessageComponent.createFromText(prefix + itemsDeleted
                                + " items removed from "
                                + s.provider.worldObj.getWorldInfo().getWorldName() + " " + s.provider.getDimensionName()));
                    if (sender != null && details)
                        sender.sendChatToPlayer(ChatMessageComponent.createFromText(prefix + worldItemCount
                                + " items in "
                                + s.provider.worldObj.getWorldInfo().getWorldName() + " " + s.provider.getDimensionName()));
                }
            });

        } else {
            long wait = new Date().getTime() - (lastCheck.getTime() + (ForgeTools.timeout * 1000));
            wait = (long)Math.abs(wait) / 1000;
            sender.sendChatToPlayer(ChatMessageComponent.createFromText("\u00a7eYou must wait " + wait + " seconds before trying again or use the force option."));
            return;
        }

        if (details) {
            // Send extra info if details are needed
            sender.sendChatToPlayer(ChatMessageComponent.createFromText("Top 5 chunks by loose item count:"));
            ItemChunkRef[] sortedList = ItemChunkRef.getSortedChunkList(items);
            for (int i = 0; i < sortedList.length && i < 5; ++i) {
                ItemChunkRef cr = sortedList[i];
                Chunk c = cr.getChunk();
                String worldName = c.worldObj.getWorldInfo().getWorldName();
                String dimName = c.worldObj.provider.getDimensionName();
                sender.sendChatToPlayer(ChatMessageComponent.createFromText(worldName + " " + dimName + " (" + (c.xPosition * 16) + ", " + (c.zPosition * 16) + ") " + cr.getValue() + " items"));
            }
        }

        if (!(details || kill || killall))
            sender.sendChatToPlayer(ChatMessageComponent.createFromText(total + " loose items in all worlds"));
    }



    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return hasEnhancedPermissions(sender);
    }


}
