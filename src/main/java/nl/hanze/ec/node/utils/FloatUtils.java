package nl.hanze.ec.node.utils;

import org.json.JSONObject;

import java.math.BigDecimal;

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
}
