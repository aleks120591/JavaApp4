package lenacom.filer.action;

import lenacom.filer.path.PathAttributes;
import lenacom.filer.path.PathUtils;
import lenacom.filer.path.processor.NativePathProcessor;
import lenacom.filer.path.processor.OverwriteProcessor;
import lenacom.filer.path.processor.SafePathVisitor;
import lenacom.filer.progress.PathProgress;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

abstract class CopyMoveOverwriteProcessor extends OverwriteProcessor {
    private final CopyOption[] options;
    private final CopyOption[] optionsWithReplaceExisting;
    private final SafePathVisitor visitor;

    public CopyMoveOverwriteProcessor(SafePathVisitor visitor, PathProgress progress, CopyOption[] options) {
        super(NativePathProcessor.getInstance(), NativePathProcessor.getInstance(), progress);
        this.visitor = visitor;
        this.options = options;

        optionsWithReplaceExisting = new CopyOption[options.length + 1];
        System.arraycopy(options, 0, optionsWithReplaceExisting, 0, options.length);
        optionsWithReplaceExisting[options.length] = StandardCopyOption.REPLACE_EXISTING;
    }

    protected Path getTarget(Path source) {
        return getTargetDirectory().resolve(visitor.getSourceDirectory().relativize(source));
    }

    @Override
    protected void overwriteFile(Path source, Path target) throws Exception {
        if (PathUtils.existsNoFollowLink(target) && PathAttributes.isReadonly(target)) {
            PathAttributes.setReadonly(target, false);
        }
        processFile(source, target, optionsWithReplaceExisting);
    }

    @Override
    protected void processFile(Path source, Path target) throws Exception {
        processFile(source, target, options);
    }

    @Override
    protected void processDirectory(Path source, Path target) throws Exception {
        processDirectory(source, target, options);
    }

    @Override
    protected void cancel() {
        visitor.cancel();
    }

    protected abstract Path getTargetDirectory();
    protected abstract void processFile(Path source, Path target, CopyOption[] options) throws IOException;
    protected abstract void processDirectory(Path source, Path target, CopyOption[] options) throws IOException;
}
