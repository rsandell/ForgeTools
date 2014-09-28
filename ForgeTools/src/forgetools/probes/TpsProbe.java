package forgetools.probes;

import forgetools.ForgeTools;
import forgetools.Graphite;
import forgetools.GraphiteConfig;
import forgetools.logic.Functions;
import forgetools.logic.LagData;

/**
 * Description
 *
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
public class TpsProbe extends Probe {

    public TpsProbe(GraphiteConfig.Metric metric) {
        super(metric);
    }

    @Override
    public void run() {
        LagData[] dimDatas = Functions.countLag();
        LagData total = Functions.countLag(null, null, ForgeTools.server.tickTimeArray);

        Graphite.sendTotalData(metric, "tps" , total.tps);
        Graphite.sendTotalData(metric, "tick.ms" , total.tickMS);
        Graphite.sendTotalData(metric, "tick.percent" , total.tickPct);

        for(LagData data: dimDatas) {
            Graphite.sendDimData(metric, String.valueOf(data.dim), "tps", data.tps);
            Graphite.sendDimData(metric, String.valueOf(data.dim), "tick.ms", data.tickMS);
            Graphite.sendDimData(metric, String.valueOf(data.dim), "tick.percent", data.tickPct);
        }
    }
}
