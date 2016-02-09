package com.onkiup.minedroid.idea;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.*;
import com.intellij.xml.XmlSchemaProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by chedim on 6/1/15.
 */
public class XmlWatcher implements ModuleComponent, VirtualFileListener {

    protected Module mModule;

    protected TimerTask buildTask = null;

    public XmlWatcher(Module module) {
        mModule = module;
    }

    public void initComponent() {
    }

    public void disposeComponent() {
        // TODO: insert component disposal logic here
    }

    @NotNull
    public String getComponentName() {
        return "MineDroid.Parser";
    }

    public void projectOpened() {
        // called when project is opened
    }

    public void projectClosed() {
        // called when project is being closed
    }

    public void moduleAdded() {
        VirtualFileManager.getInstance().addVirtualFileListener(this);
        scheduleBuilder();
    }

    @Override
    public void propertyChanged(@NotNull VirtualFilePropertyEvent virtualFilePropertyEvent) {

    }

    @Override
    public void contentsChanged(@NotNull VirtualFileEvent virtualFileEvent) {
        if (!virtualFileEvent.getFileName().endsWith(".xml")) return;
        scheduleBuilder();
    }

    @Override
    public void fileCreated(@NotNull VirtualFileEvent virtualFileEvent) {
        if (!virtualFileEvent.getFileName().endsWith(".xml")) return;
        scheduleBuilder();
    }

    @Override
    public void fileDeleted(@NotNull VirtualFileEvent virtualFileEvent) {
        if (!virtualFileEvent.getFileName().endsWith(".xml")) return;
        scheduleBuilder();
    }

    @Override
    public void fileMoved(@NotNull VirtualFileMoveEvent event) {
        if (!event.getFileName().endsWith(".xml")) return;
        scheduleBuilder();
    }

    @Override
    public void fileCopied(@NotNull VirtualFileCopyEvent event) {
        if (!event.getFileName().endsWith(".xml")) return;
        scheduleBuilder();
    }

    @Override
    public void beforePropertyChange(@NotNull VirtualFilePropertyEvent virtualFilePropertyEvent) {

    }

    @Override
    public void beforeContentsChange(@NotNull VirtualFileEvent virtualFileEvent) {

    }

    @Override
    public void beforeFileDeletion(@NotNull VirtualFileEvent virtualFileEvent) {

    }

    @Override
    public void beforeFileMovement(@NotNull VirtualFileMoveEvent virtualFileMoveEvent) {
    }


    private void scheduleBuilder() {
        if (buildTask != null) {
            buildTask.cancel();
        }

        Timer t = new Timer();
        t.schedule(buildTask = new TimerTask() {
            @Override
            public void run() {
                Builder.build(mModule);
                buildTask = null;
            }
        }, 1000);
    }
}
