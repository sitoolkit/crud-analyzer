package org.sitoolkit.util.crudanalyzer.domain.crud;

import java.util.List;

import org.sitoolkit.util.crudanalyzer.domain.crud.jsqlparser.CrudFinderJsqlparserImpl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SqlHolderProcessor {

    CrudFinder crudFinder = new CrudFinderJsqlparserImpl();

    public CrudMatrix process(SqlHolder holder, List<TableDef> tableDefs) {
        CrudMatrix matrix = new CrudMatrix();
        matrix.getTableDefs().addAll(tableDefs);

        holder.getQueryMap().entrySet().stream().forEach(entry -> {

            String function = entry.getKey();
            String sqlText = entry.getValue();
            log.info("repositoryMethod:{}, sqlText:{}", function, sqlText);
            String placeHolderLessSqlText = sqlText.replaceAll("#\\{.*?\\}", "1");
            log.info("placeHolderLessSqlText:{}", placeHolderLessSqlText);

            TableCrud tableCrud = crudFinder.findCrud(placeHolderLessSqlText);

            tableCrud.getMap().keySet().stream()
                    .forEach(table -> tableCrud.getMap().get(table).stream().forEach(
                            crud -> matrix.add(function, new TableDef(table), crud, sqlText)));
        });

        return matrix;
    }

}
