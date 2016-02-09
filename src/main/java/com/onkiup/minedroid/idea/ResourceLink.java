package com.onkiup.minedroid.idea;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chedim on 7/26/15.
 */
class ResourceLink {
    protected List<EnvParams> envs = new ArrayList<EnvParams>();
    protected String name;
    protected String type;

    ResourceLink(String name, String type) {
        this.name = name;
        this.type = type;
    }

    void addEnv(EnvParams env) {
        envs.add(env);
    }

    String getName() {
        return name;
    }

    String getFieldType() {
        return "ResourceLink";
    }


    @Override
    public String toString() {
        String[] envValues = new String[envs.size()];
        for (int i = 0; i < envs.size(); i++) {
            envValues[i] = envs.get(i).toString();
        }
        return "new ResourceLink(MODID, \""+type+"\", \""+name+"\", new EnvParams[] { "+ String.join(", ", envValues) + "})";
    }
}
