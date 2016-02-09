package com.onkiup.minedroid.idea;

import groovy.json.StringEscapeUtils;

/**
 * Created by chedim on 7/26/15.
 */
class Plural {
    public String zero, one, two, few, many, other;


    @Override
    public String toString() {
        String gen =  "new Plural(";
        gen += (zero == null ? "null" : '"' + StringEscapeUtils.escapeJava(zero) + '"') + ", ";
        gen += (one == null ? "null" : '"' + StringEscapeUtils.escapeJava(one) + '"') + ", ";
        gen += (two == null ? "null" : '"' + StringEscapeUtils.escapeJava(two) + '"') + ", ";
        gen += (few == null ? "null" : '"' + StringEscapeUtils.escapeJava(few) + '"') + ", ";
        gen += (many == null ? "null" : '"' + StringEscapeUtils.escapeJava(many) + '"') + ", ";
        gen += (other == null ? "null" : '"' + StringEscapeUtils.escapeJava(other) + '"') + ", ";

        gen += ")";

        return gen;
    }
}
