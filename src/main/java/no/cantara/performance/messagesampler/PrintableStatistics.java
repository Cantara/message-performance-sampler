package no.cantara.performance.messagesampler;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.PrintStream;

public class PrintableStatistics {
    private final String description;
    private final DescriptiveStatistics statistics;

    PrintableStatistics(String description, DescriptiveStatistics statistics) {
        this.description = description;
        this.statistics = statistics;
    }

    public void printToStandardOutput() {
        printTo(System.out);
    }

    public void printToStandardError() {
        printTo(System.err);
    }

    public void printTo(PrintStream out) {
        out.println(description);
        out.println("N      = " + statistics.getN());
        out.println("avg    = " + statistics.getMean());
        out.println("stddev = " + statistics.getStandardDeviation());
        out.println("min    = " + statistics.getMin());
        out.println("max    = " + statistics.getMax());
        out.println("sum    = " + statistics.getSum());
        out.println("50%    = " + statistics.getPercentile(50));
        out.println("95%    = " + statistics.getPercentile(90));
        out.println("99%    = " + statistics.getPercentile(99));
        out.println("99.9%  = " + statistics.getPercentile(99.9));
    }

    public DescriptiveStatistics getStatistics() {
        return statistics;
    }
}
