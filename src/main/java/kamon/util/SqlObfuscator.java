package kamon.util;

import com.google.common.base.Joiner;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class SqlObfuscator {
    private static final String SINGLE_QUOTE = "'(?:[^']|'')*?(?:\\\\'.*|'(?!'))";
    private static final String DOUBLE_QUOTE = "\"(?:[^\"]|\"\")*?(?:\\\\\".*|\"(?!\"))";
    private static final String DOLLAR_QUOTE = "(\\$(?!\\d)[^$]*?\\$).*?(?:\\1|$)";
    private static final String ORACLE_QUOTE = "q'\\[.*?(?:\\]'|$)|q'\\{.*?(?:\\}'|$)|q'<.*?(?:>'|$)|q'\\(.*?(?:\\)'|$)";
    private static final String COMMENT = "(?:#|--).*?(?=\\r|\\n|$)";
    private static final String MULTILINE_COMMENT = "/\\*(?:[^/]|/[^*])*?(?:\\*/|/\\*.*)";
    private static final String UUID = "\\{?(?:[0-9a-f]\\-*){32}\\}?";
    private static final String HEX = "0x[0-9a-f]+";
    private static final String BOOLEAN = "\\b(?:true|false|null)\\b";
    private static final String NUMBER = "-?\\b(?:[0-9_]+\\.)?[0-9_]+([eE][+-]?[0-9_]+)?";
    public static final String OBFUSCATED_SETTING = "obfuscated";
    public static final String RAW_SETTING = "raw";
    public static final String OFF_SETTING = "off";

    private SqlObfuscator() {
    }

    public abstract String obfuscateSql(String var1);

//    public abstract String obfuscateSql(String var1, String var2);

    public boolean isObfuscating() {
        return false;
    }

    public static SqlObfuscator getDefaultSqlObfuscator() {
        return new DefaultSqlObfuscator();
    }

    static SqlObfuscator getNoObfuscationSqlObfuscator() {
        return new SqlObfuscator(){

            @Override
            public String obfuscateSql(String sql) {
                return sql;
            }

//            @Override
//            public String obfuscateSql(String sql, String dialect) {
//                return sql;
//            }
        };
    }

    static SqlObfuscator getNoSqlObfuscator() {
        return new SqlObfuscator(){

            @Override
            public String obfuscateSql(String sql) {
                return null;
            }

//            @Override
//            public String obfuscateSql(String sql, String dialect) {
//                return null;
//            }
        };
    }

    public static SqlObfuscator getCachingSqlObfuscator(SqlObfuscator sqlObfuscator) {
        if (sqlObfuscator.isObfuscating()) {
            return new CachingSqlObfuscator(sqlObfuscator);
        }
        return sqlObfuscator;
    }

    static class CachingSqlObfuscator
            extends SqlObfuscator {
        private final Map<String, String> cache = new HashMap<String, String>();
        private final SqlObfuscator sqlObfuscator;

        public CachingSqlObfuscator(SqlObfuscator sqlObfuscator) {
            super();
            this.sqlObfuscator = sqlObfuscator;
        }

        @Override
        public String obfuscateSql(String sql) {
            String obfuscatedSql = this.cache.get(sql);
            if (obfuscatedSql == null) {
                obfuscatedSql = this.sqlObfuscator.obfuscateSql(sql);
                this.cache.put(sql, obfuscatedSql);
            }
            return obfuscatedSql;
        }

//        @Override
//        public String obfuscateSql(String sql, String dialect) {
//            String obfuscatedSql = this.cache.get(sql);
//            if (obfuscatedSql == null) {
//                obfuscatedSql = this.sqlObfuscator.obfuscateSql(sql, dialect);
//                this.cache.put(sql, obfuscatedSql);
//            }
//            return obfuscatedSql;
//        }

        @Override
        public boolean isObfuscating() {
            return this.sqlObfuscator.isObfuscating();
        }
    }

    static class DefaultSqlObfuscator
            extends SqlObfuscator {
        private static final Pattern ALL_DIALECTS_PATTERN;
        private static final Pattern ALL_UNMATCHED_PATTERN;
//        private static final Pattern MYSQL_DIALECT_PATTERN;
//        private static final Pattern MYSQL_UNMATCHED_PATTERN;
//        private static final Pattern POSTGRES_DIALECT_PATTERN;
//        private static final Pattern POSTGRES_UNMATCHED_PATTERN;
//        private static final Pattern ORACLE_DIALECT_PATTERN;
//        private static final Pattern ORACLE_UNMATCHED_PATTERN;

        DefaultSqlObfuscator() {
            super();
        }


        @Override
        public String obfuscateSql(String sql) {
            if (sql == null || sql.length() == 0) {
                return sql;
            }

            String obfuscatedSql = ALL_DIALECTS_PATTERN.matcher(sql).replaceAll("?");
            return this.checkForUnmatchedPairs(ALL_UNMATCHED_PATTERN, obfuscatedSql);
        }

//        @Override
//        public String obfuscateSql(String sql, String dialect) {
//            if (sql == null || sql.length() == 0) {
//                return sql;
//            }
//            if (dialect.equals("mysql")) {
//                String obfuscatedSql = MYSQL_DIALECT_PATTERN.replacer("?").replace(sql);
//                return this.checkForUnmatchedPairs(MYSQL_UNMATCHED_PATTERN, obfuscatedSql);
//            }
//            if (dialect.equals("postgresql") || dialect.equals("postgres")) {
//                String obfuscatedSql = POSTGRES_DIALECT_PATTERN.replacer("?").replace(sql);
//                return this.checkForUnmatchedPairs(POSTGRES_UNMATCHED_PATTERN, obfuscatedSql);
//            }
//            if (dialect.equals("oracle")) {
//                String obfuscatedSql = ORACLE_DIALECT_PATTERN.replacer("?").replace(sql);
//                return this.checkForUnmatchedPairs(ORACLE_UNMATCHED_PATTERN, obfuscatedSql);
//            }
//            return this.obfuscateSql(sql);
//        }

        @Override
        public boolean isObfuscating() {
            return true;
        }

        private String checkForUnmatchedPairs(Pattern pattern, String obfuscatedSql) {
            return pattern.matcher(obfuscatedSql).find() ? "?" : obfuscatedSql;
        }

        static {
            String allDialectsPattern = Joiner.on("|").join(SqlObfuscator.SINGLE_QUOTE, SqlObfuscator.DOUBLE_QUOTE, SqlObfuscator.DOLLAR_QUOTE, SqlObfuscator.ORACLE_QUOTE, SqlObfuscator.COMMENT, SqlObfuscator.MULTILINE_COMMENT, SqlObfuscator.UUID, SqlObfuscator.HEX, SqlObfuscator.BOOLEAN, SqlObfuscator.NUMBER);
            ALL_DIALECTS_PATTERN = Pattern.compile(allDialectsPattern);
            ALL_UNMATCHED_PATTERN = Pattern.compile("'|\"|/\\*|\\*/|\\$");
        }
    }

}