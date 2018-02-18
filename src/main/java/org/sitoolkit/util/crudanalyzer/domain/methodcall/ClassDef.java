package org.sitoolkit.util.crudanalyzer.domain.methodcall;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ClassDef {

    private String pkg;
    private String name;
    private List<MethodDef> methods = new ArrayList<>();

}
