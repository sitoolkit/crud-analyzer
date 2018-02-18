package org.sitoolkit.util.crudanalyzer.domain.crud;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableCrud {
    private Map<String, Set<CrudType>> map = new HashMap<>();
    private TableDef table;
    private CrudType crud;

    public void put(String table, CrudType crud) {
        Set<CrudType> cruds = map.computeIfAbsent(table, key -> new HashSet<>());
        cruds.add(crud);
    }

    public Set<String> getCrud(String table) {
        return map.getOrDefault(table, new HashSet<>()).stream().map(crud -> crud.toString())
                .collect(Collectors.toSet());
    }
}