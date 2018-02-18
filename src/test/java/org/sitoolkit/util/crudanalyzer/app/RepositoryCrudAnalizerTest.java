package org.sitoolkit.util.crudanalyzer.app;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.file.Paths;

import org.junit.Test;

public class RepositoryCrudAnalizerTest {

    RepositoryCrudAnalizer analizer = new RepositoryCrudAnalizer();

    @Test
    public void test() {
        int result = analizer.execute(Paths.get("crud-analyzer-test", "src", "main"),
                Paths.get("src", "test", "resources", "tables.txt"),
                Paths.get("target", "crud.csv"));
        assertThat(result, is(0));
    }

}
