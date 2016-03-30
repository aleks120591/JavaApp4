package lenacom.filer.progress;

import java.util.List;

public interface PathProgress {
    public TimeRecorder getTimeRecorder();

    public void beforeProcessing(List<PublishedPath> paths);

    public void afterProcessing(long value);

    public void close();

    public void skipValue(long value);
}
