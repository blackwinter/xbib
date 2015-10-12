package org.xbib.io.redis.cluster;

import com.google.common.util.concurrent.AbstractFuture;
import org.xbib.io.redis.RedisChannelWriter;
import org.xbib.io.redis.protocol.CommandArgs;
import org.xbib.io.redis.protocol.CommandKeyword;
import org.xbib.io.redis.protocol.CommandOutput;
import org.xbib.io.redis.protocol.ProtocolKeyword;
import org.xbib.io.redis.protocol.RedisCommand;
import io.netty.buffer.ByteBuf;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 */
class ClusterCommand<K, V, T> extends AbstractFuture<T> implements RedisCommand<K, V, T> {

    private RedisCommand<K, V, T> command;
    private RedisChannelWriter<K, V> retry;
    private int executions;
    private int executionLimit;

    ClusterCommand(RedisCommand<K, V, T> command, RedisChannelWriter<K, V> retry, int executionLimit) {
        this.command = command;
        this.retry = retry;
        this.executionLimit = executionLimit;
    }

    @Override
    public CommandOutput<K, V, T> getOutput() {
        return command.getOutput();
    }

    @Override
    public void complete() {
        executions++;

        try {
            if (executions < executionLimit && (isMoved() || isAsk())) {
                retry.write(this);
                return;
            }
        } catch (Exception e) {
            setException(e);
            command.complete();
            return;
        }

        command.complete();
    }

    public boolean isMoved() {
        if (getError() != null && getError().startsWith(CommandKeyword.MOVED.name())) {
            return true;
        }
        return false;
    }

    public boolean isAsk() {
        if (getError() != null && getError().startsWith(CommandKeyword.ASK.name())) {
            return true;
        }
        return false;
    }

    @Override
    public void addListener(Runnable listener, Executor executor) {
        command.addListener(listener, executor);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return command.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return command.isCancelled();
    }

    @Override
    public boolean isDone() {
        return command.isDone();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return command.get(timeout, unit);
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return command.get();
    }

    @Override
    public String getError() {
        return command.getError();
    }

    @Override
    public CommandArgs<K, V> getArgs() {
        return command.getArgs();
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) {
        return command.await(timeout, unit);
    }

    public int getExecutions() {
        return executions;
    }

    public int getExecutionLimit() {
        return executionLimit;
    }

    @Override
    public void encode(ByteBuf buf) {
        command.encode(buf);
    }

    @Override
    public boolean setException(Throwable exception) {
        return command.setException(exception);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [command=").append(command);
        sb.append(", executions=").append(executions);
        sb.append(']');
        return sb.toString();
    }

    @Override
    public ProtocolKeyword getType() {
        return command.getType();
    }
}
