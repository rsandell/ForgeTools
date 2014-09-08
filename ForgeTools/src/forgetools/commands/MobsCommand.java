package forgetools.commands;

import cpw.mods.fml.common.FMLCommonHandler;
import forgetools.ForgeTools;
import forgetools.logic.Functions;
import forgetools.logic.MobData;
import forgetools.logic.MobType;
import forgetools.logic.WorldMobsVisitor;
import forgetools.util.ItemChunkRef;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.INpc;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

import java.util.Date;
import java.util.HashMap;

public class MobsCommand extends ForgeToolsGenericCommand {
    private Date lastCheck = new Date();    // Last time the chunk list was updated
    private HashMap<Chunk, Integer> mobs = new HashMap<Chunk, Integer>();    // Chunk list
    private MobData total;    // Total number of items in all worlds

    public MobsCommand(String cmds) {
        super(cmds);
    }

    @Override
    public String getCommandUsage(ICommandSender par1ICommandSender) {
        return "/" + cmdName + " [detail | kill <passive | hostile | npc | all> [radius] ] [force]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (!FMLCommonHandler.instance().getEffectiveSide().isServer()) return;

        EntityPlayerMP player = null;
        if (!sender.getCommandSenderName().equals("Server"))
            player = getCommandSenderAsPlayer(sender);

        //MinecraftServer server = ForgeTools.server;
        boolean details = false, kill = false, force = false;
        MobType type = MobType.none;
        float radius = ForgeTools.killRadius;

        if (args.length > 4) throw new WrongUsageException(getCommandUsage(sender));

        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("detail") || args[0].equalsIgnoreCase("d"))
                details = true;
            else if (args[0].equalsIgnoreCase("kill") || args[0].equalsIgnoreCase("k"))
                kill = true;
            else if (args[0].equalsIgnoreCase("force") || args[0].equalsIgnoreCase("f"))
                force = true;
            else throw new WrongUsageException(getCommandUsage(sender));
        }
        if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("force") || args[1].equalsIgnoreCase("f"))
                force = true;
            else if (args[1].equalsIgnoreCase("passive"))
                type = MobType.passive;
            else if (args[1].equalsIgnoreCase("hostile"))
                type = MobType.hostile;
            else if (args[1].equalsIgnoreCase("npc"))
                type = MobType.npc;
            else if (args[1].equalsIgnoreCase("all"))
                type = MobType.all;
            else throw new WrongUsageException(getCommandUsage(sender));
        }
        if (args.length >= 3) {
            if (args[2].equalsIgnoreCase("force") || args[2].equalsIgnoreCase("f"))
                force = true;
            else {
                try {
                    radius = Float.parseFloat(args[2]);
                } catch (Exception e) {
                    throw new WrongUsageException("Invalid radius.");
                }
            }
        }
        if (args.length == 4) {
            if (args[2].equalsIgnoreCase("force") || args[2].equalsIgnoreCase("f"))
                force = true;
            else throw new WrongUsageException(getCommandUsage(sender));
        }

        if (lastCheck.getTime() < new Date().getTime() - (ForgeTools.timeout * 1000) || force || kill) {
            mobs.clear();

            lastCheck = new Date();

            total = Functions.calcMobs(sender, player, details, kill, type, radius, mobs, new WorldMobsVisitor() {
                @Override
                public void visit(ICommandSender sender, boolean playerInWorld, MobData worldData, WorldServer s, boolean details, boolean kill, MobType type) {
                    String prefix = (playerInWorld) ? "\u00a72" : "";
                    if (details) {
                        sender.sendChatToPlayer(ChatMessageComponent.createFromText(prefix +
                                worldData.getAmtHos() + " hostile, " +
                                worldData.getAmtPas() + " passive, and " +
                                worldData.getAmtNPC() + " NPCs spawned in " +
                                s.provider.worldObj.getWorldInfo().getWorldName() + " " + s.provider.getDimensionName()));
                    }
                    if (kill) {
                        sender.sendChatToPlayer(ChatMessageComponent.createFromText(prefix + worldData.getAmtRemoved() + " " +
                                type + " mobs removed from " +
                                s.provider.worldObj.getWorldInfo().getWorldName() + " " + s.provider.getDimensionName()));
                    }
                }


            });

            sender.sendChatToPlayer(ChatMessageComponent.createFromText(total.getTotal() + " mobs in all worlds."));
            if (details) {
                // Send extra info if details are needed
                sender.sendChatToPlayer(ChatMessageComponent.createFromText("Top 5 chunks by mob count:"));
                ItemChunkRef[] sortedList = ItemChunkRef.getSortedChunkList(mobs);
                for (int i = 0; i < sortedList.length && i < 5; ++i) {
                    ItemChunkRef cr = sortedList[i];
                    Chunk c = cr.getChunk();
                    String worldName = c.worldObj.getWorldInfo().getWorldName();
                    String dimName = c.worldObj.provider.getDimensionName();
                    sender.sendChatToPlayer(ChatMessageComponent.createFromText(worldName + " " + dimName + " (" + (c.xPosition * 16) + ", " + (c.zPosition * 16) + ") " + cr.getValue() + " mobs"));
                }
            }
        } else {
            long wait = new Date().getTime() - (lastCheck.getTime() + (ForgeTools.timeout * 1000));
            wait = (long)Math.abs(wait) / 1000;
            sender.sendChatToPlayer(ChatMessageComponent.createFromText("\u00a7eYou must wait " + wait + " seconds before trying again or use the force option."));
            return;
        }
    }

    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return hasEnhancedPermissions(sender);
    }


}
