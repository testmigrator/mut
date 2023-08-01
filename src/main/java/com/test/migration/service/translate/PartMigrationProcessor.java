package com.test.migration.service.translate;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.test.migration.antlr.java.Java8Parser;
import com.test.migration.entity.Constants;
import com.test.migration.service.translate.bnf.common.method.MethodHeaderTranslate;
import com.test.migration.service.translate.bnf.statement.BlockStatementTranslate;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PartMigrationProcessor {

    public String doPartMigrationTranslate(ParserRuleContext ctx) {
        BlockStatementTranslate blockStatementTranslate = new BlockStatementTranslate();
        // 1. 构建部分迁移model
        MethodDeclarationNode methodDeclarationNode = buildBSModel(ctx);

        // 2. 填充hint
        List<BlockStatementTreeNode> blockStatementTreeNodes = methodDeclarationNode.blockStatementTreeNodes;
        fillBSModel(blockStatementTreeNodes);

        // 3.1 执行部分迁移bs调整策略，构建调整策略map
        for (BlockStatementTreeNode blockStatementTreeNode : blockStatementTreeNodes) {
            executePartMigrationModifyStrategy(blockStatementTreeNode);
        }

        // 3.2 深度优先遍历 设置调整策略
        dfsModify(blockStatementTreeNodes);

        // 4. 二次转换，并合并出部分迁移结果
        List<String> translateBlockStatements = methodDeclarationNode.blockStatementTreeNodes.stream()
                .map(x -> blockStatementTranslate.translateBlockStatement(x.blockStatement))
                .collect(Collectors.toList());

        // 5. 部分迁移条件过滤：条件1：必须存在api调用 条件2：必须存在assert
        boolean isSatisfyMigrateCondition = isSatisfyMigrateCondition(translateBlockStatements);
        if (!isSatisfyMigrateCondition) {
            return StringUtils.EMPTY;
        }

        // 类匹配映射替换
        translateBlockStatements = replaceClassMappingIfNeed(translateBlockStatements);

        String methodBlockStatements = Joiner.on("").join(translateBlockStatements);
        return methodDeclarationNode.methodHeader + " { " + methodBlockStatements + " } ";
    }


    /**
     * 解决类匹配一对多关系
     * 一个方法中有多个bs，需要对每个bs做处理，因此返回值类型是List
     *
     * @param blockStatementTranslateCodes
     * @return
     */
    private List<String> replaceClassMappingIfNeed(List<String> blockStatementTranslateCodes) {
        List<String> translateCodeStrList = blockStatementTranslateCodes.stream()
                .filter(x -> x.contains(Constants.ClassMappingOneToManySplitter))
                .collect(Collectors.toList());

        // 如果不存在多对一类匹配 不用替换
        if (CollectionUtils.isEmpty(translateCodeStrList)) {
            return blockStatementTranslateCodes;
        }

        // 挨个处理
        return blockStatementTranslateCodes.stream().map(bsCode -> {
            // 获取T1$T2$S1
            String classMapping = Splitter.on(" ").splitToList(bsCode).stream()
                    .filter(x -> x.contains(Constants.ClassMappingOneToManySplitter))
                    .findFirst().orElse(null);
            if (classMapping == null) {
                return bsCode;
            }

            List<String> classMappingList = Splitter.on(Constants.ClassMappingOneToManySplitter).splitToList(classMapping);
            // 规则：最后一个元素是sourceClass
            String sourceClass = classMappingList.get(classMappingList.size() - 1);
            List<String> targetClasses = classMappingList.subList(0, classMappingList.size() - 1);

            List<MappingRuleLoader.MethodInvocationTypeNameRule> targetClassList = MappingRuleLoader.apiMapping.stream()
                    .filter(apiMapping -> StringUtils.equals(apiMapping.getSourceClassName(), sourceClass))
                    .filter(apiMapping -> targetClasses.contains(apiMapping.getTargetClassName()))
                    .filter(apiMapping -> isExistTargetApiInvocation(blockStatementTranslateCodes, apiMapping.getTargetMethodName()))
                    .collect(Collectors.toList());

            Set<String> allTargetClassSet = Sets.newHashSet();
            for (MappingRuleLoader.MethodInvocationTypeNameRule mapping : targetClassList) {
                allTargetClassSet.add(mapping.getTargetClassName());
            }

            // 如果只有一个targetClass，直接替换，否则给出hint（不知道要翻译成什么）
            String newBsCode = "";
            if (allTargetClassSet.size() == 1) {
                String target = allTargetClassSet.stream().findFirst().orElse("");
                newBsCode = bsCode.replaceAll(Pattern.quote(classMapping), target);
            } else {
                //给出hint
                String replaceCode = bsCode.replaceAll(Pattern.quote(classMapping), sourceClass);
                String multipleClassMapping = Joiner.on(",").join(targetClasses.stream().distinct().collect(Collectors.toList()));
                newBsCode = Constants.Hint.multipleClassMappingMessage +"("+sourceClass+"):"+ multipleClassMapping + "\n" + replaceCode;
            }
            return newBsCode;
        }).collect(Collectors.toList());

    }

    private boolean isExistTargetApiInvocation(List<String> translateCodeStrList, String targetMethodName) {
        return translateCodeStrList.stream()
                .anyMatch(translateCode -> translateCode.contains(targetMethodName));
    }

    private boolean isSatisfyMigrateCondition(List<String> translateBlockStatements) {
        boolean isExistApiInvocation = false;
        boolean isExistAssert = false;
        for (String blockStatement : translateBlockStatements) {
            // api

            // assert
            if (blockStatement.toLowerCase().contains("assert")) {
                isExistAssert = true;
            }
        }

        return isExistAssert;
    }

    /**
     *
     */
    private BlockStatementTreeNode buildMethodBlockStatementTree(ParserRuleContext parserRuleContext) {
        if (parserRuleContext.getRuleIndex() != Java8Parser.RULE_blockStatement) {
            return null;
        }

        BlockStatementTreeNode treeNode = new BlockStatementTreeNode();
        treeNode.blockStatement = parserRuleContext;

        // 构建直接子blockStatement

        // DFS遍历 遇到bs即加入到subBlockStatementTreeNodes，然后返回上一个节点跳过该节点继续遍历
        List<ParserRuleContext> subBlockStatementTreeNodes = Lists.newArrayList();
        Stack<ParserRuleContext> stack = new Stack<>();
        for (int i = parserRuleContext.getChildCount() - 1; i >= 0; i--) {
            if (parserRuleContext.getChild(i) instanceof RuleContext) {
                stack.push((ParserRuleContext) parserRuleContext.getChild(i));
            }
        }

        while (!stack.isEmpty()) {
            ParserRuleContext ruleContext = stack.pop();
            if (ruleContext.getRuleIndex() == Java8Parser.RULE_blockStatement) {
                subBlockStatementTreeNodes.add(ruleContext);
                continue;
            }

            for (int i = ruleContext.getChildCount() - 1; i >= 0; i--) {
                if (ruleContext.getChild(i) instanceof RuleContext) {
                    stack.push((ParserRuleContext) ruleContext.getChild(i));
                }
            }
        }

        //递归构建直接子bs
        treeNode.subBlockStatementTreeNodes = subBlockStatementTreeNodes.stream()
                .map(this::buildMethodBlockStatementTree)
                .collect(Collectors.toList());

        return treeNode;
    }

    /**
     * b1 b2 b3
     *
     * @param blockStatementTreeNodes
     */
    private void dfsModify(List<BlockStatementTreeNode> blockStatementTreeNodes) {
        BlockStatementTreeNode parent = new BlockStatementTreeNode();

        parent.blockStatement = null;
        parent.translateBlockStatement = StringUtils.EMPTY;
        parent.subBlockStatementTreeNodes = blockStatementTreeNodes;
        parent.bsStartIndex = StringUtils.EMPTY;
        parent.hint = StringUtils.EMPTY;
        parent.nonBlockStatementHint = StringUtils.EMPTY;

        processDfs(parent);
    }

    private void processDfs(BlockStatementTreeNode blockStatementTreeNode) {
        boolean setRemove = false;
        Stack<BlockStatementTreeNode> stack = new Stack<>();
        stack.push(blockStatementTreeNode);
        // dfs
        while (!stack.isEmpty()) {
            BlockStatementTreeNode statementTreeNode = stack.pop();
            String key = statementTreeNode.bsStartIndex;

            if (setRemove) {
                blockStatementModifyMap.put(key, "remove");
            } else {
                // 如果遇到调整策略是remove，那么后续的所有bs均改为remove
                if (StringUtils.equals("remove", blockStatementModifyMap.getOrDefault(key, "keep"))) {
                    setRemove = true;
                }
            }

            for (int i = statementTreeNode.subBlockStatementTreeNodes.size() - 1; i >= 0; i--) {
                stack.push(statementTreeNode.subBlockStatementTreeNodes.get(i));
            }
        }
    }

    private void executePartMigrationModifyStrategy(BlockStatementTreeNode blockStatementTreeNode) {
        String key = blockStatementTreeNode.bsStartIndex;

        // 初始化默认值
        blockStatementModifyMap.put(key, "keep");

        // 策略1 如果存在nonBlockStatementHint，直接remove
        if (StringUtils.isNotBlank(blockStatementTreeNode.nonBlockStatementHint)) {
            blockStatementModifyMap.put(key, "remove");
        }

        if (blockStatementTreeNode.isTranslateBlockStatementFail()) {
            blockStatementModifyMap.put(key, "remove");
        }

        // 递归处理子bs
        for (BlockStatementTreeNode statementTreeNode : blockStatementTreeNode.subBlockStatementTreeNodes) {
            executePartMigrationModifyStrategy(statementTreeNode);
        }
    }

    private void fillBSModel(List<BlockStatementTreeNode> blockStatementTreeNodes) {
        if (blockStatementTreeNodes.isEmpty()) {
            return;
        }

        BlockStatementTranslate blockStatementTranslate = new BlockStatementTranslate();
        for (BlockStatementTreeNode blockStatementTreeNode : blockStatementTreeNodes) {
            ParserRuleContext blockStatement = blockStatementTreeNode.blockStatement;

            // 填充bs直接转换后的结果
            blockStatementTreeNode.translateBlockStatement = blockStatementTranslate.translateBlockStatement(blockStatement);

            // 填充bs源码开始行号
            blockStatementTreeNode.bsStartIndex = String.valueOf(blockStatement.getStart().getStartIndex());

            // 填充bs转换的hint
            blockStatementTreeNode.hint = TranslateHint.misMatchCodesToString();

            // 填充非子句部分的hint
            blockStatementTreeNode.nonBlockStatementHint = fetchNonBlockStatementHint(blockStatementTreeNode.hint);

            // 递归填充子bs
            fillBSModel(blockStatementTreeNode.subBlockStatementTreeNodes);
        }
    }

    /**
     *
     */
    private String fetchNonBlockStatementHint(String blockStatementHint) {
        if (StringUtils.isBlank(blockStatementHint)) {
            return StringUtils.EMPTY;
        }

        String[] bsHints = blockStatementHint.split(TranslateHint.BS_HINT_TAG);
        if (bsHints.length < 2) {
            return StringUtils.EMPTY;
        }
        return bsHints[1];
    }

    private MethodDeclarationNode buildBSModel(ParserRuleContext ctx) {
        MethodDeclarationNode methodDeclarationNode = new MethodDeclarationNode();
        ParserRuleContext methodHeaderRule = null;
        ParserRuleContext methodBodyRule = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (!(child instanceof RuleContext)) {
                continue;
            }
            if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_methodHeader) {
                methodHeaderRule = (ParserRuleContext) child;
            }

            if (((RuleContext) child).getRuleIndex() == Java8Parser.RULE_methodBody) {
                methodBodyRule = (ParserRuleContext) child;
            }
        }

        List<ParserRuleContext> blockStatements = fetchBlockStatementsByMethodBody(methodBodyRule);

        // methodHeader
        MethodHeaderTranslate methodHeaderTranslate = new MethodHeaderTranslate();
        methodDeclarationNode.methodHeader = methodHeaderTranslate.translateMethodHeader(methodHeaderRule);

        // method的直接bs
        methodDeclarationNode.blockStatementTreeNodes = blockStatements.stream()
                .map(this::buildMethodBlockStatementTree)
                .collect(Collectors.toList());

        return methodDeclarationNode;
    }

    private List<ParserRuleContext> fetchBlockStatementsByMethodBody(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_methodBody) {
            return Lists.newArrayList();
        }

        ParseTree child = ctx.getChild(0);
        boolean isRuleContext = child instanceof RuleContext;
        if (!isRuleContext) {
            return Lists.newArrayList();
        }

        // { }
        if (child.getChildCount() == 2) {
            return Lists.newArrayList();
        }

        List<ParserRuleContext> blockStatementsList = Lists.newArrayList();
        for (int i = 0; i < child.getChildCount(); i++) {
            ParseTree subChild = child.getChild(i);
            if (!(subChild instanceof RuleContext)) {
                continue;
            }
            // blockStatements
            for (int j = 0; j < subChild.getChildCount(); j++) {
                // blockStatement
                blockStatementsList.add((ParserRuleContext) subChild.getChild(j));
            }
        }

        return blockStatementsList;
    }

    // 部分迁移-代码数据结构设计

    /**
     * 1. 构建bs model(BlockStatementTreeNode)
     * 2. 执行bs的translate，收集hint
     * 3. 执行部分迁移bs调整策略，构建调整策略map
     * 4. 执行bs的translate，依据上述map，构建出部分迁移最终结果。
     */

    /**
     * bs(blockStatement)调整策略map
     * key：bs的代码行号 + $ + bs经过translate之后的字符串（唯一确定是哪个bs）
     * value：调整策略（keep，remove）
     */
    public static Map<String, String> blockStatementModifyMap = Maps.newHashMap();

    public static class MethodDeclarationNode {

        public String methodHeader;

        public List<BlockStatementTreeNode> blockStatementTreeNodes;
    }

    /**
     * 完整的代码语句：method/if/for等
     */
    public static class BlockStatementTreeNode {

        /**
         * 完整的代码语句，比如一个完整的method，for，if结构等
         */
        ParserRuleContext blockStatement;

        String translateBlockStatement;

        String bsStartIndex;

        /**
         * 直接子blockStatement
         */
        List<BlockStatementTreeNode> subBlockStatementTreeNodes;

        /**
         * 整体hint
         */
        String hint;

        /**
         * nonBlockStatement转换失败时，对应的失败信息收集到nonBlockStatementHint
         */
        String nonBlockStatementHint;


        public boolean isTranslateBlockStatementFail() {
            if (StringUtils.isBlank(hint)) {
                return false;
            }
            List<String> hintList = Splitter.on(TranslateHint.BS_HINT_TAG).splitToList(hint).stream()
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());

            return !hintList.isEmpty();
        }
    }


}
