package no.cantara.performance.messagesampler;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.time.Duration;
import java.time.ZonedDateTime;

public class PrintableStatistics {
    private final String name;
    private final String type;
    private final String unit;
    private final DescriptiveStatistics latencyStatistics;
    private final ZonedDateTime sampleBegin;
    private final ZonedDateTime sampleEnd;

    private final ThreadLocal<JsonGenerator> jsonGeneratorThreadLocal = new ThreadLocal<>();

    PrintableStatistics(String name, String type, String unit, ZonedDateTime sampleBegin, ZonedDateTime sampleEnd, DescriptiveStatistics latencyStatistics) {
        this.name = name;
        this.type = type;
        this.unit = unit;
        this.latencyStatistics = latencyStatistics;
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
        out.println(getJson());
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
            {
                jg.writeObjectFieldStart("throughput");
                long durationMillis = Duration.between(sampleBegin, sampleEnd).toMillis();
                jg.writeNumberField("messages/second", 1000.0 * latencyStatistics.getN() / durationMillis);
                jg.writeNumberField("messages", latencyStatistics.getN());
                jg.writeNumberField("milliseconds", durationMillis);
                jg.writeEndObject();
            }
            {
                jg.writeObjectFieldStart("latency");
                jg.writeNumberField("N", latencyStatistics.getN());
                if (latencyStatistics.getN() > 0) {
                    double mean = latencyStatistics.getMean();
                    jg.writeNumberField("mean", mean);
                    jg.writeNumberField("stddev", latencyStatistics.getStandardDeviation());
                    jg.writeNumberField("min", latencyStatistics.getMin());
                    jg.writeNumberField("max", latencyStatistics.getMax());
                    jg.writeNumberField("sum", latencyStatistics.getSum());
                    jg.writeNumberField("50%", latencyStatistics.getPercentile(50));
                    jg.writeNumberField("95%", latencyStatistics.getPercentile(90));
                    jg.writeNumberField("99%", latencyStatistics.getPercentile(99));
                    jg.writeNumberField("99.9%", latencyStatistics.getPercentile(99.9));
                }
                jg.writeEndObject();
            }
            jg.writeEndObject();
            jg.flush();
            jg.close();
            return w.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
