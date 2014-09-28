package forgetools.probes;

import forgetools.ForgeTools;
import forgetools.Graphite;
import forgetools.GraphiteConfig;
import forgetools.logic.Functions;

/**
 * Description
 *
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
public class PlayersProbe extends Probe {

    public PlayersProbe(GraphiteConfig.Metric metric) {
        super(metric);
    }

    @Override
    public void run() {
        int total = ForgeTools.server.getCurrentPlayerCount();
        Graphite.sendTotalData(metric, "count", total);
    }
}
