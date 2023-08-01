package com.test.migration.service.invocation;

import com.test.migration.antlr.java.Java8BaseVisitor;
import com.test.migration.antlr.java.Java8Lexer;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.entity.TestMethodParseBasic;
import com.test.migration.service.preprocess.TestMethodBasicService;
import lombok.Getter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashSet;

@Getter
public class ApiInvocationVisitor extends Java8BaseVisitor<RuleNode> {

    static TestMethodBasicService testMethodBasicService = new TestMethodBasicService();

    public ApiInvocationVisitor() {
        this.testMethodParseBasic = new TestMethodParseBasic();
    }

    /**
     * 遍历器不应该做过多遍历之外的逻辑（构建/收集结果），后面可以考虑监听器方式实现
     */
    private TestMethodParseBasic testMethodParseBasic;


    @Override
    public RuleNode visitClassDeclaration(Java8Parser.ClassDeclarationContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public RuleNode visitClassMemberDeclaration(Java8Parser.ClassMemberDeclarationContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * 方法声明
     */
    @Override
    public RuleNode visitMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
        // 方法内部定义的内部方法，调用关系算到外层方法中
        TestMethodParseBasic.MethodInvocation parentMethod = testMethodParseBasic.getMethodInvocationList().stream()
                .filter(invoke -> invoke.getStartTokenIndex() < ctx.getStart().getStartIndex())
                .filter(invoke -> invoke.getEndTokenIndex() > ctx.getStop().getStopIndex())
                .findFirst().orElse(null);

        if (parentMethod != null) {
            return visitChildren(ctx);
        }

        // 获取调用者方法名
        String methodName = testMethodBasicService.fetchMethodName(ctx);
        // 方法名以test为前后缀 或者 方法带有@Test注解
        if (testMethodBasicService.isTestMethod(ctx)) {
            TestMethodParseBasic.MethodInvocation invocation = new TestMethodParseBasic.MethodInvocation();

            invocation.setCaller(methodName);
            invocation.setStartTokenIndex(ctx.getStart().getStartIndex());
            invocation.setEndTokenIndex(ctx.getStop().getStopIndex());
            testMethodParseBasic.setInvocation(invocation);
            testMethodParseBasic.getMethodInvocationList().add(invocation);

            TestMethodParseBasic.ReferenceType referenceType = new TestMethodParseBasic.ReferenceType();
            referenceType.setMethodName(methodName);
            referenceType.setMethodTotalLine(ctx.getStop().getLine() - ctx.getStart().getLine());
            testMethodParseBasic.setReferenceType(referenceType);
            testMethodParseBasic.getReferenceTypeList().add(referenceType);
        }

        // 遍历AST
        return visitChildren(ctx);
    }




    @Override
    public RuleNode visitMethodInvocation(Java8Parser.MethodInvocationContext ctx) {
        fillCalleeMethodInvocation(ctx);
        return visitChildren(ctx);
    }

    @Override
    public RuleNode visitMethodInvocation_lf_primary(Java8Parser.MethodInvocation_lf_primaryContext ctx) {
        fillCalleeMethodInvocation(ctx);
        return visitChildren(ctx);
    }

    @Override
    public RuleNode visitMethodInvocation_lfno_primary(Java8Parser.MethodInvocation_lfno_primaryContext ctx) {
        fillCalleeMethodInvocation(ctx);
        return visitChildren(ctx);
    }


    @Override
    public RuleNode visitUnannReferenceType(Java8Parser.UnannReferenceTypeContext ctx) {
        fillUseClasses(ctx);
        return visitChildren(ctx);
    }

    @Override
    public RuleNode visitReferenceType(Java8Parser.ReferenceTypeContext ctx) {
        fillUseClasses(ctx);
        return visitChildren(ctx);
    }

    @Override
    public RuleNode visitClassInstanceCreationExpression(Java8Parser.ClassInstanceCreationExpressionContext ctx) {
        fillUseClassesWithIdentifier(ctx);
        fillCalleeClassesInvocation(ctx);
        return visitChildren(ctx);
    }

    @Override
    public RuleNode visitClassInstanceCreationExpression_lf_primary(Java8Parser.ClassInstanceCreationExpression_lf_primaryContext ctx) {
        fillUseClassesWithIdentifier(ctx);
        fillCalleeClassesInvocation(ctx);
        return visitChildren(ctx);
    }

    @Override
    public RuleNode visitClassInstanceCreationExpression_lfno_primary(Java8Parser.ClassInstanceCreationExpression_lfno_primaryContext ctx) {
        fillUseClassesWithIdentifier(ctx);
        fillCalleeClassesInvocation(ctx);
        return visitChildren(ctx);
    }

    private void fillUseClassesWithIdentifier(ParserRuleContext ctx) {
        TestMethodParseBasic.ReferenceType referenceType = testMethodParseBasic.getReferenceType();
        if (referenceType == null) {
            return;
        }
        String identifier = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof TerminalNode) {
                TerminalNode terminalNode = (TerminalNode) child;
                if (terminalNode.getSymbol().getType() == Java8Lexer.Identifier) {
                    identifier = terminalNode.getText();
                    break;
                }
            }
        }
        if (StringUtils.isNotBlank(identifier)) {
            testMethodParseBasic.getReferenceType().getUseClasses().add(identifier);
        }
    }

    private void fillCalleeClassesInvocation(ParserRuleContext ctx) {
        TestMethodParseBasic.MethodInvocation invocation = testMethodParseBasic.getInvocation();
        if (invocation == null) {
            return;
        }

        // 方法调用必须出现在Caller的方法体内
        if (invocation.getStartTokenIndex() <= ctx.getStart().getStartIndex()
                && invocation.getEndTokenIndex() >= ctx.getStop().getStopIndex()) {
            String calleeMethodName = fetchCalleeClassName(ctx);
            if (invocation.getCallee() == null) {
                invocation.setCallee(new HashSet<>(Collections.singleton(calleeMethodName)));
            } else {
                invocation.getCallee().add(calleeMethodName);
            }
        }
    }


    private void fillUseClasses(ParserRuleContext ctx) {
        TestMethodParseBasic.ReferenceType referenceType = testMethodParseBasic.getReferenceType();
        if (referenceType == null) {
            return;
        }
        testMethodParseBasic.getReferenceType().getUseClasses().add(ctx.getText());
    }


    private void fillCalleeMethodInvocation(ParserRuleContext ctx) {
        // class中没有方法声明直接调用方法，暂不考虑该case
        TestMethodParseBasic.MethodInvocation invocation = testMethodParseBasic.getInvocation();
        if (invocation == null) {
            return;
        }

        // 方法调用必须出现在Caller的方法体内
        if (invocation.getStartTokenIndex() <= ctx.getStart().getStartIndex()
                && invocation.getEndTokenIndex() >= ctx.getStop().getStopIndex()) {
            String calleeMethodName = fetchCalleeMethodName(ctx);
            if (invocation.getCallee() == null) {
                invocation.setCallee(new HashSet<>(Collections.singleton(calleeMethodName)));
            } else {
                invocation.getCallee().add(calleeMethodName);
            }
            testMethodParseBasic.getReferenceType().getInvokeApis().add(calleeMethodName);
        }
    }

    private String fetchCalleeClassName(ParserRuleContext ctx) {
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof TerminalNode) {
                TerminalNode node = (TerminalNode) child;
                if (node.getSymbol().getType() == Java8Lexer.Identifier) {
                    return node.getText();
                }
            }
        }
        return StringUtils.EMPTY;
    }

    private String fetchCalleeMethodName(ParserRuleContext ctx) {
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof TerminalNode) {
                TerminalNode node = (TerminalNode) child;
                if (node.getSymbol().getType() == Java8Lexer.Identifier) {
                    return node.getText();
                }
            }
            if (child instanceof RuleContext) {
                RuleContext node = (RuleContext) child;
                if (node.getRuleIndex() == Java8Parser.RULE_methodName) {
                    return node.getText();
                }
            }
        }
        return StringUtils.EMPTY;
    }


}


