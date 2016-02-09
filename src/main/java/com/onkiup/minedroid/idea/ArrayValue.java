package com.onkiup.minedroid.idea;

import groovy.json.StringEscapeUtils;

/**
 * Created by chedim on 7/26/15.
 */
public class ArrayValue {
    public String[] value;

    @Override
    public String toString() {
        for (int i=0; i<value.length; i++) {
            value[i] = StringEscapeUtils.escapeJava(value[i]);
        }

        return "new String[] { \"" + String.join("\", \"", value) + "\" }";
    }
}
