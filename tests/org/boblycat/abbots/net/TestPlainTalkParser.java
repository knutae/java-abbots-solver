package org.boblycat.abbots.net;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TestPlainTalkParser {
    PlainTalkParser parser;
    List<List<String>> messages;
    Charset charset;
    
    @Before
    public void setUp() {
        charset = Charset.forName("UTF-8");
        messages = new ArrayList<List<String>>();
        parser = new PlainTalkParser(charset) {
            @Override
            public void receivedMessage(List<String> message) {
                messages.add(message);
            }
        };
        assertEquals(0, messages.size());
    }
    
    private void addData(String strData) {
        byte[] data = strData.getBytes(charset);
        try {
            parser.receivedData(data, data.length);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }
    
    private void checkSingleMessage(String... fields) {
        assertEquals(1, messages.size());
        List<String> message = messages.get(0);
        assertEquals(fields.length, message.size());
        for (int i = 0; i < fields.length; i++)
            assertEquals(fields[i], message.get(i));
    }
    
    @Test
    public void basic() {
        addData("foo bar baz\n");
        checkSingleMessage("foo", "bar", "baz");
    }
    
    @Test
    public void emptyWithoutEscaping() {
        addData("foo  bar\n");
        checkSingleMessage("foo", "", "bar");
    }
    
    @Test
    public void emptyWithEscaping() {
        addData("foo {0} bar\n");
        checkSingleMessage("foo", "", "bar");
    }
    
    @Test
    public void escaping() {
        addData("* {20}foo bar baz\nhello world foo{1} bar {3}{4}\n");
        checkSingleMessage("*", "foo bar baz\nhello world", "foo bar", "{4}");
    }
    
    @Test
    public void chunked() {
        addData("foo b");
        assertEquals(0, messages.size());
        addData("{5}ar ba");
        assertEquals(0, messages.size());
        addData("z\n");
        checkSingleMessage("foo", "bar baz");
    }
}
