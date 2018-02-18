package org.sitoolkit.util.crudanalyzer.domain.crud;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class TableDefReader {

	public List<TableDef> read(Path path) {
		try {
			return Files.readAllLines(path).stream().map(TableDef::new).collect(Collectors.toList());
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		
	}
}
