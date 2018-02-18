package org.sitoolkit.util.crudanalyzer.app;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Paths;

import org.junit.Test;

public class MethodCallAnalyzerTest {

    MethodCallAnalyzer analyzer = new MethodCallAnalyzer();

    @Test
    public void test() {
        int result = analyzer.execute(Paths.get("crud-analyzer-test", "src", "main", "java"));
        assertThat(result, is(0));
    }

}
