package org.xbib.tools.merge.zdb.licenseinfo;

import org.xbib.pipeline.PipelineRequest;
import org.xbib.pipeline.element.PipelineElement;
import org.xbib.tools.merge.zdb.entities.Manifestation;

public class ManifestationPipelineElement implements PipelineElement<Manifestation>, PipelineRequest {

    private Manifestation manifestation;

    private boolean forced;

    public ManifestationPipelineElement set(Manifestation manifestation) {
        this.manifestation = manifestation;
        return this;
    }

    public Manifestation get() {
        return manifestation;
    }

    public ManifestationPipelineElement setForced(boolean forced) {
        this.forced = forced;
        return this;
    }

    public boolean getForced() {
        return forced;
    }

}
