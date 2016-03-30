package lenacom.filer.zip;

import lenacom.filer.message.Messages;
import lenacom.filer.path.PathUtils;

import javax.swing.*;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipWorker {
    private ZipModel model;

    private ZipWorker(Path zip) {
        model = new ZipModel(zip);
    }

    public static ZipWorker create(Path zip)  {
        return new ZipWorker(zip);
    }

    public static ZipWorker createAndRead(Path zip) throws IOException {
        ZipWorker zipWorker = new ZipWorker(zip);
        zipWorker.model.readOnce();
        return zipWorker;
    }

    public Path getZip() {
        return model.getZip();
    }

    public Set<Path> getChildren(Path parent) {
        return model.getChildren(parent);
    }

    public ZipPathProcessor getPathProcessor() {
        return model.getProcessor();
    }

    private boolean lock() {
        if (!ZipLocker.lock(model.getZip())) {
            Messages.showMessage("err.zip.being.processed", model.getZip());
            return false;
        }
        return true;
    }

    private void unlock() {
        ZipLocker.unlock(model.getZip());
    }

    public void delete(Path[] pathsToDelete) throws IOException {
        if (!lock()) return;
        model.readOnce();
        ZipBuilderParameters params = new ZipBuilderParameters(model);
        ExcludePathFilter filter = new ExcludePathFilter(model.toRelativeZipPath(pathsToDelete));
        params.setFilter(filter);
        ZipBuilder.rebuild(params);
    }

    public void packAll(Path[] paths) throws IOException {
        if (!lock()) return;
        ZipBuilderParameters params = new ZipBuilderParameters(model);
        params.setPathsToAdd(paths);
        params.setDeleteAddedPaths(false);
        ZipBuilder.build(params);
    }

    public void copyIn(Path[] source, Path target) throws IOException {
        if (!lock()) return;
        model.readOnce();
        ZipBuilderParameters params = new ZipBuilderParameters(model);
        params.setPathsToAdd(source);
        params.setDeleteAddedPaths(false);
        params.setTargetDirectory(model.toRelativeZipPath(target));
        ZipBuilder.rebuild(params);
    }

    public void moveIn(Path[] source, Path target) throws IOException {
        if (!lock()) return;
        model.readOnce();
        ZipBuilderParameters params = new ZipBuilderParameters(model);
        params.setPathsToAdd(source);
        params.setDeleteAddedPaths(true);
        params.setTargetDirectory(model.toRelativeZipPath(target));
        ZipBuilder.rebuild(params);
    }

    void replaceTmpFile(Path originalZipPath, Path tmpNativePath, boolean deleteTmpFile) throws IOException {
        if (!lock()) return;

        model.readOnce();

        originalZipPath = model.toRelativeZipPath(originalZipPath);
        Path[] originalZipPaths = new Path[]{ originalZipPath };
        Path[] tmpNativePaths = new Path[]{ tmpNativePath };
        ExcludePathFilter filter = new ExcludePathFilter(originalZipPaths);

        Map<Path, Path> sourceToTarget = new HashMap<>();
        sourceToTarget.put(tmpNativePath, originalZipPath);

        ZipBuilderParameters params = new ZipBuilderParameters(model);
        params.setFilter(filter);
        params.setPathsToAdd(tmpNativePaths);
        params.setDeleteAddedPaths(deleteTmpFile);
        params.setSourceToTarget(sourceToTarget);
        ZipBuilder.rebuild(params);
    }

    public void rename(Path oldPath, Path newPath) throws IOException {
        if (!lock()) return;
        Map<Path, Path> sourceToTarget = new HashMap<>();
        sourceToTarget.put(model.toRelativeZipPath(oldPath), model.toRelativeZipPath(newPath));
        model.readOnce();
        ZipBuilderParameters params = new ZipBuilderParameters(model);
        params.setSourceToTarget(sourceToTarget);
        ZipBuilder.rebuild(params);
    }

    public void unpackAll(Path targetDirectory) throws IOException {
        if (!lock()) return;
        model.readOnce();
        ZipToNativeExtractor.copyAll(model, targetDirectory);
    }

    //this method is executed without progress and lock
    public ExtractedTmpFile extractTempFile(Path path) throws IOException {
        assert(SwingUtilities.isEventDispatchThread());
        Path relativePath = model.toRelativeZipPath(path);
        Path tmpFile = null;
        try (
            ZipInputStream zis = new ZipInputStream(Files.newInputStream(model.getZip()))
        ){
            ZipEntry entry;
            while((entry = zis.getNextEntry()) != null) {
                Path entryPath = Paths.get(entry.getName());
                if (relativePath.equals(entryPath) && !entry.isDirectory()) {
                    tmpFile = PathUtils.createTempFile(relativePath.getFileName().toString() /*we need to extract .zip as .zip*/);
                    try (
                        BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(tmpFile))
                    ) {
                        ZipUtils.writeEntry(zis, out);
                    }
                    tmpFile.toFile().deleteOnExit();
                }
            }
        }

        return new ExtractedTmpFile(path, tmpFile);
    }

    public void copyOut(Path[] source, Path targetDirectory) throws IOException {
        if (!lock()) return;
        model.readOnce();
        ZipToNativeExtractor.copy(model, model.toRelativeZipPath(source), targetDirectory);
    }

    public void moveOut(Path[] source, Path targetDirectory) throws IOException {
        if (!lock()) return;
        model.readOnce();
        ZipToNativeExtractor.move(model, model.toRelativeZipPath(source), targetDirectory);
    }

    public void copyOut(ZipWorker targetZipWorker, Path[] source, Path targetDirectory) throws IOException {
        if (!lock()) return;
        if (!targetZipWorker.lock()) {
            unlock();
            return;
        }
        model.readOnce();
        ZipToZipExtractor.copy(model, model.toRelativeZipPath(source),
                targetZipWorker.model, targetZipWorker.model.toRelativeZipPath(targetDirectory));
    }

    public void moveOut(ZipWorker targetZipWorker, Path[] source, Path targetDirectory) throws IOException {
        if (!lock()) return;
        if (!targetZipWorker.lock()) {
            unlock();
            return;
        }
        model.readOnce();
        ZipToZipExtractor.move(model, model.toRelativeZipPath(source),
                targetZipWorker.model, targetZipWorker.model.toRelativeZipPath(targetDirectory));
    }

    private void add(Path path, boolean directory) throws IOException {
        if (!lock()) return;
        model.readOnce();

        Path zipParent = model.getZip().getParent();
        Path tmpNativePath = directory?
            PathUtils.createTempDirectoryInDirectory(zipParent) :
            PathUtils.createTempFileInDirectory(zipParent);
        tmpNativePath.toFile().deleteOnExit();
        Map<Path, Path> sourceToTarget = new HashMap<>();
        sourceToTarget.put(tmpNativePath, model.toRelativeZipPath(path));

        ZipBuilderParameters params = new ZipBuilderParameters(model);
        params.setSourceToTarget(sourceToTarget);
        params.setPathsToAdd(new Path[]{ tmpNativePath });
        params.setDeleteAddedPaths(true);

        ZipBuilder.rebuild(params);
    }

    public void addDirectory(Path path) throws IOException {
        add(path, true);
    }

    public void addFile(Path path) throws IOException {
        add(path, false);
    }

    public void setDirectorySize(Path path, long size) {
        model.setDirectorySize(path, size);
    }
}
