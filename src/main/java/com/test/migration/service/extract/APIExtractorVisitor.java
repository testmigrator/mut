package com.test.migration.service.extract;


import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8BaseVisitor;
import com.test.migration.antlr.java.Java8Lexer;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.service.preprocess.TestMethodBasicService;
import lombok.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class APIExtractorVisitor extends Java8BaseVisitor<RuleNode> {

    private String filePath;
    private Integer taskId;

    private List<CtxLine> ctxLineList = Lists.newArrayList();

    private List<MethodModel> methodModelList = Lists.newArrayList();

    static TestMethodBasicService methodBasicService = new TestMethodBasicService();

    public APIExtractorVisitor(Integer taskId, String filePath) {
        this.taskId = taskId;
        this.filePath = filePath;
    }

    /**
     */
    @Override
    public RuleNode visitMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
        CtxLine ctxLine = CtxLine.builder()
                .startLine(ctx.getStart().getLine())
                .endLine(ctx.getStop().getLine())
                .type(2)
                .text("")
                .build();
        ctxLineList.add(ctxLine);
        MethodModel methodModel = buildMethodModel(ctx);
        methodModelList.add(methodModel);
        return visitChildren(ctx);
    }

    @Override
    public RuleNode visitInterfaceMethodDeclaration(Java8Parser.InterfaceMethodDeclarationContext ctx) {
        CtxLine ctxLine = CtxLine.builder()
                .startLine(ctx.getStart().getLine())
                .endLine(ctx.getStop().getLine())
                .type(2)
                .text("")
                .build();
        ctxLineList.add(ctxLine);
        MethodModel methodModel = buildMethodModel(ctx);
        methodModelList.add(methodModel);
        return visitChildren(ctx);
    }


    @Override
    public RuleNode visitConstructorDeclarator(Java8Parser.ConstructorDeclaratorContext ctx) {
        CtxLine ctxLine = CtxLine.builder()
                .startLine(ctx.getStart().getLine())
                .endLine(ctx.getStop().getLine())
                .type(2)
                .text("")
                .build();
        ctxLineList.add(ctxLine);
        MethodModel methodModel = new MethodModel();
        methodModel.setStartLine(ctx.getStart().getLine());

        ParserRuleContext simpleTypeNameCtx = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree childCtx = ctx.getChild(i);
            if (childCtx instanceof RuleContext && ((RuleContext) childCtx).getRuleIndex() == Java8Parser.RULE_simpleTypeName) {
                simpleTypeNameCtx = (ParserRuleContext) childCtx;
            }
        }
        String constructName = simpleTypeNameCtx == null ? "" : simpleTypeNameCtx.getText();
        methodModel.setMethodName(constructName);
        methodModelList.add(methodModel);

        return visitChildren(ctx);
    }

    @Override
    public RuleNode visitClassDeclaration(Java8Parser.ClassDeclarationContext ctx) {
        CtxLine ctxLine = CtxLine.builder()
                .startLine(ctx.getStart().getLine())
                .endLine(ctx.getStop().getLine())
                .type(0)
                .text("")
                .build();

        ctxLineList.add(ctxLine);
        return visitChildren(ctx);
    }

    @Override
    public RuleNode visitFieldDeclaration(Java8Parser.FieldDeclarationContext ctx) {
        CtxLine ctxLine = CtxLine.builder()
                .startLine(ctx.getStart().getLine())
                .endLine(ctx.getStop().getLine())
                .type(1)
                .text("")
                .build();

        ctxLineList.add(ctxLine);
        return visitChildren(ctx);
    }

    private MethodModel buildMethodModel(ParserRuleContext ctx) {
        MethodModel methodModel = new MethodModel();
        methodModel.setStartLine(ctx.getStart().getLine());
        methodModel.setMethodName(methodBasicService.fetchMethodName(ctx));
        methodModel.setMethodParameter(methodBasicService.fetchMethodParameter(ctx));
        methodModel.setMethodReturn(methodBasicService.fetchMethodReturn(ctx));

        return methodModel;
    }

    public void fillCodeComment(List<Token> tokens) {
        List<CtxLine> comments = Lists.newArrayList();
        for (Token t : tokens) {
            if (t.getType() == Java8Lexer.COMMENT || t.getType() == Java8Lexer.LINE_COMMENT) {
                String text = t.getText().replace("\\", "")
                        .replace("/", "")
                        .replace("\\\\", "")
                        .replace("*", "")
                        .replace("\n", "");

                CtxLine comment = CtxLine.builder()
                        .startLine(t.getLine())
                        .endLine(-1)
                        .text(text)
                        .type(3)
                        .build();

                comments.add(comment);
            }
        }
        List<CtxLine> ctxLineList = getCtxLineList();
        comments = comments.stream()
                .filter(comment -> keep(ctxLineList, comment))
                .collect(Collectors.toList());

        comments.addAll(ctxLineList);

        comments = comments.stream()
                .sorted(Comparator.comparing(CtxLine::getStartLine))
                .collect(Collectors.toList());

        methodModelList = methodModelList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        fillComment(methodModelList, comments);
    }

    private void fillComment(List<MethodModel> methodModelList, List<CtxLine> comments) {
        Map<Integer, CtxLine> methodMap = comments.stream()
                .filter(x -> x.getType() == 2)
                .collect(Collectors.toMap(CtxLine::getStartLine, Function.identity()));

        for (MethodModel methodModel : methodModelList) {
            CtxLine ctxLine = methodMap.get(methodModel.getStartLine());
            if (ctxLine == null) {
                continue;
            }
            String methodComment = "";
            String classComment = "";
            for (int i = 0; i < comments.size(); i++) {
                CtxLine line = comments.get(i);
                if (line.getType() != 2 || ctxLine.getStartLine() != line.getStartLine()) {
                    continue;
                }

                if (i == 0) {
                    continue;
                }

                CtxLine preCtxLine = comments.get(i - 1);
                if (preCtxLine.getType() == 3) {
                    methodComment = preCtxLine.getText();
                }

                for (int j = i - 1; j > 0; j--) {
                    CtxLine parentCtxLine = comments.get(j);
                    if (parentCtxLine.getType() == 0) {
                        if (parentCtxLine.getStartLine() <= line.getStartLine() && parentCtxLine.getEndLine() >= line.getEndLine()) {
                            CtxLine preParentCtxLine = comments.get(j - 1);
                            if (preParentCtxLine.getType() == 3) {
                                classComment = preParentCtxLine.getText();
                            }
                        }
                    }
                }
            }

            methodModel.setMethodComment(methodComment);
            methodModel.setClassComment(classComment);
        }
    }

    private boolean keep(List<CtxLine> ctxLineList, CtxLine comment) {
        CtxLine firstClass = ctxLineList.stream()
                .filter(x -> x.getType() == 0)
                .min(Comparator.comparing(CtxLine::getStartLine))
                .orElse(null);
        if (firstClass == null) {
            return false;
        }

        int startLine = comment.getStartLine();
        boolean keep = true;
        for (CtxLine ctxLine : ctxLineList) {
            if (ctxLine.getType() == 0 && firstClass.getStartLine() == ctxLine.getStartLine()) {
                continue;
            }
            if (startLine >= ctxLine.getStartLine() && startLine <= ctxLine.getEndLine()) {
                keep = false;
                break;
            }
        }
        return keep;
    }




    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class MethodModel {
        int startLine;

        String classComment;

        String className;

        String methodComment;

        String methodName;

        String methodParameter;

        String methodReturn;
    }


    @Data
    @Builder
    public static class CtxLine {
        // 0:class/interface, 1:property, 2:method, 3:comment
        int type;
        int startLine;
        int endLine;
        String text;
    }
}
