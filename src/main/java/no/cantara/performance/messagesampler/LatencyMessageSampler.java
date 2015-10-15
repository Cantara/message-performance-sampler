package no.cantara.performance.messagesampler;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Timer;
import java.util.TimerTask;

public class LatencyMessageSampler {

   private class PerformanceMessageSamplerTimerTask extends TimerTask {
        @Override
        public void run() {
            PrintableStatistics latency;
            synchronized (timewindowLock) {
                timewindowEnd = ZonedDateTime.now(ZoneId.systemDefault());
                latency = getPrintableTimewindowStatistics();
                resetTimeWindow();
            }
            callback.timewindowStatistics(latency);
        }
    }

    protected final TimeWindowSampleCallback callback;
    protected final int timeWindowSampleIntervalMs;

    protected final Object initializationLock = new Object();
    protected Timer timer;

    protected final Object cumulativeStatisticsLock = new Object();
    protected ZonedDateTime cumulativeStart;
    protected DescriptiveStatistics latencyStatistics = new DescriptiveStatistics();
    protected long minSentTimeMillis = Long.MAX_VALUE;
    protected DescriptiveStatistics throughputStatistics = new DescriptiveStatistics();

    protected final Object timewindowLock = new Object();
    protected ZonedDateTime timewindowStart;
    protected ZonedDateTime timewindowEnd;
    protected DescriptiveStatistics timewindowLatencyStatistics = new DescriptiveStatistics();
    protected long timewindowMinSentTimeMillis = Long.MAX_VALUE;
    protected DescriptiveStatistics timewindowThroughputStatistics = new DescriptiveStatistics();

    public LatencyMessageSampler(TimeWindowSampleCallback callback, int timeWindowSampleIntervalMs) {
        this.callback = callback;
        this.timeWindowSampleIntervalMs = timeWindowSampleIntervalMs;
    }

    public void stopTimerBasedSampler() {
        synchronized (initializationLock) {
            timer.cancel();
            timer = null;
        }
    }

    private void resetTimeWindow() {
        timewindowStart = timewindowEnd;
        timewindowMinSentTimeMillis = Long.MAX_VALUE;
        timewindowLatencyStatistics = new DescriptiveStatistics();
        timewindowThroughputStatistics = new DescriptiveStatistics();
    }

    public PrintableStatistics getPrintableStatistics() {
        synchronized (cumulativeStatisticsLock) {
            return new PrintableStatistics("performance", "cumulative", "milliseconds", cumulativeStart, ZonedDateTime.now(), latencyStatistics.copy());
        }
    }

    public PrintableStatistics getPrintableTimewindowStatistics() {
        synchronized (timewindowLock) {
            return new PrintableStatistics("performance", "time-window", "milliseconds", timewindowStart, timewindowEnd, timewindowLatencyStatistics.copy());
        }
    }

    public void addMessage(long sentTimeMillis, long receivedTimeMillis) {
        synchronized (initializationLock) {
            if (timer == null) {
                ZonedDateTime now = ZonedDateTime.now();
                synchronized (cumulativeStatisticsLock) {
                    cumulativeStart = now;
                    minSentTimeMillis = Long.MAX_VALUE;
                    latencyStatistics = new DescriptiveStatistics();
                    throughputStatistics = new DescriptiveStatistics();
                }
                synchronized (timewindowLock) {
                    timewindowEnd = now;
                    resetTimeWindow();
                }
                timer = new Timer(true);
                timer.scheduleAtFixedRate(new PerformanceMessageSamplerTimerTask(), timeWindowSampleIntervalMs, timeWindowSampleIntervalMs);
            }
        }
        synchronized (cumulativeStatisticsLock) {
            addLatency(sentTimeMillis, receivedTimeMillis);
        }
        synchronized (timewindowLock) {
            addTimewindowLatency(sentTimeMillis, receivedTimeMillis);
        }
    }

    private void addLatency(long sentTimeMillis, long receivedTimeMillis) {
        long durationMillis = receivedTimeMillis - sentTimeMillis;
        latencyStatistics.addValue(durationMillis);
    }

    private void addTimewindowLatency(long sentTimeMillis, long receivedTimeMillis) {
        long timewindowDurationMillis = receivedTimeMillis - sentTimeMillis;
        timewindowLatencyStatistics.addValue(timewindowDurationMillis);
    }
}
