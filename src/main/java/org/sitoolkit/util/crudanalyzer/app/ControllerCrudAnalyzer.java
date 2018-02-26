package org.sitoolkit.util.crudanalyzer.app;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.sitoolkit.util.crudanalyzer.domain.crud.CrudMatrix;
import org.sitoolkit.util.crudanalyzer.domain.crud.CrudMatrixProcessor;
import org.sitoolkit.util.crudanalyzer.domain.crud.CrudMatrixWriter;
import org.sitoolkit.util.crudanalyzer.domain.crud.CrudMatrixWriter.Option;
import org.sitoolkit.util.crudanalyzer.domain.methodcall.MethodCallDictionary;
import org.sitoolkit.util.crudanalyzer.domain.methodcall.MethodCallReader;
import org.sitoolkit.util.crudanalyzer.infra.config.Config;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ControllerCrudAnalyzer {

    RepositoryCrudAnalizer repoCrudAnalizer = new RepositoryCrudAnalizer();

    MethodCallReader methodCallReader = new MethodCallReader();

    CrudMatrixProcessor processor = new CrudMatrixProcessor();

    CrudMatrixWriter writer = new CrudMatrixWriter();

    public static void main(String[] args) {
    	Path srcDir = Paths.get(Config.getInstance().getSrcDir());
		Path resDir = Paths.get(Config.getInstance().getResDir());
		Path tableDefFile = null;
		Path outFile = Paths.get(Config.getInstance().getOutFile());
		new ControllerCrudAnalyzer().execute(srcDir, resDir, tableDefFile, outFile);
    }

    public int execute(Path srcDir, Path resDir, Path tableDefFile, Path outFile) {

    	ExecutorService executor = Executors.newCachedThreadPool();
        try {
        	Future<MethodCallDictionary> fdictionary = executor.submit(() -> 
        		methodCallReader.read(srcDir)
        	);

        	Future<CrudMatrix> fmatrix = executor.submit(() -> repoCrudAnalizer.read(resDir, tableDefFile));

        	MethodCallDictionary dictionary = fdictionary.get();
        	CrudMatrix matrix = fmatrix.get();
        	
            matrix.setCrudRowMap(processor
                    .repository2controller(processor.function2signature(matrix.getCrudRowMap(), dictionary), dictionary));

            writer.write(matrix, outFile, Option.REPOSITORY_FUNCTION);
        } catch (Exception e) {
            log.error("Unexpected Exception", e);
        } finally {
        	executor.shutdown();
        }

        return 0;
    }

}
