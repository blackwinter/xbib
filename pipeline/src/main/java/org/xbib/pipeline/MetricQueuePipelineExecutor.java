
package org.xbib.pipeline;

import org.xbib.metric.MeterMetric;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MetricQueuePipelineExecutor<R extends PipelineRequest, P extends Pipeline<R>>
        extends QueuePipelineExecutor<R,P> {

    protected MeterMetric metric;

    @Override
    public MetricQueuePipelineExecutor<R,P> setConcurrency(int concurrency) {
        super.setConcurrency(concurrency);
        return this;
    }

    @Override
    public MetricQueuePipelineExecutor<R,P> setPipelineProvider(PipelineProvider<P> provider) {
        super.setPipelineProvider(provider);
        return this;
    }

    @Override
    public MetricQueuePipelineExecutor<R,P> setSink(PipelineSink<R> sink) {
        super.setSink(sink);
        return this;
    }

    @Override
    public MetricQueuePipelineExecutor<R,P> prepare() {
        super.prepare();
        return this;
    }

    @Override
    public MetricQueuePipelineExecutor<R,P> execute() {
        metric = new MeterMetric(5L, TimeUnit.SECONDS);
        super.execute();
        return this;
    }

    @Override
    public MetricQueuePipelineExecutor<R,P> waitFor()
            throws InterruptedException, ExecutionException, IOException {
        super.waitFor();
        metric.stop();
        return this;
    }

    public MeterMetric getMetric() {
        return metric;
    }
}
