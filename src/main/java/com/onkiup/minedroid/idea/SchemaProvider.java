package com.onkiup.minedroid.idea;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.xml.XmlSchemaProvider;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.HashMap;

/**
 * Created by chedim on 7/27/15.
 */
public class SchemaProvider extends XmlSchemaProvider {
    protected XmlFile cached;

    protected static HashMap<String, VirtualFile> schemas = new HashMap<>();

    static {
        schemas.put("http://onkiup.com/minecraft/xml", getXmlFile("schemas/minedroid.xsd"));
        schemas.put("http://onkiup.com/minecraft/xml/drawables", getXmlFile("schemas/minedroid-drawables.xsd"));
        schemas.put("http://onkiup.com/minecraft/xml/views", getXmlFile("schemas/minedroid-views.xsd"));
        schemas.put("http://onkiup.com/minecraft/xml/resources", getXmlFile("schemas/minedroid-resources.xsd"));
        schemas.put("http://onkiup.com/minecraft/xml/styles", getXmlFile("schemas/minedroid-styles.xsd"));
    }

    @Nullable
    @Override
    public XmlFile getSchema(String s, Module module, PsiFile psiFile) {
        if (schemas.containsKey(s)) {
            VirtualFile file = schemas.get(s);
            return (XmlFile) PsiManager.getInstance(module.getProject()).findFile(file);
        }
        return null;
    }

    @Override
    public boolean isAvailable(XmlFile file) {
        String ns = file.getDocument().getRootTag().getNamespace();
        return schemas.containsKey(ns);
    }

    protected static URL getResource(String name) {
        ClassLoader cl = SchemaProvider.class.getClassLoader();
        if (cl == null) {
            // A system class.
            return ClassLoader.getSystemResource(name);
        }
        return cl.getResource(name);
    }

    protected static VirtualFile getXmlFile(String name) {
        URL resource = getResource(name);
        VirtualFile file =  VfsUtil.findFileByURL(resource);
        if (file.getFileType() instanceof XmlFileType) {
            return file;
        }
        return null;
    }
}
