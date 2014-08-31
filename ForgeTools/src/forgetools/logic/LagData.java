package forgetools.logic;

/**
* Description
*
* @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
*/
public class LagData {
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
