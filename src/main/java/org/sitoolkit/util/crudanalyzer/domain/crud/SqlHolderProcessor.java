package org.sitoolkit.util.crudanalyzer.domain.crud;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.sitoolkit.util.crudanalyzer.domain.crud.jsqlparser.CrudFinderJsqlparserImpl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SqlHolderProcessor {

	private static final Pattern PATTERN_FOREACH = Pattern.compile("<foreach.*open=\"(.*?)\".*close=\"(.*?)\".*>");
	
    CrudFinder crudFinder = new CrudFinderJsqlparserImpl();

    public CrudMatrix process(SqlHolder holder, List<TableDef> tableDefs) {
        CrudMatrix matrix = new CrudMatrix();
        matrix.getTableDefs().addAll(tableDefs);


        holder.getQueryMap().entrySet().stream().forEach(entry -> {

            String function = entry.getKey();
            String sqlText = entry.getValue();
            log.debug("repositoryMethod:{}, sqlText:{}", function, sqlText);
            String editedSqlText = edit(sqlText);
            log.debug("editedSqlText:{}", editedSqlText);

            CrudFindResult result = crudFinder.findCrud(editedSqlText);
            
            if (result.isError()) {
            	matrix.addError(function, sqlText, editedSqlText, result.getErrMsg());
            }

            result.getMap().keySet().stream()
                    .forEach(table -> result.getMap().get(table).stream().forEach(
                            crud -> matrix.add(function, new TableDef(table), crud, sqlText)));
        });
        
        return matrix;
    }

    
    String edit(String sqlText) {
    	sqlText = sqlText.toLowerCase();
    	
        sqlText = sqlText.replace("<set>", "set");
        sqlText = sqlText.replaceAll(",\\s</set>", "");

        sqlText = sqlText.replace("<choose>", "");
        sqlText = sqlText.replace("</choose>", "");

        sqlText = sqlText.replaceAll("<when.*?>", "");
        sqlText = sqlText.replace("</when>", "");

        sqlText = sqlText.replaceAll("<otherwise>.*</otherwise>", "");
        
        sqlText = sqlText.replace("<where>", "where");
        sqlText = sqlText.replace("</where>", "");
        
        sqlText = sqlText.replaceAll("select\\s*<include.*?/>\\sfrom", "select * from");
        sqlText = sqlText.replaceAll("<include.*?/>", "");


        sqlText = sqlText.replaceAll("<if.*?>", "");
        sqlText = sqlText.replace("</if>", "");
        
        sqlText = sqlText.replaceAll("first_value(.*) as ", "");

		Matcher foreachMatcher = PATTERN_FOREACH.matcher(sqlText);
		if (foreachMatcher.find()) {
			sqlText = sqlText.replaceAll("<foreach.*?>", "(");
			sqlText = sqlText.replace("</foreach>", ")");
		}

		sqlText = sqlText.replaceAll("<foreach.*?>", "");
		sqlText = sqlText.replace("</foreach>", "");

		
        sqlText = sqlText.replaceAll("#\\{.*?\\}", "'replaced'");
        sqlText = sqlText.replaceAll("\\$\\{.*?\\}", "replaced");

        return sqlText;
    }
}
