package com.test.migration.service.translate.replace;

import java.util.List;

public class FieldAccessReplace {

    /**
     * primary '.' Identifier
     */
    public String replaceStructure1(List<String> originals) {
        String primary = originals.get(0);
        String identifier = originals.get(2);

        String newStructure = primary + "->" + identifier;
        return newStructure;
    }

    /**
     * 'super' '.' Identifier
     */
    public String replaceStructure2(List<String> originals) {
        String identifier = originals.get(2);

        String newStructure = "super" + "->" + identifier;
        return newStructure;
    }

    /**
     * typeName '.' 'super' '.' Identifier
     */
    public String replaceStructure3(List<String> originals) {
        String typeName = originals.get(0);
        String identifier = originals.get(4);

        String newStructure = typeName + "->" + "super" + "->" + identifier;
        return newStructure;
    }


}
