package com.test.migration.service.translate.replace;

import java.util.List;

public class UnannArrayTypeReplace {
    /**
     * primitiveType dims
     *
     * @param originals
     * @return
     */
    public String replaceStructure1(List<String> originals) {
        String primitiveType = originals.get(0);

        String newStructure = primitiveType + " * ";
        return newStructure;
    }

    /**
     * classOrInterfaceType dims
     *
     * @param originals
     * @return
     */
    public String replaceStructure2(List<String> originals) {
        String classOrInterfaceType = originals.get(0);

        String newStructure = classOrInterfaceType + " * ";
        return newStructure;
    }

    /**
     * typeVariable dims
     *
     * @param originals
     * @return
     */
    public String replaceStructure3(List<String> originals) {
        String typeVariable = originals.get(0);

        String newStructure = typeVariable + " * ";
        return newStructure;
    }
}
