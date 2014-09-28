package forgetools.probes;

import forgetools.GraphiteConfig;

/**
 * Description
 *
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
public abstract class Probe implements Runnable {
    protected final GraphiteConfig.Metric metric;

    protected Probe(GraphiteConfig.Metric metric) {
        this.metric = metric;
    }
}
