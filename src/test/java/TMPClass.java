import org.antlr.v4.runtime.ParserRuleContext;

import java.util.List;

public class TMPClass {

    public static class BlockStatementTreeNode {
        List<BlockStatementTreeNode> subBlockStatementTreeNodes;
        ParserRuleContext parserRuleContext;

        public static List<String> misMatchCodes;
    }


    public static void main(String[] args) {
        BlockStatementTreeNode blockStatementTreeNode = new BlockStatementTreeNode();

    }

    public void test() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (i + j < 1) {
                    print();
                    print();
                }
                print();
            }
            print();
        }
        print();
    }

    public void print() {
    }
}
