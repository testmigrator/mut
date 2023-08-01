package testCtx;

public class ContextReader {

    public void read(){
        System.out.println("a="+Context.a);
        System.out.println("msg="+Context.msg);
        System.out.println("====");
        for (int i = 0; i < Context.alist.size(); i++) {
            System.out.println(Context.alist.get(i));
        }
    }
}
