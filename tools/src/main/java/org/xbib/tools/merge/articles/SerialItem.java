package org.xbib.tools.merge.articles;

import org.xbib.pipeline.PipelineRequest;
import org.xbib.tools.merge.zdb.entities.Manifestation;

import java.util.Collection;

import static com.google.common.collect.Sets.newHashSet;

public class SerialItem implements PipelineRequest {

    private Integer date;

    private Collection<Manifestation> manifestations = newHashSet();

    public SerialItem() {
    }

    public void setDate(Integer date) {
        this.date = date;
    }

    public Integer getDate() {
        return date;
    }

    public void addManifestation(Manifestation manifestation) {
        this.manifestations.add(manifestation);
    }

    public Collection<Manifestation> getManifestations() {
        return manifestations;
    }

}
