package org.sitoolkit.util.crudanalyzer.domain.methodcall;

import java.util.List;
import java.util.Set;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class MethodCallVisitor extends VoidVisitorAdapter<Set<MethodDef>> {

    JavaParserFacade jpf;

    @Override
    public void visit(MethodCallExpr methodCallExpr, Set<MethodDef> methodCalls) {
        try {
            SymbolReference<ResolvedMethodDeclaration> ref = jpf.solve(methodCallExpr);

            if (ref.isSolved()) {
                ResolvedMethodDeclaration rmd = ref.getCorrespondingDeclaration();
                MethodDef methodCall = new MethodDef();
                methodCall.setSignature(rmd.getQualifiedSignature());
                log.debug("Add method call : {}", methodCall);
                methodCalls.add(methodCall);
            } else {
                log.debug("Unsolved : {}", methodCallExpr);
            }
        } catch (Exception e) {
            log.debug("Unsolved:{}, {}", methodCallExpr, e);
        }

    }

}
