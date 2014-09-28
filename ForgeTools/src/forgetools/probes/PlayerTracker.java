package forgetools.probes;

import cpw.mods.fml.common.IPlayerTracker;
import forgetools.Graphite;
import forgetools.GraphiteConfig;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;

/**
 * Description
 *
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
public class PlayerTracker implements IPlayerTracker {
    @Override
    public void onPlayerLogin(EntityPlayer player) {
        GraphiteConfig.EventConfig config = GraphiteConfig.getInstance().getEventConfig();
        if (config != null && config.isEnablePlayerLogin() && config.getEventUrl() != null) {
            try {
                Graphite.sendEvent(player.getDisplayName() + " Login", "player", "login", player.getDisplayName());
            } catch (IOException e) {
                System.err.println("Can't send player event to Graphite");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPlayerLogout(EntityPlayer player) {
        GraphiteConfig.EventConfig config = GraphiteConfig.getInstance().getEventConfig();
        if (config != null && config.isEnablePlayerLogin() && config.getEventUrl() != null) {
            try {
                Graphite.sendEvent(player.getDisplayName() + " Logout",
                        "player", "logout", player.getDisplayName());
            } catch (IOException e) {
                System.err.println("Can't send player event to Graphite");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPlayerChangedDimension(EntityPlayer player) {
        GraphiteConfig.EventConfig config = GraphiteConfig.getInstance().getEventConfig();
        if (config != null && config.isEnablePlayerChangedDimension() && config.getEventUrl() != null) {
            try {
                long dimensionId = player.worldObj.provider.dimensionId;
                Graphite.sendEvent(player.getDisplayName() + " Changed dimension to " + dimensionId,
                        "player", "dimension_change", "DIM" + dimensionId, player.getDisplayName());
            } catch (IOException e) {
                System.err.println("Can't send player event to Graphite");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPlayerRespawn(EntityPlayer player) {
        GraphiteConfig.EventConfig config = GraphiteConfig.getInstance().getEventConfig();
        if (config != null && config.isEnablePlayerRespawn() && config.getEventUrl() != null) {
            try {
                Graphite.sendEvent(player.getDisplayName() + " Respawned",
                        "player", "respawn", player.getDisplayName());
            } catch (IOException e) {
                System.err.println("Can't send player event to Graphite");
                e.printStackTrace();
            }
        }
    }
}
