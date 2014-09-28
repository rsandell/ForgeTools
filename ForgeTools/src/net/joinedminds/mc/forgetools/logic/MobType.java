package net.joinedminds.mc.forgetools.logic;

/**
 * Description
 *
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
public enum MobType {
    passive, hostile, npc, all, none;

    public boolean is(MobType type) {
        if (type == none) {
            return this == type;
        } else {
            return this == type || type == all;
        }
    }
}
