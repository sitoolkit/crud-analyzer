package org.sitoolkit.util.crudanalyzer.domain.methodcall;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import lombok.Data;

@Data
public class MethodDef {
	
    private String name;
    private String signature;
    private boolean isPublic;
    private String actionPath;
    private Set<MethodDef> methodCalls = new HashSet<>();

    public Stream<MethodDef> getMethodCallsRecursively() {
        return Stream.concat(Stream.of(this),
                methodCalls.stream().flatMap(MethodDef::getMethodCallsRecursively));
    }

}
