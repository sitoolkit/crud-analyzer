package org.sitoolkit.util.crudanalyzer.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.sitoolkit.util.crudanalyzer.domain.crud.CrudMatrix;
import org.sitoolkit.util.crudanalyzer.domain.crud.CrudMatrixWriter;
import org.sitoolkit.util.crudanalyzer.domain.crud.ErrorInfo;
import org.sitoolkit.util.crudanalyzer.domain.crud.MyBatisMapperReader;
import org.sitoolkit.util.crudanalyzer.domain.crud.RepositoryFunction;
import org.sitoolkit.util.crudanalyzer.domain.crud.RepositoryFunctionProcessor;
import org.sitoolkit.util.crudanalyzer.domain.crud.TableDef;
import org.sitoolkit.util.crudanalyzer.domain.crud.TableDefReader;
import org.sitoolkit.util.crudanalyzer.infra.config.Config;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RepositoryCrudAnalizer {

	MyBatisMapperReader mbmReader = new MyBatisMapperReader();

	RepositoryFunctionProcessor processor = new RepositoryFunctionProcessor();

	TableDefReader reader = new TableDefReader();

	CrudMatrixWriter writer = new CrudMatrixWriter();

	public static void main(String[] args) {
		int ret = new RepositoryCrudAnalizer().execute(Paths.get(Config.getInstance().getResDir()), null,
				Paths.get(Config.getInstance().getOutFile()));

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

		Pattern pattern = Pattern.compile(Config.getInstance().getRepositoryPathPattern());
		mbmReader.init();
		try {
			List<Path> paths = Files.walk(srcDir).filter(path -> pattern.matcher(path.toString()).matches())
					.collect(Collectors.toList());

			List<Path> readPaths = new ArrayList<>();
			paths.stream().parallel().forEach(repositoryXml -> {
				log.debug("read:{}", repositoryXml);
				List<RepositoryFunction> rfs = mbmReader.read(repositoryXml);

				readPaths.add(repositoryXml);
				matrix.merge(processor.process(rfs, tableDefs));
				
				if (readPaths.size() % 10 == 0) {
					log.info("Processed repository files : {} / {}", readPaths.size(), paths.size());
				}
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		matrix.getErrorMap().entrySet().stream().forEach(entry -> {
			String function = entry.getKey();
			ErrorInfo errorInfo = entry.getValue();
			log.warn("function:{}", function);
			log.warn("sqlText:{}", errorInfo.getSqlText());
			log.warn("editedSqlText:{}", errorInfo.getEditedSqlText());
			log.warn("errorMessage:{}", errorInfo.getErrorMessage());
		});

		log.info("functions:{}, errors:{}", matrix.getCrudRowMap().size(), matrix.getErrorMap().size());
		return matrix;
	}
}
