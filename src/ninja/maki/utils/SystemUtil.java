package ninja.maki.utils;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;

public class SystemUtil {
    public static void copyFile(File source, File dest) throws IOException {
        try (FileChannel inputChannel = new FileInputStream(source).getChannel(); FileChannel outputChannel = new FileOutputStream(dest).getChannel()) {
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        }
    }

    public static void convertUnicodeToAnsi(File file) throws IOException {
        String sourceCharset = "GB2312";
        String targetCharset = "UTF-8";
        if (file.isFile()) {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(file), sourceCharset);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                line =  URLEncoder.encode(line, "UTF-8");
                sb.append(line).append("\r\n");
            }
            br.close();
            isr.close();
            File targetFile = new File(file.getPath());
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(targetFile), targetCharset);
            BufferedWriter bw = new BufferedWriter(osw);
            bw.write(URLDecoder.decode(sb.toString(), "UTF-8"));
            bw.close();
            osw.close();
        }
    }

    public static String getSubString(String text, String left, String right) {
        String result;
        int zLen;
        if (left == null || left.isEmpty()) {
            zLen = 0;
        } else {
            zLen = text.indexOf(left);
            if (zLen > -1) {
                zLen += left.length();
            } else {
                zLen = 0;
            }
        }
        int yLen = text.indexOf(right, zLen);
        if (yLen < 0 || right.isEmpty()) {
            yLen = text.length();
        }
        result = text.substring(zLen, yLen);
        return result;
    }

    public static void console(String console){
        System.out.println("[*] " + console);
    }

    public static void console(){
        System.out.println();
    }
}
