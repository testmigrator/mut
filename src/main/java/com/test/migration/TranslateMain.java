package com.test.migration;

import com.test.migration.service.TranslateTestService;

public class TranslateMain {
    TranslateTestService translateTestService = new TranslateTestService();

    public static void main(String[] args) {
        TranslateMain main = new TranslateMain();
        main.process();
    }

    public void process(){
        translateTestService.translateCode();
    }
}
