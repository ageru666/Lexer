import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private String input;
    private int position;
    private int line;
    private int column;

    private static final String[] KEYWORDS = {
            "if", "else", "while", "for", "function", "return", "var", "let", "const",
            "new", "this", "super", "class", "extends", "static", "import", "export",
            "default", "from", "as", "try", "catch", "finally", "throw", "switch",
            "case", "break", "continue", "debugger", "instanceof", "typeof", "void",
            "with", "yield", "await"
    };
    private static final String[] BOOLEAN_LITERALS = { "true", "false" };
    private static final String[] SPECIAL_LITERALS = { "null", "undefined" };
    private static final String OPERATORS = "+-*/%=!&|<>^~?:";
    private static final String PUNCTUATION = ".,;(){}[]";

    public Lexer(String input) {
        this.input = input;
        this.position = 0;
        this.line = 1;
        this.column = 1;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (position < input.length()) {
            char currentChar = input.charAt(position);

            if (Character.isWhitespace(currentChar)) {
                handleWhitespace(tokens);
            } else if (Character.isLetter(currentChar) || currentChar == '_') {
                handleIdentifierOrLiteral(tokens);
            } else if (Character.isDigit(currentChar)) {
                handleNumber(tokens);
            } else if (currentChar == '"' || currentChar == '\'' || currentChar == '`') {
                handleString(tokens);
            } else if (currentChar == '/' && (peekNextChar() == '/' || peekNextChar() == '*')) {
                handleComment(tokens);
            } else if (currentChar == '/' && isPotentialRegex()) {
                handleRegExp(tokens);
            } else if (OPERATORS.indexOf(currentChar) != -1) {
                handleOperator(tokens);
            } else if (PUNCTUATION.indexOf(currentChar) != -1) {
                handlePunctuation(tokens);
            } else {
                handleUnknown(tokens);
            }
        }

        return tokens;
    }

    private void handleWhitespace(List<Token> tokens) {
        int startColumn = column;
        StringBuilder sb = new StringBuilder();
        while (position < input.length() && Character.isWhitespace(input.charAt(position))) {
            if (input.charAt(position) == '\n') {
                sb.append(input.charAt(position));
                line++;
                column = 1;
            } else {
                sb.append(input.charAt(position));
                column++;
            }
            position++;
        }
        tokens.add(new Token(TokenType.WHITESPACE, sb.toString(), line, startColumn));
    }

    private void handleIdentifierOrLiteral(List<Token> tokens) {
        int startColumn = column;
        StringBuilder sb = new StringBuilder();
        while (position < input.length() && (Character.isLetterOrDigit(input.charAt(position)) || input.charAt(position) == '_')) {
            sb.append(input.charAt(position));
            position++;
            column++;
        }
        String value = sb.toString();
        TokenType type = determineTokenType(value);
        tokens.add(new Token(type, value, line, startColumn));
    }

    private TokenType determineTokenType(String value) {
        if (isKeyword(value)) {
            return TokenType.KEYWORD;
        } else if (isBooleanLiteral(value)) {
            return TokenType.BOOLEAN;
        } else if (isSpecialLiteral(value)) {
            if (value.equals("null")) {
                return TokenType.NULL;
            } else {
                return TokenType.UNDEFINED;
            }
        } else {
            return TokenType.IDENTIFIER;
        }
    }

    private boolean isKeyword(String value) {
        for (String keyword : KEYWORDS) {
            if (keyword.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBooleanLiteral(String value) {
        for (String literal : BOOLEAN_LITERALS) {
            if (literal.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSpecialLiteral(String value) {
        for (String literal : SPECIAL_LITERALS) {
            if (literal.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private void handleNumber(List<Token> tokens) {
        int startColumn = column;
        StringBuilder sb = new StringBuilder();
        while (position < input.length() && Character.isDigit(input.charAt(position))) {
            sb.append(input.charAt(position));
            position++;
            column++;
        }
        tokens.add(new Token(TokenType.NUMBER, sb.toString(), line, startColumn));
    }

    private void handleString(List<Token> tokens) {
        int startColumn = column;
        char quote = input.charAt(position);
        StringBuilder sb = new StringBuilder();
        sb.append(quote);
        position++;
        column++;
        while (position < input.length() && input.charAt(position) != quote) {
            sb.append(input.charAt(position));
            position++;
            column++;
        }
        if (position < input.length()) {
            sb.append(input.charAt(position));
            position++;
            column++;
        }
        tokens.add(new Token(quote == '`' ? TokenType.TEMPLATE_STRING : TokenType.STRING, sb.toString(), line, startColumn));
    }

    private void handleComment(List<Token> tokens) {
        int startColumn = column;
        StringBuilder sb = new StringBuilder();
        if (input.charAt(position) == '/' && peekNextChar() == '/') {
            while (position < input.length() && input.charAt(position) != '\n') {
                sb.append(input.charAt(position));
                position++;
                column++;
            }
        } else if (input.charAt(position) == '/' && peekNextChar() == '*') {
            sb.append("/*");
            position += 2;
            column += 2;
            while (position < input.length() && !(input.charAt(position) == '*' && peekNextChar() == '/')) {
                sb.append(input.charAt(position));
                if (input.charAt(position) == '\n') {
                    line++;
                    column = 1;
                } else {
                    column++;
                }
                position++;
            }
            if (position < input.length()) {
                sb.append("*/");
                position += 2;
                column += 2;
            }
        }
        tokens.add(new Token(TokenType.COMMENT, sb.toString(), line, startColumn));
    }

    private void handleOperator(List<Token> tokens) {
        int startColumn = column;
        StringBuilder sb = new StringBuilder();
        while (position < input.length() && OPERATORS.indexOf(input.charAt(position)) != -1) {
            sb.append(input.charAt(position));
            position++;
            column++;
        }
        tokens.add(new Token(TokenType.OPERATOR, sb.toString(), line, startColumn));
    }

    private void handlePunctuation(List<Token> tokens) {
        int startColumn = column;
        tokens.add(new Token(TokenType.PUNCTUATION, Character.toString(input.charAt(position)), line, startColumn));
        position++;
        column++;
    }

    private void handleUnknown(List<Token> tokens) {
        int startColumn = column;
        tokens.add(new Token(TokenType.UNKNOWN, Character.toString(input.charAt(position)), line, startColumn));
        position++;
        column++;
    }

    private boolean isPotentialRegex() {
        int tempPosition = position + 1;
        while (tempPosition < input.length() && Character.isWhitespace(input.charAt(tempPosition))) {
            tempPosition++;
        }
        if (tempPosition < input.length() && input.charAt(tempPosition) == '/') {
            return false;
        }
        if (tempPosition < input.length() && OPERATORS.indexOf(input.charAt(tempPosition)) != -1) {
            return false;
        }
        return true;
    }

    private void handleRegExp(List<Token> tokens) {
        int startColumn = column;
        StringBuilder sb = new StringBuilder();
        sb.append('/');
        position++;
        column++;
        while (position < input.length() && input.charAt(position) != '/') {
            sb.append(input.charAt(position));
            position++;
            column++;
        }
        if (position < input.length()) {
            sb.append('/');
            position++;
            column++;
        }
        while (position < input.length() && Character.isLetter(input.charAt(position))) {
            sb.append(input.charAt(position));
            position++;
            column++;
        }
        tokens.add(new Token(TokenType.REGEXP, sb.toString(), line, startColumn));
    }

    private char peekNextChar() {
        if (position + 1 < input.length()) {
            return input.charAt(position + 1);
        }
        return '\0';
    }
}
