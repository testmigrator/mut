package com.test.migration.service.translate.bnf.common.parameter;

import com.test.migration.antlr.java.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * todo 暂不支持
 * 应用太少了：
 *   public void test1(@SafeObject ADemo this, int a, Object b){
 * <p>
 * }
 * 方法参数中的this就是ReceiverParameter
 */
public class ReceiverParameterTranslate {

    /**
     * receiverParameter
     * :	annotation* unannType (Identifier '.')? 'this'
     * ;
     */
    public String translateReceiverParameter(ParserRuleContext ctx) {
        if (ctx == null || ctx.getRuleIndex() != Java8Parser.RULE_receiverParameter) {
            return ctx == null ? "" : ctx.getText();
        }

        return ctx.getText();
    }
}
