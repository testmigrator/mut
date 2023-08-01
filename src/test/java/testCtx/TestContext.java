package testCtx;

public class TestContext {
    public static void main(String[] args) {
        ContextReader reader = new ContextReader();
        reader.read();
        System.out.println("-------------");
        System.out.println("-------------");
        System.out.println("-------------");
        WriteContext wc = new WriteContext();


        wc.writeContext();
        reader.read();


    }
}
