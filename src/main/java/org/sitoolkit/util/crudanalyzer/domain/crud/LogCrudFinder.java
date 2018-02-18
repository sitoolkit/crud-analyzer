package org.sitoolkit.util.crudanalyzer.domain.crud;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sitoolkit.util.crudanalyzer.infra.filescan.LineProcessor;
import org.sitoolkit.util.crudanalyzer.infra.filescan.ScanningContext;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogCrudFinder implements LineProcessor<CrudMatrix> {

    private static final String RESULT_KEY = "crudLogMap";

    private static final String SQL_LOG_PREFIX = "==>  Preparing:";

    private static final int SQL_LOG_PREFIX_END = SQL_LOG_PREFIX.length() + 1;

    /**
     * "[/SCRN_" + 数字3桁 + 任意文字列 + "]"から"SCRN_" + 数字3桁 を抽出する正規表現
     */
    private static final Pattern ACTION_PATH_PATTERN = Pattern
            .compile("\\[(/SCRN_\\d{3}[a-zA-Z/]*)\\]");

    @Setter
    private List<TableDef> tableDefs;

    @Override
    public void process(ScanningContext context, String line) {

        int index = line.indexOf(SQL_LOG_PREFIX);

        if (index < 0) {
            return;
        }

        Matcher m = ACTION_PATH_PATTERN.matcher(line);

        if (!m.find()) {
            return;
        }

        // String actionPath = m.group(); // 何故か前後の[]が含まれてしまう。
        String actionPath = line.substring(m.start() + 1, m.end() - 1);

        CrudMatrix matrix = (CrudMatrix) context.computeIfAbsent(RESULT_KEY,
                key -> new CrudMatrix());

        String sqlText = line.substring(index + SQL_LOG_PREFIX_END);
        CrudType crudType = CrudType.parse(sqlText);

        log.info("{} {} {}", actionPath, crudType, sqlText);

        tableDefs.stream()
                .filter(table -> sqlText.toLowerCase().contains(table.getName().toLowerCase()))
                .forEach(table -> matrix.add(actionPath, table, crudType, sqlText));

    }

    @Override
    public CrudMatrix getResult(ScanningContext context) {
        return (CrudMatrix) context.get(RESULT_KEY);
    }

}
