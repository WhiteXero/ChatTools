package net.apple70cents.chattools.utils;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
//#if MC>=11903
import net.minecraft.core.registries.Registries;
//#endif

import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PlaceholderEngine: parses and renders placeholders of the form { ... }.
 * <p>
 * Features:
 * - Only quoted content ('...' or "...") is treated as string literal.
 * - Numeric tokens (integer or decimal) are recognized as numeric literals.
 * - Unquoted identifiers are treated as function names: if a mapping exists, they invoke it (zero-arg).
 * - If no mapping exists for such an identifier, an empty string is returned (to avoid ambiguity).
 * - Supports nested calls, recursion, and pipeline chaining using '|'.
 * - Each { ... } placeholder is evaluated independently.
 */
public final class PlaceholderEngine {
    // internal markers to protect escaped braces during processing
    private static final String ESC_L = "\u0000LBR\u0000";
    private static final String ESC_R = "\u0000RBR\u0000";
    private PlaceholderEngine() {
    }

    @FunctionalInterface
    public interface Resolver {
        /**
         * Resolve the function with evaluated String arguments.
         * Should return non-null (empty string allowed).
         */
        String resolve(String... args);
    }

    protected static final Map<String, Resolver> MAPPINGS = new ConcurrentHashMap<>();
    protected static final Map<String, Resolver> TempMappings = new ConcurrentHashMap<>();

    public static void addNewTempMapping(String string, Resolver resolver) {
        TempMappings.put(string, resolver);
    }

    public static void clearTempMappings() {
        TempMappings.clear();
    }

    static {
        // the number to determine whether two numbers equals to each other
        final double EPSILON = 1e-16;

        // constants
        MAPPINGS.put("PI", args -> String.valueOf(Math.PI));
        MAPPINGS.put("TAU", args -> String.valueOf(Math.PI * 2));
        MAPPINGS.put("E", args -> String.valueOf(Math.E));

        // variables
        MAPPINGS.put("pitch", args -> String.valueOf(Minecraft.getInstance().player.
                        //#if MC>=11700
                                getXRot()
                //#else
                //$$ xRot
                //#endif
        ));
        MAPPINGS.put("yaw", args -> String.valueOf(Minecraft.getInstance().player.
                        //#if MC>=11700
                                getYRot()
                //#else
                //$$ yRot
                //#endif
        ));
        MAPPINGS.put("x", args -> String.valueOf(Minecraft.getInstance().player.getX()));
        MAPPINGS.put("y", args -> String.valueOf(Minecraft.getInstance().player.getY()));
        MAPPINGS.put("z", args -> String.valueOf(Minecraft.getInstance().player.getZ()));
        MAPPINGS.put("pos", args -> String.format("(%.1f, %.1f, %.1f)", Minecraft.getInstance().player.getX(),
                Minecraft.getInstance().player.getY(), Minecraft.getInstance().player.getZ()));
        MAPPINGS.put("dimension_reg_name", args -> Minecraft.getInstance().level.dimension().location().toString());
        MAPPINGS.put("biome_reg_name",
                args -> String.valueOf(Minecraft.getInstance().level.registryAccess().lookupOrThrow(
                        //#if MC>=11903
                        Registries.BIOME
                        //#else
                        //$$ net.minecraft.core.Registry.BIOME_REGISTRY
                        //#endif
                ).getKey(Minecraft.getInstance().level.getBiome(Minecraft.getInstance().player.blockPosition())
                                //#if MC>=11800
                                .value()
                        //#endif
                )));
        MAPPINGS.put("biome",
                args -> TextUtils.transWithPrefix(MAPPINGS.get("biome_reg_name").resolve().replace(":", "."), "biome.")
                        .getString());
        MAPPINGS.put("world_time", args -> String.valueOf(Minecraft.getInstance().level.getDayTime() % 24000));
        MAPPINGS.put("game_time", args -> String.valueOf(Minecraft.getInstance().level.getGameTime()));
        MAPPINGS.put("real_time_long", args -> {
            java.time.Instant instant = java.time.Instant.now();
            java.time.ZonedDateTime currentTime = java.time.ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
            String offsetString = ZoneId.systemDefault().getRules().getOffset(instant).getId();
            return String.format("%4d/%d/%d %02d:%02d:%02d UTC%s", currentTime.getYear(),
                    currentTime.getMonth().getValue(), currentTime.getDayOfMonth(), currentTime.getHour(),
                    currentTime.getMinute(), currentTime.getSecond(), offsetString);
        });
        MAPPINGS.put("real_time_short", args -> {
            java.time.Instant instant = java.time.Instant.now();
            java.time.ZonedDateTime currentTime = java.time.ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
            return String.format("%02d:%02d:%02d", currentTime.getHour(), currentTime.getMinute(),
                    currentTime.getSecond());
        });
        MAPPINGS.put("nickname", args -> Minecraft.getInstance().player.getGameProfile()
                        //#if MC>=12109
                        .name()
                //#else
                //$$ .getName()
                //#endif
        );
        MAPPINGS.put("uuid", args -> Minecraft.getInstance().player.getGameProfile()
                //#if MC>=12109
                .id()
                //#else
                //$$ .getId()
                //#endif
                .toString());
        MAPPINGS.put("session_identifier", args -> ContextUtils.getSessionIdentifier());
        MAPPINGS.put("health", args -> String.valueOf(Minecraft.getInstance().player.getHealth()));
        MAPPINGS.put("player_count", args -> String.valueOf(Minecraft.getInstance().level.players().size()));
        MAPPINGS.put("item_in_hand",
                args -> Minecraft.getInstance().player.getMainHandItem().getHoverName().getString());
        MAPPINGS.put("item_in_hand_reg_name", args -> Minecraft.getInstance().level.registryAccess().lookupOrThrow(
                //#if MC>=11903
                Registries.ITEM
                //#else
                //$$ net.minecraft.core.Registry.ITEM_REGISTRY
                //#endif
        ).getKey(Minecraft.getInstance().player.getMainHandItem().getItem()).toString());

        // functions
        MAPPINGS.put("in_precision", args -> {
            int digits = (int) Double.parseDouble(args[1]);
            return String.format(("%." + digits + "f"), Double.parseDouble(args[0]));
        });
        MAPPINGS.put("upper", args -> args[0].toUpperCase(Locale.ROOT));
        MAPPINGS.put("lower", args -> args[0].toLowerCase(Locale.ROOT));
        MAPPINGS.put("len_of_str", args -> String.valueOf(args[0].length()));
        MAPPINGS.put("len_of_list", args -> String.valueOf(args.length));
        MAPPINGS.put("replace", args -> args[0].replace(args[1], args.length > 2 ? args[2] : ""));
        MAPPINGS.put("repeat", args -> args[0].repeat((int) Double.parseDouble(args[1])));
        MAPPINGS.put("join", args -> {
            // just join all args with no separator
            StringBuilder sb = new StringBuilder();
            for (String s : args) sb.append(s);
            return sb.toString();
        });
        MAPPINGS.put("min", args -> {
            if (args.length == 0) return "";
            double min = Double.MAX_VALUE;
            for (String s : args) {
                try {
                    double v = Double.parseDouble(s);
                    if (v < min) min = v;
                } catch (NumberFormatException ignored) {
                }
            }
            return min == Double.MAX_VALUE ? "" : String.valueOf(min);
        });
        MAPPINGS.put("max", args -> {
            if (args.length == 0) return "";
            double max = -Double.MAX_VALUE;
            for (String s : args) {
                try {
                    double v = Double.parseDouble(s);
                    if (v > max) max = v;
                } catch (NumberFormatException ignored) {
                }
            }
            return max == -Double.MAX_VALUE ? "" : String.valueOf(max);
        });
        MAPPINGS.put("sum", args -> {
            if (args.length == 0) return "";
            double sum = 0;
            for (String s : args) {
                try {
                    double v = Double.parseDouble(s);
                    sum += v;
                } catch (NumberFormatException ignored) {
                }
            }
            return String.valueOf(sum);
        });
        MAPPINGS.put("avg", args -> {
            if (args.length == 0) return "";
            double sum = 0;
            int len = 0;
            for (String s : args) {
                try {
                    double v = Double.parseDouble(s);
                    sum += v;
                    len++;
                } catch (NumberFormatException ignored) {
                }
            }
            return len == 0 ? "" : String.valueOf(sum / len);
        });
        MAPPINGS.put("subtract", args -> {
            if (args.length == 0) return "";
            double result = Double.parseDouble(args[0]);
            for (String s : args) {
                try {
                    double v = Double.parseDouble(s);
                    result -= v;
                } catch (NumberFormatException ignored) {
                }
            }
            return String.valueOf(result);
        });
        MAPPINGS.put("multiply", args -> {
            if (args.length == 0) return "";
            double result = 1;
            for (String s : args) {
                try {
                    double v = Double.parseDouble(s);
                    result *= v;
                } catch (NumberFormatException ignored) {
                }
            }
            return String.valueOf(result);
        });
        MAPPINGS.put("divide", args -> {
            if (args.length == 0) return "";
            double result = Double.parseDouble(args[0]);
            for (String s : args) {
                try {
                    double v = Double.parseDouble(s);
                    if (v != 0) result /= v;
                } catch (NumberFormatException ignored) {
                }
            }
            return String.valueOf(result);
        });
        MAPPINGS.put("mod", args -> {
            if (args.length < 2) return "";
            try {
                double a = Double.parseDouble(args[0]);
                double b = Double.parseDouble(args[1]);
                if (b == 0) return "";
                return String.valueOf(a % b);
            } catch (NumberFormatException e) {
                return "";
            }
        });
        MAPPINGS.put("gcd", args -> {
            if (args.length == 0) return "";
            long result = Long.parseLong(args[0]);
            for (String s : Arrays.stream(args).skip(1).toList()) {
                long num = Long.parseLong(s);
                while (num != 0) {
                    long tmp = num;
                    num = result % num;
                    result = tmp;
                }
            }
            return String.valueOf(result);
        });
        MAPPINGS.put("round", args -> String.valueOf(Math.round(Double.parseDouble(args[0]))));
        MAPPINGS.put("floor", args -> String.valueOf((int) Math.floor(Double.parseDouble(args[0]))));
        MAPPINGS.put("ceil", args -> String.valueOf((int) Math.ceil(Double.parseDouble(args[0]))));
        MAPPINGS.put("flip", args -> ThreadLocalRandom.current().nextBoolean() ? "HEAD" : "TAIL");
        MAPPINGS.put("random", args -> {
            if (args.length == 0) return String.valueOf(ThreadLocalRandom.current().nextDouble());
            else if (args.length == 1) {
                try {
                    long max = Long.parseLong(args[0]);
                    if (max <= 0) return "0";
                    return String.valueOf(ThreadLocalRandom.current().nextLong(max));
                } catch (NumberFormatException e) {
                    return "0";
                }
            } else {
                try {
                    long min = Long.parseLong(args[0]);
                    long max = Long.parseLong(args[1]);
                    if (min >= max) return String.valueOf(min);
                    return String.valueOf(ThreadLocalRandom.current().nextLong(min, max));
                } catch (NumberFormatException e) {
                    return "0";
                }
            }
        });
        MAPPINGS.put("sqrt", args -> String.valueOf(Math.sqrt(Double.parseDouble(args[0]))));
        MAPPINGS.put("cbrt", args -> String.valueOf(Math.cbrt(Double.parseDouble(args[0]))));
        MAPPINGS.put("pow", args -> String.valueOf(Math.pow(Double.parseDouble(args[0]), Double.parseDouble(args[1]))));
        MAPPINGS.put("sin", args -> String.valueOf(Math.sin(Double.parseDouble(args[0]))));
        MAPPINGS.put("cos", args -> String.valueOf(Math.cos(Double.parseDouble(args[0]))));
        MAPPINGS.put("tan", args -> String.valueOf(Math.abs(
                Double.parseDouble(MAPPINGS.get("cos").resolve(args))) < EPSILON ? Double.POSITIVE_INFINITY : Math.tan(
                Double.parseDouble(args[0]))));
        MAPPINGS.put("csc", args -> String.valueOf(1.0 / Double.parseDouble(MAPPINGS.get("sin").resolve(args))));
        MAPPINGS.put("sec", args -> String.valueOf(1.0 / Double.parseDouble(MAPPINGS.get("cos").resolve(args))));
        MAPPINGS.put("cot", args -> String.valueOf(1.0 / Double.parseDouble(MAPPINGS.get("tan").resolve(args))));
        MAPPINGS.put("asin", args -> String.valueOf(Math.asin(Double.parseDouble(args[0]))));
        MAPPINGS.put("arcsin", args -> MAPPINGS.get("asin").resolve(args));
        MAPPINGS.put("acos", args -> String.valueOf(Math.acos(Double.parseDouble(args[0]))));
        MAPPINGS.put("arccos", args -> MAPPINGS.get("acos").resolve(args));
        MAPPINGS.put("atan", args -> String.valueOf(Math.atan(Double.parseDouble(args[0]))));
        MAPPINGS.put("arctan", args -> MAPPINGS.get("atan").resolve(args));
        MAPPINGS.put("lg", args -> String.valueOf(Math.log10(Double.parseDouble(args[0]))));
        MAPPINGS.put("log10", args -> MAPPINGS.get("lg").resolve(args));
        MAPPINGS.put("ln", args -> String.valueOf(Math.log(Double.parseDouble(args[0]))));
        MAPPINGS.put("log", args -> {
            if (args.length == 1) return MAPPINGS.get("log10").resolve(args);
            return String.valueOf(Math.log(Double.parseDouble(args[0])) / Math.log(Double.parseDouble(args[1])));
        });
        MAPPINGS.put("exp", args -> String.valueOf(Math.exp(Double.parseDouble(args[0]))));
        MAPPINGS.put("abs", args -> String.valueOf(Math.abs(Double.parseDouble(args[0]))));
        MAPPINGS.put("clamp", args -> String.valueOf(
                Math.min(Math.max(Double.parseDouble(args[0]), Double.parseDouble(args[1])),
                        Double.parseDouble(args[2]))));
        MAPPINGS.put("sgn", args -> {
            double num = Double.parseDouble(args[0]);
            if (Math.abs(num) < EPSILON || Double.isNaN(num)) {
                return "0";
            } else {
                return (num > 0) ? "1" : "-1";
            }
        });
        MAPPINGS.put("to_rad", args -> String.valueOf(Math.toRadians(Double.parseDouble(args[0]))));
        MAPPINGS.put("to_deg", args -> String.valueOf(Math.toDegrees(Double.parseDouble(args[0]))));

        // conditionals
        MAPPINGS.put("is_true", args -> {
            if (args.length == 0) return "false";
            String condition = args[0];
            if (!condition.isEmpty() && !condition.equals("0") && !condition.equalsIgnoreCase("false")) {
                return "true";
            } else {
                return "false";
            }
        });
        MAPPINGS.put("eq", args -> {
            if (args.length < 2) return "false";
            String a = args[0];
            String b = args[1];
            return a.equals(b) || Math.abs(Double.parseDouble(a) - Double.parseDouble(b)) < EPSILON ? "true" : "false";
        });
        MAPPINGS.put("not", args -> {
            if (args.length == 0) return "true";
            return MAPPINGS.get("is_true").resolve(args[0]).equals("true") ? "false" : "true";
        });
        MAPPINGS.put("or", args -> {
            for (String s : args) {
                if (MAPPINGS.get("is_true").resolve(s).equals("true")) {
                    return "true";
                }
            }
            return "false";
        });
        MAPPINGS.put("and", args -> {
            for (String s : args) {
                if (MAPPINGS.get("is_true").resolve(s).equals("false")) {
                    return "false";
                }
            }
            return "true";
        });
        MAPPINGS.put("if", args -> {
            String condition = MAPPINGS.get("is_true").resolve(args[0]);
            if (args.length == 1) return condition;
            String thenPart = args[1];
            String elsePart = args.length >= 3 ? args[2] : "";
            if ("true".equals(condition)) {
                return thenPart;
            } else {
                return elsePart;
            }
        });
        MAPPINGS.put("greater", args -> {
            if (args.length < 2) return "false";
            try {
                double a = Double.parseDouble(args[0]);
                double b = Double.parseDouble(args[1]);
                return a > b ? "true" : "false";
            } catch (NumberFormatException e) {
                return "false";
            }
        });
        MAPPINGS.put("less", args -> {
            if (args.length < 2) return "false";
            try {
                double a = Double.parseDouble(args[0]);
                double b = Double.parseDouble(args[1]);
                return a < b ? "true" : "false";
            } catch (NumberFormatException e) {
                return "false";
            }
        });
        MAPPINGS.put("greater_eq", args -> MAPPINGS.get("not").resolve(MAPPINGS.get("less").resolve(args)));
        MAPPINGS.put("less_eq", args -> MAPPINGS.get("not").resolve(MAPPINGS.get("greater").resolve(args)));
        MAPPINGS.put("contains", args -> {
            if (args.length < 2) return "false";
            String haystack = args[0];
            String needle = args[1];
            return haystack.contains(needle) ? "true" : "false";
        });
    }

    // debug prints
    private static final boolean DEBUG = FabricLoader.getInstance().isDevelopmentEnvironment();
    private static final ThreadLocal<Deque<String>> DEBUG_STACK = ThreadLocal.withInitial(ArrayDeque::new);

    private static void debugPush(String info) {
        if (DEBUG) {
            DEBUG_STACK.get().push(info);
            System.out.println("[PlaceholderEngine][STACK PUSH] " + info + " | Stack: " + DEBUG_STACK.get());
        }
    }

    private static void debugPop() {
        if (DEBUG) {
            String info = DEBUG_STACK.get().isEmpty() ? "<empty>" : DEBUG_STACK.get().peek();
            System.out.println("[PlaceholderEngine][STACK POP] " + info + " | Stack: " + DEBUG_STACK.get());
            if (!DEBUG_STACK.get().isEmpty()) DEBUG_STACK.get().pop();
        }
    }

    private static void debugPrint(String msg) {
        if (DEBUG) {
            System.out.println("[PlaceholderEngine][DEBUG] " + msg + " | Stack: " + DEBUG_STACK.get());
        }
    }

    // simple outermost placeholder matcher (we assume no unmatched '}' inside)
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]*)\\}");

    public static String apply(String template) {
        if (template == null) return null;
        // Preprocess escaped braces so they won't be treated as placeholders.
        String processed = preprocessEscapedBraces(template);

        Matcher m = PLACEHOLDER_PATTERN.matcher(processed);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String body = m.group(1).trim();
            String replacement;
            try {
                debugPrint("Evaluating placeholder: {" + body + "}");
                replacement = evaluateBody(body);
                if (replacement == null) replacement = "";
                replacement = Matcher.quoteReplacement(replacement);
            } catch (Exception e) {
                debugPrint("Exception in evaluating {" + body + "}: " + e);
                replacement = Matcher.quoteReplacement(m.group(0));
            }
            m.appendReplacement(sb, replacement);
        }
        m.appendTail(sb);

        // Restore escaped braces markers back to literal braces
        return sb.toString().replace(ESC_L, "{").replace(ESC_R, "}");
    }

    /**
     * Preprocess input to replace escaped braces ("\{" or "\}") with internal markers.
     * Handles runs of backslashes correctly: only an odd count of consecutive '\' directly
     * before a brace makes that brace escaped. Pairs of '\' produce real '\' characters.
     */
    private static String preprocessEscapedBraces(String input) {
        if (input == null || input.isEmpty()) return input;
        StringBuilder out = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); ) {
            char c = input.charAt(i);
            if (c == '\\') {
                int j = i;
                // count consecutive backslashes
                while (j < input.length() && input.charAt(j) == '\\') j++;
                int count = j - i;
                // if next char exists and is a brace, we need special handling
                if (j < input.length() && (input.charAt(j) == '{' || input.charAt(j) == '}')) {
                    // append the pairs as literal backslashes (each pair -> one backslash)
                    int pairs = count / 2;
                    for (int p = 0; p < pairs; p++) out.append('\\');
                    // if odd, that means the brace is escaped -> append marker
                    if ((count % 2) == 1) {
                        out.append(input.charAt(j) == '{' ? ESC_L : ESC_R);
                    } else {
                        // even -> brace is not escaped, append the brace itself
                        out.append(input.charAt(j));
                    }
                    i = j + 1; // skip the backslash-run and the brace
                } else {
                    // no brace after backslashes -> just copy them as-is
                    for (int p = 0; p < count; p++) out.append('\\');
                    i = j;
                }
            } else {
                out.append(c);
                i++;
            }
        }
        return out.toString();
    }

    // Evaluate the content inside { ... }
    private static String evaluateBody(String body) {
        debugPush("evaluateBody: {" + body + "}");
        try {
            Parser parser = new Parser(body);
            Expr expr = parser.parseExpression();
            String result = expr.evaluate();
            debugPrint("Result of {" + body + "}: " + result);
            return result;
        } finally {
            debugPop();
        }
    }

    // Abstract expression node
    private static abstract class Expr {
        abstract String evaluate();
    }

    // Literal (only from quoted strings or numeric tokens)
    private static class Literal extends Expr {
        final String value;

        Literal(String v) {
            this.value = v == null ? "" : v;
        }

        @Override
        String evaluate() {
            debugPush("Literal: '" + value + "'");
            try {
                debugPrint("Literal value: '" + value + "'");
                return value;
            } finally {
                debugPop();
            }
        }

        @Override
        public String toString() {
            return "Lit(" + value + ")";
        }
    }

    // Function call node
    private static class FuncCall extends Expr {
        final String name;
        final List<Expr> args;

        FuncCall(String name, List<Expr> args) {
            this.name = name;
            this.args = args;
        }

        @Override
        String evaluate() {
            debugPush("FuncCall: " + name + "(" + args + ")");
            try {
                String[] evaluated = new String[args.size()];
                for (int i = 0; i < args.size(); i++) evaluated[i] = args.get(i).evaluate();
                Resolver resolver = MAPPINGS.get(name);
                if (resolver == null) {
                    resolver = TempMappings.get(name);
                }
                if (resolver != null) {
                    try {
                        String r = resolver.resolve(evaluated);
                        debugPrint("FuncCall '" + name + "' result: '" + r + "'");
                        return r == null ? "" : r;
                    } catch (Exception e) {
                        debugPrint("Exception in FuncCall '" + name + "': " + e);
                        return "";
                    }
                } else {
                    debugPrint("No mapping for function: '" + name + "'");
                    return "";
                }
            } finally {
                debugPop();
            }
        }

        @Override
        public String toString() {
            return "Call(" + name + "," + args + ")";
        }
    }

    // Parser: recursive-descent, supports pipelines with '|'
    private static class Parser {
        private final String s;
        private int p = 0;

        Parser(String s) {
            this.s = s == null ? "" : s;
        }

        // parse expression and support chaining pipelines left-to-right
        Expr parseExpression() {
            Expr left = parseTerm();
            skipWhitespace();
            while (peek() == '|') {
                consume('|');
                skipWhitespace();
                // right side should be function name (optionally with args)
                String fname = parseIdentifier();
                if (fname.isEmpty()) throw new RuntimeException("Expected function name after '|'");
                List<Expr> extraArgs = new ArrayList<>();
                skipWhitespace();
                if (peek() == '(') {
                    consume('(');
                    skipWhitespace();
                    if (peek() != ')') {
                        do {
                            skipWhitespace();
                            extraArgs.add(parseExpression()); // allow nested expressions as args
                            skipWhitespace();
                        } while (peek() == ',' && consumeIf(','));
                    }
                    consume(')');
                }
                // compose: new call where left is first arg
                List<Expr> newArgs = new ArrayList<>();
                newArgs.add(left);
                newArgs.addAll(extraArgs);
                left = new FuncCall(fname, newArgs);
                skipWhitespace();
            }
            return left;
        }

        // parse a term: quoted literal, parenthesized expr, function call with parentheses, or bare token:
        // - quoted -> Literal
        // - parenthesized -> Expr
        // - token with '(' -> function call
        // - bare token: if numeric -> Literal; else if mapped function exists -> zero-arg FuncCall; else -> empty Literal
        Expr parseTerm() {
            skipWhitespace();
            char c = peek();
            if (c == '"' || c == '\'') {
                String lit = parseQuoted();
                return new Literal(lit);
            } else if (c == '(') {
                consume('(');
                Expr e = parseExpression();
                skipWhitespace();
                consume(')');
                return e;
            } else {
                String ident = parseIdentifier();
                skipWhitespace();
                if (!ident.isEmpty() && peek() == '(') {
                    // function call with parentheses
                    consume('(');
                    List<Expr> args = new ArrayList<>();
                    skipWhitespace();
                    if (peek() != ')') {
                        do {
                            skipWhitespace();
                            args.add(parseExpression());
                            skipWhitespace();
                        } while (peek() == ',' && consumeIf(','));
                    }
                    consume(')');
                    return new FuncCall(ident, args);
                } else {
                    // bare token: check number OR mapping OR else empty
                    if (isNumber(ident)) {
                        return new Literal(ident);
                    } else if (MAPPINGS.containsKey(ident) || TempMappings.containsKey(ident)) {
                        // zero-arg call to mapped function
                        return new FuncCall(ident, Collections.emptyList());
                    } else {
                        // No quotes and not a number or mapped function -> treat as empty to avoid ambiguity
                        return new Literal("");
                    }
                }
            }
        }

        // parse quoted string, supports backslash escaping
        private String parseQuoted() {
            char quote = s.charAt(p++);
            StringBuilder sb = new StringBuilder();
            while (p < s.length()) {
                char ch = s.charAt(p++);
                if (ch == '\\' && p < s.length()) {
                    char nx = s.charAt(p++);
                    sb.append(nx);
                } else if (ch == quote) {
                    break;
                } else {
                    sb.append(ch);
                }
            }
            return sb.toString();
        }

        // parse an identifier token (letters, digits, .-_$)
        private String parseIdentifier() {
            skipWhitespace();
            int start = p;
            while (p < s.length()) {
                char c = s.charAt(p);
                if (Character.isLetterOrDigit(c) || c == '_' || c == '.' || c == '-' || c == '$') p++;
                else break;
            }
            return s.substring(start, p);
        }

        // parse a token until comma / ')' / '|' (trimmed)
        private String parseToken() {
            skipWhitespace();
            int start = p;
            while (p < s.length()) {
                char c = s.charAt(p);
                if (c == ',' || c == ')' || c == '|') break;
                p++;
            }
            return s.substring(start, p).trim();
        }

        private boolean isNumber(String tok) {
            if (tok == null || tok.isEmpty()) return false;
            // simple number regex: optional sign, digits, optional fractional part
            return tok.matches("[+-]?\\d+(\\.\\d+)?");
        }

        private void skipWhitespace() {
            while (p < s.length() && Character.isWhitespace(s.charAt(p))) p++;
        }

        private char peek() {
            return p < s.length() ? s.charAt(p) : '\0';
        }

        private boolean consumeIf(char c) {
            if (peek() == c) {
                p++;
                return true;
            }
            return false;
        }

        private char consume(char c) {
            if (peek() != c) throw new RuntimeException("Expected '" + c + "' at pos " + p);
            p++;
            return c;
        }
    }
}
