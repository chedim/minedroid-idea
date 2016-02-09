package com.onkiup.minedroid.idea;

import groovy.json.StringEscapeUtils;

/**
 * Created by chedim on 7/26/15.
 */
public class EnvValue extends EnvParams {
    public Object value;

    EnvValue() {
        super();
    }

    EnvValue(String[] parts) {
        super(parts);
    }

    EnvValue(String[] parts, Object value) {
        super(parts);
        this.value = value;
    }

    @Override
    protected EnvValue clone() {
        EnvValue copy = new EnvValue();
        copy.value = value;

        copy.graphics = this.graphics;
        copy.mode = mode;
        copy.lang = lang;
        copy.version = version;

        return copy;
    }

    @Override
    public String toString() {
        String name = super.toString();
        name = name.substring(13, name.length() - 1);
        String gen = "new EnvValue" + name;
        gen += ", \"" + StringEscapeUtils.escapeJava(value.toString()) + "\")";

        return gen;
    }
}
