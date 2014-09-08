package forgetools;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * Description
 *
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
public class GraphiteConfig {
    public static final String DEFAULT_HOST = "localhost";
    public static final String DEFAULT_PREFIX = "minecraft.";
    public static final int DEFAULT_PORT = 2003;
    public static final long DEFAULT_INTERVAL = TimeUnit.MINUTES.toSeconds(1);
    public static final int MIN_INTERVAL = 10;
    public static final boolean DEFAULT_ENABLED = false;
    public static final String GENERAL_CATEGORY = "graphite";
    public static final String METRICS_CATEGORY = "graphite_metrics";
    private String prefix;
    private boolean enabled;
    private String serverHost;
    private int serverPort;
    private int interval;

    public GraphiteConfig(Configuration config) {
        config.addCustomCategoryComment(GENERAL_CATEGORY, "General config of your graphite server");
        this.enabled = config.get(GENERAL_CATEGORY, "enabled", DEFAULT_ENABLED, "Is reporting to graphite enabled.").getBoolean(DEFAULT_ENABLED);
        this.serverHost = config.get(GENERAL_CATEGORY, "host", DEFAULT_HOST, "The hostname of the graphite server").getString();
        this.serverPort  = config.get(GENERAL_CATEGORY, "port", DEFAULT_PORT, "The port of the graphite server where it's listening for incoming data").getInt(DEFAULT_PORT);
        Property prefixProp = getPrefix(config);
        this.prefix = prefixProp.getString();
        this.prefix = this.prefix.trim().replaceAll("\\s", " ");
        if (!this.prefix.endsWith(".")) {
            this.prefix = this.prefix + ".";
        }
        if (!prefixProp.getString().equals(this.prefix)) {
            prefixProp.set(this.prefix);
        }
        Property intervalProp = getInterval(config);
        this.interval = intervalProp.getInt((int)DEFAULT_INTERVAL);
        if (this.interval < MIN_INTERVAL) {
            this.interval = MIN_INTERVAL;
            intervalProp.set(this.interval);
        }

        for(Metric m : Metric.values()) {
            m.configure(config);
        }
    }

    private Property getInterval(Configuration config) {
        return config.get(GENERAL_CATEGORY, "interval", DEFAULT_INTERVAL, "Number of seconds between each measurement. Minimum 10.");
    }

    static enum Metric {
        drops("{0}.total.drops", "{0}.dim.{1}.drops"),
        tps("{0}.total.tps", "{0}.dim.{1}.tps"),
        loadedChunks("{0}.total.loaded_chunks", "{0}.dim.{1}.loaded_chunks"),
        mobs("{0}.total.mobs", "{0}.dim.{1}.mobs"),
        players("{0}.total.players", "{0}.dim.{1}.players");

        final String pattern_total_default;
        final String pattern_dim_default;

        private boolean enabled = false;
        private String patternTotal;
        private String patternDim;

        Metric(String pattern_total_default, String pattern_dim_default) {
            this.pattern_total_default = pattern_total_default;
            this.pattern_dim_default = pattern_dim_default;
        }

        public void configure(Configuration config) {
            this.enabled = config.get(METRICS_CATEGORY, this.name() + "_enabled", true,
                    "If this metric is enabled or not.").getBoolean(true);
            this.patternTotal = config.get(METRICS_CATEGORY, this.name() + "_total_pattern", pattern_total_default,
                    "The name pattern of the total count of this metric. 0 = prefix").getString();
            this.patternDim = config.get(METRICS_CATEGORY, this.name() + "_dim_pattern", pattern_dim_default,
                    "The name pattern of the individual dimensions count of this metric. 0 = prefix, 1 = dimension id").getString();
        }

        boolean isEnabled() {
            return enabled;
        }

        String getPatternTotal() {
            return patternTotal;
        }

        String getPatternDim() {
            return patternDim;
        }
    }

    private Property getPrefix(Configuration config) {
        return config.get(GENERAL_CATEGORY, "prefix", DEFAULT_PREFIX, "The name prefix for the data sent to graphite");
    }

    public void setDefaultPrefix(Configuration config, MinecraftServer server) {
        if (DEFAULT_PREFIX.equals(this.prefix)) {
            String name = server.getEntityWorld().getWorldInfo().getWorldName();
            if (name != null && !name.isEmpty()) {
                this.prefix = DEFAULT_PREFIX
                        + getServerIdentity(server) + "."
                        + name.replaceAll("\\s", "_")
                        + ".";

            } else {
                this.prefix = DEFAULT_PREFIX
                        + getServerIdentity(server) + ".";
            }
            getPrefix(config).set(this.prefix);
            config.save();
        }
    }

    private String getServerIdentity(MinecraftServer server) {
        String id = server.getServerHostname();
        if (id == null || id.trim().isEmpty()) {
            id = getHostName();
        } else {
            id = id.trim();
        }
        return id + "_" + String.valueOf(server.getServerPort());
    }

    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }
}
