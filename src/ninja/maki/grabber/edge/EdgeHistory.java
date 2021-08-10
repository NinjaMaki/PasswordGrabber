package ninja.maki.grabber.edge;

import ninja.maki.utils.SystemUtil;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class EdgeHistory {
    public static void grab() {
        SystemUtil.console();
        String historyTemp = System.getProperty("java.io.tmpdir") + "edgehistory.tmp";
        String historyPath = System.getProperty("user.home") + "\\AppData\\Local\\Microsoft\\Edge\\User Data\\Default\\History";
        File historyTempFile = new File(historyTemp);
        try {
            historyTempFile = File.createTempFile("edgehistory", ".tmp");
            historyTemp = historyTempFile.getAbsolutePath();
        }catch (IOException e){
            SystemUtil.console("Failed to create temp file.");
        }
        File historyPathFile = new File(historyPath);
        if(historyPathFile.exists()) {
            try {
                try {
                    SystemUtil.copyFile(historyPathFile, historyTempFile);
                    SystemUtil.console("Copy \"" + historyPath + "\" to \"" + historyTemp + "\".");
                } catch (IOException e) {
                    SystemUtil.console("Failed to copy \"" + historyPath + "\" to \"" + historyTemp + "\".");
                }
                Connection connection;
                Statement statement;
                try {
                    Class.forName("org.sqlite.JDBC");
                    connection = DriverManager.getConnection("jdbc:sqlite:" + historyTemp);
                    connection.setAutoCommit(false);
                    SystemUtil.console("Connect database \"" + historyTemp + "\".");
                    statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT url,title from urls");
                    SystemUtil.console();
                    SystemUtil.console("==================================================================");
                    while (resultSet.next()) {
                        String url = resultSet.getString("url");
                        String title = resultSet.getString("title");
                        SystemUtil.console();
                        SystemUtil.console("URL -> " + url);
                        SystemUtil.console("Title -> " + title);
                    }
                    SystemUtil.console();
                    SystemUtil.console("==================================================================");
                    resultSet.close();
                    statement.close();
                    connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    SystemUtil.console("Failed to get info.");
                }
            }catch (Exception e) {
                SystemUtil.console(e.getMessage());
            }
        }else {
            SystemUtil.console("\"" + historyPath + "\" is not founded.");
        }
        SystemUtil.console();
        if(historyTempFile.exists()) {
            if(historyTempFile.delete()) {
                SystemUtil.console("Delete \"" + historyTemp + "\".");
            }else {
                SystemUtil.console("Failed to delete \"" + historyTemp + "\".");
            }
        }
    }
}
