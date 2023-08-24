package com.test.migration.entity;

import com.test.migration.service.invocation.ApiInvocationVisitor;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TestMethodParseBasic {

//    private String testClassName;

//    private int methodTotalLine;
//
//    private List<String> invokeApis = new ArrayList<>();
//
//    private List<String> useClasses = new ArrayList<>();
//
//    private List<String> mappingApis = new ArrayList<>();
//
//    private List<String> mappingClasses = new ArrayList<>();

    /**
     * tmp
     */
    private MethodInvocation invocation;


    private List<MethodInvocation> methodInvocationList = new ArrayList<>();

    /**
     * tmp
     */
    private ReferenceType referenceType;

    private List<ReferenceType> referenceTypeList = new ArrayList<>();


    @Data
    public static class MethodInvocation {
        private String caller;
        private Integer startTokenIndex;
        private Integer endTokenIndex;
        private Set<String> callee;
    }

    /**
     * tmp
     */
    @Data
    public static class ReferenceType {
        private String methodName;
        private int methodTotalLine;
        private List<String> invokeApis = new ArrayList<>();
        private List<String> useClasses = new ArrayList<>();
        private List<String> mappingApis = new ArrayList<>();
        private List<String> mappingClasses = new ArrayList<>();
    }


}
