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
        out.printf("N      = %8d\n", statistics.getN());
        out.printf("avg    = %12.3f\n", statistics.getMean());
        out.printf("stddev = %12.3f\n", statistics.getStandardDeviation());
        out.printf("min    = %12.3f\n", statistics.getMin());
        out.printf("max    = %12.3f\n", statistics.getMax());
        out.printf("sum    = %12.3f\n", statistics.getSum());
        out.printf("50%%    = %12.3f\n", statistics.getPercentile(50));
        out.printf("95%%    = %12.3f\n", statistics.getPercentile(90));
        out.printf("99%%    = %12.3f\n", statistics.getPercentile(99));
        out.printf("99.9%%  = %12.3f\n", statistics.getPercentile(99.9));
    }

    public DescriptiveStatistics getStatistics() {
        return statistics;
    }
}
