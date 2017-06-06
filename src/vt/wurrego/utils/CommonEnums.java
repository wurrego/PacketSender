package vt.wurrego.utils;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Class holding common enums
 * Created by wurrego on 5/7/17.
 */
public class CommonEnums {

    public enum StateStatus {
        OFF(0),
        IDLE(1),
        LOW(2),
        HIGH(3);

        private int value;

        private StateStatus(int value) {this.value = value; }

        private static final Map<Integer, StateStatus> lookup = new HashMap<Integer, StateStatus>();

        static {
            for (StateStatus d : EnumSet.allOf(StateStatus.class))
                lookup.put(d.value, d);
        }

        public static StateStatus getValue(int i) { return lookup.get(i); }

        @Override
        public String toString() {

            String s;
            switch (this) {
                case OFF:
                    s = new String("OFF");
                    break;
                case IDLE:
                    s = new String("IDLE");
                    break;
                case LOW:
                    s = new String("LOW");
                    break;
                case HIGH:
                    s = new String("HIGH");
                    break;
                default:
                    s = new String("Undefined Data Type");
                    break;
            }
            return s;
        }
    }





}
