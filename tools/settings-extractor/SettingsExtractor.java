import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SettingsExtractor — extracts CongoCC's authoritative settings registry from
 * {@code AppSettings.java} and, optionally, diffs it against the settings
 * reference documentation so the two can be kept in sync.
 *
 * <h2>Why this exists</h2>
 * The Settings Reference chapter is hand-written, but the set of recognized
 * settings lives in {@code AppSettings.java} as three comma-delimited constants
 * ({@code booleanSettings}, {@code stringSettings}, {@code integerSettings}).
 * When the source adds, removes, or retypes a setting, the prose can silently
 * drift. This tool reads those constants as the single source of truth and
 * reports any current setting that the documentation does not mention.
 *
 * <h2>Running it</h2>
 * No build step is needed; run it as a single-file program with a JDK 17+:
 * <pre>{@code
 *   # List the current settings, grouped by type:
 *   java SettingsExtractor.java /path/to/AppSettings.java
 *
 *   # Emit a reStructuredText list-table fragment:
 *   java SettingsExtractor.java /path/to/AppSettings.java --rst
 *
 *   # Diff against the docs (exit status is non-zero if any current
 *   # setting is undocumented — suitable as a CI check):
 *   java SettingsExtractor.java /path/to/AppSettings.java \
 *        --diff source/docs/reference/settings.rst
 *
 *   # Run the built-in self-tests:
 *   java SettingsExtractor.java --self-test
 * }</pre>
 *
 * <h2>Scope and limitations</h2>
 * The tool extracts each setting's <em>name</em> and <em>type</em>, which is
 * what changes when settings are added or removed. It does <em>not</em> infer
 * default values — those are encoded in per-setting getter logic that is not
 * safe to parse mechanically — so defaults remain hand-documented. The diff
 * therefore guards against the most common and most damaging drift (a setting
 * appearing or disappearing) while leaving prose details to human review.
 *
 * <h2>Exit status</h2>
 * <ul>
 *   <li>{@code 0} — success (and, for {@code --diff}, every current setting is
 *       documented);</li>
 *   <li>{@code 1} — extraction failed, an input file was unusable, or
 *       {@code --diff} found undocumented settings;</li>
 *   <li>{@code 2} — the command line was malformed.</li>
 * </ul>
 */
public class SettingsExtractor {

    /** The three type-classified setting constants declared in AppSettings.java. */
    private static final String[] TYPE_FIELDS = {"booleanSettings", "stringSettings", "integerSettings"};
    /** The documentation type label that corresponds to each constant above. */
    private static final String[] TYPE_NAMES = {"boolean", "string", "integer"};

    /**
     * Setting names are {@code UPPER_SNAKE_CASE} and every current setting
     * contains at least one underscore. Requiring an underscore when scanning
     * the documentation keeps the real settings while filtering out unrelated
     * upper-case tokens (such as {@code NAME}, {@code EOF}, or {@code TOKEN})
     * that also appear in inline-literal backticks.
     */
    private static final Pattern DOC_SETTING = Pattern.compile("``([A-Z][A-Z0-9]*_[A-Z0-9_]+)``");

    // ---- Entry point ------------------------------------------------------

    public static void main(String[] args) {
        try {
            System.exit(run(args));
        } catch (UsageException ue) {
            System.err.println("Usage error: " + ue.getMessage());
            System.err.println();
            printUsage(System.err);
            System.exit(2);
        } catch (ExtractionException ee) {
            System.err.println("Error: " + ee.getMessage());
            System.exit(1);
        } catch (IOException ioe) {
            System.err.println("Error: I/O failure: " + ioe.getMessage());
            System.exit(1);
        }
    }

    /**
     * Parses the command line and performs the requested action.
     *
     * @return the process exit status.
     */
    static int run(String[] args) throws IOException {
        if (args.length == 1 && args[0].equals("--self-test")) {
            return selfTest();
        }
        Path appSettings = null;
        Path diffRst = null;
        boolean rst = false;
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            switch (a) {
                case "-h", "--help" -> { printUsage(System.out); return 0; }
                case "--rst" -> rst = true;
                case "--diff" -> {
                    if (i + 1 >= args.length) {
                        throw new UsageException("--diff requires the path to the settings .rst file");
                    }
                    diffRst = Paths.get(args[++i]);
                }
                default -> {
                    if (a.startsWith("-")) {
                        throw new UsageException("unknown flag: " + a);
                    }
                    if (appSettings != null) {
                        throw new UsageException("unexpected extra argument: " + a);
                    }
                    appSettings = Paths.get(a);
                }
            }
        }
        if (appSettings == null) {
            throw new UsageException("missing required argument: path to AppSettings.java");
        }
        if (rst && diffRst != null) {
            throw new UsageException("--rst and --diff cannot be combined");
        }

        Map<String, String> settings = parseSettings(requireReadableFile(appSettings, "AppSettings.java"));

        if (diffRst != null) {
            int undocumented = diff(settings, requireReadableFile(diffRst, "settings .rst"), System.out);
            return undocumented == 0 ? 0 : 1;
        }
        if (rst) {
            printRst(settings, System.out);
        } else {
            printPlain(settings, System.out);
        }
        return 0;
    }

    // ---- Extraction -------------------------------------------------------

    /** Reads {@code AppSettings.java} and returns a sorted {name -> type} map. */
    static Map<String, String> parseSettings(Path javaFile) throws IOException {
        return parseSettings(Files.readString(javaFile), javaFile.toString());
    }

    /**
     * Extracts the settings registry from the text of {@code AppSettings.java}.
     *
     * @param src   the Java source.
     * @param label a label for the source (a file path) used in error messages.
     * @return a sorted map of setting name to type ({@code "boolean"},
     *         {@code "string"}, or {@code "integer"}).
     * @throws ExtractionException if a constant cannot be found or a setting is
     *         listed under more than one type.
     */
    static Map<String, String> parseSettings(String src, String label) {
        Map<String, String> result = new TreeMap<>();
        for (int t = 0; t < TYPE_FIELDS.length; t++) {
            String field = TYPE_FIELDS[t];
            String type = TYPE_NAMES[t];
            String value = extractFieldStringValue(src, field, label);
            for (String name : splitNames(value)) {
                String previous = result.put(name, type);
                if (previous != null && !previous.equals(type)) {
                    throw new ExtractionException("setting '" + name + "' is listed under both '"
                            + previous + "' and '" + type + "' in " + label);
                }
            }
        }
        if (result.isEmpty()) {
            throw new ExtractionException("no settings found in " + label
                    + " — the AppSettings format may have changed");
        }
        return result;
    }

    /**
     * Returns the concatenated contents of all string literals in the
     * initializer of the named {@code String} field. This handles a value that
     * is split across several lines with the {@code +} operator, as the
     * AppSettings constants are.
     */
    static String extractFieldStringValue(String src, String field, String label) {
        Matcher m = Pattern.compile("String\\s+" + Pattern.quote(field) + "\\s*=").matcher(src);
        if (!m.find()) {
            throw new ExtractionException("could not find the '" + field + "' constant in " + label
                    + " (expected a 'String " + field + " = ...;' declaration); "
                    + "the AppSettings format may have changed");
        }
        StringBuilder collected = new StringBuilder();
        boolean inString = false;
        boolean foundEnd = false;
        for (int i = m.end(); i < src.length(); i++) {
            char c = src.charAt(i);
            if (inString) {
                if (c == '\\' && i + 1 < src.length()) {
                    // Preserve the escaped character literally; setting-name
                    // lists do not use escapes, but this keeps parsing robust.
                    collected.append(src.charAt(++i));
                } else if (c == '"') {
                    inString = false;
                } else {
                    collected.append(c);
                }
            } else if (c == '"') {
                inString = true;
            } else if (c == ';') {
                foundEnd = true;
                break;
            }
        }
        if (!foundEnd) {
            throw new ExtractionException("unterminated initializer for '" + field + "' in " + label);
        }
        return collected.toString();
    }

    /** Splits a comma-delimited list, trimming whitespace and dropping empties. */
    static List<String> splitNames(String value) {
        List<String> names = new ArrayList<>();
        for (String part : value.split(",")) {
            String name = part.trim();
            if (!name.isEmpty()) {
                names.add(name);
            }
        }
        return names;
    }

    // ---- Diff against the documentation -----------------------------------

    /** Returns the setting-shaped tokens documented (in backticks) in the text. */
    static Set<String> documentedSettings(String docText) {
        Set<String> tokens = new TreeSet<>();
        Matcher m = DOC_SETTING.matcher(docText);
        while (m.find()) {
            tokens.add(m.group(1));
        }
        return tokens;
    }

    /** Returns the current settings that are absent from {@code documented}, sorted. */
    static List<String> undocumented(Map<String, String> settings, Set<String> documented) {
        List<String> result = new ArrayList<>();
        for (String name : settings.keySet()) {
            if (!documented.contains(name)) {
                result.add(name);
            }
        }
        return result;
    }

    /**
     * Compares the extracted settings against those documented in the given
     * .rst file and prints a report.
     *
     * @return the number of current settings that are undocumented.
     */
    static int diff(Map<String, String> settings, Path rst, PrintStream out) throws IOException {
        Set<String> documented = documentedSettings(Files.readString(rst));
        List<String> undocumented = undocumented(settings, documented);

        List<String> orphaned = new ArrayList<>();
        for (String token : documented) {
            if (!settings.containsKey(token)) {
                orphaned.add(token);
            }
        }

        out.println("Current settings (from AppSettings.java): " + settings.size());
        out.println("Setting-shaped tokens documented:         " + documented.size());
        out.println();

        if (undocumented.isEmpty()) {
            out.println("OK: every current setting is documented.");
        } else {
            out.println("UNDOCUMENTED current settings (" + undocumented.size()
                    + ") — these must be added to the docs:");
            for (String name : undocumented) {
                out.println("    " + name + "  (" + settings.get(name) + ")");
            }
        }
        out.println();
        out.println("Documented but not a current setting (" + orphaned.size()
                + ") — review (may be deprecated, a synonym, or unrelated):");
        for (String token : orphaned) {
            out.println("    " + token);
        }
        return undocumented.size();
    }

    // ---- Output formats ---------------------------------------------------

    /** Prints the settings grouped by type, for quick reading. */
    static void printPlain(Map<String, String> settings, PrintStream out) {
        out.println("CongoCC settings extracted from AppSettings.java (" + settings.size() + " total)");
        for (int t = 0; t < TYPE_NAMES.length; t++) {
            String type = TYPE_NAMES[t];
            List<String> names = new ArrayList<>();
            for (Map.Entry<String, String> e : settings.entrySet()) {
                if (e.getValue().equals(type)) {
                    names.add(e.getKey());
                }
            }
            out.println();
            out.println(type + " (" + names.size() + "):");
            for (String name : names) {
                out.println("    " + name);
            }
        }
    }

    /** Emits a reStructuredText list-table fragment of the settings. */
    static void printRst(Map<String, String> settings, PrintStream out) {
        out.println(".. list-table:: Settings recognized by CongoCC (from AppSettings.java)");
        out.println("   :header-rows: 1");
        out.println("   :widths: 60 40");
        out.println();
        out.println("   * - Setting");
        out.println("     - Type");
        for (Map.Entry<String, String> e : settings.entrySet()) {
            out.println("   * - ``" + e.getKey() + "``");
            out.println("     - " + e.getValue());
        }
    }

    // ---- Helpers ----------------------------------------------------------

    /** Validates that the path is an existing, readable regular file. */
    static Path requireReadableFile(Path path, String what) {
        if (!Files.exists(path)) {
            throw new ExtractionException("the " + what + " does not exist: " + path);
        }
        if (Files.isDirectory(path)) {
            throw new ExtractionException("the " + what + " path is a directory, not a file: " + path);
        }
        if (!Files.isReadable(path)) {
            throw new ExtractionException("the " + what + " file is not readable: " + path);
        }
        return path;
    }

    static void printUsage(PrintStream out) {
        out.println("Usage:");
        out.println("    java SettingsExtractor.java <AppSettings.java> [--rst]");
        out.println("    java SettingsExtractor.java <AppSettings.java> --diff <settings.rst>");
        out.println("    java SettingsExtractor.java --self-test");
        out.println();
        out.println("Options:");
        out.println("    --rst              emit a reStructuredText list-table fragment");
        out.println("    --diff <file>      report current settings missing from <file>");
        out.println("                       (exit status 1 if any are missing)");
        out.println("    --self-test        run the built-in tests");
        out.println("    -h, --help         show this help");
    }

    // ---- Errors -----------------------------------------------------------

    /** Thrown for a malformed command line (exit status 2). */
    static final class UsageException extends RuntimeException {
        UsageException(String message) { super(message); }
    }

    /** Thrown when extraction or an input file fails (exit status 1). */
    static final class ExtractionException extends RuntimeException {
        ExtractionException(String message) { super(message); }
    }

    // ---- Built-in tests ---------------------------------------------------

    @FunctionalInterface
    interface Check {
        boolean run() throws Exception;
    }

    /**
     * Runs the built-in tests and prints a PASS/FAIL line for each.
     *
     * @return the number of failures (0 means all passed).
     */
    static int selfTest() {
        int failures = 0;

        failures += check("parse three constants", () -> {
            String src = """
                class X {
                    private final String booleanSettings = ",ALPHA,BETA,";
                    private final String stringSettings  = ",GAMMA,";
                    private final String integerSettings = ",DELTA,";
                }
                """;
            Map<String, String> m = parseSettings(src, "test");
            return m.size() == 4
                    && "boolean".equals(m.get("ALPHA")) && "boolean".equals(m.get("BETA"))
                    && "string".equals(m.get("GAMMA")) && "integer".equals(m.get("DELTA"));
        });

        failures += check("multi-line + concatenation", () -> {
            String src = """
                private final String booleanSettings = ",ALPHA,"
                        + "BETA,GAMMA,";
                private final String stringSettings = ",DELTA,";
                private final String integerSettings = ",EPSILON,";
                """;
            Map<String, String> m = parseSettings(src, "test");
            return m.keySet().equals(new TreeSet<>(List.of("ALPHA", "BETA", "GAMMA", "DELTA", "EPSILON")));
        });

        failures += check("duplicate name within one type is fine", () -> {
            String src = """
                private final String booleanSettings = ",ALPHA,";
                private final String stringSettings = ",GAMMA,GAMMA,";
                private final String integerSettings = ",DELTA,";
                """;
            return parseSettings(src, "test").get("GAMMA").equals("string");
        });

        failures += check("missing constant is reported", () -> {
            String src = "private final String booleanSettings = \",ALPHA,\";";
            try {
                parseSettings(src, "test");
                return false;
            } catch (ExtractionException expected) {
                return expected.getMessage().contains("stringSettings");
            }
        });

        failures += check("name in two types is reported", () -> {
            String src = """
                private final String booleanSettings = ",CLASH,";
                private final String stringSettings = ",CLASH,";
                private final String integerSettings = ",DELTA,";
                """;
            try {
                parseSettings(src, "test");
                return false;
            } catch (ExtractionException expected) {
                return expected.getMessage().contains("CLASH");
            }
        });

        failures += check("documented-token scan requires an underscore", () -> {
            Set<String> t = documentedSettings("``BASE_NAME`` ``NAME`` ``EOF`` ``TREE_BUILDING_ENABLED``");
            return t.contains("BASE_NAME") && t.contains("TREE_BUILDING_ENABLED")
                    && !t.contains("NAME") && !t.contains("EOF");
        });

        failures += check("diff finds undocumented settings", () -> {
            Map<String, String> s = new TreeMap<>(Map.of("FOO_BAR", "boolean", "BAZ_QUX", "string"));
            List<String> undoc = undocumented(s, new TreeSet<>(Set.of("FOO_BAR")));
            return undoc.equals(List.of("BAZ_QUX"));
        });

        System.out.println();
        System.out.println(failures == 0
                ? "All self-tests passed."
                : (failures + " self-test(s) FAILED."));
        return failures;
    }

    private static int check(String name, Check check) {
        try {
            boolean ok = check.run();
            System.out.println((ok ? "  pass  " : "  FAIL  ") + name);
            return ok ? 0 : 1;
        } catch (Throwable e) {
            System.out.println("  FAIL  " + name + "  (threw " + e.getClass().getSimpleName()
                    + ": " + e.getMessage() + ")");
            return 1;
        }
    }
}
