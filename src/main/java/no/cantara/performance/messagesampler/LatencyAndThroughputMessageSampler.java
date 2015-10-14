package no.cantara.performance.messagesampler;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.Timer;
import java.util.TimerTask;

public class LatencyAndThroughputMessageSampler {

   private class PerformanceMessageSamplerTimerTask extends TimerTask {
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
    protected long minSentTimeMillis = Long.MAX_VALUE;
    protected final DescriptiveStatistics throughputStatistics = new DescriptiveStatistics();

    protected final Object timewindowLock = new Object();
    protected long timewindowStartMillis = -1;
    protected DescriptiveStatistics timewindowLatencyStatistics = new DescriptiveStatistics();
    protected long timewindowMinSentTimeMillis = Long.MAX_VALUE;
    protected DescriptiveStatistics timewindowThroughputStatistics = new DescriptiveStatistics();

    public LatencyAndThroughputMessageSampler() {
    }

    public void stopTimerBasedSampler() {
        synchronized (initializationLock) {
            timer.cancel();
            timer = null;
        }
    }

    private void resetTimeWindow() {
        synchronized (timewindowLock) {
            timewindowMinSentTimeMillis = Long.MAX_VALUE;
            timewindowLatencyStatistics = new DescriptiveStatistics();
            timewindowThroughputStatistics = new DescriptiveStatistics();
            timewindowStartMillis = System.currentTimeMillis();
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
            long durationMillis = System.currentTimeMillis() - timewindowStartMillis;
            return new PrintableStatistics("\"Time-window statistics for Latency\nmilliseconds in timewindow: " + durationMillis, timewindowLatencyStatistics.copy());
        }
    }

    public PrintableStatistics getPrintableTimewindowThroughputStatistics() {
        synchronized (timewindowLock) {
            long durationMillis = System.currentTimeMillis() - timewindowStartMillis;
            return new PrintableStatistics("\"Time-window statistics for Throughput\nmilliseconds in timewindow: " + durationMillis, timewindowThroughputStatistics.copy());
        }
    }

    public void addMessage(long sentTimeMillis, long receivedTimeMillis) {
        synchronized (initializationLock) {
            if (timer == null) {
                timer = new Timer(true);
                timer.scheduleAtFixedRate(new PerformanceMessageSamplerTimerTask(), 1000, 1000);
                resetTimeWindow();
            }
        }
        synchronized (cumulativeStatisticsLock) {
            addLatency(sentTimeMillis, receivedTimeMillis);
            addThroughput(sentTimeMillis, receivedTimeMillis);
        }
        synchronized (timewindowLock) {
            addTimewindowLatency(sentTimeMillis, receivedTimeMillis);
            addTimewindowThroughput(sentTimeMillis, receivedTimeMillis);
        }
    }

    private void addLatency(long sentTimeMillis, long receivedTimeMillis) {
        long durationMillis = receivedTimeMillis - sentTimeMillis;
        latencyStatistics.addValue(durationMillis);
    }

    private void addThroughput(long sentTimeMillis, long receivedTimeMillis) {
        if (sentTimeMillis < minSentTimeMillis) {
            minSentTimeMillis = sentTimeMillis;
        }
        double durationMillis = (receivedTimeMillis - minSentTimeMillis);
        double durationSecond = durationMillis / 1000.0;
        double throughputMsgsPerSecond = (throughputStatistics.getN() + 1) / durationSecond;
        throughputStatistics.addValue(throughputMsgsPerSecond);
    }

    private void addTimewindowLatency(long sentTimeMillis, long receivedTimeMillis) {
        long timewindowDurationMillis = receivedTimeMillis - sentTimeMillis;
        timewindowLatencyStatistics.addValue(timewindowDurationMillis);
    }

    private void addTimewindowThroughput(long sentTimeMillis, long receivedTimeMillis) {
        if (sentTimeMillis < timewindowMinSentTimeMillis) {
            timewindowMinSentTimeMillis = sentTimeMillis;
        }
        double durationMillis = (receivedTimeMillis - timewindowMinSentTimeMillis);
        double durationSecond = durationMillis / 1000.0;
        double throughputMsgsPerSecond = (timewindowThroughputStatistics.getN() + 1) / durationSecond;
        timewindowThroughputStatistics.addValue(throughputMsgsPerSecond);
    }
}
