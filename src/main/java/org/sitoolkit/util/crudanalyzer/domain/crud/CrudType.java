package org.sitoolkit.util.crudanalyzer.domain.crud;

import java.util.SortedMap;
import java.util.TreeMap;

public enum CrudType {
    CREATE("insert"), REFERENCE("select"), UPDATE, DELETE, MERGE, NA;

    private String sql;

    private CrudType() {
        this.sql = name().toLowerCase();
    }

    private CrudType(String sql) {
        this.sql = sql;
    }

    @Override
    public String toString() {
        return name().substring(0, 1);
    }

    public static CrudType parse(String sql) {
        if (sql == null || sql.isEmpty()) {
            return NA;
        }

        String lowerSql = sql.trim().toLowerCase();

        if (lowerSql.startsWith("with")) {

            return findFirstCrudTypeInSql(lowerSql);

        } else {

            for (CrudType crud : values()) {
                if (lowerSql.startsWith(crud.sql)) {
                    return crud;
                }
            }

        }

        return NA;
    }

    static CrudType findFirstCrudTypeInSql(String sql) {

        SortedMap<Integer, CrudType> cmap = new TreeMap<>();
        for (CrudType crud : values()) {
            int crudTypeIndex = sql.indexOf(crud.sql);
            if (crudTypeIndex > 0) {
                cmap.put(crudTypeIndex, crud);
            }
        }

        return cmap.getOrDefault(cmap.firstKey(), NA);
    }

}
