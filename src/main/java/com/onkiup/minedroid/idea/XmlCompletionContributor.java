package com.onkiup.minedroid.idea;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupItem;
import com.intellij.ide.PsiActionSupportFactory;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.*;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.util.PsiElementFilter;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlElementType;
import com.intellij.util.ProcessingContext;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by chedim on 6/1/15.
 */
public class XmlCompletionContributor extends CompletionContributor {
    public static final String NS = "http://onkiup.com/minecraft/xml";


    public XmlCompletionContributor() {
        super();
        PsiElementPattern.Capture<PsiElement> xmlnsVal =
                PlatformPatterns.psiElement(XmlElementType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .withText(NS);

        PsiElementPattern.Capture<PsiElement> xmlns =
                PlatformPatterns.psiElement(XmlElementType.XML_ATTRIBUTE)
                        .withLanguage(XMLLanguage.INSTANCE)
//                        .withChild(xmlnsVal)
                ;

        PsiElementPattern.Capture<PsiElement> parent =
                PlatformPatterns.psiElement(XmlElementType.XML_TAG)
                        .withLanguage(XMLLanguage.INSTANCE)
//                        .withChild(xmlns)
                ;
        PsiElementPattern.Capture<PsiElement> attribute =
                PlatformPatterns.psiElement(XmlElementType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .withLanguage(XMLLanguage.INSTANCE)
                        .withTreeParent(parent);

        extend(CompletionType.BASIC, attribute, new Completer());
    }

    public static class Completer extends CompletionProvider<CompletionParameters> {

        public Completer() {

        }

        @Override
        protected void addCompletions(CompletionParameters p, ProcessingContext processingContext, final CompletionResultSet result) {
            final Collection<LookupElement> variants = new ArrayList<LookupElement>();

            PsiElement psi = p.getPosition();
            Module module = ModuleUtil.findModuleForPsiElement(psi);

            // parsing entered value
            String text = psi.getText().replaceAll("IntellijIdeaRulezzz ", "");
            String[] cursor = psi.getText().split("IntellijIdeaRulezzz ");
            if (text.length() == 0 || text.charAt(0) != '@') return;
            text = text.substring(1);
            String[] parts = text.split(":");
            PsiClass[] sources = PsiShortNamesCache.getInstance(module.getProject()).getClassesByName("R", module.getModuleScope());
            if (parts.length == 2) {
                // TODO: minedroid bound
            }

            text = parts[parts.length - 1];

            parts = text.split("\\/");

            for (final PsiClass source: sources) {
                final String[] finalParts = parts;
                PsiTreeUtil.collectElements(source, new PsiElementFilter() {
                    @Override
                    public boolean isAccepted(final PsiElement element) {
                        if (element instanceof PsiClass && element != source) {
                            if (((PsiClass) element).getNameIdentifier().getText().contains(finalParts[0])) {
                                PsiTreeUtil.collectElements(element, new PsiElementFilter() {
                                    @Override
                                    public boolean isAccepted(PsiElement psiElement) {
                                        if (psiElement instanceof PsiField) {
                                            if (finalParts.length < 2 || finalParts[1].length() == 0 ||
                                                    ((PsiField) psiElement).getNameIdentifier().getText().contains(finalParts[1])) {
                                                result.addElement(LookupElementBuilder.create("" +
                                                                ((PsiClass) element).getNameIdentifier().getText() + "/" +
                                                                ((PsiField) psiElement).getNameIdentifier().getText()
                                                ).withCaseSensitivity(false));
                                            }
                                        }
                                        return false;
                                    }
                                });
                            }
                        }
                        return false;
                    }
                });

            }
        }
    }

    @Override
    public void fillCompletionVariants(CompletionParameters parameters, CompletionResultSet result) {
        PsiElement e = parameters.getPosition();
        if (e.getNode().getElementType() == XmlElementType.XML_ATTRIBUTE_VALUE_TOKEN) {
            if (e.getText().charAt(0) == '@' || e.getText().length() == 0) {
                PsiElement root = e.getContainingFile().getFirstChild();
                for (PsiElement top : root.getChildren()) {
                    if (top.getNode().getElementType() == XmlElementType.XML_TAG) {
                        for (PsiElement attrs : top.getChildren()) {
                            if (attrs.getNode().getElementType() == XmlElementType.XML_ATTRIBUTE) {
                                if (attrs.getText().contains(NS)) {
                                    new Completer().addCompletions(parameters, new ProcessingContext(), result);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
