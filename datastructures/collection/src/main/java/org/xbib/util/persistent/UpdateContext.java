package org.xbib.util.persistent;

public final class UpdateContext<T> implements Merger<T> {

    private UncommittedContext<T> context;

    public UpdateContext() {
        this(1, null);
    }

    public UpdateContext(int expectedUpdates) {
        this(expectedUpdates, null);
    }

    public UpdateContext(int expectedUpdates, Merger<T> merger) {
        this.context = new UncommittedContext<>(expectedUpdates, merger);
    }

    public void merger(Merger<T> merger) {
        context.merger = merger;
    }

    public int expectedUpdates() {
        return context.expectedUpdates;
    }

    public void commit() {
        this.context = null;
    }

    public boolean isCommitted() {
        return context == null;
    }

    public boolean isSameAs(UpdateContext<?> other) {
        return this.context != null && this.context == other.context;
    }

    public void validate() {
        if (isCommitted()) {
            throw new IllegalStateException("This update is already committed");
        }
    }

    public boolean hasChanged() {
        return context.change != 0;
    }

    public int getChangeAndReset() {
        return context.getChangeAndReset();
    }

    @Override
    public void insert(T newEntry) {
        context.insert(newEntry);
    }

    @Override
    public boolean merge(T oldEntry, T newEntry) {
        return context.merge(oldEntry, newEntry);
    }

    @Override
    public void delete(T oldEntry) {
        context.delete(oldEntry);
    }

    private static final class UncommittedContext<T> {

        private int expectedUpdates;

        private Merger<T> merger;

        private int change = 0;

        UncommittedContext(int expectedUpdates, Merger<T> merger) {
            this.expectedUpdates = expectedUpdates;
            this.merger = merger;
        }

        int getChangeAndReset() {
            try {
                return change;
            } finally {
                change = 0;
            }
        }

        void insert(T newEntry) {
            change = 1;
            if (merger != null) {
                merger.insert(newEntry);
            }
        }

        boolean merge(T oldEntry, T newEntry) {
            return merger == null || merger.merge(oldEntry, newEntry);
        }

        void delete(T oldEntry) {
            change = -1;
            if (merger != null) {
                merger.delete(oldEntry);
            }
        }

    }
}