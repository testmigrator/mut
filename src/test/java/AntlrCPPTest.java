//import com.test.migration.antlr.cpp.CPP14Lexer;
//import com.test.migration.antlr.cpp.CPP14Parser;
//import com.test.migration.antlr.cpp.CPP14ParserBaseVisitor;
//import com.test.migration.antlr.java.Java8Lexer;
//import com.test.migration.antlr.java.Java8Parser;
//import org.antlr.v4.runtime.CharStream;
//import org.antlr.v4.runtime.CharStreams;
//import org.antlr.v4.runtime.CommonTokenStream;
//import org.antlr.v4.runtime.tree.ParseTree;
//import org.testng.annotations.Test;
//
//import java.io.IOException;
//
//public class AntlrCPPTest {
//
//    @Test
//    public void testRead(){
//        String filePath = "/Users/gaoyi/IdeaProjects/TestMigrationV2/demo/harmony/animator.cpp";
//        CharStream inputStream = null;
//        try {
//            inputStream = CharStreams.fromFileName(filePath);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        CPP14Parser parser = new CPP14Parser(new CommonTokenStream(new CPP14Lexer(inputStream)));
//        CPP14ParserBaseVisitor visitor = new CPP14ParserBaseVisitor();
//        ParseTree parseTree = parser.translationUnit();
//        visitor.visit(parseTree);
//    }
//
//}
