package org.sitoolkit.util.crudanalyzer.domain.crud;

import lombok.Data;
import lombok.Getter;

@Data
public class TableDef implements Comparable<TableDef> {

	private String name;
	
	@Getter
	private String nameToFind;

	@Override
	public int compareTo(TableDef o) {
		return this.getName().compareTo(o.getName());
	}

	public TableDef(String name) {
		super();
		this.name = name;
		this.nameToFind = " " + name.toLowerCase() + " ";
	}
}
