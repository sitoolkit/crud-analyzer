package org.sitoolkit.util.crudanalyzer.domain.crud.jsqlparser;

import org.sitoolkit.util.crudanalyzer.domain.crud.CrudFinder;
import org.sitoolkit.util.crudanalyzer.domain.crud.CrudType;
import org.sitoolkit.util.crudanalyzer.domain.crud.TableCrud;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;

@Slf4j
public class CrudFinderJsqlparserImpl implements CrudFinder {

    @Override
    public TableCrud findCrud(String sqlText) {
        TableCrud tableCrud = new TableCrud();

        try {
            Statement stmt = CCJSqlParserUtil.parse(sqlText);

            if (stmt instanceof Insert) {
                Insert insert = (Insert) stmt;
                tableCrud.put(insert.getTable().getName(), CrudType.CREATE);

                findCrudFromSelect(insert.getSelect(), tableCrud);

            } else if (stmt instanceof Select) {

                findCrudFromSelect((Select) stmt, tableCrud);

            } else if (stmt instanceof Update) {
                Update update = (Update) stmt;
                update.getTables().stream()
                        .forEach(table -> tableCrud.put(table.getName(), CrudType.UPDATE));

                if (update.getExpressions() != null) {
                    update.getExpressions().stream()
                            .forEach(expr -> findReferenceFromExpression(expr, tableCrud));
                }

                findReferenceFromExpression(update.getWhere(), tableCrud);

            } else if (stmt instanceof Delete) {
                Delete delete = (Delete) stmt;
                tableCrud.put(delete.getTable().getName(), CrudType.DELETE);

            } else if (stmt instanceof Merge) {
                Merge merge = (Merge) stmt;
                tableCrud.put(merge.getTable().getName(), CrudType.MERGE);

                findReferenceFromExpression(merge.getUsingSelect(), tableCrud);

                if (merge.getUsingTable() != null) {
                    tableCrud.put(merge.getUsingTable().getName(), CrudType.REFERENCE);
                }
            }

        } catch (JSQLParserException e) {
            log.warn("SQL parse error", e);
        }

        return tableCrud;
    }

    void findCrudFromSelect(Select select, TableCrud tableCrud) {

        if (select == null) {
            return;
        }

        findReferenceFromSatement(select, tableCrud);

        select.getSelectBody().accept(new SelectVisitorAdapter() {

            @Override
            public void visit(PlainSelect plainSelect) {
                if (plainSelect.getIntoTables() != null) {
                    plainSelect.getIntoTables().stream()
                            .forEach(table -> tableCrud.put(table.getName(), CrudType.CREATE));
                }
            }

        });

    }

    void findReferenceFromSatement(Statement stmt, TableCrud tableCrud) {

        if (stmt == null) {
            return;
        }

        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();

        tablesNamesFinder.getTableList(stmt).stream()
                .forEach(table -> tableCrud.put(table, CrudType.REFERENCE));

    }

    void findReferenceFromExpression(Expression expr, TableCrud tableCrud) {

        if (expr == null) {
            return;
        }
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();

        tablesNamesFinder.getTableList(expr).stream()
                .forEach(table -> tableCrud.put(table, CrudType.REFERENCE));

    }
}
