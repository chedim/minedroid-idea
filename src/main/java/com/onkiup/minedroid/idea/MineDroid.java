package com.onkiup.minedroid.idea;

import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import groovy.io.FileType;
import groovy.json.JsonSlurper;
import groovy.util.Node;
import groovy.util.XmlParser;
import groovy.xml.Namespace;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chedim on 7/26/15.
 */
public class MineDroid {
    public static final String NS = "http://onkiup.com/minecraft/xml";
    static final int typeId = 0x10000000;
    static final int typeArray = 0x20000000;
    static final int typePlural = 0x30000000;
    // allows a child to resize to fit it's content
    public static final int WRAP_CONTENT = -20000;
    // disallows a child from resizing
    public static final int MATCH_PARENT = -10000;

    protected VirtualFile resDir, srcDir;

    protected HashMap<String, HashMap<String, ResourceLink>> resources =
            new HashMap<String, HashMap<String, ResourceLink>>();

    protected HashMap<String, HashMap<String, ArrayList<EnvValue>>> values =
            new HashMap<String, HashMap<String, ArrayList<EnvValue>>>();

    protected ArrayList<String> ids = new ArrayList<String>();

    protected static final Namespace ns = new Namespace("http://onkiup.com/minecraft/xml");
    protected static HashMap<String, PsiElement> elementsById = new HashMap<>();

    public MineDroid(VirtualFile resDir, VirtualFile srcDir) {
        this.resDir = resDir;
        this.srcDir = srcDir;
    }

    public synchronized void process() throws Exception {
        VirtualFile infoFile = resDir.findChild("mcmod.info");
        JsonSlurper json = new JsonSlurper();
        if (infoFile != null && infoFile.exists()) {
            Map modinfo = (Map) ((List) json.parse(infoFile.getInputStream())).get(0);
            String pkg = (String) modinfo.get("package");
            String modid = (String) modinfo.get("modid");
            VirtualFile assetsDir = resDir.findChild("assets");
            if (assetsDir == null) return;
            assetsDir = assetsDir.findChild(modid);
            processAssets(assetsDir);
            String generated = generateR(pkg, modid);
            if (generated != null && generated.length() > 0) {
                File output = new File (srcDir.getPath(), pkg.replaceAll("\\.", "/"));
                output.mkdirs();
                output = new File(output, "R.java");
                try {
                    PrintWriter writer = new PrintWriter(output, "UTF-8");
                    writer.write(generated);
                    writer.close();
                    LocalFileSystem.getInstance().refreshAndFindFileByIoFile(output);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void processAssets(VirtualFile dir) {
        resources.clear();
        values.clear();
        ids.clear();
        if (dir.exists()) {
            for (VirtualFile f: dir.getChildren()) {
                String name = f.getName();
                if (name.startsWith(".") || !f.isDirectory()) continue;
                String[] parts = name.split("/-/");
                String type = parts[0];
                if (type.equals("values")) {
                    processValues(parts, f);
                } else {
                    processResources(type, parts, f);
                }
            }
        }
    }

    private void processValues(String[] qualifier, VirtualFile dir) {
        EnvValue env = new EnvValue(qualifier);
        try {
            for (VirtualFile sub: dir.getChildren()) {
                if (sub.getName().startsWith(".") || sub.isDirectory()) continue;
                try {
                    Node xml = new XmlParser().parse(sub.getInputStream());
                    for (Object it: xml.children()) {
                        Node child = (Node) it;
                        String type = (String) child.name().toString();
                        String name = (String) child.attribute("name").toString();
                        EnvValue v = env.clone();
                        if (type.equals("string")) {
                            v.value = child.text();
                        } else if (type.equals("array")) {
                            v.value = parseNodeArray(child);
                        } else if (type.equals("plural")) {
                            v.value = parseNodePlural(child);
                        }

                        if (!values.containsKey(type)) {
                            values.put(type, new HashMap<String, ArrayList<EnvValue>>());
                        }

                        if (!values.get(type).containsKey(name)) {
                            values.get(type).put(name, new ArrayList<EnvValue>());
                        }

                        values.get(type).get(name).add(v);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    protected static ArrayValue parseNodeArray(Node node) {
        List children = node.children();
        int count = children.size();
        ArrayValue result = new ArrayValue();
        result.value = new String[count];
        for (int i = 0; i < count; i++) {
            Node child = (Node) children.get(i);
            result.value[i] = child.text();
        }

        return result;
    }

    static Plural parseNodePlural(Node node) {
        Plural result = new Plural();
        Class pluralClass = Plural.class;
        for (Object it : node.children()) {
            Node child = (Node) it;
            try {
                Field target = pluralClass.getField((String) child.attribute("quantity"));
                target.set(result, child.text());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }


    protected void processResources(String type, String[] parts, VirtualFile dir) {
        if (!resources.containsKey(type)) {
            resources.put(type, new HashMap<String, ResourceLink>());
        }

        EnvParams p = new EnvParams(parts);

        FileType ft = (type.equals("ninepatches") ? FileType.DIRECTORIES : FileType.FILES);

        for (VirtualFile it: dir.getChildren()) {
            String name = it.getName();

            if (name.startsWith(".")) continue;
            if (it.isDirectory() && ft == FileType.FILES) continue;
            if (!it.isDirectory() && ft == FileType.DIRECTORIES) continue;

            if (!resources.get(type).containsKey(name)) {
                if (type.equals("styles")) resources.get(type).put(name, createStyle(it));
                else resources.get(type).put(name, new ResourceLink(name, type));
            }

            int l = name.length();
            if (type.equals("layouts") && name.endsWith(".xml")) {
                try {
                    Node file = new XmlParser().parse(it.getInputStream());
                    parseIds(file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try {
                resources.get(type).get(name).addEnv(p);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    protected void parseIds(Node node) {
        String id = (String) node.attribute(ns.get("id"));
        if (id == null) id = (String) node.attribute("id");
        if (id != null && id.length() > 0) {
            if (!ids.contains(id)) {
                ids.add(id);
            };
        }

        for (Object it: node.children()) {
            parseIds((Node) it);
        }
    }

    protected static Style createStyle(VirtualFile source) {
        try {
            Node node = new XmlParser().parse(source.getInputStream());
            String parent = (String) node.attribute("parent");
            return new Style(source.getName(), parent);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    protected String generateR(String pkg, String modid) {
        String generated = "package " + pkg + ";\n\n" +
                "import com.onkiup.minedroid.gui.resources.*;\n" +
                "import com.onkiup.minedroid.gui.MineDroid;\n\n";

        generated += "/**\n";
        generated += " * This class is auto generated.\n";
        generated += " * Manually made changes will be discarded.\n";
        generated += "**/\n";
        generated += "public final class R {\n";
        generated += "\tfinal static String MODID = \"" + modid + "\";\n";

        generated += genIdsClass();
        generated += genValuesClass("string", "string");
        generated += genValuesClass("array", "array");
        generated += genValuesClass("plural", "plural");
        generated += genValuesClass("dimen", "dimen");
        generated += genValuesClass("color", "color");

        generated += genResourceClass("layout", "layouts");
        generated += genResourceClass("drawable", "drawables");
        generated += genResourceClass("texture", "textures");
        generated += genResourceClass("ninepatch", "ninepatches");
        generated += genResourceClass("style", "styles");
        generated += genResourceClass("raw", "raw");

        generated += "}";

        return generated;
    }

    protected String genIdsClass() {
        String generated = "\tpublic final static class id {\n";
        for (int i = 0; i < ids.size(); i++) {
            String it = ids.get(i).replaceAll("^@id/", "");
            generated += "\t\tpublic final static int "+it+" = " + (typeId + i) + ";\n";
        }

        generated += "\t}\n\n";

        return generated;
    }

    protected String genResourceClass(String className, String type) {
        if (!resources.containsKey(type)) return "";

        String generated = "\tpublic final static class "+className+" {\n";
        for (String it: resources.get(type).keySet()) {
            ResourceLink item = resources.get(type).get(it);
            String name = item.getName();

            String field = item.getName();
            if (!type.equals("ninepatches")) field = field.substring(0, field.length() - 4);

            String fType = item.getFieldType();
            generated += "\t\tpublic final static "+fType+" "+field+" = " +
                    item.toString().replaceAll("\n", "\n\t\t\t") + ";\n";
        }

        generated += "\t}\n\n";

        return generated;
    }


    protected String genValuesClass(String className, String type) {
        if (!values.containsKey(type)) return "";

        String generated = "\tpublic final static class "+className+" {\n";
        for (String name: values.get(type).keySet()) {
            ArrayList<String> eps = new ArrayList<String>();
            for (Object val: values.get(type).get(name)) {
                if (className.equals("dimen")) {
                    if (val.toString().equals("wrap_content")) val = String.valueOf(WRAP_CONTENT);
                    if (val.toString().equals("match_parent")) val = String.valueOf(MATCH_PARENT);
                }
                eps.add(val.toString());
            }
            generated += "\t\tpublic final static ValueLink "+name+" = new ValueLink(new EnvValue[] { ";
            generated += String.join(", ", eps) + " });\n";
        }

        generated += "\t}\n\n";

        return generated;
    }

}
