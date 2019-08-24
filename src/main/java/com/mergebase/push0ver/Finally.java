package com.mergebase.push0ver;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

public class Finally {

    public static void rethrowIfUnchecked(Throwable t) {
        if (t instanceof Error) {
            throw (Error) t;
        } else if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        }
    }

    public static void close(Object... closeArgs) {
        if (closeArgs == null || closeArgs.length == 0) {
            return;
        }

        LinkedList<Throwable> closingProblems = new LinkedList<Throwable>();
        for (Object o : closeArgs) {
            if (o == null) {
                continue;
            }
            try {
                if (o instanceof ResultSet)
                    ((ResultSet) o).close();
                else if (o instanceof Statement)
                    ((Statement) o).close();
                else if (o instanceof Connection)
                    ((Connection) o).close();
                else if (o instanceof Reader)
                    ((Reader) o).close();
                else if (o instanceof Writer)
                    ((Writer) o).close();
                else if (o instanceof InputStream)
                    ((InputStream) o).close();
                else if (o instanceof OutputStream)
                    ((OutputStream) o).close();
                else if (o instanceof JarFile)
                    ((JarFile) o).close();
                else if (o instanceof ZipFile)
                    ((ZipFile) o).close();
                else if (o instanceof Process)
                    ((Process) o).destroy();
                else {
                    throw new IllegalArgumentException("cannot close: " + o.getClass());
                }
            } catch (Throwable t) {
                closingProblems.add(t);
            }
        }

        if (!closingProblems.isEmpty()) {
            Throwable t = closingProblems.get(0);
            rethrowIfUnchecked(t);
            throw new RuntimeException("Failed to close something: " + t, t);
        }
    }

    public static void exampleUsage() throws Exception {

        InputStream in = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            in = new FileInputStream("/var/tmp/test.txt");
            isr = new InputStreamReader(in, "utf8");
            br = new BufferedReader(isr);
            String line = br.readLine();
            while (line != null) {
                line = br.readLine();
            }
        } finally {
            // closes in same order as arguments are provided
            // (br first, then isr, then in).
            Finally.close(br, isr, in);
        }
    }

}
