package br.ufrgs.inf.dicelydone.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

public class LineReader {

    private InputStream mInnerStream;
    private Reader mInnerReader;

    boolean mEnded = false;

    public LineReader(InputStream stream) {
        mInnerStream = stream;
        try {
            mInnerReader = new InputStreamReader(stream, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public String readLine() throws IOException {
        if (mEnded) return null;

        StringBuilder line = new StringBuilder();

        while(true) {
            int c = mInnerReader.read();

            if (c == '\n' || c == '\0') {
                break;
            }

            if (c < 0) {
                mEnded = true;
                break;
            }

            line.append((char)c);
        }

        return line.toString();
    }
}
