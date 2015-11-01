package org.xbib.io.negotiate;

import java.util.List;

public class Negotiation {

    private final List<MediaRangeSpec> ranges;

    private MediaRangeSpec bestMatchingVariant = null;

    private MediaRangeSpec bestDefaultVariant = null;

    private double bestMatchingQuality = 0;

    private double bestDefaultQuality = 0;

    Negotiation(List<MediaRangeSpec> ranges) {
        this.ranges = ranges;
    }

    MediaRangeSpec negotiate(List<VariantSpec> variantSpecs) {
        for (VariantSpec variant : variantSpecs) {
            if (variant.isDefault()) {
                evaluateDefaultVariant(variant.getMediaType());
            }
            evaluateVariant(variant.getMediaType());
            for (MediaRangeSpec alias : variant.getAliases()) {
                evaluateVariantAlias(alias, variant.getMediaType());
            }
        }
        return (bestMatchingVariant == null) ? bestDefaultVariant : bestMatchingVariant;
    }

    private void evaluateVariantAlias(MediaRangeSpec variant, MediaRangeSpec isAliasFor) {
        if (variant.getBestMatch(ranges) == null) {
            return;
        }
        double q = variant.getBestMatch(ranges).getQuality();
        if (q * variant.getQuality() > bestMatchingQuality) {
            bestMatchingVariant = isAliasFor;
            bestMatchingQuality = q * variant.getQuality();
        }
    }

    private void evaluateVariant(MediaRangeSpec variant) {
        evaluateVariantAlias(variant, variant);
    }

    private void evaluateDefaultVariant(MediaRangeSpec variant) {
        if (variant.getQuality() > bestDefaultQuality) {
            bestDefaultVariant = variant;
            bestDefaultQuality = 0.00001 * variant.getQuality();
        }
    }
}