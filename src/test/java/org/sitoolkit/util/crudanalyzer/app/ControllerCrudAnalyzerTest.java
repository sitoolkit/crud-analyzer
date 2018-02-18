package org.sitoolkit.util.crudanalyzer.app;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Paths;

import org.junit.Test;

public class ControllerCrudAnalyzerTest {

    ControllerCrudAnalyzer analyzer = new ControllerCrudAnalyzer();

    @Test
    public void test() {
        int result = analyzer.execute(Paths.get("crud-analyzer-test", "src", "main", "java"),
                Paths.get("crud-analyzer-test", "src", "main", "resources"),
                Paths.get("tables.txt"), Paths.get("target", "crud.csv"));
        assertThat(result, is(0));
    }

}
