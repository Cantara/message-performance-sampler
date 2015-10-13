package no.cantara;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.Timer;
import java.util.TimerTask;

public class PerformanceSampler {

   private class PerformanceSamplerTimerTask extends TimerTask {
        @Override
        public void run() {
            getPrintableTimewindowLatencyStatistics().printToStandardOutput();
            getPrintableTimewindowThroughputStatistics().printToStandardOutput();
            resetTimeWindow();
        }
    }

    protected final Object initializationLock = new Object();
    protected Timer timer;

    protected final Object cumulativeStatisticsLock = new Object();
    protected final DescriptiveStatistics latencyStatistics = new DescriptiveStatistics();
    protected long minSentTimeNanos = Long.MAX_VALUE;
    protected final DescriptiveStatistics throughputStatistics = new DescriptiveStatistics();

    protected final Object timewindowLock = new Object();
    protected long timewindowStartNanos = -1;
    protected DescriptiveStatistics timewindowLatencyStatistics = new DescriptiveStatistics();
    protected long timewindowMinSentTimeNanos = Long.MAX_VALUE;
    protected DescriptiveStatistics timewindowThroughputStatistics = new DescriptiveStatistics();

    public PerformanceSampler() {
    }

    private void resetTimeWindow() {
        synchronized (timewindowLock) {
            minSentTimeNanos = Long.MAX_VALUE;
            timewindowLatencyStatistics = new DescriptiveStatistics();
            timewindowMinSentTimeNanos = Long.MAX_VALUE;
            timewindowThroughputStatistics = new DescriptiveStatistics();
            timewindowStartNanos = System.nanoTime();
        }
    }

    public PrintableStatistics getPrintableLatencyStatistics() {
        synchronized (cumulativeStatisticsLock) {
            return new PrintableStatistics("\"Cumulative statistics for Latency", latencyStatistics.copy());
        }
    }

    public PrintableStatistics getPrintableThroughputStatistics() {
        synchronized (cumulativeStatisticsLock) {
            return new PrintableStatistics("\"Cumulative statistics for Throughput", throughputStatistics.copy());
        }
    }

    public PrintableStatistics getPrintableTimewindowLatencyStatistics() {
        synchronized (timewindowLock) {
            return new PrintableStatistics("\"Time-window statistics for Latency\nwindow-size: " + timewindowStartNanos, timewindowLatencyStatistics.copy());
        }
    }

    public PrintableStatistics getPrintableTimewindowThroughputStatistics() {
        synchronized (timewindowLock) {
            return new PrintableStatistics("\"Time-window statistics for Throughput", timewindowThroughputStatistics.copy());
        }
    }

    public void addMessage(long sentTimeNanos, long receivedTimeNanos) {
        synchronized (initializationLock) {
            if (timer == null) {
                timer = new Timer(true);
                timer.scheduleAtFixedRate(new PerformanceSamplerTimerTask(), 1000, 1000);
                resetTimeWindow();
            }
        }
        synchronized (cumulativeStatisticsLock) {
            addLatency(sentTimeNanos, receivedTimeNanos);
            addThroughput(sentTimeNanos, receivedTimeNanos);
        }
        synchronized (timewindowLock) {
            addTimewindowLatency(receivedTimeNanos);
            addTimewindowThroughput(sentTimeNanos, receivedTimeNanos);
        }
    }

    private void addLatency(long sentTimeNanos, long receivedTimeNanos) {
        long durationNanos = receivedTimeNanos - sentTimeNanos;
        double latencyMicro = durationNanos / 1000.0;
        latencyStatistics.addValue(latencyMicro);
    }

    private void addThroughput(long sentTimeNanos, long receivedTimeNanos) {
        if (sentTimeNanos < minSentTimeNanos) {
            minSentTimeNanos = sentTimeNanos;
        }
        double throughput = (receivedTimeNanos - minSentTimeNanos) / (double) throughputStatistics.getN();
        throughputStatistics.addValue(throughput);
    }

    private void addTimewindowLatency(long receivedTimeNanos) {
        long timewindowDurationNanos = receivedTimeNanos - timewindowMinSentTimeNanos;
        double timewindowLatencyMicro = timewindowDurationNanos / 1000.0;
        timewindowLatencyStatistics.addValue(timewindowLatencyMicro);
    }

    private void addTimewindowThroughput(long sentTimeNanos, long receivedTimeNanos) {
        if (sentTimeNanos < timewindowMinSentTimeNanos) {
            timewindowMinSentTimeNanos = sentTimeNanos;
        }
        double timewindowThroughput = (receivedTimeNanos - timewindowMinSentTimeNanos) / (double) timewindowThroughputStatistics.getN();;
        timewindowThroughputStatistics.addValue(timewindowThroughput);
    }
}
