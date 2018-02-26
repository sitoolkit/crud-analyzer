package org.sitoolkit.util.crudanalyzer.domain.crud;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.sitoolkit.util.crudanalyzer.infra.filescan.LineProcessor;
import org.sitoolkit.util.crudanalyzer.infra.filescan.ScanningContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyBatisRepositoryLineProcessor implements LineProcessor<SqlHolder> {

    private static final String RESULT_KEY = "sqlHolder";

    private static final Pattern FUNCTION_NAME_PATTERN = Pattern.compile("id=\"([a-zA-Z0-9]*)\"");

    private static final String[] LINE_PREFIX_IGNORE = new String[] { "<![cdata[", "]]>" };

    private static final String[] LINE_PREFIX_START = new String[] { "<insert", "<select",
            "<update", "<delete" };

    private static final String[] LINE_PREFIX_END = new String[] { "</insert", "</select",
            "</update", "</delete" };

    @Override
    public void process(ScanningContext context, String line) {

        SqlHolder holder = (SqlHolder) context.computeIfAbsent(RESULT_KEY, key -> new SqlHolder());

        String trimedLine = line.trim().toLowerCase();

        if (trimedLine.startsWith("<!--")) {
            holder.startSkipping();
        }

        if (holder.isSkipping() && trimedLine.startsWith("-->")) {
            holder.endSkiping();
            return;
        }

        if (holder.isSkipping()) {
            return;
        }

        if (StringUtils.startsWithAny(trimedLine, LINE_PREFIX_START)) {
            holder.startForWaitingGt(buildFunctionName(context.getFilePath(), line));
        }

        if (holder.isWaitingForGt() && trimedLine.endsWith(">")) {
            holder.startCollecting();
            return;
        }

        if (!holder.isCollecting()) {
            return;
        }

        if (StringUtils.startsWithAny(trimedLine, LINE_PREFIX_END)) {
            holder.endCollecting();
            return;
        }

        if (StringUtils.startsWithAny(trimedLine, LINE_PREFIX_IGNORE)) {
            return;
        }

        if (holder.isCollecting()) {
        	trimedLine = StringUtils.substringBefore(trimedLine, "--");
            holder.addQueryLine(trimedLine);
        }

    }

    @Override
    public SqlHolder getResult(ScanningContext context) {
        return (SqlHolder) context.get(RESULT_KEY);
    }

    String buildFunctionName(Path filePath, String str) {
        String functionName = StringUtils.substringBefore(filePath.getFileName().toString(),
                ".xml");

        return functionName + "." + retriveFunction(str);
    }

    String retriveFunction(String str) {
        try {
            Matcher m = FUNCTION_NAME_PATTERN.matcher(str);
            m.find();
            return m.group(1);
        } catch (Exception e) {
            log.warn("fail to retrive function name", e);
            return str;
        }
    }

}
