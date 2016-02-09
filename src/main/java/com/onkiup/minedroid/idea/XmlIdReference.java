package com.onkiup.minedroid.idea;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class XmlIdReference implements PsiReference {
    private XmlAttributeValue source;
    private PsiElement target;

    public XmlIdReference(XmlAttributeValue source) {
        this.source = source;
        XmlAttribute parent = (XmlAttribute) source.getParent();
        String ns = parent.getNamespace();
        if ((ns.length() == 0 || ns.equals(MineDroid.NS)) && parent.getName().equalsIgnoreCase("id")) {
            target = source.getParent().getParent();
        } else {
            // looking for element...
            XmlTag root = (XmlTag) source.getParent().getParent().getParent();      // omg ^_^
            if (root == null) {
                root = (XmlTag) source.getParent().getParent();
            }
            target = searchTagById(root, source.getValue());
        }
    }

    @Override
    public PsiElement getElement() {
        return source;
    }

    @Override
    public TextRange getRangeInElement() {
        return new TextRange(1, source.getValue().length() + 1);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return target;
    }

    @NotNull
    @Override
    public String getCanonicalText() {
        if (target != null) {
            XmlTag tag = (XmlTag) target.getParent().getParent();
            return tag.getName() + " id=" + source.getValue();
        }
        return source.getText();
    }

    @Override
    public PsiElement handleElementRename(String s) throws IncorrectOperationException {
        throw new IncorrectOperationException();
    }

    @Override
    public PsiElement bindToElement(@NotNull PsiElement psiElement) throws IncorrectOperationException {
        throw new IncorrectOperationException();
    }

    @Override
    public boolean isReferenceTo(PsiElement psiElement) {
        return target != null && target.equals(psiElement);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }

    @Override
    public boolean isSoft() {
        return false;
    }

    private XmlTag searchTagById(XmlTag in, String id) {
        for (XmlAttribute attr : in.getAttributes()) {
            if (attr.getName().equals("id")) {
                if (attr.getValue().equals(id)) {
                    return in;
                }
            }
        }

        for (XmlTag child : in.getSubTags()) {
            XmlTag result = searchTagById(child, id);
            if (result != null) {
                return result;
            }
        }

        return null;
    }
}
