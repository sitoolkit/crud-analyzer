package org.sitoolkit.util.crudanalyzer.domain.crud.jsqlparser;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.sitoolkit.util.crudanalyzer.domain.crud.TableCrud;
import org.sitoolkit.util.crudanalyzer.domain.crud.jsqlparser.CrudFinderJsqlparserImpl;

public class CrudFinderJsqlparserImplTest {

    CrudFinderJsqlparserImpl finder = new CrudFinderJsqlparserImpl();

    Set<String> toSet(String... cruds) {
        return Stream.of(cruds).collect(Collectors.toSet());
    }

    @Test
    public void testInsertSelect() {
        TableCrud tableCrud = finder.findCrud("INSERT INTO tab_1 (col_1) SELECT col_1 FROM tab_2");

        assertThat(tableCrud.getCrud("tab_1"), is(toSet("C")));
        assertThat(tableCrud.getCrud("tab_2"), is(toSet("R")));

        tableCrud = finder.findCrud("SELECT col_1 INTO tab_2 FROM tab_1");

        assertThat(tableCrud.getCrud("tab_1"), is(toSet("R")));
        assertThat(tableCrud.getCrud("tab_2"), is(toSet("C")));
    }

    @Test
    public void testSelectJoin() {
        TableCrud tableCrud = finder
                .findCrud("SELECT * FROM tab_1 t1 JOIN tab_2 t2 ON t1.col_1 = t2.col_2");

        assertThat(tableCrud.getCrud("tab_1"), is(toSet("R")));
        assertThat(tableCrud.getCrud("tab_2"), is(toSet("R")));
    }

    @Test
    public void testSelectSub() {
        TableCrud tableCrud = finder.findCrud(
                "SELECT * FROM tab_1 t1 WHERE EXISTS (SELECT 1 FROM tab_2 t2 WHERE t2.col_1 = t1.col_1)");

        assertThat(tableCrud.getCrud("tab_1"), is(toSet("R")));
        assertThat(tableCrud.getCrud("tab_2"), is(toSet("R")));
    }

    @Test
    public void testSelectWith() {
        TableCrud tableCrud = finder
                .findCrud("WITH w1 AS (SELECT * FROM tab_1 t1) SELECT * FROM w1");

        assertThat(tableCrud.getCrud("tab_1"), is(toSet("R")));
    }

    @Test
    public void testUpdateSelect() {
        TableCrud tableCrud = finder.findCrud(
                "UPDATE tab_1 t1 SET col_1 = (SELECT col_1 FROM tab_2 t2 WHERE t2.col_2 = t1.col_2)");

        assertThat(tableCrud.getCrud("tab_1"), is(toSet("U")));
        assertThat(tableCrud.getCrud("tab_2"), is(toSet("R")));

        tableCrud = finder.findCrud(
                "UPDATE tab_1 t1 SET col_1 = 'x' WHERE EXISTS (SELECT 1 FROM tab_2 t2 WHERE t2.col_2 = t1.col_2)");

        assertThat(tableCrud.getCrud("tab_1"), is(toSet("U")));
        assertThat(tableCrud.getCrud("tab_2"), is(toSet("R")));
    }

    @Test
    public void testMerge() {
        TableCrud tableCrud = finder.findCrud(
                "MERGE INTO tab_1 t1 USING (SELECT col_1 FROM tab_2) t2 ON (t1.col_1 = t2.col_1) WHEN MATCHED THEN UPDATE SET col_2 = 1");

        assertThat(tableCrud.getCrud("tab_1"), is(toSet("M")));
        assertThat(tableCrud.getCrud("tab_2"), is(toSet("R")));
    }
}
