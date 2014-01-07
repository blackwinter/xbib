
package org.xbib.io.compress.xz;

import org.xbib.io.compress.xz.simple.ARM;
import org.xbib.io.compress.xz.simple.ARMThumb;
import org.xbib.io.compress.xz.simple.IA64;
import org.xbib.io.compress.xz.simple.PowerPC;
import org.xbib.io.compress.xz.simple.SPARC;
import org.xbib.io.compress.xz.simple.SimpleFilter;
import org.xbib.io.compress.xz.simple.X86;

import java.io.InputStream;

class BCJDecoder extends BCJCoder implements FilterDecoder {
    private final long filterID;
    private final int startOffset;

    BCJDecoder(long filterID, byte[] props)
            throws UnsupportedOptionsException {
        assert isBCJFilterID(filterID);
        this.filterID = filterID;

        if (props.length == 0) {
            startOffset = 0;
        } else if (props.length == 4) {
            int n = 0;
            for (int i = 0; i < 4; ++i) {
                n |= (props[i] & 0xFF) << (i * 8);
            }

            startOffset = n;
        } else {
            throw new UnsupportedOptionsException(
                    "Unsupported BCJ filter properties");
        }
    }

    public int getMemoryUsage() {
        return SimpleInputStream.getMemoryUsage();
    }

    public InputStream getInputStream(InputStream in) {
        SimpleFilter simpleFilter = null;

        if (filterID == X86_FILTER_ID) {
            simpleFilter = new X86(false, startOffset);
        } else if (filterID == POWERPC_FILTER_ID) {
            simpleFilter = new PowerPC(false, startOffset);
        } else if (filterID == IA64_FILTER_ID) {
            simpleFilter = new IA64(false, startOffset);
        } else if (filterID == ARM_FILTER_ID) {
            simpleFilter = new ARM(false, startOffset);
        } else if (filterID == ARMTHUMB_FILTER_ID) {
            simpleFilter = new ARMThumb(false, startOffset);
        } else if (filterID == SPARC_FILTER_ID) {
            simpleFilter = new SPARC(false, startOffset);
        } else {
            assert false;
        }

        return new SimpleInputStream(in, simpleFilter);
    }
}
