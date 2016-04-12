package org.xbib.openurl.internal.entities;

import org.xbib.openurl.descriptors.Descriptor;
import org.xbib.openurl.entities.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EntityImpl<D extends Descriptor> implements Entity<D> {

    private final List<D> descriptors;

    protected EntityImpl(Collection<D> descriptors) {
        this.descriptors = new ArrayList<>();
        if (descriptors != null) {
            this.descriptors.addAll(descriptors);
        }
    }

    @Override
    public Collection<D> getDescriptors() {
        return descriptors;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < descriptors.size(); i++) {
            sb.append(descriptors.get(i)).append("\n");
        }
        return sb.toString();
    }

    @Override
    public void addDescriptor(D descriptor) {
        descriptors.add(descriptor);
    }
}
