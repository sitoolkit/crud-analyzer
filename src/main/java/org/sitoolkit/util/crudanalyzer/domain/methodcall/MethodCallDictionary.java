package org.sitoolkit.util.crudanalyzer.domain.methodcall;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;

public class MethodCallDictionary {

    @Getter
    private List<ClassDef> classDefs = new ArrayList<>();

    private Map<String, List<MethodDef>> methodCallMap = new HashMap<>();

    public void add(ClassDef classDef) {
        classDefs.add(classDef);

        classDef.getMethods().stream().forEach(methodDef -> {
            methodCallMap.put(methodDef.getSignature(), methodDef.getMethodCalls());
        });
    }

    public void solveMethodCalls() {
        classDefs.stream().forEach(classDef -> {
            classDef.getMethods().stream().forEach(methodDef -> {
                methodDef.getMethodCalls().stream().forEach(methodCall -> {

                    if (methodCall.getMethodCalls().isEmpty()) {
                        List<MethodDef> calledMethods = methodCallMap
                                .get(methodCall.getSignature());
                        if (calledMethods != null) {
                            methodCall.setMethodCalls(calledMethods);
                        }
                    }
                });
            });
        });

    }
}
