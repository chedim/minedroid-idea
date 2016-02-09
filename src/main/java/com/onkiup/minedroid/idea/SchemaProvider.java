package com.onkiup.minedroid.idea;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.FileViewProviderFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.xml.XmlFileImpl;
import com.intellij.psi.xml.XmlFile;
import com.intellij.xml.XmlSchemaProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by chedim on 7/27/15.
 */
public class SchemaProvider extends XmlSchemaProvider {
    protected XmlFile cached;
    @Nullable
    @Override
    public XmlFile getSchema(String s, Module module, PsiFile psiFile) {
        if (s.length() == 0 || s.equals("http://onkiup.com/minecraft/xml")) {
            if (cached == null) {
                final URL resource = getResource("minedroid.xsd");
                final VirtualFile file = VfsUtil.findFileByURL(resource);
                return (XmlFile) PsiManager.getInstance(module.getProject()).findFile(file).copy();
            }
            return cached;
        }
        return null;
    }

    @Override
    public boolean isAvailable(XmlFile file) {
        return true;
    }

    protected URL getResource(String name) {
        ClassLoader cl = getClass().getClassLoader();
        if (cl == null) {
            // A system class.
            return ClassLoader.getSystemResource(name);
        }
        return cl.getResource(name);
    }
}
