import java.util.List;

public class LexerTest {
    public static void main(String[] args) {
        String input = "let x = 42;\nfunction test() { return x + 1; }\n// This is a comment\n/* This is a\nmulti-line comment */\nlet isTrue = true;\nlet isFalse = false;\nlet nothing = null;\nlet undef = undefined;\nlet regex = /abc*/gi;";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();

        for (Token token : tokens) {
            System.out.println(token);
        }
    }
}
