package no.cantara.performance.messagesampler;

public interface TimeWindowSampleCallback {
    void latencyAndThroughput(PrintableStatistics latency, PrintableStatistics throughput);
}
