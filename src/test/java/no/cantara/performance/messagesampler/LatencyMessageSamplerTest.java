package no.cantara.performance.messagesampler;

public class LatencyMessageSamplerTest {
    public static void main(String[] args) throws InterruptedException {
        final Object waitBus = new Object();
        TimeWindowSampleCallback callback = latency -> latency.printToStandardError();
        LatencyMessageSampler sampler = new LatencyMessageSampler(callback, 100);
        for (int i=0; i<40; i++) {
            long sentTime = System.currentTimeMillis();
            synchronized (waitBus) {
                waitBus.wait(10, 0);
            }
            sampler.addMessage(sentTime, System.currentTimeMillis());
        }
        sampler.getPrintableStatistics().printToStandardOutput();
    }
}
