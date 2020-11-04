package com.zlimbo.bc;

import com.zlimbo.bc.controller.MainWindowController;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;

@SpringBootApplication
//@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class BcApplication extends AbstractJavaFxApplicationSupport {

    public static void main(String[] args) {
        launch(BcApplication.class, MainWindowController.class, args);
    }

}
