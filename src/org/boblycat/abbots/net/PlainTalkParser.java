package org.boblycat.abbots.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ProtocolException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public abstract class PlainTalkParser {
    private enum ReadState {
        Normal,
        ReadingRawLength,
        ReadingRawData,
    }
    
    Charset charset;
    ByteArrayOutputStream currentField;
    ArrayList<String> fields;
    ReadState state;
    int remainingRaw;
    
    public PlainTalkParser(Charset charset) {
        this.charset = charset;
        currentField = new ByteArrayOutputStream();
        fields = new ArrayList<String>();
        state = ReadState.Normal;
    }
    
    private void processNormal(byte b) throws IOException {
        switch (b) {
        case '{':
            state = ReadState.ReadingRawLength;
            remainingRaw = 0;
            break;
        case ' ':
            fields.add(currentField.toString(charset.name()));
            currentField.reset();
            break;
        case '\r':
            // ignored
            break;
        case '\n':
            fields.add(currentField.toString(charset.name()));
            currentField.reset();
            receivedMessage(fields);
            fields = new ArrayList<String>();
            break;
        default:
            currentField.write(b);
            break;
        }
    }
    
    private void processRawLength(byte b) throws ProtocolException {
        if (b == '}') {
            if (remainingRaw > 0)
                state = ReadState.ReadingRawData;
            else
                state = ReadState.Normal;
            return;
        }
        int digit = b - ((byte) '0');
        if (digit < 0 || digit > 9)
            throw new ProtocolException("Error reading raw length");
        remainingRaw = 10 * remainingRaw + digit;
    }
    
    private void processRawByte(byte b) {
        currentField.write(b);
        remainingRaw--;
        if (remainingRaw == 0)
            state = ReadState.Normal;
    }
    
    public void receivedData(byte[] data, int length) throws IOException {
        for (int i = 0; i < length; i++) {
            byte b = data[i];
            switch (state) {
            case Normal:
                processNormal(b);
                break;
            case ReadingRawLength:
                processRawLength(b);
                break;
            case ReadingRawData:
                processRawByte(b);
                break;
            }
        }
    }
    
    public abstract void receivedMessage(List<String> message);
}
