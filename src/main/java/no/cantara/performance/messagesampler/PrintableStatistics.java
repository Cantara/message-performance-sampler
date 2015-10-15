package no.cantara.performance.messagesampler;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.time.ZonedDateTime;

public class PrintableStatistics {
    private final String name;
    private final String type;
    private final String unit;
    private final DescriptiveStatistics statistics;
    private final ZonedDateTime sampleBegin;
    private final ZonedDateTime sampleEnd;

    private final ThreadLocal<JsonGenerator> jsonGeneratorThreadLocal = new ThreadLocal<>();

    PrintableStatistics(String name, String type, String unit, ZonedDateTime sampleBegin, ZonedDateTime sampleEnd, DescriptiveStatistics statistics) {
        if (sampleBegin == null) {
            throw new IllegalArgumentException("sampleBegin cannot be null");
        }
        if (sampleEnd == null) {
            throw new IllegalArgumentException("sampleEnd cannot be null");
        }
        this.name = name;
        this.type = type;
        this.unit = unit;
        this.statistics = statistics;
        this.sampleBegin = sampleBegin;
        this.sampleEnd = sampleEnd;
    }

    public void printToStandardOutput() {
        printTo(System.out);
    }

    public void printToStandardError() {
        printTo(System.err);
    }

    public void printTo(PrintStream out) {
        out.println(name);
        out.println(type);
        out.println(unit);
        out.println(sampleBegin.toString());
        out.println(sampleEnd.toString());
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

    public String getJson() {
        try {
            StringWriter w = new StringWriter();
            JsonGenerator jg = jsonGeneratorThreadLocal.get();
            if (jg == null) {
                jg = new JsonFactory().createGenerator(w);
                jsonGeneratorThreadLocal.set(jg);
            }
            jg.useDefaultPrettyPrinter();
            jg.writeStartObject();
            jg.writeStringField("name", name);
            jg.writeStringField("type", type);
            jg.writeStringField("unit", unit);
            jg.writeStringField("sample-bgn", sampleBegin.toString());
            jg.writeStringField("sample-end", sampleEnd.toString());
            jg.writeObjectFieldStart("statistics");
            jg.writeNumberField("N", statistics.getN());
            if (statistics.getN() > 0) {
                double mean = statistics.getMean();
                jg.writeNumberField("mean", mean);
                jg.writeNumberField("stddev", statistics.getStandardDeviation());
                jg.writeNumberField("min", statistics.getMin());
                jg.writeNumberField("max", statistics.getMax());
                jg.writeNumberField("sum", statistics.getSum());
                jg.writeNumberField("50%", statistics.getPercentile(50));
                jg.writeNumberField("95%", statistics.getPercentile(90));
                jg.writeNumberField("99%", statistics.getPercentile(99));
                jg.writeNumberField("99.9%", statistics.getPercentile(99.9));
            }
            jg.writeEndObject();
            jg.writeEndObject();
            jg.flush();
            jg.close();
            return w.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public DescriptiveStatistics getStatistics() {
        return statistics;
    }
}
