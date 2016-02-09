package com.onkiup.minedroid.idea;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.ProcessingContext;
import com.intellij.xml.XmlNSDescriptor;
import org.jetbrains.annotations.NotNull;

public class XmlIdReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement target, @NotNull ProcessingContext processingContext) {
        if (acceptsTarget(target)) {
            return new PsiReference[] {new XmlIdReference((XmlAttributeValue) target)};
        }
        return new PsiReference[0];
    }

    @Override
    public boolean acceptsTarget(@NotNull PsiElement target) {
        if (target instanceof XmlAttributeValue && checkXmlFile(target)) {
            String value = ((XmlAttributeValue) target).getValue();
            if (value.startsWith("@id/") || value.startsWith("@minedroid:id/")) {
                return true;
            }
        }
        return false;
    }

    private boolean checkXmlFile(@NotNull PsiElement element) {
        PsiFile targetFile = element.getContainingFile();
        if (targetFile instanceof XmlFile) {
            XmlFile file = (XmlFile) targetFile;
            XmlNSDescriptor descriptor = file.getDocument().getRootTagNSDescriptor();
            if (descriptor.toString().equalsIgnoreCase(MineDroid.NS)) {
                return true;
            }

            XmlAttribute[] attrs = file.getRootTag().getAttributes();
            for (XmlAttribute attr : attrs) {
                if (attr.getNamespace().equalsIgnoreCase("xmlns") || attr.getName().startsWith("xmlns")) {
                    if (attr.getValue().equalsIgnoreCase(MineDroid.NS)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
