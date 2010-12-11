package org.boblycat.abbots.net;

import java.nio.charset.Charset;
import java.util.List;

public class PlainTalk {
    private Charset charset;
    
    public PlainTalk(Charset charset) {
        this.charset = charset;
    }
    
    public String messageString(List<String> message) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String str: message) {
            if (first)
                first = false;
            else
                sb.append(' ');
            sb.append(maybeEscape(str));
        }
        sb.append('\n');
        return sb.toString();
    }
    
    public byte[] messageBytes(List<String> message) {
        return messageString(message).getBytes(charset);
    }

    private String maybeEscape(String str) {
        boolean escape = false;
        for (char c: str.toCharArray()) {
            if (Character.isWhitespace(c) || c == '{') {
                escape = true;
                break;
            }
        }
        if (escape)
            return "{" + str.getBytes(charset).length + "}" + str;
        else
            return str;
    }
}
