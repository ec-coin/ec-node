package nl.hanze.ec.node.utils;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.HashMap;

public class FloatUtils {
    public static float parse(JSONObject json, String key) {
        float amount;
        if (json.get(key) instanceof Integer) {
            amount = ((Integer) json.get(key)).floatValue();
        } else {
            amount = ((BigDecimal) json.get(key)).floatValue();
        }

        return amount;
    }

    public static float parse(HashMap<?, ?> obj, String key) {
        float amount;
        if (obj.get(key) instanceof Integer) {
            amount = ((Integer) obj.get(key)).floatValue();
        } else {
            amount = ((BigDecimal) obj.get(key)).floatValue();
        }

        return amount;
    }
}
