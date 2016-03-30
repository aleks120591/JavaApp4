package lenacom.filer;

import lenacom.filer.config.Bookmarks;
import lenacom.filer.config.LookAndFeel;
import lenacom.filer.message.Errors;
import lenacom.filer.root.RootFrame;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class Main {

    private static class ExceptionHandler
            implements Thread.UncaughtExceptionHandler {

        public void uncaughtException(Thread thread, Throwable t) {
            System.gc(); //handle java.lang.OutOfMemoryError: Java heap space
            Errors.showError(t);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "-debug".equalsIgnoreCase(args[0])) {
            //redirect error to log file
            File file = new File("filer_error.log");
            FileOutputStream fos = new FileOutputStream(file);
            PrintStream ps = new PrintStream(fos);
            System.setErr(ps);
            System.setOut(ps);
        }

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                init();
            }
        });
    }

    private static void init() {
        assert(SwingUtilities.isEventDispatchThread());

        //set look & feel before creating panels
        String lookAndFeelClassName = LookAndFeel.getLookAndFeelClassName();
        if (lookAndFeelClassName != null) {
            try {
                UIManager.setLookAndFeel(lookAndFeelClassName);
                SwingUtilities.updateComponentTreeUI(RootFrame.getRoot());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                Errors.showError(e);
            }
        }

        RootFrame.show();
        Bookmarks.getBookmarks();
    }
}



