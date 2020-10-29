import javax.swing.plaf.nimbus.State;
import javax.swing.text.DefaultCaret;
import java.util.*;

public class Parser {
    // Recursive descent parser that inputs a C++Lite program and 
    // generates its abstract syntax.  Each method corresponds to
    // a concrete syntax grammar rule, which appears as a comment
    // at the beginning of the method.
  
    Token token;          // current token from the input stream
    Lexer lexer;
  
    public Parser(Lexer ts) { // Open the C++Lite source program
        lexer = ts;                          // as a token stream, and
        token = lexer.next();            // retrieve its first Token
    }
  
    private String match (TokenType t) {
        String value = token.value();
        if (token.type().equals(t))
            token = lexer.next();
        else
            error(t);
        return value;
    }
  
    private void error(TokenType tok) {
        System.err.println("Syntax error: expecting: " + tok 
                           + "; saw: " + token);
        System.exit(1);
    }
  
    private void error(String tok) {
        System.err.println("Syntax error: expecting: " + tok 
                           + "; saw: " + token);
        System.exit(1);
    }

  
    public Program program() {
        // Program --> int main ( ) '{' Declarations Statements '}'
        TokenType[ ] header = {TokenType.Int, TokenType.Main,
                          TokenType.LeftParen, TokenType.RightParen};
        for (int i=0; i<header.length; i++)   // bypass "int main ( )"
            match(header[i]);
        match(TokenType.LeftBrace);
        // student exercise - Done.
        Declarations ds = declarations();
        Block b = new Block();
        Statement s;
        while (isStatement()) {
            s = statement();
            b.members.add(s);
        }
        match(TokenType.RightBrace);
        return new Program(ds, b);  // student exercise - Done.
    }
  
    private Declarations declarations () {
        // Declarations --> { Declaration }
        Declarations ds = new Declarations();
        while (isType()){
            declaration(ds);
        }
        return ds;  // student exercise - Done.
    }
  
    private void declaration (Declarations ds) {
        // Declaration  --> Type Identifier { , Identifier } ;
        // student exercise - Done.
        Type t = type();    // get type
        Variable v = new Variable(match(TokenType.Identifier)); // get if Identifier else error
        Declaration d = new Declaration(v, t);  // make Declaration(Variable, Type)
        ds.add(d);  // add in list

        // recognize continuous variable declarations
        while (isComma()){   // if Token is ','
            token = lexer.next();   // get token from lexer
            v = new Variable(match(TokenType.Identifier));
            d = new Declaration(v, t);
            ds.add(d);
        }
        match(TokenType.Semicolon); // check if semicolon else error
    }
  
    private Type type () {
        // Type  -->  int | bool | float | char 
        Type t = null;
        if (token.type().equals(TokenType.Int)) {
            t = Type.INT;
        }
        else if (token.type().equals(TokenType.Bool)){
            t = Type.BOOL;
        }
        else if (token.type().equals(TokenType.Float)){
            t = Type.FLOAT;
        }
        else if (token.type().equals(TokenType.Char)){
            t = Type.CHAR;
        }
        token = lexer.next();
        // student exercise - Done
        return t;          
    }
  
    private Statement statement() {
        // Statement --> ; | Block | Assignment | IfStatement | WhileStatement
        Statement s = null;
        if (isSemicolon())
            s = new Skip();
        else if (isLeftBrace())
            s = statement();
        else if (isIdentifier())
            s = assignment();
        else if (isIf())
            s = ifStatement();
        else if (isWhile())
            s = whileStatement();
        else
            error("; | Block | Assignment | IfStatement | WhileStatement");
        // student exercise - Done.
        return s;
    }
  
    private Block statements () {
        // Block --> '{' Statements '}'
        Block b = new Block();
        Statement s = null;
        match(TokenType.LeftBrace);
        while (isStatement()) {
            s = statement();
            b.members.add(s);
        }
        match(TokenType.RightBrace);
        // student exercise - Done.
        return b;
    }
  
    private Assignment assignment () {
        // Assignment --> Identifier = Expression ;
        Variable v = new Variable(match(TokenType.Identifier));
        match(TokenType.Assign); // '=' : Assign
        Expression e = expression();
        match(TokenType.Semicolon);
        return new Assignment(v, e);  // student exercise - Done.
    }
  
    private Conditional ifStatement () {
        // IfStatement --> if ( Expression ) Statement [ else Statement ] match(TokenType.If);
        match(TokenType.LeftParen);
        Expression e = expression();
        match(TokenType.RightParen);
        Statement s = statement();
        if (isElse()) {
            Statement s2 = statements();
            return new Conditional(e, s, s2);
        }
        return new Conditional(e, s);  // student exercise - Done.
    }
  
    private Loop whileStatement () {
        // WhileStatement --> while ( Expression ) Statement
        match(TokenType.While);
        match(TokenType.LeftParen);
        Expression e = expression();
        match(TokenType.RightParen);
        Statement s = statement();
        return new Loop(e, s);  // student exercise - Done.
    }

    private Expression expression () {
        // Expression --> Conjunction { || Conjunction }
        Expression e = conjunction();
        while (isOr()) {
            Operator op = new Operator(match(token.type()));
            Expression e2 = conjunction();
            e = new Binary(op, e, e2);
        }
        return e;  // student exercise - Done.
    }
  
    private Expression conjunction () {
        // Conjunction --> Equality { && Equality }
        Expression e = equality();
        while (isAnd()){
            Operator op = new Operator(match(token.type()));
            Expression e2 = equality();
            e = new Binary(op, e, e2);
        }
        return e;  // student exercise - Done.
    }
  
    private Expression equality () {
        // Equality --> Relation [ EquOp Relation ]
        Expression e = relation();
        while (isEqualityOp()) {
            Operator op = new Operator(match(token.type()));
            Expression e2 = relation();
            e = new Binary(op, e, e2);
        }
        return e;  // student exercise - Done.
    }

    private Expression relation (){
        // Relation --> Addition [RelOp Addition]
        Expression e = addition();
        while (isRelationalOp()) {
            Operator op = new Operator(match(token.type()));
            Expression e2 = addition();
            e = new Binary(op, e, e2);
        }
        return e;  // student exercise - Done.
    }

    private Expression addition () {
        // Addition --> Term { AddOp Term }
        Expression e = term();
        while (isAddOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term2 = term();
            e = new Binary(op, e, term2);
        }
        return e;
    }
  
    private Expression term () {
        // Term --> Factor { MultiplyOp Factor }
        Expression e = factor();
        while (isMultiplyOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term2 = factor();
            e = new Binary(op, e, term2);
        }
        return e;
    }
  
    private Expression factor() {
        // Factor --> [ UnaryOp ] Primary 
        if (isUnaryOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term = primary();
            return new Unary(op, term);
        }
        else return primary();
    }
  
    private Expression primary () {
        // Primary --> Identifier | Literal | ( Expression )
        //             | Type ( Expression )
        Expression e = null;
        if (token.type().equals(TokenType.Identifier)) {
            e = new Variable(match(TokenType.Identifier));
        } else if (isLiteral()) {
            e = literal();
        } else if (token.type().equals(TokenType.LeftParen)) {
            token = lexer.next();
            e = expression();       
            match(TokenType.RightParen);
        } else if (isType( )) {
            Operator op = new Operator(match(token.type()));
            match(TokenType.LeftParen);
            Expression term = expression();
            match(TokenType.RightParen);
            e = new Unary(op, term);
        } else error("Identifier | Literal | ( | Type");
        return e;
    }

    private Value literal( ) {
        // Literal --> Integer | Boolean | Float | Char
        Value v = null;
        String s = token.value();

        if (isIntLiteral()) {
            v = new IntValue(Integer.parseInt(s));
        }
        else if (isTrue()) {
            v = new BoolValue(true);
        } else if (isFalse()) {
            v = new BoolValue(false);
        }
        else if (isFloatLiteral()) {
            v = new FloatValue(Float.parseFloat(s));
        }
        else if (isCharLiteral()) {
            v = new CharValue(s.charAt(0));
        }
        else {
            error("Integer | Boolean | Float | Char");
        }
        token = lexer.next();
        return v;  // student exercise - Done.
    }
  

    private boolean isElse() {
        return token.type().equals(TokenType.Else);
    }

    private boolean isAddOp( ) {
        return token.type().equals(TokenType.Plus) ||
               token.type().equals(TokenType.Minus);
    }
    
    private boolean isMultiplyOp( ) {
        return token.type().equals(TokenType.Multiply) ||
               token.type().equals(TokenType.Divide);
    }
    
    private boolean isUnaryOp( ) {
        return token.type().equals(TokenType.Not) ||
               token.type().equals(TokenType.Minus);
    }

    private boolean isEqualityOp( ) {
        return token.type().equals(TokenType.Equals) ||
            token.type().equals(TokenType.NotEqual);
    }

    private boolean isAnd() {
        return token.type().equals(TokenType.And);
    }

    private boolean isOr() {
        return token.type().equals(TokenType.Or);
    }

    private boolean isRelationalOp( ) {
        return token.type().equals(TokenType.Less) ||
               token.type().equals(TokenType.LessEqual) || 
               token.type().equals(TokenType.Greater) ||
               token.type().equals(TokenType.GreaterEqual);
    }
    
    private boolean isType( ) {
        return token.type().equals(TokenType.Int)
            || token.type().equals(TokenType.Bool) 
            || token.type().equals(TokenType.Float)
            || token.type().equals(TokenType.Char);
    }
    
    private boolean isLiteral( ) {
        return isIntLiteral() || isBooleanLiteral() || isFloatLiteral() || isCharLiteral();
    }

    private boolean isIntLiteral() {
        return token.type().equals(TokenType.IntLiteral);
    }

    private boolean isFloatLiteral() {
        return token.type().equals(TokenType.FloatLiteral);
    }

    private boolean isCharLiteral() {
        return token.type().equals(TokenType.CharLiteral);
    }

    private boolean isBooleanLiteral( ) {
        return isTrue() || isFalse();
    }

    private boolean isTrue() {
        return token.type().equals(TokenType.True);
    }

    private boolean isFalse() {
        return token.type().equals(TokenType.False);
    }


    private boolean isSemicolon() {
        return token.type().equals(TokenType.Semicolon);
    }

    private boolean isLeftBrace() {
        return token.type().equals(TokenType.LeftBrace);
    }

    private boolean isRightBrace() {
        return token.type().equals(TokenType.RightBrace);
    }

    private boolean isIf() {
        return token.type().equals(TokenType.If);
    }

    private boolean isWhile() {
        return token.type().equals(TokenType.While);
    }

    private boolean isIdentifier() {
        return token.type().equals(TokenType.Identifier);
    }

    private boolean isComma() {
        return token.type().equals(TokenType.Comma);
    }

    // check statement
    private boolean isStatement() {
        // Statement -> ;|Block|Assignment|IfStatement|WhileStatement
        return isSemicolon() || isLeftBrace() || isIf() || isWhile() || isIdentifier();
    }

    public static void main(String args[]) {
        Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
        System.out.println("Begin parsing... " + args[0] + "\n");
        prog.display();           // display abstract syntax tree
    } //main

} // Parser
