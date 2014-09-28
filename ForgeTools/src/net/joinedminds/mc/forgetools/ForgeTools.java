package net.joinedminds.mc.forgetools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cpw.mods.fml.common.registry.GameRegistry;
import net.joinedminds.mc.forgetools.probes.PlayerTracker;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import net.joinedminds.mc.forgetools.commands.DimIDCommand;
import net.joinedminds.mc.forgetools.commands.DropsCommand;
import net.joinedminds.mc.forgetools.commands.ForgeToolsCommand;
import net.joinedminds.mc.forgetools.commands.HealCommand;
import net.joinedminds.mc.forgetools.commands.InventoryCommand;
import net.joinedminds.mc.forgetools.commands.LagCommand;
import net.joinedminds.mc.forgetools.commands.LoadedChunksCommand;
import net.joinedminds.mc.forgetools.commands.MobsCommand;
import net.joinedminds.mc.forgetools.commands.SmiteCommand;


@Mod(modid="ForgeToolsToGraphite", name="Forge Tools To Graphite", version="1.0")
@NetworkMod(clientSideRequired=false, serverSideRequired=false)
public class ForgeTools
{
	private static final String dropsDName = "drops";
	private static final String dimIDDName = "dimid";
	private static final String forgeToolsDName = "forgeTools";
	private static final String healDName = "heal";
	private static final String inventoryDName = "inventory";
	private static final String lagDName = "lag";
	private static final String loadedChunksDName = "loadedChunks";
	private static final String mobsDName = "mobs";
	private static final String smiteDName = "smite";
	
	public static MinecraftServer server;
	public static ArrayList<String> advancedUsers;
	public static HashMap<String, String> commandsToLoad;
	public static boolean regexMatch;
	public static Configuration config;
	public static float killRadius;
	public static int timeout;
    private static boolean serverStarted = false;
    private static ScheduledExecutorService graphiteExecutor;

    @EventHandler
	public void Init(FMLInitializationEvent event)
	{
	}
	
	@EventHandler
	public void PreInit(FMLPreInitializationEvent event)
	{
		commandsToLoad = new HashMap<String, String>();
		config = new Configuration(event.getSuggestedConfigurationFile());
		reloadConfig();
	}

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        GameRegistry.registerPlayerTracker(new PlayerTracker());
    }
	
	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		server = FMLCommonHandler.instance().getMinecraftServerInstance();
		ServerCommandManager manager = (ServerCommandManager)server.getCommandManager();
		
		if(commandsToLoad.containsKey(dropsDName))
			manager.registerCommand(new DropsCommand(commandsToLoad.get(dropsDName)));
		if(commandsToLoad.containsKey(dimIDDName))
			manager.registerCommand(new DimIDCommand(commandsToLoad.get(dimIDDName)));
		if(commandsToLoad.containsKey(forgeToolsDName))
			manager.registerCommand(new ForgeToolsCommand(commandsToLoad.get(forgeToolsDName)));
		if(commandsToLoad.containsKey(healDName))
			manager.registerCommand(new HealCommand(commandsToLoad.get(healDName)));
		if(commandsToLoad.containsKey(inventoryDName))
			manager.registerCommand(new InventoryCommand(commandsToLoad.get(inventoryDName)));
		if(commandsToLoad.containsKey(lagDName))
			manager.registerCommand(new LagCommand(commandsToLoad.get(lagDName)));
		if(commandsToLoad.containsKey(loadedChunksDName))
			manager.registerCommand(new LoadedChunksCommand(commandsToLoad.get(loadedChunksDName)));
		if(commandsToLoad.containsKey(mobsDName))
			manager.registerCommand(new MobsCommand(commandsToLoad.get(mobsDName)));
		if(commandsToLoad.containsKey(smiteDName))
			manager.registerCommand(new SmiteCommand(commandsToLoad.get(smiteDName)));
		boolean changed = GraphiteConfig.getInstance().setDefaultPrefix(config, server);
        changed = changed || GraphiteConfig.getInstance().setDefaultEventTags(config, server);
        if (changed) {
            config.save();
        }
		System.out.println("ForgeTools: Registered " + commandsToLoad.keySet().size() + " commands");
		System.out.println("ForgeTools: Registered " + advancedUsers.size() + " advanced users");
        serverStarted = true;
        setupGraphiteLogging();
	}
	
	public static void reloadConfig()
	{
		config.load();
		
		String advUsersRaw = config.get(config.CATEGORY_GENERAL, "advancedUsers", "").getString();
		advancedUsers = new ArrayList<String>();
		advancedUsers.addAll(Arrays.asList(advUsersRaw.split(",\\s*")));
		
		regexMatch = config.get(config.CATEGORY_GENERAL, "regexSearch", false).getBoolean(false);
		
		ArrayList<Property> cmdData = new ArrayList<Property>();
			
		killRadius = (float)config.get(config.CATEGORY_GENERAL,  "killRadius", 32d).getDouble(32d);
		timeout = config.get(config.CATEGORY_GENERAL, "timeout", 10).getInt(10);
		
		cmdData.add(config.get("commands", dimIDDName, "dimid", "Commands should be a comma separated list of values.\n" +
				 																 "The first in the list will be used as the default, while the rest of the list will be aliases for that command.\n" +
				 																 "Leave the value blank if you wish to disable the command."));
		cmdData.add(config.get("commands", dropsDName, "drops"));
		cmdData.add(config.get("commands", forgeToolsDName, "forgetools,ft"));
		cmdData.add(config.get("commands", healDName, "heal"));
		cmdData.add(config.get("commands", inventoryDName, "inventory,inv"));
		cmdData.add(config.get("commands", lagDName, "lag,tps"));
		cmdData.add(config.get("commands", loadedChunksDName, "loadedchunks,lc"));
		cmdData.add(config.get("commands", mobsDName, "mobs"));
		cmdData.add(config.get("commands", smiteDName, "smite"));
		
		for(Property cmd : cmdData) {
			if(cmd.wasRead() && !cmd.getString().isEmpty())
				commandsToLoad.put(cmd.getName(), cmd.getString());
		}

        GraphiteConfig.configure(config);
		
		config.save();
        setupGraphiteLogging();
	}

    private static void setupGraphiteLogging() {
        if(serverStarted) {
            if (graphiteExecutor != null) {
                graphiteExecutor.shutdownNow();
                graphiteExecutor = null;
            }
            if (GraphiteConfig.getInstance().isEnabled()) {
                graphiteExecutor = Executors.newScheduledThreadPool(1);
                for(GraphiteConfig.Metric m : GraphiteConfig.Metric.values()) {
                    if (m.isEnabled()) {
                        try {
                            graphiteExecutor.scheduleAtFixedRate(m.newProbe(), 0, GraphiteConfig.getInstance().getInterval(), TimeUnit.SECONDS);
                        } catch (Exception e) {
                            System.err.println("Could not start the Graphite probe of " + m.name());
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public static void saveConfigUpdates()
	{
		String users = "";
		for(String s : advancedUsers)
		{
			users += s +", ";
		}
		users = users.substring(0, users.length()-2);
		config.get(config.CATEGORY_GENERAL, "advancedUsers",  "").set(users);
		config.save();
	}
}
