package org.sitoolkit.util.crudanalyzer.domain.crud;

public interface CrudFinder {

    TableCrud findCrud(String sqlText);

}
