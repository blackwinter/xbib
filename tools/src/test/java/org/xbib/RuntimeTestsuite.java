package org.xbib;

import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.xbib.tools.RunnerTest;

@RunWith(Categories.class)
@Categories.IncludeCategory(RuntimeTests.class)
@Suite.SuiteClasses( { RunnerTest.class })
public class RuntimeTestsuite {
}
