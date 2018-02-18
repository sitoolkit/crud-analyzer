package org.sitoolkit.util.crudanalyzer.domain.crud;

import java.util.HashMap;
import java.util.Map;

import org.sitoolkit.util.crudanalyzer.domain.methodcall.MethodCallDictionary;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CrudMatrixProcessor {

    public Map<String, CrudRow> convert(Map<String, CrudRow> oldMap,
            MethodCallDictionary dictionary) {
        Map<String, CrudRow> newMap = new HashMap<>();
        dictionary.getClassDefs().stream()
                .filter(classDef -> classDef.getName().endsWith("Repository")).forEach(repoDef -> {

                    repoDef.getMethods().stream().forEach(method -> {
                        CrudRow crudRow = oldMap.get(repoDef.getName() + "." + method.getName());
                        newMap.put(method.getSignature(), crudRow);
                    });
                });
        return newMap;
    }

    public Map<String, CrudRow> convert2(Map<String, CrudRow> oldMap,
            MethodCallDictionary dictionary) {

        Map<String, CrudRow> newMap = new HashMap<>();

        dictionary.getClassDefs().stream()
                .filter(classDef -> classDef.getName().endsWith("Controller"))
                .forEach(controllerDef -> {
                    controllerDef.getMethods().stream().forEach(method -> {

                        method.getMethodCallsRecursively().forEach(m -> {
                            log.debug("{}", m.getSignature());
                            CrudRow crudRow = oldMap.get(m.getSignature());
                            if (crudRow != null) {
                                newMap.put(method.getSignature(), crudRow);
                            }
                        });
                    });
                });

        return newMap;

    }
}
