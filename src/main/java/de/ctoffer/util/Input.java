package de.ctoffer.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public enum Input {
    ;

    public static String readNextLine(final InputStream in) {
        try (Scanner sc = new Scanner(closeProtection(in))) {
            return sc.nextLine();
        }
    }

    public static InputStream closeProtection(final InputStream inputStream) {
        return new CloseProtection(inputStream);
    }
}

class CloseProtection extends InputStream
{
    private final InputStream is;

    public CloseProtection(final InputStream is)
    {
        this.is = is;
    }

    @Override
    public int read() throws IOException
    { return is.read(); }

    @Override
    public int read(byte[] arg0) throws IOException
    { return is.read(arg0); }

    @Override
    public int read(byte[] arg0, int arg1, int arg2) throws IOException
    { return is.read(arg0, arg1, arg2); }

    @Override
    public long skip(long arg0) throws IOException
    { return is.skip(arg0); }

    @Override
    public int available() throws IOException
    { return is.available(); }

    @Override
    public synchronized void mark(int arg0)
    { is.mark(arg0); }

    @Override
    public synchronized void reset() throws IOException
    { is.reset(); }

    @Override
    public boolean markSupported()
    { return is.markSupported(); }

    @Override
    public void close() throws IOException
    {}
}