package org.sitoolkit.util.crudanalyzer.app;

import java.nio.file.Path;

import org.sitoolkit.util.crudanalyzer.domain.crud.CrudMatrix;
import org.sitoolkit.util.crudanalyzer.domain.crud.CrudMatrixProcessor;
import org.sitoolkit.util.crudanalyzer.domain.crud.CrudMatrixWriter;
import org.sitoolkit.util.crudanalyzer.domain.methodcall.MethodCallDictionary;
import org.sitoolkit.util.crudanalyzer.domain.methodcall.MethodCallReader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ControllerCrudAnalyzer {

    RepositoryCrudAnalizer repoCrudAnalizer = new RepositoryCrudAnalizer();

    MethodCallReader methodCallReader = new MethodCallReader();

    CrudMatrixProcessor processor = new CrudMatrixProcessor();

    CrudMatrixWriter writer = new CrudMatrixWriter();

    public static void main(String[] args) {

    }

    public int execute(Path srcDir, Path resDir, Path tableDefFile, Path outFile) {

        try {
            MethodCallDictionary dictionary = methodCallReader.read(srcDir);

            CrudMatrix matrix = repoCrudAnalizer.read(resDir, tableDefFile);

            matrix.setCrudRowMap(processor
                    .convert2(processor.convert(matrix.getCrudRowMap(), dictionary), dictionary));

            writer.write(matrix, outFile);
        } catch (Exception e) {
            log.error("Unexpected Exception", e);
        }

        return 0;
    }

}
