package forgetools.commands;

import cpw.mods.fml.common.FMLCommonHandler;
import forgetools.ForgeTools;
import forgetools.logic.Functions;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;

import java.util.Hashtable;

public class LagCommand extends ForgeToolsGenericCommand {
    public LagCommand(String cmds) {
        super(cmds);
    }

    public String getCommandUsage(ICommandSender par1ICommandSender) {
        return "/" + cmdName + " [detail | d | current | c | <dim_id>]";
    }

    public void processCommand(ICommandSender sender, String[] args) {
        if (!FMLCommonHandler.instance().getEffectiveSide().isServer()) return;

        boolean details = false, current = false;

        EntityPlayerMP player = null;
        if (!sender.getCommandSenderName().equals("Server"))
            player = getCommandSenderAsPlayer(sender);

        Integer dim = null;

        if (args.length > 1) throw new WrongUsageException(getCommandUsage(sender));
        else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("detail") || args[0].equalsIgnoreCase("d"))
                details = true;
            else if (args[0].equalsIgnoreCase("current") || args[0].equalsIgnoreCase("c"))
                current = true;
            else {
                try {
                    dim = Integer.parseInt(args[0]);
                } catch (Exception e) {
                    sender.sendChatToPlayer(ChatMessageComponent.createFromText("Invalid dimension ID."));
                    return;
                }
            }
        }

        MinecraftServer server = ForgeTools.server;

        if (current || dim != null) {
            if (current && sender.getCommandSenderName().equals("Server")) {
                sender.sendChatToPlayer(ChatMessageComponent.createFromText("Invalid option. As the console you are not in a dimension!"));
                return;
            }

            int dimension = (dim != null) ? dim : player.dimension;
            String dimName;
            try {
                dimName = server.worldServerForDimension(dimension).provider.getDimensionName();
            } catch (Exception ex) {
                sender.sendChatToPlayer(ChatMessageComponent.createFromText("\u00a74" + dim + " is not a valid dimension id"));
                return;
            }
            Functions.LagData data = Functions.countLag(dimension);

            sender.sendChatToPlayer(ChatMessageComponent.createFromText(textColor(data.tps) + dimName
                    + " tick: "
                    + data.tps + " tps (" + data.tickMS + "ms, " + data.tickPct + "%)"));
        } else if (details) {
            Functions.LagData[] data = Functions.countLag();
            for (Functions.LagData d : data) {
                sender.sendChatToPlayer(ChatMessageComponent.createFromText(textColor(d.tps) + d.dimName
                        + " tick: "
                        + d.tps + " tps (" + d.tickMS + "ms, " + d.tickPct + "%)"));
            }

        } else {
            Functions.LagData data = Functions.countLag(null, null, server.tickTimeArray);
            sender.sendChatToPlayer(ChatMessageComponent.createFromText(textColor(data.tps)
                    + "Tick: "
                    + data.tps + " tps (" + data.tickMS + "ms, " + data.tickPct + "%)"));
        }

    }

    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return hasEnhancedPermissions(sender);
    }



    private String textColor(double tps) {
        if (tps >= 15)
            return "\u00a72";
        else if (tps >= 10 && tps < 15)
            return "\u00a7e";
        else
            return "\u00a74";
    }


}
