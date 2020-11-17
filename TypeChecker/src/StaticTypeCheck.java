// StaticTypeCheck.java

import java.util.*;

// Static type checking for Clite is defined by the functions 
// V and the auxiliary functions typing and typeOf.  These
// functions use the classes in the Abstract Syntax of Clite.


public class StaticTypeCheck {

    public static TypeMap typing (Declarations d) {
        // create Type Map
        TypeMap map = new TypeMap();
        for (Declaration di : d)
            map.put(di.v, di.t);
        return map;
    }

    public static void check(boolean test, String msg) {
        if (test)  return;
        System.err.println(msg);
        // System.exit(1);
        System.exit(1);
    }

    public static void V (Declarations d) {
        for (int i=0; i<d.size() - 1; i++)
            for (int j=i+1; j<d.size(); j++) {
                Declaration di = d.get(i);
                Declaration dj = d.get(j);
                check( ! (di.v.equals(dj.v)),
                       "duplicate declaration: " + dj.v);
            }
    } 

    public static void V (Program p) {
        V (p.decpart);
        V (p.body, typing (p.decpart));
    } 

    public static Type typeOf (Expression e, TypeMap tm) {
        // retrive the type of a variable and return type
        if (e instanceof Value) return ((Value)e).type;
        if (e instanceof Variable) {
            Variable v = (Variable)e;
            check (tm.containsKey(v), "undefined variable: " + v);
            return (Type) tm.get(v);
        }
        if (e instanceof Binary) {
            Binary b = (Binary)e;
            if (b.op.ArithmeticOp( ))
                if (typeOf(b.term1,tm)== Type.FLOAT)
                    return (Type.FLOAT);
                else return (Type.INT);
            if (b.op.RelationalOp( ) || b.op.BooleanOp( )) 
                return (Type.BOOL);
        }
        if (e instanceof Unary) {
            Unary u = (Unary)e;
            if (u.op.NotOp( ))        return (Type.BOOL);
            else if (u.op.NegateOp( )) return typeOf(u.term,tm);
            else if (u.op.intOp( ))    return (Type.INT);
            else if (u.op.floatOp( )) return (Type.FLOAT);
            else if (u.op.charOp( ))  return (Type.CHAR);
        }
        throw new IllegalArgumentException("should never reach here");
    } 

    public static void V (Expression e, TypeMap tm) {
        if (e instanceof Value) 
            return;
        if (e instanceof Variable) { 
            Variable v = (Variable)e;
            check( tm.containsKey(v)
                   , "undeclared variable: " + v);
            return;
        }
        if (e instanceof Binary) {
            Binary b = (Binary) e;
            Type typ1 = typeOf(b.term1, tm);
            Type typ2 = typeOf(b.term2, tm);
            V (b.term1, tm);
            V (b.term2, tm);
            if (b.op.ArithmeticOp( ))  
                check( typ1 == typ2 &&
                       (typ1 == Type.INT || typ1 == Type.FLOAT)
                       , "type error for " + b.op);
            else if (b.op.RelationalOp( )) 
                check( typ1 == typ2 , "type error for " + b.op);
            else if (b.op.BooleanOp( )) 
                check( typ1 == Type.BOOL && typ2 == Type.BOOL,
                       b.op + ": non-bool operand");
            else
                throw new IllegalArgumentException("should never reach here");
            return;
        }
        if (e instanceof Unary) {
            // 단항 연산자인 경우
            Unary u = (Unary)e;
            Type tp = typeOf(u.term, tm);
            V(u.term, tm);

            if (u.op.NegateOp())    // 연산자 - 인 경우
                check((tp == Type.INT || tp == Type.FLOAT), "type error for " + u.op);
            else if (u.op.NotOp())  // 연산자 ! 인 경우
                check((tp == tp.BOOL), "type error for " + u.op);
            else if (u.op.intOp())  // (float|char) -> (int) conversion 만 가능
                check((tp == Type.FLOAT || tp == Type.CHAR), "type conversion error for " + u.op);
            else if (u.op.floatOp())    // (int) -> (float) conversion 만 가능
                check((tp==Type.INT), "type conversion error for " + u.op);
            else if (u.op.charOp())     // (int) -> (char) conversion 만 가능
                check((tp==Type.INT), "type conversion error for " + u.op);
            else
                throw new IllegalArgumentException("should never reach here");
            return;
        }
        // student exercise - Done.
        throw new IllegalArgumentException("should never reach here");
    }

    public static void V (Statement s, TypeMap tm) {
        if ( s == null )
            throw new IllegalArgumentException( "AST error: null statement");
        if (s instanceof Skip) return;  // Skip일 경우 Skip
        if (s instanceof Assignment) {
            // Statement가 Assignment 일 경우
            Assignment a = (Assignment)s;
            check( tm.containsKey(a.target)
                   , " undefined target in assignment: " + a.target);
            V(a.source, tm);
            Type ttype = (Type)tm.get(a.target);
            Type srctype = typeOf(a.source, tm);
            if (ttype != srctype) {
                if (ttype == Type.FLOAT)
                    check( srctype == Type.INT
                           , "mixed mode assignment to " + a.target);
                else if (ttype == Type.INT)
                    check( srctype == Type.CHAR
                           , "mixed mode assignment to " + a.target);
                else
                    check( false
                           , "mixed mode assignment to " + a.target);
            }
            return;
        }
        else if (s instanceof  Block) {
            // Statement가 Block 일 경우
            Block b = (Block) s;
            // recursively type check with function V
            for (Statement m : b.members)
                V(m, tm);
            return;
        }
        else if (s instanceof Conditional) {
            Conditional c = (Conditional) s;
            V(c.test, tm);
            Type tp = typeOf(c.test, tm);
            if (tp == Type.BOOL){   // 조건식이 BOOL type 이라면
                V(c.thenbranch, tm);
                V(c.elsebranch, tm);
            }
            else
                check( false
                        , "type error for " + c.test);
            return;
        }
        else if (s instanceof Loop) {
            Loop l = (Loop) s;
            V(l.test, tm);
            Type tp = typeOf(l.test, tm);
            if (tp == Type.BOOL)    // 조건식이 BOOL type 이라면
                V(l.body, tm);
            else
                check(false, "type error for " + l.test);
            return;
        }
        else
            throw new IllegalArgumentException("should never reach here");
        // student exercise - Done.
    }

    public static void main(String args[]) {
        Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
        prog.display();           // student exercise - Done.
        System.out.println("\nBegin type checking...");
        System.out.println("Type map:");
        TypeMap map = typing(prog.decpart);
        map.display();   // student exercise
        V(prog);
    } //main

} // class StaticTypeCheck

