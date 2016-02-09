package com.onkiup.minedroid.idea;

/**
 * Created by chedim on 7/26/15.
 */
public class EnvParams {
    String lang;
    String version;
    String mode;
    String graphics;

    EnvParams() {

    }

    EnvParams(String[] parts) {
        String[] params = new String[4];
        int minIndex = 4;
        for (int i = parts.length - 1; i > 0; i--) {
            String test = parts[i];
            int index = -1;
            if (isVersion(test)) {
                index = 0;
            } else if (isGraphic(test)) {
                index = 1;
            } else if (isMode(test)) {
                index = 2;
            } else if (isLang(test)) {
                index = 3;
            } else {
                continue;
            }
            if (index >= minIndex) throw new RuntimeException("Invalid qualifier order: " + String.join("-", parts));
            minIndex = index;
            params[index] = test;
        }

        version = params[0] == null ? "null" : '"' + params[0] + '"';
        graphics = params[1] == null ? "null" : "EnvParams.Graphics." + params[1].toUpperCase();
        mode = params[2] == null ? "null": "EnvParams.Mode." + params[2].toUpperCase();
        lang = params[3] == null ? "null" : '"' + params[3] + '"';
    }

    protected boolean isLang(String test) {
        return test.length() == 2;
    }

    protected boolean isVersion(String test) {
        return test.matches("/\\d\\./");
    }

    protected boolean isMode(String test) {
        return test.equals("local") || test.equals("remote");
    }

    protected boolean isGraphic(String test) {
        return test.equals("fancy") || test.equals("fast");
    }

    @Override
    public String toString() {
        return "new EnvParams("+lang+", "+version+", "+graphics+", "+mode+")";
    }

    protected static String getMCVersion(String v) {
        String[] version = v.split("/\\./");
        int result = 0;
        for (int i = 0; i < Math.min(version.length, 3); i++) {
            result += Integer.valueOf(version[i]) * Math.pow(10, 3 - i);
        }
        return String.valueOf(result);
    }

    @Override
    protected EnvParams clone() {
        EnvParams copy = new EnvParams();
        copy.graphics = graphics;
        copy.lang = lang;
        copy.mode = mode;
        copy.version = version;

        return copy;
    }
}
