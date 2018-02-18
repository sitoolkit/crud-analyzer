package org.sitoolkit.util.crudanalyzer.domain.methodcall;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.JavaParser;
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
            Files.walk(srcDir).filter(file -> file.toFile().getName().endsWith(".java"))
                    .forEach(javaFile -> {
                        readJava(javaFile).ifPresent(classDef -> dictionary.add(classDef));
                    });
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

        Path jarList = Paths.get("jar-list.txt");

        if (jarList.toFile().exists()) {
            try {
                Files.lines(jarList).forEach(line -> {
                    try {
                        combinedTypeSolver.add(JarTypeSolver.getJarTypeSolver(line));
                        log.info("jar is added. {}", line);
                    } catch (IOException e) {
                        log.warn("warn ", e);
                    }
                });

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        jpf = JavaParserFacade.get(combinedTypeSolver);
        methodCallVisitor = new MethodCallVisitor(jpf);
    }

    public Optional<ClassDef> readJava(Path javaFile) {
        log.info("Read java : {}", javaFile);

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
            methodDef.setName(declaredMethod.getName());
            methodDef.setSignature(declaredMethod.getQualifiedSignature());
            log.info("Add method declaration : {}", methodDef);
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
}
