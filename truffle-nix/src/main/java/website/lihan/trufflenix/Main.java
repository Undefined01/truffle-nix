package website.lihan.trufflenix;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

public final class Main {
    private static final String LANG = "nix";

    public static void main(String[] args) throws IOException {
        Source source;
        Map<String, String> options = new HashMap<>();
        String file = null;
        boolean launcherOutput = true;
        for (String arg : args) {
            if (arg.equals("--disable-launcher-output")) {
                launcherOutput = false;
            } else if (parseOption(options, arg)) {
                continue;
            } else {
                if (file == null) {
                    file = arg;
                }
            }
        }

        if (file == null) {
            // @formatter:off
            source = Source.newBuilder(LANG, new InputStreamReader(System.in), "<stdin>").interactive(!launcherOutput).build();
            // @formatter:on
        } else {
            source = Source.newBuilder(LANG, new File(file)).interactive(!launcherOutput).build();
        }

        System.exit(executeSource(source, System.in, System.out, options, launcherOutput));
    }

    private static int executeSource(Source source, InputStream in, PrintStream out, Map<String, String> options, boolean launcherOutput) {
        Context context;
        PrintStream err = System.err;
        try {
            System.err.println("Executing source");
            context = Context.newBuilder(LANG).in(in).out(out).options(options).allowAllAccess(true).build();
        } catch (IllegalArgumentException e) {
            err.println(e.getMessage());
            return 1;
        }

        if (launcherOutput) {
            out.println("== running on " + context.getEngine());
        }

        try {
            Value result = context.eval(source);
            if (launcherOutput && !result.isNull()) {
                out.println(result.toString());
            }
            return 0;
        } catch (PolyglotException ex) {
            if (ex.isInternalError()) {
                // for internal errors we print the full stack trace
                ex.printStackTrace();
            } else {
                err.println(ex.getMessage());
            }
            return 1;
        } finally {
            context.close();
        }
    }

    private static boolean parseOption(Map<String, String> options, String arg) {
        if (arg.length() <= 2 || !arg.startsWith("--")) {
            return false;
        }
        int eqIdx = arg.indexOf('=');
        String key;
        String value;
        if (eqIdx < 0) {
            key = arg.substring(2);
            value = null;
        } else {
            key = arg.substring(2, eqIdx);
            value = arg.substring(eqIdx + 1);
        }

        if (value == null) {
            value = "true";
        }
        int index = key.indexOf('.');
        String group = key;
        if (index >= 0) {
            group = group.substring(0, index);
        }
        options.put(key, value);
        return true;
    }

}