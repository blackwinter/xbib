package org.xbib.graphics.vector;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.xbib.graphics.vector.eps.EPSTests;
import org.xbib.graphics.vector.intermediate.IRTests;
import org.xbib.graphics.vector.pdf.PDFTests;
import org.xbib.graphics.vector.svg.SVGTests;
import org.xbib.graphics.vector.util.UtilTests;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestUtilsTest.class,
        UtilTests.class,
        IRTests.class,
        VectorGraphics2DTest.class,
        EPSTests.class,
        PDFTests.class,
        SVGTests.class
})
public class AllTests {
    static {
        System.setProperty("java.awt.headless", "true");
    }
}
