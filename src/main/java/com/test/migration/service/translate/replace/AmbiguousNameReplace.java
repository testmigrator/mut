package com.test.migration.service.translate.replace;

import java.util.List;

public class AmbiguousNameReplace {

    /**
     * ambiguousName '.' Identifier
     *
     * @param originals
     * @return
     */
    public String replaceStructure2(List<String> originals) {
        String ambiguousName = originals.get(0);
        String identifier = originals.get(2);

        String newStructure = ambiguousName + "->" + identifier;
        return newStructure;
    }

}
