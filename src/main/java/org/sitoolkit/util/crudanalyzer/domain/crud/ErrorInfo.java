package org.sitoolkit.util.crudanalyzer.domain.crud;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorInfo {
	private String sqlText;
	private String editedSqlText;
	private String errorMessage; 
}