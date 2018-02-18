package org.sitoolkit.util.crudanalyzer.domain.methodcall;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import lombok.Data;

@Data
public class MethodDef {

    private String name;
    private String signature;
    private List<MethodDef> methodCalls = new ArrayList<>();

    public Stream<MethodDef> getMethodCallsRecursively() {
        return Stream.concat(Stream.of(this),
                methodCalls.stream().flatMap(MethodDef::getMethodCallsRecursively));
    }

}
