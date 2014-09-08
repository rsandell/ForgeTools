package forgetools.logic;

/**
 * Description
 *
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
public class MobData {
    private int amtHos = 0;
    private int amtPas = 0;
    private int amtNPC = 0;
    private int amtRemoved = 0;

    public int getTotal() {
        return amtHos + amtPas + amtNPC;
    }

    public int getAmtHos() {
        return amtHos;
    }

    public void incAmtHos() {
        amtHos++;
    }

    public int getAmtPas() {
        return amtPas;
    }

    public void incAmtPas() {
        amtPas++;
    }

    public int getAmtNPC() {
        return amtNPC;
    }

    public void incAmtNPC() {
        amtNPC++;
    }

    public int getAmtRemoved() {
        return amtRemoved;
    }

    public void incAmtRemoved() {
        amtRemoved++;
    }

    public void incTotal(MobData worldData) {
        amtHos += worldData.amtHos;
        amtPas += worldData.amtPas;
        amtNPC += worldData.amtNPC;
        amtRemoved += worldData.amtRemoved;
    }
}
