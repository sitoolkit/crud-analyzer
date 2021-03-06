package org.sitoolkit.util.crudanalyzer.domain.crud;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CrudMatrixWriter {

	// private static final String REG_EXP_WILDCARD = "*";
	private static final String CSV_ENCLOSER = "\"";
	private static final String CSV_SEPARATOR = ",";

	public void write(CrudMatrix matrix, Path path) {
		write(matrix, path, Option.SQL_TEXT);
	}

	public enum Option {
		SQL_TEXT, REPOSITORY_FUNCTION;
	}

	public void write(CrudMatrix matrix, Path path, Option option) {

		List<String> outputLines = new ArrayList<>();

		List<String> headerLine = new ArrayList<>();
		headerLine.add("function");
		headerLine.add("action / table");
		SortedSet<TableDef> tables = matrix.getAllTablesOrderByName();
		tables.stream().forEach(table -> headerLine.add(table.getName()));
		headerLine.add(option.name().toLowerCase().replace("_", " "));
		outputLines.add(String.join(CSV_SEPARATOR, headerLine));

		outputLines.addAll(buildBodyLines(matrix, tables, option));

		// outputLines.add(buildCountIf(CrudType.UPDATE, tables.size() + 1,
		// outputLines.size()));
		// outputLines.add(buildCountIf(CrudType.DELETE, tables.size() + 1,
		// outputLines.size() - 1));
		// outputLines.add(buildCountIf(CrudType.MERGE, tables.size() + 1,
		// outputLines.size() - 2));
		// outputLines.add(buildSum(tables.size() + 1, outputLines.size()));

		try {
			Files.write(path, outputLines);
			
		} catch (IOException e) {
			path = path.resolveSibling(path.getFileName() + Long.toString(System.currentTimeMillis()));
			try {
				Files.write(path, outputLines);
			} catch (IOException e1) {
				throw new IllegalArgumentException(e);
			}
		}
		
		log.info("write file:{}", path);
	}

	List<String> buildBodyLines(CrudMatrix matrix, SortedSet<TableDef> tables, Option option) {
		return matrix.getCrudRowMap().entrySet().stream().map(functionCrudRow -> {
			String function = functionCrudRow.getKey();
			CrudRow crudRow = functionCrudRow.getValue();

			List<String> line = new ArrayList<>();
			line.add(CSV_ENCLOSER + function + CSV_ENCLOSER);
			line.add(CSV_ENCLOSER + crudRow.getActionPath() + CSV_ENCLOSER);

			tables.stream().forEach(table -> {
				Set<CrudType> crudTypes = crudRow.getType(table);

				if (crudTypes == null) {
					line.add(StringUtils.EMPTY);
				} else {
					line.add(crudTypes.stream().map(CrudType::toString).collect(Collectors.joining()));
				}
			});

			switch (option) {
			case SQL_TEXT:
				tables.stream().forEach(table -> {
					Set<String> sqls = crudRow.getSql(table);

					if (sqls != null) {
						sqls.stream().forEach(sql -> line.add(CSV_ENCLOSER + escape(sql) + CSV_ENCLOSER));
					}
				});
				break;
			case REPOSITORY_FUNCTION:
				crudRow.getRepositoryFunctions().stream().forEach(repoFunc -> line.add(repoFunc));
				break;
			}

			return String.join(CSV_SEPARATOR, line);
		}).collect(Collectors.toList());
	}

	String escape(String sqlText) {
		if (StringUtils.isEmpty(sqlText)) {
			return "";
		}
		return sqlText.replace(CSV_ENCLOSER, CSV_ENCLOSER + CSV_ENCLOSER).replace("\r\n", "").replace("\n", "");
	}

	// private String buildSum(int rowSize, int lineSize) {
	// return "SUM" + CSV_SEPARATOR + IntStream.range(1, rowSize).mapToObj(i ->
	// {
	// return formatFormula("\"=SUM(%s)\"", lineSize - 2, lineSize, i, null);
	// }).collect(Collectors.joining(CSV_SEPARATOR));
	// }

	// String buildCountIf(CrudType type, int rowSize, int lineSize) {
	// return type.toString() + CSV_SEPARATOR + IntStream.range(1,
	// rowSize).mapToObj(i -> {
	// return formatFormula("\"=COUNTIF(%s, \"\"%s\"\")\"", 2, lineSize, i,
	// REG_EXP_WILDCARD + type.toString() + REG_EXP_WILDCARD);
	// }).collect(Collectors.joining(CSV_SEPARATOR));
	// }
	//
	// String formatFormula(String format, int startWith, int lineSize, int
	// index, String value) {
	// String rowAlfa = toAlfa(index) + "$";
	// String topCell = rowAlfa + startWith;
	// String buttomCell = rowAlfa + lineSize;
	// if (StringUtils.isEmpty(value)) {
	// return String.format(format, topCell + ":" + buttomCell);
	// } else {
	// return String.format(format, topCell + ":" + buttomCell, value);
	// }
	// }
	//
	// public static String toAlfa(final int number) {
	// int temp = number;
	// int alfaRange = ('Z' - 'A') + 1;
	// StringBuilder sb = new StringBuilder();
	// do {
	// sb.append((char) ('A' + (temp % alfaRange)));
	// temp /= alfaRange;
	// } while (0 < temp--);
	// return sb.reverse().toString();
	// }
}
