package org.sitoolkit.util.crudanalyzer.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.sitoolkit.util.crudanalyzer.domain.crud.CrudMatrix;
import org.sitoolkit.util.crudanalyzer.domain.crud.CrudMatrixWriter;
import org.sitoolkit.util.crudanalyzer.domain.crud.MyBatisRepositoryLineProcessor;
import org.sitoolkit.util.crudanalyzer.domain.crud.SqlHolder;
import org.sitoolkit.util.crudanalyzer.domain.crud.SqlHolderProcessor;
import org.sitoolkit.util.crudanalyzer.domain.crud.TableDef;
import org.sitoolkit.util.crudanalyzer.domain.crud.TableDefReader;
import org.sitoolkit.util.crudanalyzer.infra.filescan.FileScanner;
import org.sitoolkit.util.crudanalyzer.infra.filescan.ScanningContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RepositoryCrudAnalizer {

    FileScanner scanner = new FileScanner();

    MyBatisRepositoryLineProcessor finder = new MyBatisRepositoryLineProcessor();

    SqlHolderProcessor processor = new SqlHolderProcessor();

    TableDefReader reader = new TableDefReader();

    CrudMatrixWriter writer = new CrudMatrixWriter();

    public static void main(String[] args) {

        String dir = args.length == 0 ? "." : args[0];
        int ret = new RepositoryCrudAnalizer().execute(Paths.get(dir), Paths.get("tables.txt"),
                Paths.get("crud.csv"));

        System.exit(ret);
    }

    public int execute(Path srcDir, Path tableListFile, Path outFile) {
        try {
            CrudMatrix matrix = read(srcDir, tableListFile);

            log.info("action count : {}", matrix.getCrudRowMap().size());

            writer.write(matrix, outFile);

            return 0;
        } catch (Exception e) {
            log.error("error occurs", e);
            return 1;
        }
    }

    public CrudMatrix read(Path srcDir, Path tableDefFile) {
        CrudMatrix matrix = new CrudMatrix();
        List<TableDef> tableDefs = reader.read(tableDefFile);
        matrix.getTableDefs().addAll(tableDefs);

        try {
            Files.walk(srcDir).filter(path -> path.toString().endsWith("Repository.xml"))
                    .forEach(repositoryXml -> {
                        ScanningContext ctx = scanner.scan(repositoryXml, finder);

                        SqlHolder holder = finder.getResult(ctx);

                        matrix.merge(processor.process(holder, tableDefs));
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return matrix;
    }
}
