/*
** TODO: * Fix up exceptions in places to be nicer somehow. Especially
**         for read-related stuffs
**       * eql? needn't be primitive, given more, other, primitives: type? or something
**       * use long more?
**       * maybe solve nil/null-deficiency by making readSymbol return null for symbol nil (instead of the other path to make
**         nil separate from null, which caused java type mayhem)
**       * Compare numbers using a method similar to compareTo? (implement neg? and plus? as subrs and then do the rest in lisp)
**            (defun < (a b) (neg? (- a b))) or similar
**       * Compare numbers implementing just two comparisons (== and < or <= or similar) then bootstrap the rest from there?
**       * Think about lexical scoping... dynamic scoping might be more of a PITA than I thought initially (dynamic wins on
**         ease of implementation... _but_). Lexical might not need be so difficult given passable environments, also nlambdas
**         as a method for recursion would be sort of cute in this case (or do we use the y-combinator? =p)
**       * Think about a procedure abstract class or interface, for all things having something called "apply"
**       * Try later to move away from pure list structure for exprs, instead substituting with a subclass of LispProcedure
**         possibly internally containing the same list structure, this is going to make lexical scoping among other things
**         much smoother (as well as removing serious amounts of clutter from eval)
*/

import java.io.*;
import java.util.*;
import java.math.*;
import java.util.regex.*;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class ljsp {
    public static final Symbol t              = intern("t");
    public static final Symbol standardOutput = intern("*standard-output*");
    public static final Symbol standardInput  = intern("*standard-input*");
    public static final Symbol lambda         = intern("lambda");
    public static final Symbol flambda        = intern("flambda");
    public static final Symbol quote          = intern("quote");
    public static final Symbol _if            = intern("if");
    public static final Symbol macro          = intern("macro");

    // KLUDGE: since we use null to represent nil (silly! it just creates lots of troubles, think before you code ;_;) this is needed everywhere and your car
    public static String toStringOrNull(LispObject obj) { return (obj != null) ? obj.toString() : "nil"; }

    // Here be dragons, heavily optimized stackery
    private static final int STACK_SIZE = 32768*2;
    private static int stackSize = 0;
    private static LispObject[] stack = new LispObject[STACK_SIZE];
    private static final void saveEnvironment() { ++stackSize; } // Basically create a hole separating stack frames
    private static final void restoreEnvironment() {
        --stackSize;
        for (; stack[stackSize] != null; stackSize -= 2) {
            ((Symbol)stack[stackSize]).value = stack[stackSize-1];
            stack[stackSize] = null; stack[stackSize-1] = null; }}
    private static final void bind(Symbol sbl, LispObject value) {
        LispObject oldValue = sbl.value;
        sbl.value = value;
        for (int i = stackSize-1; stack[i] != null; i -= 2) if (stack[i] == sbl) return;      // Avoid creating buried bindings
        stack[stackSize++] = oldValue;
        stack[stackSize++] = sbl; }
    
    // Evaluates a list of expressions and returns a freshly allocated list with the results
    private static final Cons evlis(Cons list) {
        Cons result, last;
        if (list == null) return null;
        result = last = new Cons(evalHead(list.car), null);
        for (Cons c = (Cons)list.cdr; c != null; c = (Cons)c.cdr)
            last = (Cons)(last.cdr = new Cons(evalHead(c.car), null));
        return result; }

    // Like evlis, but returns a freshly allocated java array with the results instead
    private static final LispObject[] evlisArray(Cons list) {
        LispObject[] res = new LispObject[(list == null) ? 0 : list.length()];
        int i = 0;
        for (Cons c = list; c != null; c = (Cons)c.cdr)
            res[i++] = evalHead(c.car);
        return res;
    }
    
    // for evalling stuff not in tail position, saves and restores environment
    private static final LispObject evalHead(LispObject obj) {
        LispObject res;
        saveEnvironment();
        try     { res = eval(obj);      } // The try-finally might have a slight (near-negligable) speed-impact but it is safer
        finally { restoreEnvironment(); }
        return res; }
    
    // TODO: Macros, done, but strangely. Maybe I should just go for fexprs and implement macros with them.
    //         Maybe i should displace macros from java, as an optimization.
    //       Should I make lambdas without any args use less stack? Is it possible?
    // The heart and blood of any interpreter, eval
    // The need for tail call optimization calls for some ugly hacks (which also
    // renders this function near unsplittable, explaining it's hugeness).
    // think of every obj = xxx; continue; sequence as a tail-recursive call to eval(xxx), even though 
    // it might all look like a while loop. This function was more readable before implementing tail call optimization.
    // TL;DR: It doesn't look pretty, but it gets the job done....
    // Also: there is pseudo-lisp commentary, since all the casting makes the java-code messylicous,
    //       but hard to avoid when implementing a dynamically typed language in a statically typed one.
    public static final LispObject eval(LispObject obj) {
        while (true) {                                     
            if (obj instanceof Symbol)
                return ((Symbol)obj).value;
            else if (obj instanceof Cons) {
                Cons list = (Cons)obj;
                if (list.car == _if) {
                    LispObject res = evalHead(((Cons)list.cdr).car); // (eval-head (cadr list))
                    if (res != null) {
                        obj = ((Cons)((Cons)list.cdr).cdr).car; continue; }             // (eval-tail (caddr list))
                    else if (((Cons)((Cons)list.cdr).cdr).cdr != null) {                // (cddr list)
                        obj = ((Cons)((Cons)((Cons)list.cdr).cdr).cdr).car; continue; } // (eval-tail (cadddr list))
                    else return null; }
                else if (list.car == quote)
                    return ((Cons)list.cdr).car;                // (cadr list)
                else if (list.car == lambda || list.car == flambda || list.car == macro)
                    return list;                                // Lambdas, flambdas and macros are self-quoting 
                else { /* just brace for it (apply fn to args) */
                    LispObject first = evalHead(list.car);
                    if (first instanceof Cons) {
                        Cons f1rst = (Cons)first;               // Java's being stupid, not letting me reuse the identifier "first"
                        if (f1rst.car == lambda || f1rst.car == flambda) {
                            Cons lambdaVars = (Cons)((Cons)f1rst.cdr).car; // (cadr f1rst)
                            Cons lambdaBody = (Cons)((Cons)f1rst.cdr).cdr; // (cddr f1rst)
                            Cons argList    = (Cons)list.cdr;              // (cdr list)
                            if (lambdaVars != null) {                      // lambda expects variables, this is the hairy part
                                // When lambdaVars.car == null we are only interested in rest-param, thus no args is ok.
                                if (argList == null && lambdaVars.car != null) throw new RuntimeException("Too few args (zero in fact): " + obj);
                                Cons evalledArgs = (f1rst.car != flambda) ? evlis(argList) : argList; // Fexprs get a reference 
                                if (lambdaVars.car == null) // null car of varlist means we _only_ want rest-parameter
                                    bind((Symbol)lambdaVars.cdr, evalledArgs);
                                else
                                    for (Cons c = lambdaVars;; c = (Cons)c.cdr) {
                                        if (c.cdr == null) {
                                            if (evalledArgs.cdr != null) throw new RuntimeException("Too many args: " + obj);
                                            bind((Symbol)c.car, evalledArgs.car);
                                            break; }
                                        if (!(c.cdr instanceof Cons)) { // rest-parameter
                                            bind((Symbol)c.car, evalledArgs.car);
                                            bind((Symbol)c.cdr, evalledArgs.cdr);
                                            break; }
                                        bind((Symbol)c.car, evalledArgs.car);
                                        evalledArgs = (Cons)evalledArgs.cdr;
                                        if (evalledArgs == null) throw new RuntimeException("Too few args: " + obj); }} // Phew... hairy...
                            if (lambdaBody == null) return null;                                                        // I've no body
                            for (; lambdaBody.cdr != null; lambdaBody = (Cons)lambdaBody.cdr) evalHead(lambdaBody.car); // Eval body sequentially
                            obj = lambdaBody.car; continue; /* (eval-tail (car lambda-body)) */ } /* you got all that? */
                        else if (f1rst.car == macro) { // KLUDGE: kinda strange implementation of macro, huh?
                            // (eval-tail (eval-head `((lambda ,@(cdr f1rst)) ',list)))
                            // (eval-tail (eval-head (list (cons 'lambda (cdr f1rst)) (list 'quote list)))
                            obj = evalHead(cons(cons(lambda, f1rst.cdr), cons(cons(quote, cons(list, null)), null))); continue; }
                        else
                            throw new RuntimeException("You can't just pretend lists to be functions, when they aren't: " + obj.toString()); }
                    else if (first instanceof LispSubr) 
                        // (apply first (evlis-array (cdr list)))
                        return ((LispSubr)first).apply(evlisArray((Cons)list.cdr));
                    else
                        throw new RuntimeException("Dina fiskar är dåliga: " + toStringOrNull(obj)); }}
            else
                return obj; }}
    public static LispObject print(LispObject obj, LispStream stream) {
        LispStream s = (stream != null) ? stream : (LispStream)standardOutput.value;
        if (obj != null) obj.printObject(s);
        else             s.writeJavaString("nil");          // Due to the funnyness of null as nil
        s.terpri();
        return obj; }
    public  static Cons       cons(LispObject car, LispObject cdr)       { return new Cons(car, cdr);                                                   }
    public  static LispObject car(Cons list)                             { return (list == null) ? null : list.car;                                     }
    public  static LispObject cdr(Cons list)                             { return (list == null) ? null : list.cdr;                                     }
    private static Symbol     intern(String str)                         { return (new Symbol(str)).intern();                                           }
    public  static LispObject read(LispStream stream) throws IOException { return ((stream != null) ? stream : (LispStream)standardInput.value).read(); }
    public  static LispFixnum readChar(LispStream stream) throws IOException {
        return new LispFixnum(((stream != null) ? stream : (LispStream)standardInput.value).readJavaChar()); }
    public  static LispInteger writeChar(LispInteger ch, LispStream stream) throws IOException {
        (stream != null ? stream : (LispStream)standardOutput.value).writeJavaChar((char)ch.toJavaInt()); return ch; }
    public  static LispObject eq (LispObject obj1, LispObject obj2)      { return obj1 == obj2 ? t : null;                                              }
    public  static Cons       symbols()                                  { return Symbol.getSymbols();                                                  }
    public  static LispObject symbolValue(Symbol sbl)                    { return sbl.value;                                                            }
    public  static LispObject atom(LispObject obj)                       { return (obj instanceof Cons) ? null : t;                                     }
    private static long genSymCounter = 0;
    public  static LispObject gensym()                                   { return new Symbol("G" + genSymCounter++);                                    }
    public  static LispObject eql(LispObject a, LispObject b)            { return (a == null || b == null)    ? eq(a, b) : 
                                                                                  !a.getClass().isInstance(b) ? null :
                                                                                  (a instanceof LispNumber)   ? (((LispNumber)a).equals((LispNumber)b) ? t : null) :
                                                                                                                eq(a, b); }
    // Gives me everything I need to bootstrap my lisp
    public static void initEnvironment() {
        t.value = t;
        
        standardOutput.value = new LispStream(null     , System.out);
        standardInput.value  = new LispStream(System.in, null);

        // Omnomnom redundancy
        intern("cons").value         = new LispSubr("cons", 2)         { public LispObject subr (LispObject[] o) { return cons(o[0], o[1]);                                    }};
        intern("car").value          = new LispSubr("car", 1)          { public LispObject subr (LispObject[] o) { return car((Cons)o[0]);                                     }};
        intern("cdr").value          = new LispSubr("cdr", 1)          { public LispObject subr (LispObject[] o) { return cdr((Cons)o[0]);                                     }};
        intern("rplaca").value       = new LispSubr("rplaca", 2)       { public LispObject subr (LispObject[] o) { ((Cons)o[0]).car = o[1]; return o[0];                       }};
        intern("rplacd").value       = new LispSubr("rplacd", 2)       { public LispObject subr (LispObject[] o) { ((Cons)o[0]).cdr = o[1]; return o[0];                       }};
        intern("print").value        = new LispSubr("print", 1, 2)     { public LispObject subr (LispObject[] o) { return print(o[0],(o.length>1)?(LispStream)o[1]:null);      }};
        intern("eq?").value          = new LispSubr("eq?", 2)          { public LispObject subr (LispObject[] o) { return eq(o[0], o[1]);                                      }};
        intern("atom?").value        = new LispSubr("atom?", 1)        { public LispObject subr (LispObject[] o) { return atom(o[0]);                                          }};
        intern("set").value          = new LispSubr("set", 2)          { public LispObject subr (LispObject[] o) { return ((Symbol)o[0]).value = o[1];                         }};
        intern("eval").value         = new LispSubr("eval", 1)         { public LispObject subr (LispObject[] o) { return eval(o[0]);                                          }};
        intern("symbols").value      = new LispSubr("symbols", 0)      { public LispObject subr (LispObject[] o) { return symbols();                                           }};
        intern("symbol-value").value = new LispSubr("symbol-value", 1) { public LispObject subr (LispObject[] o) { return symbolValue((Symbol)o[0]);                           }};
        intern("gensym").value       = new LispSubr("gensym", 0)       { public LispObject subr (LispObject[] o) { return gensym();                                            }};
        intern("+").value            = new LispSubr("+", 2)            { public LispObject subr (LispObject[] o) { return ((LispNumber)o[0]).add((LispNumber)o[1]);            }};
        intern("-").value            = new LispSubr("-", 2)            { public LispObject subr (LispObject[] o) { return ((LispNumber)o[0]).sub((LispNumber)o[1]);            }};
        intern("*").value            = new LispSubr("*", 2)            { public LispObject subr (LispObject[] o) { return ((LispNumber)o[0]).mul((LispNumber)o[1]);            }};
        intern("/").value            = new LispSubr("/", 2)            { public LispObject subr (LispObject[] o) { return ((LispNumber)o[0]).div((LispNumber)o[1]);            }};
        intern("mod").value          = new LispSubr("mod", 2)          { public LispObject subr (LispObject[] o) { return ((LispInteger)o[0]).mod((LispInteger)o[1]);          }};
        intern("ash").value          = new LispSubr("ash", 2)          { public LispObject subr (LispObject[] o) { return ((LispInteger)o[0]).ash((LispInteger)o[1]);          }};
        intern("eql?").value         = new LispSubr("eql?", 2)         { public LispObject subr (LispObject[] o) { return eql(o[0], o[1]);                                     }};
        intern("=").value            = new LispSubr("=", 2)            { public LispObject subr (LispObject[] o) { return ((LispNumber)o[0]).equals((LispNumber)o[1])?t:null;  }};
        intern("exit").value         = new LispSubr("exit", 0, 1)      { public LispObject subr (LispObject[] o) {
            System.exit((o.length < 1) ? 0 : ((LispNumber)o[0]).toJavaInt()); return null; }};
        intern("get-time").value     = new LispSubr("get-time", 0)     { public LispObject subr (LispObject[] o) { return new LispFixnum(System.currentTimeMillis());          }};
        intern("read-char").value    = new LispSubr("read-char", 0, 1) { public LispObject subr (LispObject[] o) {
            try                   { return readChar((o.length > 0) ? (LispStream)o[0] : null); }
            catch (IOException e) { throw new RuntimeException("An IOException just occured to me, " + this.toString()); }}};
        intern("write-char").value   = new LispSubr("write-char", 1, 2){ public LispObject subr (LispObject[] o) {
            try                   { return writeChar((LispInteger)o[0], ((o.length > 1) ? (LispStream)o[1] : null));     }
            catch (IOException e) { throw new RuntimeException("An IOException just occured to me, " + this.toString()); }}}; 
        intern("read").value         = new LispSubr("read", 0, 1)      { public LispObject subr (LispObject[] o) {
            try                   { return read((o.length > 0) ? (LispStream)o[0] : null); }
            catch (IOException e) { throw new RuntimeException("An IOException just ocurred to me, " + this.toString()); }}}; }

    public static void main(String args[]) {
        initEnvironment();
        LispObject wir = null;
        while (true)                                        // REPL, with some wrapping...
            try { while (true) { print(evalHead(wir = read(null)), null); wir = null; }}
            catch (IOException e) { System.out.println("Caught an exceptional IOException: " + e +
                                                       "\nWhile passing by: " + ljsp.toStringOrNull(wir)); }
            catch (RuntimeException e) { System.out.println("\u0007*** Caught an exceptional exception: " + e +
                                                            "\nWhile passing by: " + ljsp.toStringOrNull(wir) + 
                                                            "\nrestarting REPL..."); }}}

class LispObject { public void printObject(LispStream stream) { stream.writeJavaString(this.toString()); }}

abstract class LispSubr extends LispObject {
    private String name;
    private int minArgs, maxArgs;
    public LispSubr(String name) { this(name, 0, Integer.MAX_VALUE); } // FIXME: Don't know if this constructor will see much use
    public LispSubr(String name, int numArgs) { this(name, numArgs, numArgs); }
    public LispSubr(String name, int minArgs, int maxArgs) { this.name = name; this.minArgs = minArgs; this.maxArgs = maxArgs; }
    public final LispObject apply(LispObject[] o) {
        if (o.length < minArgs) throw new RuntimeException("Too few args when calling subr: "  + name);
        if (o.length > maxArgs) throw new RuntimeException("Too many args when calling subr: " + name);
        return subr(o); }
    public abstract LispObject subr(LispObject[] objects);
    public String toString() { return "#<subr " + name + ">"; }}

class Cons extends LispObject {
    public LispObject car;
    public LispObject cdr;

    public Cons(LispObject car, LispObject cdr) { this.car = car; this.cdr = cdr; }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (Cons list = this;; list = (Cons)list.cdr)
            if        (list.cdr == null)          { sb.append(ljsp.toStringOrNull(list.car)); break; }
            else if (!(list.cdr instanceof Cons)) { sb.append(ljsp.toStringOrNull(list.car)).append(" . ").append(list.cdr.toString()); break; } // Handle dotted lists
            else                                  sb.append(ljsp.toStringOrNull(list.car)).append(" "); 
        sb.append(")");
        return sb.toString(); }

    public int length() { int i = 0; for (Cons c = this; c != null; c = (Cons)c.cdr) ++i; return i; } // TODO: Use long here?
}

// TODO: implement symbol table as a Map of some sort for speedier interning?
class Symbol extends LispObject {
    private static Cons symbols = null;
    public static Cons getSymbols() { return symbols; }

    public static Symbol findSymbol(String str) { return findSymbol(str, symbols); }
    private static Symbol findSymbol(String str, Cons list) { // FIXME: Make a loop out of this recursion
        if (list == null)                            return null;
        else if (str.equals(((Symbol)list.car).str)) return (Symbol)list.car;
        else                                         return findSymbol(str, (Cons)list.cdr); }

    public LispObject value;
    private String str;
    private boolean interned;

    public String getStr() { return this.str; }

    public Symbol intern() {
        if (this.interned) return this;

        Symbol sbl;
        if ((sbl = findSymbol(this.str)) == null) {
            symbols = new Cons(this, symbols);
            this.interned = true;
            return this; }
        else
            return sbl; }

    public Symbol(String str) { this.str = str; this.interned = false; }

    public String toString() { return this.interned ? this.str : "#:" + this.str; }}

// Numerical tower, a.k.a. royal pain to implement nicely, multimethods are not native to java.
abstract class LispNumber extends LispObject {
    public static final Pattern REGEX = Pattern.compile("^[+-]?\\d*\\.?(?:\\d+e)?\\d+$");
    public static final boolean javaStringMatchesLispNumber(String str) { return REGEX.matcher(str).matches(); }
    
    protected static final LispBignum coerceFixnumToBignum(LispNumber nbr) { return new LispBignum(((LispFixnum)nbr).toJavaLong()); }
    protected static final LispFlonum coerceIntegerToFlonum(LispNumber nbr) { return new LispFlonum(((LispInteger)nbr).toJavaDouble()); }
    
    public static LispNumber parse(String str) {
        try { return LispFixnum.parse(str); }              // KLUDGE: Will you dance the try-catch dance with me?
        catch (NumberFormatException e) {
            try { return LispBignum.parse(str); }
            catch (NumberFormatException ee) {
                return LispFlonum.parse(str); }}}
    
    public abstract LispNumber add(LispNumber n);
    public abstract LispNumber sub(LispNumber n);
    public abstract LispNumber mul(LispNumber n);
    public abstract LispNumber div(LispNumber n);

    public abstract boolean equals(LispNumber n);

    public abstract int    toJavaInt();                     // FIXME: Kind of redundant
    public abstract long   toJavaLong();
    public abstract float  toJavaFloat();                   // FIXME: Kind of redundant
    public abstract double toJavaDouble(); }

final class LispFlonum extends LispNumber {
    private double n; 
    public static LispNumber parse(String str) { return new LispFlonum(Double.parseDouble(str)); }
    public LispFlonum(double nbr) { n = nbr; }

    public LispFlonum add(LispFlonum nbr) { return new LispFlonum(n + nbr.n); }
    public LispFlonum sub(LispFlonum nbr) { return new LispFlonum(n - nbr.n); }
    public LispFlonum mul(LispFlonum nbr) { return new LispFlonum(n * nbr.n); }
    public LispFlonum div(LispFlonum nbr) { return new LispFlonum(n / nbr.n); }

    public boolean equals(LispNumber nbr) { return (nbr instanceof LispInteger) ? n == nbr.toJavaDouble() : n == ((LispFlonum)nbr).n; }
    public LispFlonum add(LispNumber nbr) { return (nbr instanceof LispInteger) ? add(coerceIntegerToFlonum(nbr)) : add((LispFlonum)nbr); }
    public LispFlonum sub(LispNumber nbr) { return (nbr instanceof LispInteger) ? sub(coerceIntegerToFlonum(nbr)) : sub((LispFlonum)nbr); }
    public LispFlonum mul(LispNumber nbr) { return (nbr instanceof LispInteger) ? mul(coerceIntegerToFlonum(nbr)) : mul((LispFlonum)nbr); }
    public LispFlonum div(LispNumber nbr) { return (nbr instanceof LispInteger) ? div(coerceIntegerToFlonum(nbr)) : div((LispFlonum)nbr); }

    public String toString()     { return "" + n; }
    public int    toJavaInt()    { return (int)n; }
    public long   toJavaLong()   { return (long)n; }
    public float  toJavaFloat()  { return (float)n; }
    public double toJavaDouble() { return (double)n; }}

abstract class LispInteger extends LispNumber {
    public static LispInteger parse(String str) { return LispFixnum.parse(str); } // TODO: be smart choose proper subclass

    public abstract LispInteger mod(LispInteger n); 
    public abstract LispInteger ash(LispInteger n); }

final class LispBignum extends LispInteger {
    private BigInteger n;

    public static LispBignum parse(String str) { return new LispBignum(new BigInteger(str)); }
    public LispBignum(BigInteger nbr) { n = nbr;                     }
    public LispBignum(long nbr)       { n = BigInteger.valueOf(nbr); }
    public LispBignum(int nbr)        { n = BigInteger.valueOf((long)nbr); }
    public LispBignum add(LispBignum nbr) { return new LispBignum(n.add(nbr.n)); }
    public LispBignum sub(LispBignum nbr) { return new LispBignum(n.subtract(nbr.n)); }
    public LispBignum mul(LispBignum nbr) { return new LispBignum(n.multiply(nbr.n)); }
    public LispNumber div(LispBignum nbr) { return new LispBignum(n.divide(nbr.n)); } // TODO : RATIONALS
    public LispBignum mod(LispBignum nbr) { return new LispBignum(n.remainder(nbr.n)); }
    
    public LispNumber add(LispNumber nbr) {
        return (nbr instanceof LispFlonum) ? (new LispFlonum(n.doubleValue())).add((LispFlonum)nbr) : 
               (nbr instanceof LispFixnum) ? add(coerceFixnumToBignum(nbr)) :
                                             add((LispBignum)nbr); }
    public LispNumber sub(LispNumber nbr) {
        return (nbr instanceof LispFlonum) ? (new LispFlonum(n.doubleValue())).sub((LispFlonum)nbr) : 
               (nbr instanceof LispFixnum) ? sub(coerceFixnumToBignum(nbr)) :
                                             sub((LispBignum)nbr); }
    public LispNumber mul(LispNumber nbr) {
        return (nbr instanceof LispFlonum) ? (new LispFlonum(n.doubleValue())).mul((LispFlonum)nbr) : 
               (nbr instanceof LispFixnum) ? mul(coerceFixnumToBignum(nbr)) :
                                             mul((LispBignum)nbr); }
    public LispNumber div(LispNumber nbr) {
        return (nbr instanceof LispFlonum) ? (new LispFlonum(n.doubleValue())).div((LispFlonum)nbr) : 
               (nbr instanceof LispFixnum) ? div(coerceFixnumToBignum(nbr)) :
                                             div((LispBignum)nbr); }
    public LispInteger mod(LispInteger nbr) { return (nbr instanceof LispFixnum) ? mod(coerceFixnumToBignum(nbr)) : mod((LispBignum)nbr); }
    public LispInteger ash(LispInteger nbr) { return new LispBignum(n.shiftLeft(nbr.toJavaInt())); } // FIXME: Only well-defined between -2^31 and 2^31-1 inclusive
    public boolean equals(LispNumber nbr) { return (nbr instanceof LispFixnum) ? equals(coerceFixnumToBignum((LispFixnum)nbr)) :
                                                   (nbr instanceof LispFlonum) ? nbr.equals(this) :
                                                                                 n.equals(((LispBignum)nbr).n); }

    public String toString()     { return n.toString() + "BN"; } // TODO: Remove the BN part when you feel like it
    public int    toJavaInt()    { return n.intValue(); }
    public long   toJavaLong()   { return n.longValue(); }
    public float  toJavaFloat()  { return n.floatValue(); }
    public double toJavaDouble() { return n.doubleValue(); }
    public BigInteger toJavaBigInteger() { return n; }}

final class LispFixnum extends LispInteger {
    private final long n;

    public static LispFixnum parse(String str) { return new LispFixnum(Long.parseLong(str)); }
    public LispFixnum(long nbr) { n = nbr; }
    
    public LispInteger add(LispFixnum nbr) {
        LispFixnum res = new LispFixnum(n + nbr.n);
        if (((this.n^res.n) & (nbr.n^res.n)) < 0)                  // Check overflow
            return (new LispBignum(n)).add(new LispBignum(nbr.n)); // Redo addition with bignums and return
        return res; }
    public LispInteger sub(LispFixnum nbr) {
        LispFixnum res = new LispFixnum(n - nbr.n);
        if (((this.n^res.n) & (-nbr.n^res.n)) < 0)          // Check overflow 
            return (new LispBignum(n)).sub(new LispBignum(nbr.n)); 
        return res; }
    public LispInteger mul(LispFixnum nbr) {
        // If nlz(x) + nlz(~x) + nlz(y) + nlz(~y) < 65 multiplication _might_ overflow
        if (Long.numberOfLeadingZeros(Math.abs(n)) + Long.numberOfLeadingZeros(Math.abs(nbr.n)) < 65) 
            return (new LispBignum(n)).mul(new LispBignum(nbr.n)); 
        return new LispFixnum(n * nbr.n); }
    public LispNumber div(LispFixnum nbr) { return new LispFixnum(n / nbr.n); } // TODO: RATIONAAAALS? (and overflow for that matter)
    public LispInteger mod(LispFixnum nbr) { return new LispFixnum(n % nbr.n); } // Can impossibly overflow?
    public LispInteger ash(LispFixnum nbr) { return new LispFixnum((nbr.n > 0) ? n << nbr.n : n >> -nbr.n); }     // TODO: overflow left

    public LispNumber add(LispNumber nbr) {
        return (nbr instanceof LispBignum) ? (new LispBignum(n)).add((LispBignum)nbr) :
               (nbr instanceof LispFlonum) ? (new LispFlonum((double)n)).add((LispFlonum)nbr) :
                                             add((LispFixnum)nbr); }
    public LispNumber sub(LispNumber nbr) {
        return (nbr instanceof LispBignum) ? (new LispBignum(n)).sub((LispBignum)nbr) :
               (nbr instanceof LispFlonum) ? (new LispFlonum((double)n)).sub((LispFlonum)nbr) :
                                              sub((LispFixnum)nbr); }
    public LispNumber mul(LispNumber nbr) {
        return (nbr instanceof LispBignum) ? (new LispBignum(n)).mul((LispBignum)nbr) :
               (nbr instanceof LispFlonum) ? (new LispFlonum((double)n).mul((LispFlonum)nbr)) : 
                                             mul((LispFixnum)nbr); }
    public LispNumber div(LispNumber nbr) {
        return (nbr instanceof LispBignum) ? (new LispBignum(n)).div((LispBignum)nbr) :
               (nbr instanceof LispFlonum) ? (new LispFlonum((double)n)).div((LispFlonum)nbr) : 
                                             div((LispFixnum)nbr); }
    public LispInteger mod(LispInteger nbr) {
        return (nbr instanceof LispBignum) ? (new LispBignum(n)).mod((LispBignum)nbr) :
                                             mod((LispFixnum)nbr); }
    public LispInteger ash(LispInteger nbr) { return ash((LispFixnum)nbr); } 
    public boolean equals(LispNumber nbr) { return (nbr instanceof LispFixnum) ? n == ((LispFixnum)nbr).n : nbr.equals(this); }
    
    public String toString()     { return "" + n; }
    public int    toJavaInt()    { return (int)n;  }
    public long   toJavaLong()   { return (long)n; }
    public float  toJavaFloat()  { return (float)n; }
    public double toJavaDouble() { return (double)n; }}

// class LispRatio extends LispNumber
// {
// }

// Stream
class LispStream extends LispObject {
    private BufferedReader inputStream;
    private PrintStream outputStream;
    private Stack<Character> pushbackStack;

    public LispStream(InputStream inputStream, OutputStream outputStream) {
        try { this.inputStream  = (inputStream  != null) ? new BufferedReader(new InputStreamReader(inputStream, "UTF-8")) : null; } catch (UnsupportedEncodingException e) {}
        this.outputStream = (outputStream != null) ? new PrintStream(outputStream) : null;
        if (inputStreamP()) pushbackStack = new Stack<Character>(); }

    public void writeJavaString(String str) { outputStream.print(str); } // Throws NullPointerException when not outputstream FIXME?
    public void terpri()                    { outputStream.println();  } // Throws NullPointerException when not outputstream FIXME?

    public void writeJavaChar(char ch) throws IOException { outputStream.print(ch); }
    public char readJavaChar() throws IOException {          // Throws NullPointerException when not inputstream FIXME?
        if (pushbackStack.empty()) return (char)inputStream.read();
        else                       return pushbackStack.pop(); }
    public char peekJavaChar() throws IOException {           // Throws NullPointerException when not inputstream FIXME?
        if (pushbackStack.empty()) return pushbackStack.push((char)inputStream.read());
        else                       return pushbackStack.peek(); }
    public void unreadJavaChar(char ch) { pushbackStack.push(ch); } // Throws NullPointerException when not inputstream FIXME?
    public void skipWhiteSpaceAndComments() throws IOException {
        char tmp = readJavaChar();
        while (Character.isWhitespace(tmp) || tmp == ';') {
            if (tmp == ';') // if we find ; discard everything to, and with, newline
                while (readJavaChar() != '\n');
            tmp = readJavaChar(); }
        unreadJavaChar(tmp); }
    
    public boolean inputStreamP()  { return (inputStream  != null) ? true : false; }
    public boolean outputStreamP() { return (outputStream != null) ? true : false; }

    // TODO: Make reader(s) Better(TM)... maybe...
    // FIXME: Superfluous ending paren causes infinite loop

    // Messssssssy, who'da thunk reading lists would be so messy
    private Cons readList() throws IOException {
        Cons list, last;
        this.readJavaChar();                                // Discard one character (should be '(')
        this.skipWhiteSpaceAndComments();
        char ch = this.peekJavaChar();
        if (ch == ')') { // Empty list
            this.readJavaChar();                            // Discard ')'
            return null; }
        // First iteration of loop is wierd, and thus unrolled
        list = last = new Cons(this.read(), null); 
        this.skipWhiteSpaceAndComments();
        ch = this.peekJavaChar();
        while (ch != ')') {
            if (ch == '.') { // Handle dotted lists, wee!
                this.readJavaChar();                        // Discard '.'
                this.skipWhiteSpaceAndComments();
                ch = this.peekJavaChar();
                if (ch == ')') throw new RuntimeException("You now have me confuzzled, don't you want something after the dot?");
                last.cdr = this.read();
                this.skipWhiteSpaceAndComments();
                if (this.peekJavaChar() != ')') throw new RuntimeException("You just might want to end the list with parentheses, even though you're a prick.");
                break; }
            last = (Cons)(last.cdr = new Cons(this.read(), null));
            this.skipWhiteSpaceAndComments();
            ch = this.peekJavaChar(); }
        this.readJavaChar();                                // Discard ')'
        return list; }

    private Symbol readQuotedSymbol() throws IOException {
        StringBuilder sb = new StringBuilder();
        char ch;
        this.readJavaChar();                                // Discard '|'
        while ((ch = this.readJavaChar()) != '|') sb.append(ch);
        return sb.toString().equals("nil") ? null : new Symbol(sb.toString()).intern(); }

    private Cons readQuote() throws IOException { this.readJavaChar(); return new Cons(ljsp.quote, new Cons(this.read(), null)); }

    private LispObject dispatchFence() throws IOException {
        this.readJavaChar();
        if (this.readJavaChar() == ';') this.read();        // Commment out a sexp
        else throw new RuntimeException("Syntax Errol: dispatchFence()"); // FIXME: wierd behaviour when we hit this branch i believe
        return this.read(); }

    // FIXME: Things starting with numbers but not qualifying as numbers should be read as symbols
    //        superfluous ending paren results in endless loop
    public LispObject read() throws IOException {
        if (!this.inputStreamP()) throw new RuntimeException("You can't read what you can't read man, get over it.");

        this.skipWhiteSpaceAndComments();
        char ch = this.peekJavaChar();
        switch (ch) {
            case ')':
                this.readJavaChar();                        // Discard the lonely brace
                throw new RuntimeException("Lonely ending brace");
            case '.':
                this.readJavaChar();                        // Discard the stray .
                throw new RuntimeException("Stray dot");
            case '#':
                return this.dispatchFence();
            case '(':
                return this.readList();
            case '\'':
                return this.readQuote();
            case '|':
                return this.readQuotedSymbol();
            default:                                        // An atom, (well ||-style symbols are atoms too)
                StringBuilder sb = new StringBuilder();
                for (ch = this.readJavaChar();
                     !Character.isWhitespace(ch) && ch != '(' && ch != ')';
                     ch = this.readJavaChar())
                    sb.append(ch);
                this.unreadJavaChar(ch);
                String str = sb.toString();
                if (LispNumber.javaStringMatchesLispNumber(str))          // Is a number
                    return LispNumber.parse(str);
                else // Is a symbol: Funnyness since nil not separated from java null (early bad decision)
                    return str.equals("nil") ? null : new Symbol(str).intern(); }}}
