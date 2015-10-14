package no.cantara.performance.messagesampler;

public class LatencyAndThroughputMessageSamplerTest {
    public static void main(String[] args) throws InterruptedException {
        final Object waitBus = new Object();
        TimeWindowSampleCallback callback = new TimeWindowSampleCallback() {
            @Override
            public void latencyAndThroughput(PrintableStatistics latency, PrintableStatistics throughput) {
                System.out.println(latency.getJson());
                System.out.println(throughput.getJson());
            }
        };
        LatencyAndThroughputMessageSampler sampler = new LatencyAndThroughputMessageSampler(callback, 2000);
        for (int i=0; i<500; i++) {
            long sentTime = System.currentTimeMillis();
            synchronized (waitBus) {
                waitBus.wait(10, 0);
            }
            sampler.addMessage(sentTime, System.currentTimeMillis());
        }
        System.out.println(sampler.getPrintableLatencyStatistics().getJson());
        System.out.println(sampler.getPrintableThroughputStatistics().getJson());
    }
}
