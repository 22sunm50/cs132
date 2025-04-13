import java.util.*;

public class Parse {
    // A list of tokens produced from lexical analysis by the scanner
    public ArrayList<TokenType> tokens;
    // The current token in the token list
    public TokenType token;

    public enum TokenType {
        LBRACE , RBRACE , LPAREN , RPAREN , SEMICOLON , NOT ,
        IF, ELSE , WHILE , TRUE , FALSE ,
        SYSTEM_OUT_PRINTLN , EOF
    }

    public void printFailureAndExit() { // 🍅
        System.out.println("Parse error");
        System.exit(1);
    }

    public ArrayList<TokenType> scanner(char[] input) {
        ArrayList<TokenType> tokens = new ArrayList<>();
        int cur_state = 0;
        for (int i = 0; i < input.length; i++){
            // skip whitespace, continue to the next character
            if (cur_state == 0 && (input[i] == ' ' || input[i] == '\t' || input[i] == '\n')) {
                continue;
            }
            // curr state q0
            if (cur_state == 0) {
                if (input[i] == 'i') cur_state = 1; // read a char 'i' move to q1
                else if (input[i] == 'e') cur_state = 3; // read a char 'e' move to q3
                else if (input[i] == 'w') cur_state = 7;
                else if (input[i] == 't') cur_state = 12;
                else if (input[i] == 'f') cur_state = 16;
                else if (input[i] == '!'){
                    tokens.add(TokenType.NOT);
                    cur_state = 0;
                }
                else if (input[i] == '('){
                    tokens.add(TokenType.LPAREN);
                    cur_state = 0;
                }
                else if (input[i] == ')'){
                    tokens.add(TokenType.RPAREN);
                    cur_state = 0;
                }
                else if (input[i] == '{'){
                    tokens.add(TokenType.LBRACE);
                    cur_state = 0;
                }
                else if (input[i] == '}'){
                    tokens.add(TokenType.RBRACE);
                    cur_state = 0;
                }
                else if (input[i] == ';'){
                    tokens.add(TokenType.SEMICOLON);
                    cur_state = 0;
                }
                else if (input[i] == 'S') cur_state = 20;
                else { // 🍅: this is an unrecognized token, do error handling or simply skip
                    printFailureAndExit();
                }
            }
            // ============== REST OF 'if' ==============
            else if (cur_state == 1) { 
                if (input[i] == 'f') {
                    cur_state = 2; // read 'f', move to q2 (which is a final state for "if")
                    tokens.add(TokenType.IF); // add the token "if" to the list
                    cur_state = 0; // reset to the initial state for the next token
                }
                else printFailureAndExit();
            }

            // ============== REST OF 'else' ==============
            else if (cur_state == 3) { 
                if (input[i] == 'l') cur_state = 4;
                else printFailureAndExit();
            }
            else if (cur_state == 4) { 
                if (input[i] == 's') cur_state = 5;
                else printFailureAndExit();
            }
            else if (cur_state == 5) { 
                if (input[i] == 'e') {
                    cur_state = 6; // final state of 'else'
                    tokens.add(TokenType.ELSE); // add the token "else" to the list
                    cur_state = 0; // reset to the initial state
                }
                else printFailureAndExit();
            }

            // ============== REST OF 'while' ==============
            else if (cur_state == 7) { 
                if (input[i] == 'h') cur_state = 8;
                else printFailureAndExit();
            }
            else if (cur_state == 8) { 
                if (input[i] == 'i') cur_state = 9;
                else printFailureAndExit();
            }
            else if (cur_state == 9) { 
                if (input[i] == 'l') cur_state = 10;
                else printFailureAndExit();
            }
            else if (cur_state == 10) { 
                if (input[i] == 'e') {
                    cur_state = 11; // final state of 'else'
                    tokens.add(TokenType.WHILE); // add the token "else" to the list
                    cur_state = 0; // reset to the initial state
                }
                else printFailureAndExit();
            }

            // ============== REST OF 'true' ==============
            else if (cur_state == 12) { 
                if (input[i] == 'r') cur_state = 13;
                else printFailureAndExit();
            }
            else if (cur_state == 13) { 
                if (input[i] == 'u') cur_state = 14;
                else printFailureAndExit();
            }
            else if (cur_state == 14) { 
                if (input[i] == 'e') {
                    cur_state = 15;
                    tokens.add(TokenType.TRUE);
                    cur_state = 0;
                }
                else printFailureAndExit();
            }

            // ============== REST OF 'false' ==============
            else if (cur_state == 16) { 
                if (input[i] == 'a') cur_state = 17;
                else printFailureAndExit();
            }
            else if (cur_state == 17) { 
                if (input[i] == 'l') cur_state = 18;
                else printFailureAndExit();
            }
            else if (cur_state == 18) { 
                if (input[i] == 's') cur_state = 19;
                else printFailureAndExit();
            }
            else if (cur_state == 19) { 
                if (input[i] == 'e') {
                    cur_state = 20;
                    tokens.add(TokenType.FALSE);
                    cur_state = 0;
                }
                else printFailureAndExit();
            }

            // ============== REST OF 'System.out.println' ==============
            else if (cur_state == 20) { 
                if (input[i] == 'y') cur_state = 21;
                else printFailureAndExit();
            }
            else if (cur_state == 21) { 
                if (input[i] == 's') cur_state = 22;
                else printFailureAndExit();
            }
            else if (cur_state == 22) { 
                if (input[i] == 't') cur_state = 23;
                else printFailureAndExit();
            }
            else if (cur_state == 23) { 
                if (input[i] == 'e') cur_state = 24;
                else printFailureAndExit();
            }
            else if (cur_state == 24) { 
                if (input[i] == 'm') cur_state = 25;
                else printFailureAndExit();
            }
            else if (cur_state == 25) { 
                if (input[i] == '.') cur_state = 26;
                else printFailureAndExit();
            }
            else if (cur_state == 26) { 
                if (input[i] == 'o') cur_state = 27;
                else printFailureAndExit();
            }
            else if (cur_state == 27) { 
                if (input[i] == 'u') cur_state = 28;
                else printFailureAndExit();
            }
            else if (cur_state == 28) { 
                if (input[i] == 't') cur_state = 29;
                else printFailureAndExit();
            }
            else if (cur_state == 29) { 
                if (input[i] == '.') cur_state = 30;
                else printFailureAndExit();
            }
            else if (cur_state == 30) { 
                if (input[i] == 'p') cur_state = 31;
                else printFailureAndExit();
            }
            else if (cur_state == 31) { 
                if (input[i] == 'r') cur_state = 32;
                else printFailureAndExit();
            }
            else if (cur_state == 32) { 
                if (input[i] == 'i') cur_state = 33;
                else printFailureAndExit();
            }
            else if (cur_state == 33) { 
                if (input[i] == 'n') cur_state = 34;
                else printFailureAndExit();
            }
            else if (cur_state == 34) { 
                if (input[i] == 't') cur_state = 35;
                else printFailureAndExit();
            }
            else if (cur_state == 35) { 
                if (input[i] == 'l') cur_state = 36;
                else printFailureAndExit();
            }
            else if (cur_state == 36) { 
                if (input[i] == 'n') {
                    cur_state = 37;
                    tokens.add(TokenType.SYSTEM_OUT_PRINTLN);
                    cur_state = 0;
                }
                else printFailureAndExit();
            }
        }
        return tokens;
    }

    //////////////////////// Parse ////////////////////////

    private void parse( ArrayList<TokenType> inputTokens ) {
        this.tokens = inputTokens;
        this.token = tokens.get(0);
        // token = nextToken();

        // the first production rule we always try to parse:
        start();
        // what we always try to parse after:
        eat(TokenType.EOF);
    }

    // A method used to consume / parse a token. Prints “Parse error” and exits if expected != token
    private void eat(TokenType expected){
        if (token == expected){ 
            tokens.remove(0);
            token = tokens.isEmpty() ? TokenType.EOF : tokens.get(0);
        } else {
            printFailureAndExit();
        }
    }

    // parse the S rule in the Minijava grammar
    private void start() {
        // ============ { ============
        if (token.equals(TokenType.LBRACE)) {
            eat(TokenType.LBRACE);
            l_expr();
            eat(TokenType.RBRACE);
        }
        // ============ “System.out.println” ============
        else if ( token.equals(TokenType.SYSTEM_OUT_PRINTLN) ) {
            eat(TokenType.SYSTEM_OUT_PRINTLN);
            eat(TokenType.LPAREN);
            expr();
            eat(TokenType.RPAREN);
            eat(TokenType.SEMICOLON);
        }
        // ============ if ============
        else if (token.equals(TokenType.IF)) {
            eat(TokenType.IF);
            eat(TokenType.LPAREN);
            expr();
            eat(TokenType.RPAREN);
            start();
            eat(TokenType.ELSE);
            start();
        }
        // ============ while ============
        else if (token.equals(TokenType.WHILE)) {
            eat(TokenType.WHILE);
            eat(TokenType.LPAREN);
            expr();
            eat(TokenType.RPAREN);
            start();
        }
        
        else { // if we don’t see the correct token
            printFailureAndExit();
        }
    }

    // parse the E rule in the Minijava grammar
    private void expr() {
        // parse true
        if ( token.equals(TokenType.TRUE) ) {
            eat(TokenType.TRUE);
        }
        // parse false 
        else if ( token.equals(TokenType.FALSE) ) {
            eat(TokenType.FALSE);
        } 
        // parse "! E"
        else if ( token.equals(TokenType.NOT) ) {
            eat(TokenType.NOT);
            expr();
        } 
        else { // if we don’t see the correct token
            printFailureAndExit();
        }
    }

    // L rule
    private void l_expr(){
        if (token.equals(TokenType.RBRACE)) return;
        start();
        l_expr();
    }

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        String input = scan.useDelimiter("\\A").hasNext() ? scan.next() : "";
        scan.close();

        Parse parser = new Parse();
        ArrayList<TokenType> tokens = parser.scanner(input.toCharArray());
        tokens.add(TokenType.EOF);

        parser.parse(tokens);
        System.out.println("Program parsed successfully");
    }
}
























// ///////////////////// LECTURE SLIDE /////////////////////
// Token token;
// void eat(char a){
//     if (token == a) { token = next_token();} // go to next token if it matches as expected
//                     { error(); }             // else, error out
// }
// void goal() { token = next_token(); expr(); eat(EOF); } // set to next token, call the expression, and then expect to be end of line
// void expr() { term(); expr_prime(); }
// void expr_prime() {
//     if (token == PLUS)
//         { eat(PLUS); expr(); }
//     else if (token == MINUS)
//         { eat(MINUS); expr(); }
//     else { }
// }

// void term() { 
//     factor(); 
//     term_prime(); 
// }

// void term_prime() {
//     if (token = MULT)
//         { eat(MULT); term(); }
//     else if (token = ID)
//         { eat(ID); }
//     else error();
// }

// ///////////////////// DISCUSSION WORKSHEET /////////////////////
// public class Parse {
//       // A list of tokens produced from lexical analysis by the scanner
//       public ArrayList<String> tokens;
//       // The current token in the token list
//       public String token;

//       // A method used to get the next token in the list of tokens
//       private String nextToken();
//       // A method used to consume / parse a token. Prints “Parse error”
//       // and exits if expected != token
//       private void eat( String expected );

//       // Prints “Parse error” to standard output and exits the program
//       public printFailureAndExit();
// }

// // parse the S rule in the Minijava grammar
// private void start() {
//       // TODO: parse System.out.println
//       if ( token.equals(“System.out.println”) ) {
//            eat(“System.out.println”);
//            eat(“(”);
//            expr();
//            eat(“)”);
//            eat(“;”);
//      } else {
//            // TODO: What should we do if we don’t see the correct token?
//            printFailureAndExit();
//     }
// }

// // parse the E rule in the Minijava grammar
// private void expr() {
//       // TODO: parse true
//       if ( token.equals(“true”) ) {
//            eat(“true”);
//      } elif ( token.equals(“false” ) ) {
//             // TODO: parse false
//            eat(“false”);
//      } else {
//            // TODO: What should we do if we don’t see the correct token?
//            printFailureAndExit();
//     }
// }

// // parse the E rule in the Minijava grammar
// private void parse( ArrayList<String> tokens ) {
//       tokens = tokens;
//       token = nextToken();

//       // TODO: What is the first production rule we always try to parse?


//      start();
//      // TODO: What do we always try to parse after?
//      eat(“<EOF>”);
// }

// ///////////////////// DISCUSSION SLIDE /////////////////////
// public ArrayList<TokenType> scanner(char[] input) {
//     ArrayList<TokenType> tokens = new ArrayList<>();
//     int cur_state = 0;
//     for (int i = 0; i < input.length; i++){
//         if (cur_state == 0 && (input[i] == ' ' || input[i] == '\t' || input[i] == '\n')) {
//             // skip whitespace, continue to the next character
//             continue;
//         }
//         if (cur_state == 0) { // curr state q0
//             if (input[i] == 'i') cur_state = 1; // read a char 'i' move to q1
//             else if (input[i] == 'e') cur_state = 3; // read a char 'i' move to q3
//             else ..; // 🍅: this is an unrecognized token, do error handling or simply skip
//         }
//         else if (cur_state = 1) { // curr state q1
//             if (input[i] == 'f') {
//                 cur_state = 2; // read 'f', move to q2 (which is a final state for "if")
//                 tokens.add(TokenType.IF); // add the token "if" to the list
//                 cur_state = 0; // reset to the initial state for the next token
//             }
//             else ..; // 🍅: this is an unrecognized token, do error handling or simply skip
//         }
//     }
// }