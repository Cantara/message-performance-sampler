package no.cantara.performance.messagesampler;

public class LatencyAndThroughputMessageSamplerTest {
    public static void main(String[] args) throws InterruptedException {
        final Object waitBus = new Object();
        LatencyAndThroughputMessageSampler sampler = new LatencyAndThroughputMessageSampler();
        for (int i=0; i<500; i++) {
            long sentTime = System.nanoTime();
            synchronized (waitBus) {
                waitBus.wait(10, 0);
            }
            sampler.addMessage(sentTime, System.nanoTime());
        }
        sampler.getPrintableLatencyStatistics().printToStandardOutput();
        sampler.getPrintableThroughputStatistics().printToStandardOutput();
    }
}
