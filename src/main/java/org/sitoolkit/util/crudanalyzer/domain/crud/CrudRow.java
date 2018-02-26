package org.sitoolkit.util.crudanalyzer.domain.crud;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Data;


@Data
public class CrudRow {
	
	private String actionPath;
	private Map<TableDef, Set<CrudType>> cellMap = new HashMap<>();
	private Map<TableDef, Set<String>> sqlTextMap = new HashMap<>();
	private Set<String> repositoryFunctions = new HashSet<>();
	
	public CrudRow(String actionPath) {
		super();
		this.actionPath = actionPath;
	}
	
	public void add(TableDef table, CrudType type, String sqlText) {
		 Set<CrudType> types = cellMap.computeIfAbsent(table, key -> new HashSet<>());
		 types.add(type);
		 Set<String> sqls = sqlTextMap.computeIfAbsent(table, key -> new HashSet<>());
		 sqls.add(sqlText);
	}
	
	public Set<CrudType> getType(TableDef table) {
		return cellMap.get(table);
	}
	
	public Set<String> getSql(TableDef table) {
		return sqlTextMap.get(table);
	}
	
	public CrudRow merge(CrudRow mergingCrud) {
		
		mergingCrud.getCellMap().entrySet().stream().forEach(cellMapEntry -> {
			Set<CrudType> existingSet = cellMap.computeIfAbsent(cellMapEntry.getKey(), key -> new HashSet<>());
			existingSet.addAll(cellMapEntry.getValue());
		});
		
		mergingCrud.getSqlTextMap().entrySet().stream().forEach(sqlTextMapEntry -> {
			Set<String> existingSet = sqlTextMap.computeIfAbsent(sqlTextMapEntry.getKey(), key -> new HashSet<>());
			existingSet.addAll(sqlTextMapEntry.getValue());
		});
		
		repositoryFunctions.addAll(mergingCrud.getRepositoryFunctions());
		
		return mergingCrud;
	}
}
