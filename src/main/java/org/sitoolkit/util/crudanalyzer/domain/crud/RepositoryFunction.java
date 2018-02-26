package org.sitoolkit.util.crudanalyzer.domain.crud;

import lombok.Data;

@Data
public class RepositoryFunction {
	private String function;
	private String sqlText = "";
}