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

	public CrudRow(String actionPath) {
		super();
		this.actionPath = actionPath;
	}
	
	public CrudRow(CrudRow crudRow) {
		this.actionPath = crudRow.getActionPath();
		crudRow.getCellMap().entrySet().forEach(entry -> {
			Set<CrudType> newType = new HashSet<>();
			entry.getValue().stream().forEach(newType::add);
			this.cellMap.put(entry.getKey(), newType);
		});
		crudRow.getSqlTextMap().entrySet().forEach(entry -> {
			Set<String> newSql = new HashSet<>();
			entry.getValue().stream().forEach(newSql::add);
			this.sqlTextMap.put(entry.getKey(), newSql);
		});
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
	
	public CrudRow merge(CrudRow crud) {
		crud.getCellMap().entrySet().stream().forEach(entry -> {
			Set<CrudType> existingSet = cellMap.get(entry.getKey());

			Set<CrudType> newType = new HashSet<>();
			entry.getValue().stream().forEach(newType::add);
			if (existingSet == null) {
				cellMap.put(entry.getKey(), newType);
			} else {
				existingSet.addAll(newType);
			}
		});
		crud.getSqlTextMap().entrySet().stream().forEach(entry -> {
			Set<String> existingSet = sqlTextMap.get(entry.getKey());

			Set<String> newSql = new HashSet<>();
			entry.getValue().stream().forEach(newSql::add);
			if (existingSet == null) {
				sqlTextMap.put(entry.getKey(), newSql);
			} else {
				existingSet.addAll(newSql);
			}
		});
		return crud;
	}
}
