package forgetools;

import forgetools.probes.DropsProbe;
import forgetools.probes.LoadedChunksProbe;
import forgetools.probes.MobsProbe;
import forgetools.probes.PlayersProbe;
import forgetools.probes.TpsProbe;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * Description
 *
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
public class GraphiteConfig {
    private static GraphiteConfig instance;
    public static final String DEFAULT_HOST = "localhost";
    public static final String DEFAULT_PREFIX = "minecraft.";
    public static final int DEFAULT_PORT = 2003;
    public static final int DEFAULT_INTERVAL = (int)TimeUnit.MINUTES.toSeconds(1);
    public static final int MIN_INTERVAL = 10;
    public static final boolean DEFAULT_ENABLED = false;
    public static final String GENERAL_CATEGORY = "graphite";
    public static final String METRICS_CATEGORY = "graphite_metrics";
    private String prefix;
    private boolean enabled;
    private String serverHost;
    private int serverPort;
    private int interval;
    private EventConfig eventConfig;

    protected GraphiteConfig() {

    }

    private void _configure(Configuration config) {
        config.addCustomCategoryComment(GENERAL_CATEGORY, "General config of your graphite server");
        this.enabled = config.get(GENERAL_CATEGORY, "enabled", DEFAULT_ENABLED, "Is reporting to graphite enabled.").getBoolean(DEFAULT_ENABLED);
        this.serverHost = config.get(GENERAL_CATEGORY, "host", DEFAULT_HOST, "The hostname of the graphite server").getString();
        this.serverPort = config.get(GENERAL_CATEGORY, "port", DEFAULT_PORT, "The port of the graphite server where it's listening for incoming data").getInt(DEFAULT_PORT);
        Property prefixProp = getPrefix(config);
        this.prefix = prefixProp.getString();
        this.prefix = this.prefix.trim().replaceAll("\\s", " ");
        while (this.prefix.endsWith(".")) {
            this.prefix = StringUtils.removeEnd(this.prefix, ".");
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

        for (Metric m : Metric.values()) {
            m.configure(config);
        }

        this.eventConfig = EventConfig.configured(config);
    }

    public static GraphiteConfig getInstance() {
        if (instance == null) {
            instance = new GraphiteConfig();
        }
        return instance;
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getServerHost() {
        return serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getInterval() {
        return interval;
    }

    public EventConfig getEventConfig() {
        return eventConfig;
    }

    private Property getInterval(Configuration config) {
        return config.get(GENERAL_CATEGORY, "interval", DEFAULT_INTERVAL, "Number of seconds between each measurement. Minimum 10.");
    }

    public static void configure(Configuration config) {
        getInstance()._configure(config);
    }

    public static enum Metric {
        drops("{0}.total.drops", "{0}.dim.{1}.drops", DropsProbe.class),
        tps("{0}.total.tps", "{0}.dim.{1}.tps", TpsProbe.class),
        loadedChunks("{0}.total.loaded_chunks", "{0}.dim.{1}.loaded_chunks", LoadedChunksProbe.class),
        mobs("{0}.total.mobs", "{0}.dim.{1}.mobs", MobsProbe.class),
        players("{0}.total.players", "{0}.dim.{1}.players", PlayersProbe.class);

        final String pattern_total_default;
        final String pattern_dim_default;
        final Class<? extends Runnable> probeClass;

        private boolean enabled = false;
        private String patternTotal;
        private String patternDim;

        Metric(String pattern_total_default, String pattern_dim_default, Class<? extends Runnable> probeClass) {
            this.pattern_total_default = pattern_total_default;
            this.pattern_dim_default = pattern_dim_default;
            this.probeClass = probeClass;
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

        public Runnable newProbe() throws IllegalAccessException, InstantiationException, InvocationTargetException {
            try {
                Constructor<? extends Runnable> constructor = probeClass.getConstructor(getClass());
                return constructor.newInstance(this);
            } catch (NoSuchMethodException e) {
                return probeClass.newInstance();
            }
        }
    }

    private Property getPrefix(Configuration config) {
        return config.get(GENERAL_CATEGORY, "prefix", DEFAULT_PREFIX, "The name prefix for the data sent to graphite");
    }

    public boolean setDefaultPrefix(Configuration config, MinecraftServer server) {
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
            return true;
        }
        return false;
    }

    public boolean setDefaultEventTags(Configuration config, MinecraftServer server) {
        if (eventConfig != null && (eventConfig.getDefaultTags() == null || eventConfig.getDefaultTags().length <= 0)) {
            String name = server.getEntityWorld().getWorldInfo().getWorldName();
            String identityTag;
            if (name != null && !name.isEmpty()) {
                identityTag = getServerIdentity(server) + "-" + name.replaceAll("\\s", "_");
            } else {
                identityTag = getServerIdentity(server);
            }
            eventConfig.setDefaultTags(new String[]{identityTag});
            EventConfig.getDefaultTags(config).set(eventConfig.getDefaultTags());
            return true;
        }
        return false;
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


    public static class EventConfig {
        public static final String CATEGORY = "graphite_events";
        public static final String DEFAULT_URL = "http://localhost/graphite";

        private String[] defaultTags = null;
        private final boolean enablePlayerLogin;
        private final boolean enablePlayerRespawn;
        private final boolean enablePlayerChangedDimension;
        private final URL url;
        private URL eventUrl;

        protected EventConfig(boolean enablePlayerLogin, boolean enablePlayerRespawn, boolean enablePlayerChangedDimension, String[] defaultTags, URL url) {
            this.enablePlayerLogin = enablePlayerLogin;
            this.enablePlayerRespawn = enablePlayerRespawn;
            this.enablePlayerChangedDimension = enablePlayerChangedDimension;
            this.defaultTags = defaultTags;
            this.url = url;
            if (url != null) {
                String s = this.url.toString();
                if (!s.endsWith("/")) {
                    s += "/events/";
                } else {
                    s += "events/";
                }
                try {
                    this.eventUrl = new URL(s);
                } catch (MalformedURLException e) {
                    this.eventUrl = null;
                    e.printStackTrace();
                }
            } else {
                this.eventUrl = null;
            }
        }

        public String[] getDefaultTags() {
            return defaultTags;
        }

        public void setDefaultTags(String[] defaultTags) {
            this.defaultTags = defaultTags;
        }

        public boolean isEnablePlayerLogin() {
            return enablePlayerLogin;
        }

        public boolean isEnablePlayerRespawn() {
            return enablePlayerRespawn;
        }

        public boolean isEnablePlayerChangedDimension() {
            return enablePlayerChangedDimension;
        }

        public URL getUrl() {
            return url;
        }

        public URL getEventUrl() {
            return this.eventUrl;
        }

        public static EventConfig configured(Configuration config) {
            config.addCustomCategoryComment(CATEGORY, "Special events that could be logged to graphite.");
            String urlString = config.get(CATEGORY, "url", DEFAULT_URL, "The base url of your graphite server.").getString();
            URL url = null;
            try {
                url = new URL(urlString);
            } catch (MalformedURLException e) {
                System.err.println("Failed to parse the URL " + url);
                e.printStackTrace();
            }
            if (url != null && !("http".equalsIgnoreCase(url.getProtocol()) || "https".equalsIgnoreCase(url.getProtocol()))) {
                System.err.println("The graphite url has a bad protocol!");
                url = null;
            }
            boolean playerLogin = config.get(CATEGORY, "playerLogin_enabled", false, "If player login/logout should be logged as an event.").getBoolean(false);
            boolean playerRespawn = config.get(CATEGORY, "playerRespawn_enabled", false, "If player respawn should be logged as an event.").getBoolean(false);
            boolean playerChangeDimension = config.get(CATEGORY, "playerChangeDimension_enabled", false, "If it should be logrgen an event when a player changes dimension.").getBoolean(false);

            EventConfig evcfg = new EventConfig(playerLogin, playerRespawn, playerChangeDimension, EventConfig.getDefaultTags(config).getStringList(), url);
            return evcfg;
        }

        public static Property getDefaultTags(Configuration config) {
            return config.get(CATEGORY, "default_tags", new String[]{}, "Tags that should be in all events to identify this server instance.");
        }
    }
}
