package org.sitoolkit.util.crudanalyzer.app;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.sitoolkit.util.crudanalyzer.domain.methodcall.MethodCallDictionary;
import org.sitoolkit.util.crudanalyzer.domain.methodcall.MethodCallReader;
import org.sitoolkit.util.crudanalyzer.domain.methodcall.MethodCallWriter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MethodCallAnalyzer {

    MethodCallReader methodCallReader = new MethodCallReader();

    MethodCallWriter writer = new MethodCallWriter();

    public static void main(String[] args) {
        String srcDir = args.length == 0 ? "." : args[0];
        System.exit(new MethodCallAnalyzer().execute(Paths.get(srcDir)));
    }

    public int execute(Path srcDir) {

        try {

            MethodCallDictionary dictionary = methodCallReader.read(srcDir);

            log.info(writer.write(dictionary.getClassDefs()));

        } catch (Exception e) {
            log.error("Unexpected Excetion", e);
            return 1;
        }

        return 0;
    }

}
