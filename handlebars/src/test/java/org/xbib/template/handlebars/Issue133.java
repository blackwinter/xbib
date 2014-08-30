
package org.xbib.template.handlebars;

import org.junit.Test;
import org.xbib.template.handlebars.util.StringUtil;

import java.io.IOException;

public class Issue133 extends AbstractTest {

    @Override
    protected Handlebars newHandlebars() {
        Handlebars handlebars = new Handlebars();
        handlebars.registerHelpers(this);
        return handlebars;
    }

    @Test
    public void issue133() throws IOException {
        shouldCompileTo("{{times nullvalue 3}}", $("nullvalue", null), "");

        shouldCompileTo("{{times nullvalue 3}}", $("nullvalue", "a"), "aaa");
    }

    public String times(final String string, final Integer length, final Options options) {
        if (string == null) {
            return "";
        } else {
            return StringUtil.repeat(string, length);
        }
    }
}