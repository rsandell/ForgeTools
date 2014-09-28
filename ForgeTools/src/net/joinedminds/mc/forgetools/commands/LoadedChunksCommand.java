package net.joinedminds.mc.forgetools.commands;

import net.joinedminds.mc.forgetools.logic.Functions;
import net.joinedminds.mc.forgetools.logic.LoadedChunksVisitor;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.FMLCommonHandler;

public class LoadedChunksCommand extends ForgeToolsGenericCommand
{

	public LoadedChunksCommand(String cmds)
	{
		super(cmds);
	}

	public String getCommandUsage(ICommandSender par1ICommandSender)
    {
    	return  "/" + cmdName + " [detail | d]";
    }
	
	public void processCommand(ICommandSender sender, String[] args)
	{
		if(!FMLCommonHandler.instance().getEffectiveSide().isServer()) return;
		
		EntityPlayerMP player = null;
		if(!sender.getCommandSenderName().equals("Server"))
			player = getCommandSenderAsPlayer(sender);
		
		boolean details = false;
		
		if(args.length > 1) throw new WrongUsageException(getCommandUsage(sender));
		else if (args.length == 1)
			if (args[0].equalsIgnoreCase("detail") || args[0].equalsIgnoreCase("d")) details = true;
		else throw new WrongUsageException(getCommandUsage(sender));

        int total = Functions.countLoadedChunks(sender, player, details, new LoadedChunksVisitor() {
            @Override
            public void visit(ICommandSender sender, EntityPlayerMP player, boolean details, boolean playerInWorld, WorldServer s, int chunkSize) {
                String prefix = (playerInWorld) ? "\u00a72" :  "" ;
                if (details) sender.sendChatToPlayer(ChatMessageComponent.createFromText(prefix + chunkSize
                        + " force loaded chunks in "
                        + s.provider.worldObj.getWorldInfo().getWorldName()
                        + " " + s.provider.getDimensionName()));
            }
        });

        if(!details) sender.sendChatToPlayer(ChatMessageComponent.createFromText(total + " force loaded chunks in all worlds"));
		
	}



    public boolean canCommandSenderUseCommand(ICommandSender sender)
	{
		return hasEnhancedPermissions(sender);
	}



}
