package testCtx;

import com.google.common.collect.Lists;

import java.util.List;

public class WriteContext {

    public void writeContext(){
        Context.a=5;
        int a1 =0;
        int a2 =1;
        int a3=2;
        List<Integer> alist = Lists.newArrayList();
        alist.add(a1);
        alist.add(a2);
        alist.add(a3);
        Context.alist =alist;
        Context.msg="write....";

    }
}
