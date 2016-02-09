package com.onkiup.minedroid.idea;

import groovy.json.StringEscapeUtils;

import java.util.List;

/**
 * Created by chedim on 7/26/15.
 */
class Style extends ResourceLink {
    protected List<Style> children;
    protected String parent;
    protected String name;

    Style(String name) {
        super(name, "styles");
    }

    Style(String name, String parent) {
        this(name);
        this.parent = parent;
    }

    public void addChild(Style child) {
        children.add(child);
    }


    @Override
    public String toString() {
//        String result = "class "+name[0..-5]+" extends Style {\n";
//        result += "\tprotected static Style getInstance() {\n";
        String result = "new Style("+super.toString()+", R.class";
        if (parent != null) {
            result += ", \""+ StringEscapeUtils.escapeJava(parent)+"\"";
        }

        result += ")";
//        result += "\t}\n\n";

//        for (Style child: children) {
//            result += children.toString().replaceAll("\\n", "\n\t") + "\n";
//        }

//        result += "}";

        return result;
    }

    @Override
    String getFieldType() {
        return "Style";
    }
}
