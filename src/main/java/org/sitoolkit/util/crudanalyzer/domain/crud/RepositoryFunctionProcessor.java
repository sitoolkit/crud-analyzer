package org.sitoolkit.util.crudanalyzer.domain.crud;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sitoolkit.util.crudanalyzer.domain.crud.jsqlparser.CrudFinderJsqlparserImpl;

public class RepositoryFunctionProcessor {

    CrudFinder crudFinder = new CrudFinderJsqlparserImpl();

    public CrudMatrix process(List<RepositoryFunction> repoFuncs, List<TableDef> tableDefs) {
        CrudMatrix matrix = new CrudMatrix();
        matrix.getTableDefs().addAll(tableDefs);


        repoFuncs.stream().forEach(repoFunc -> {

        	if (StringUtils.isEmpty(repoFunc.getSqlText())) {
        		return;
        	}
        	
            CrudFindResult result = crudFinder.findCrud(repoFunc.getSqlText());
            
            if (result.isError()) {
            	matrix.addError(repoFunc.getFunction(), repoFunc.getSqlText(), "", result.getErrMsg());
            }

            result.getMap().keySet().stream()
                    .forEach(table -> result.getMap().get(table).stream().forEach(
                            crud -> matrix.add(repoFunc.getFunction(), new TableDef(table), crud, repoFunc.getSqlText())));
        });
        
        return matrix;
    }

}
