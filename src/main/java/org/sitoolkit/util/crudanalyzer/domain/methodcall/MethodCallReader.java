package org.sitoolkit.util.crudanalyzer.domain.methodcall;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.sitoolkit.util.crudanalyzer.infra.config.Config;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedParameterDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MethodCallReader {

	private JavaParserFacade jpf;

	private MethodCallVisitor methodCallVisitor;

	public MethodCallDictionary read(Path srcDir) {
		MethodCallDictionary dictionary = new MethodCallDictionary();

		init(srcDir);

		try {
			Pattern p = Pattern.compile(Config.getInstance().getJavaFilePattern());
			List<Path> files = Files.walk(srcDir).filter(file -> p.matcher(file.toFile().getName()).matches())
					.collect(Collectors.toList());
			
			
			files.stream().forEach(javaFile -> {
				readJava(javaFile).ifPresent(classDef -> dictionary.add(classDef));
				
				int readCount = dictionary.getClassDefs().size();
				if (readCount % 10 == 0) {
					log.info("Processed java files : {} / {} ", readCount, files.size());
				}
			});
			
			// JavaParserFacade seems to be NOT thread-safe
//			files.stream().parallel().forEach(javaFile -> {
//				readJava(javaFile).ifPresent(classDef -> dictionary.add(classDef));
//			});
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

		dictionary.solveMethodCalls();

		return dictionary;
	}

	public void init(Path srcDir) {
		CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
		combinedTypeSolver.add(new ReflectionTypeSolver());
		combinedTypeSolver.add(new JavaParserTypeSolver(srcDir.toFile()));

		Path jarList = Paths.get(Config.getInstance().getJarList());

		if (jarList.toFile().exists()) {
			try {
				String jarListStr = new String(Files.readAllBytes(jarList));
				
				for(String line : jarListStr.split(File.pathSeparator + "|" + System.lineSeparator())) {
					try {
						combinedTypeSolver.add(JarTypeSolver.getJarTypeSolver(line));
						log.info("jar is added. {}", line);
					} catch (IOException e) {
						log.warn("warn ", e);
					}
				}

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		jpf = JavaParserFacade.get(combinedTypeSolver);
		methodCallVisitor = new MethodCallVisitor(jpf);
	}

	public Optional<ClassDef> readJava(Path javaFile) {
		log.debug("Read java : {}", javaFile);

		try {
			CompilationUnit compilationUnit = JavaParser.parse(javaFile);
			ClassDef classDef = new ClassDef();
			String typeName = compilationUnit.getPrimaryTypeName().get();
			classDef.setName(typeName);
			classDef.setPkg(compilationUnit.getPackageDeclaration().get().getNameAsString());

			compilationUnit.getClassByName(typeName).ifPresent(clazz -> {
				classDef.setMethods(readMethodDefs(clazz));
			});

			compilationUnit.getInterfaceByName(typeName).ifPresent(interfaze -> {
				classDef.setMethods(readMethodDefs(interfaze));
			});

			log.debug("Read class : {}", classDef);

			return Optional.of(classDef);
		} catch (IOException e) {
			log.warn("IOException", e);
			return Optional.empty();
		}
	}

	List<MethodDef> readMethodDefs(ClassOrInterfaceDeclaration typeDec) {
		List<MethodDef> methodDefs = new ArrayList<>();

		jpf.getTypeDeclaration(typeDec).getDeclaredMethods().forEach(declaredMethod -> {

			MethodDef methodDef = new MethodDef();
			methodDef.setPublic(declaredMethod.accessSpecifier() == AccessSpecifier.PUBLIC);
			methodDef.setName(declaredMethod.getName());
			methodDef.setSignature(declaredMethod.getQualifiedSignature());
			log.debug("Add method declaration : {}", methodDef);
			methodDefs.add(methodDef);

			if (!typeDec.isInterface()) {
				typeDec.getMethods().stream().forEach(method -> {
					if (equalMethods(declaredMethod, method)) {
						method.accept(methodCallVisitor, methodDef.getMethodCalls());
					}
				});
			}

		});

		return methodDefs;
	}

	boolean equalMethods(ResolvedMethodDeclaration m1, MethodDeclaration m2) {
		if (!m1.getName().equals(m2.getNameAsString())) {
			return false;
		}

		if (m1.getNumberOfParams() != m2.getParameters().size()) {
			return false;
		}

		for (int i = 0; i < m1.getNumberOfParams(); i++) {
			ResolvedParameterDeclaration p1 = m1.getParam(i);
			Parameter p2 = m2.getParameter(i);

			if (!p1.getName().endsWith(p2.getNameAsString())) {
				return false;
			}
		}

		return true;
	}
	
	public static void main(String[] args) throws IOException {
		String str = new String(Files.readAllBytes(Paths.get("jar-list.txt")));
		
		for(String line : str.split(File.pathSeparator + "|" + System.lineSeparator())) {
			System.out.println(line);
		}
	}
}
