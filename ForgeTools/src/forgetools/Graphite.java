package forgetools;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import forgetools.util.SelfManagingSocket;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.text.MessageFormat;

/**
 * Description
 *
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
public class Graphite {

    private static SelfManagingSocket out;

    public static void sendDimData(GraphiteConfig.Metric metric, String dim, String nameExtra, Number value) {
        String db = MessageFormat.format(metric.getPatternDim(), GraphiteConfig.getInstance().getPrefix(), dim);
        db = addExtra(nameExtra, db);
        sendData(db, value);
    }

    public static void sendTotalData(GraphiteConfig.Metric metric, String nameExtra, Number value) {
        String db = MessageFormat.format(metric.getPatternTotal(), GraphiteConfig.getInstance().getPrefix());
        db = addExtra(nameExtra, db);
        sendData(db, value);
    }

    private static String addExtra(String nameExtra, String db) {
        if (nameExtra != null && !nameExtra.isEmpty()) {
            if (!db.endsWith(".") && !nameExtra.startsWith(".")) {
                db += ".";
            }
            db += nameExtra;
        }
        return db;
    }

    public static void sendData(String db, Number value) {
        long time = System.currentTimeMillis() / 1000;
        sendData(db, time, value);
    }

    public static void sendData(String db, long time, Number value) {
        setupConnection();
        if(!value.toString().contains(".")) {
            System.out.printf("%s %d %d%n", db, value, time);
            out.printf("%s %d %d%n", db, value, time);
        } else {
            System.out.printf("%s %f %d%n", db, value, time);
            out.printf("%s %f %d%n", db, value, time);
        }
    }

    private static synchronized void setupConnection() {
        if (out == null) {
            out = new SelfManagingSocket(GraphiteConfig.getInstance().getServerHost(), GraphiteConfig.getInstance().getServerPort());
        }
    }


    public static void sendEvent(String what, String... tags) throws IOException {
        GraphiteConfig.EventConfig config = GraphiteConfig.getInstance().getEventConfig();
        if (config != null) {
            URL url = config.getEventUrl();
            if (url != null) {
                JsonObject j = new JsonObject();
                j.addProperty("what", what);
                JsonArray array = new JsonArray();
                for (String t : tags) {
                    array.add(new JsonPrimitive(t));
                }
                j.add("tags", array);

                String data = j.toString();

                Proxy proxy = ForgeTools.server == null ? null : ForgeTools.server.getServerProxy();
                if (proxy == null) {
                    proxy = Proxy.NO_PROXY;
                }

                HttpURLConnection http = (HttpURLConnection)url.openConnection(proxy);
                http.setRequestMethod("POST");
                http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                http.setRequestProperty("Content-Length", "" + data.getBytes().length);
                http.setRequestProperty("Content-Language", "en-US");
                http.setUseCaches(false);
                http.setDoInput(true);
                http.setDoOutput(true);
                try (DataOutputStream out = new DataOutputStream(http.getOutputStream())) {
                    out.writeBytes(data);
                    out.flush();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream()))) {
                        StringBuffer str = new StringBuffer();
                        String s1;

                        while ((s1 = reader.readLine()) != null) {
                            str.append(s1);
                            str.append('\r');
                        }
                    }
                }
            }
        }
    }
}
