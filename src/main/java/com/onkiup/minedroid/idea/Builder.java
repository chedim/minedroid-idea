package com.onkiup.minedroid.idea;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.util.ProgressIndicatorBase;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.jps.model.java.JavaResourceRootType;
import org.jetbrains.jps.model.java.JavaSourceRootType;

import java.util.List;

/**
 * Created by chedim on 6/2/15.
 */
public class Builder {
    final static String[] p = new String[]{"minedroid"};
    final static String c = "gradle";

    public static void build(final Module module) {
        final ProgressIndicator indicator = new ProgressIndicatorBase();
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(
                new Task.Backgroundable(module.getProject(), "Generating R file for module " + module.getName()) {
                    @Override
                    public void run(ProgressIndicator progressIndicator) {
                        try {
                            progressIndicator.setText(module.getName());
                            ModuleRootManager man;
                            try {
                                man = ModuleRootManager.getInstance(module);
                            } catch (AssertionError err) {
                                // already disposed...
                                return;
                            }

                            final List<VirtualFile> resources = man.getSourceRoots(JavaResourceRootType.RESOURCE);
                            final List<VirtualFile> sources = man.getSourceRoots(JavaSourceRootType.SOURCE);
                            if (resources == null || sources == null) return;
                            VirtualFile sourceDir = null;
                            progressIndicator.setText2("Searching for generated sources...");
                            if (sources.size() > 0) {
                                sourceDir = sources.get(0);
                                for (VirtualFile src : sources) {
                                    if (src.getName().contains("gen")) sourceDir = src;
                                }
                            } else return;

                            progressIndicator.setText2("Building R.java...");
                            for (VirtualFile res : resources) {
                                progressIndicator.setText2("Processing <" + res.getName() + ">...");
                                MineDroid md = new MineDroid(res, sourceDir);
                                md.process();
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, indicator);

    }

}
