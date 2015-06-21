
package org.xbib.pipeline.queue;

import org.xbib.metric.MeterMetric;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.pipeline.PipelineRequest;
import org.xbib.pipeline.PipelineSink;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MetricQueuePipelineExecutor<T, R extends PipelineRequest, P extends Pipeline<T,R>>
        extends QueuePipelineExecutor<T,R,P> {

    protected MeterMetric metric;

    @Override
    public MetricQueuePipelineExecutor<T,R,P> setConcurrency(int concurrency) {
        super.setConcurrency(concurrency);
        return this;
    }

    @Override
    public MetricQueuePipelineExecutor<T,R,P> setPipelineProvider(PipelineProvider<P> provider) {
        super.setPipelineProvider(provider);
        return this;
    }

    @Override
    public MetricQueuePipelineExecutor<T,R,P> setSink(PipelineSink<T> sink) {
        super.setSink(sink);
        return this;
    }

    @Override
    public MetricQueuePipelineExecutor<T,R,P> prepare() {
        super.prepare();
        return this;
    }

    @Override
    public MetricQueuePipelineExecutor<T,R,P> execute() {
        metric = new MeterMetric(5L, TimeUnit.SECONDS);
        super.execute();
        return this;
    }

    @Override
    public MetricQueuePipelineExecutor<T,R,P> waitFor()
            throws InterruptedException, ExecutionException, IOException {
        super.waitFor();
        metric.stop();
        return this;
    }

    public MeterMetric getMetric() {
        return metric;
    }
}
