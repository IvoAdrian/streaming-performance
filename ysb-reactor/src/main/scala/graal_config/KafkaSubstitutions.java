package graal_config;

//Adapted from Source: https://github.com/micronaut-projects/micronaut-kafka/blob/master/kafka/src/main/java/io/micronaut/configuration/kafka/graal/KafkaSubstitutions.java

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.apache.kafka.common.metrics.KafkaMetric;
import org.apache.kafka.common.metrics.Metrics;
import org.apache.kafka.common.metrics.MetricsReporter;
import org.apache.kafka.common.record.CompressionType;
import org.apache.kafka.common.utils.AppInfoParser;

import java.util.List;
import java.util.Map;



// Replace unsupported compression types
@TargetClass(className = "org.apache.kafka.common.record.CompressionType")
final class CompressionTypeSubs {

    @Alias @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)
    public static CompressionType SNAPPY = CompressionType.GZIP;

    /* ZSTD not existing (yet) in Kafka-Clients 0.11.
    @Alias @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)
    public static CompressionType ZSTD = CompressionType.GZIP;
    */
}

// Replace JMX metrics, no operable on GraalVM
@TargetClass(className = "org.apache.kafka.common.metrics.JmxReporter")
@Substitute
final class NoopReporter implements MetricsReporter {

    @Substitute
    public NoopReporter() {
    }

    @Substitute
    public NoopReporter(String prefix) {
    }

    @Override
    @Substitute
    public void init(List<KafkaMetric> metrics) {
    }

    @Override
    @Substitute
    public void metricChange(KafkaMetric metric) {
    }

    @Override
    @Substitute
    public void metricRemoval(KafkaMetric metric) {
    }

    @Override
    @Substitute
    public void close() {
    }

    @Override
    @Substitute
    public void configure(Map<String, ?> configs) {
    }
}

@TargetClass(AppInfoParser.class)
final class AppInfoParserNoJMX {

    @Substitute
    public static void registerAppInfo(String prefix, String id, Metrics metrics) {
        // no-op
    }

    @Substitute
    public static void unregisterAppInfo(String prefix, String id, Metrics metrics) {
        // no-op
    }
}
