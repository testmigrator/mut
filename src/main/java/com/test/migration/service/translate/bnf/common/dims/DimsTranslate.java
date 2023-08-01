package com.test.migration.service.translate.bnf.common.dims;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import utils.Log;

import java.util.List;
import java.util.stream.Collectors;

public class DimsTranslate {

    /**
     * dims
     * :	annotation* '[' ']' (annotation* '[' ']')*
     * ;
     */
    public String translateDims(ParserRuleContext ctx) {
        if ((ctx == null) || (ctx.getRuleIndex() != Java8Parser.RULE_dims)) {
            Log.error("RULE_dims error");
            return null;
        }

        List<String> leftList = Lists.newArrayList();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof TerminalNode) {
                if (StringUtils.equals("[", ctx.getChild(i).getText())) {
                    leftList.add(ctx.getChild(i).getText());
                }
            }
        }
        List<String> list = leftList.stream()
                .map(x -> "[]")
                .collect(Collectors.toList());

        return Joiner.on(" ").join(list);
    }
}
