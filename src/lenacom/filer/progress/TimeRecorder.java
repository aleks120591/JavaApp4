package lenacom.filer.progress;

public final class TimeRecorder {
    private long totalTime = 0;
    private long startTime = -1;

    public synchronized void startRecordingTime() {
        if (startTime == -1) {
            startTime = System.currentTimeMillis();
        } else {
            throw new IllegalStateException("Expected stopRecordingTime()");
        }
    }

    public synchronized void stopRecordingTime() {
        if (startTime != -1) {
            totalTime += System.currentTimeMillis() - startTime;
        } else {
            throw new IllegalStateException("Expected startRecordingTime()");
        }
        startTime = -1;
    }

    synchronized long getTotalTime() {
        long add = 0;
        if (startTime != -1) add = System.currentTimeMillis() - startTime;
        return totalTime + add;
    }
}
