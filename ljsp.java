/*
** TODO: * Fix up exceptions in places to be nicer somehow. Especially
**         for read-related stuffs
**       * Think about lexical scoping... dynamic scoping might be more of a PITA than I thought initially (dynamic wins on
**         ease of implementation... _but_). Lexical might not need be so difficult given passable environments, also nlambdas
**         as a method for recursion would be sort of cute in this case (or do we use the y-combinator? =p)
**       * Think about a procedure abstract class or interface, for all things having something called "apply"
**       * Try later to move away from pure list structure for exprs, instead substituting with a subclass of Procedure
**         possibly internally containing the same list structure, this is going to make lexical scoping among other things
**         much smoother (as well as removing serious amounts of clutter from eval)
**       * Fix up EOF-handling
**       * Fix up equals for LispNumbers and more
*/

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.math.*;
import java.util.regex.*;
import java.awt.event.*;

public final class ljsp {
    public  static final Symbol t              = intern("t");
    public  static final Symbol standardOutput = intern("*standard-output*");
    public  static final Symbol standardInput  = intern("*standard-input*");
    public  static final Symbol standardError  = intern("*standard-error*");
    public  static final Symbol lambda         = intern("lambda");
    public  static final Symbol quote          = intern("quote");
    public  static final Symbol _if            = intern("if");
    public  static final Symbol macro          = intern("macro");
    public  static final Symbol internalError  = intern("internal-error");
    private static final Symbol in             = intern("in");
    private static final Symbol out            = intern("out");

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
    private static final void bind(Symbol sbl, LispObject value) { // Bind a variable to a value. Save old value on stack.
        LispObject oldValue = sbl.value;
        sbl.value = value;
        for (int i = stackSize-1; stack[i] != null; i -= 2) if (stack[i] == sbl) return;      // Avoid creating buried bindings
        stack[stackSize++] = oldValue;
        stack[stackSize++] = sbl; }
    
    /* Evaluates a list of expressions and returns a freshly allocated list with the results */
    private static final Cons evlis(Cons list) {
        Cons result, last;
        if (list == null) return null;
        result = last = new Cons(evalHead(list.car), null);
        for (Cons c = (Cons)list.cdr; c != null; c = (Cons)c.cdr)
            last = (Cons)(last.cdr = new Cons(evalHead(c.car), null));
        return result; }

    /* Like evlis, but returns a freshly allocated java array with the results instead */
    private static final LispObject[] evlisArray(Cons list) {
        LispObject[] res = new LispObject[(list == null) ? 0 : list.length()];
        int i = 0;
        for (Cons c = list; c != null; c = (Cons)c.cdr)
            res[i++] = evalHead(c.car);
        return res; }
    
    /* For evalling stuff not in tail position, saves and restores environment */
    private static final LispObject evalHead(LispObject obj) {
        LispObject res;
        saveEnvironment();
        try     { res = evalTail(obj);      } // The try-finally might have a slight (near-negligable) speed-impact but it is safer
        finally { restoreEnvironment(); }
        return res; }
    
    /** Evaluate code in the current dynamic environment */
    public static final LispObject eval(LispObject obj) { return evalHead(obj); } // To the outside world we're known as but eval
    
    /* The heart and blood of any interpreter, eval
       The need for Tail Call Optimization, TCO, calls for some ugly hacks (which also
       renders this function near unsplittable, explaining it's hugeness).
       think of every obj = xxx; continue; sequence as a tail-recursive call to eval(xxx), even though 
       it might all look like a while loop. This function was more readable before implementing tail call optimization.
       TL;DR: It doesn't look pretty, but it gets the job done....
       Also: there is pseudo-lisp commentary, since all the casting makes the java-code messylicous,
             but hard to avoid when implementing a dynamically typed language in a statically typed one. */
    private static final LispObject evalTail(LispObject obj) {
        while (true) {                                     
            if (obj instanceof Symbol)
                return ((Symbol)obj).value;
            else if (obj instanceof Cons) {
                Cons list = (Cons)obj;
                if (list.car == _if) {    // TODO: check if there is something after else-clause and explode if that is the case
                    LispObject res = evalHead(((Cons)list.cdr).car); // (eval-head (cadr list))
                    if (res != null) {
                        obj = ((Cons)((Cons)list.cdr).cdr).car; continue; }             // (eval-tail (caddr list))
                    else if (((Cons)((Cons)list.cdr).cdr).cdr != null) {                // (cdddr list)
                        obj = ((Cons)((Cons)((Cons)list.cdr).cdr).cdr).car; continue; } // (eval-tail (cadddr list))
                    else return null; }
                else if (list.car == quote)
                    return ((Cons)list.cdr).car;            // (cadr list)
                else if (list.car == lambda || list.car == macro)
                    return list;                            // Lambdas and macros are self-quoting (Here we would also bind environment if lexical scoping)
                else { // Just brace for it, apply function to arguments
                    LispObject first = evalHead(list.car);
                    if (first instanceof Cons) {
                        Cons f1rst = (Cons)first;           // Java's being stupid, not letting me reuse the identifier "first"
                        if (f1rst.car == lambda) {
                            LispObject lambdaVar = ((Cons)f1rst.cdr).car;  // (cadr f1rst)
                            Cons lambdaBody = (Cons)((Cons)f1rst.cdr).cdr; // (cddr f1rst)
                            Cons argList    = (Cons)list.cdr;              // (cdr list)
                            if (lambdaVar != null) {                       // lambda expects variables, this is the hairy part
                                // When lambdaVar instanceof Symbol we are only interested in rest-param, thus no args is ok.
                                if (argList == null && lambdaVar instanceof Cons) throw new LispException(internalError, "Too few args (zero in fact): " + obj);
                                Cons evalledArgs = evlis(argList); // Eval the arguments
                                if (lambdaVar instanceof Symbol)   // null car of varlist means we _only_ want rest-parameter
                                    bind((Symbol)lambdaVar, evalledArgs);
                                else 
                                    for (Cons c = (Cons)lambdaVar;; c = (Cons)c.cdr) {
                                        if (c.cdr == null) {
                                            if (evalledArgs.cdr != null) throw new LispException(internalError, "Too many args: " + obj);
                                            bind((Symbol)c.car, evalledArgs.car);
                                            break; }
                                        if (!(c.cdr instanceof Cons)) { // rest-parameter
                                            bind((Symbol)c.car, evalledArgs.car);
                                            bind((Symbol)c.cdr, evalledArgs.cdr);
                                            break; }
                                        bind((Symbol)c.car, evalledArgs.car);
                                        evalledArgs = (Cons)evalledArgs.cdr;
                                        if (evalledArgs == null) throw new LispException(internalError, "Too few args: " + obj); }} // Phew... hairy...
                            if (lambdaBody == null) return null;                                                        // I've no body
                            for (; lambdaBody.cdr != null; lambdaBody = (Cons)lambdaBody.cdr) evalHead(lambdaBody.car); // Eval body sequentially, leave last form for TCO
                            obj = lambdaBody.car; continue; } /* (eval-tail (car lambda-body)) */  /* you got all that? */
                        else if (f1rst.car == macro) { // KLUDGE: kinda strange implementation of macro, huh?
                            // (eval-tail (eval-head `((lambda ,@(cdr f1rst)) ',list)))
                            // (eval-tail (eval-head (list (cons 'lambda (cdr f1rst)) (list 'quote list)))
                            obj = evalHead(cons(cons(lambda, f1rst.cdr), cons(cons(quote, cons(list, null)), null))); continue; }
                        else
                            throw new LispException(internalError, "You can't just pretend lists to be functions, when they aren't: " + obj.toString()); }
                    else if (first instanceof Procedure) 
                        // (apply first (evlis-array (cdr list)))
                        return ((Procedure)first).apply(evlisArray((Cons)list.cdr));
                    else
                        throw new LispException(internalError, "Dina fiskar är dåliga: " + toStringOrNull(obj)); }}
            else
                return obj; }}
    public static LispObject prin1(LispObject obj, LispStream stream) {
        LispStream s = (stream != null) ? stream : (LispStream)standardOutput.value;
        if (obj != null) obj.printObject(s);                // TODO: rewrite this using toStringOrNull instead (infact maybe get rid of the entire .printObject thing)
        else             s.writeJavaString("nil");          // Due to the funnyness of null as nil
        // s.terpri();
        return obj; }
    public  static Cons       cons(LispObject car, LispObject cdr)       { return new Cons(car, cdr);                                                   }
    public  static LispObject car(Cons list)                             { return (list == null) ? null : list.car;                                     }
    public  static LispObject cdr(Cons list)                             { return (list == null) ? null : list.cdr;                                     }
    public  static Symbol     intern(String str)                         { return Symbol.intern(str); }
    public  static LispObject read(LispStream stream) throws IOException { return ((stream != null) ? stream : (LispStream)standardInput.value).read(); }
    public  static LispChar   readChar(LispStream stream) throws IOException {
        return new LispChar(((stream != null) ? stream : (LispStream)standardInput.value).readJavaChar()); }
    public  static LispChar   writeChar(LispChar ch, LispStream stream) throws IOException {
        (stream != null ? stream : (LispStream)standardOutput.value).writeJavaChar(ch.ch); return ch; }
    public  static LispObject eq (LispObject obj1, LispObject obj2)      { return obj1 == obj2 ? t : null;                                              }
    public  static Cons       symbols()                                  { return Symbol.getSymbols();                                                  }
    public  static LispObject symbolValue(Symbol sbl)                    { return sbl.value;                                                            }
    public  static LispObject atom(LispObject obj)                       { return (obj instanceof Cons) ? null : t;                                     }
    private static long genSymCounter = 0;
    public  static LispObject gensym()                                   { return new Symbol("G" + genSymCounter++);                                    }
    public  static LispObject eql(LispObject a, LispObject b)            { return (a == null || b == null)    ? eq(a, b) : 
                                                                                  !a.getClass().isInstance(b) ? null :
                                                                                  (a instanceof LispChar)     ? (((LispChar)a).ch == ((LispChar)a).ch) ? t : null :
                                                                                  (a instanceof LispNumber)   ? (((LispNumber)a).equals((LispNumber)b) ? t : null) :
                                                                                                                eq(a, b); }
    
    /* Gives me everything I need to bootstrap my lisp */
    public static void initEnvironment() {
        t.value = t;

        try {
            standardOutput.value = new LispStream(null     , System.out);
            standardInput.value  = new LispStream(System.in, null);
            standardError.value  = new LispStream(null     , System.err);
        } catch (UnsupportedEncodingException e) {}         // Oh shut up! TODO: Major kludge this would seem like
        
        intern("Class").value = new JavaObject(Class.class); // All that is needed to bootstrap the Java interface.

        // Here go all SUBRs YAY! Redundancy is redundant etc. Messy is messy etc.
        intern("cons").value         = new LispSubr("cons", 2)         { public LispObject run (LispObject[] o) { return cons(o[0], o[1]);                                    }};
        intern("car").value          = new LispSubr("car", 1)          { public LispObject run (LispObject[] o) { return car((Cons)o[0]);                                     }};
        intern("cdr").value          = new LispSubr("cdr", 1)          { public LispObject run (LispObject[] o) { return cdr((Cons)o[0]);                                     }};
        intern("rplaca").value       = new LispSubr("rplaca", 2)       { public LispObject run (LispObject[] o) { ((Cons)o[0]).car = o[1]; return o[0];                       }};
        intern("rplacd").value       = new LispSubr("rplacd", 2)       { public LispObject run (LispObject[] o) { ((Cons)o[0]).cdr = o[1]; return o[0];                       }};
        intern("prin1").value        = new LispSubr("prin1", 1, 2)     { public LispObject run (LispObject[] o) { return prin1(o[0],(o.length>1)?(LispStream)o[1]:null);      }};
        intern("eq?").value          = new LispSubr("eq?", 2)          { public LispObject run (LispObject[] o) { return eq(o[0], o[1]);                                      }};
        intern("atom?").value        = new LispSubr("atom?", 1)        { public LispObject run (LispObject[] o) { return atom(o[0]);                                          }};
        intern("set").value          = new LispSubr("set", 2)          { public LispObject run (LispObject[] o) { return ((Symbol)o[0]).value = o[1];                         }};
        intern("eval").value         = new LispSubr("eval", 1)         { public LispObject run (LispObject[] o) { return eval(o[0]);                                          }};
        intern("symbols").value      = new LispSubr("symbols", 0)      { public LispObject run (LispObject[] o) { return symbols();                                           }};
        intern("symbol-value").value = new LispSubr("symbol-value", 1) { public LispObject run (LispObject[] o) { return (o[0] == null) ? null :symbolValue((Symbol)o[0]);    }};
        intern("gensym").value       = new LispSubr("gensym", 0)       { public LispObject run (LispObject[] o) { return gensym();                                            }};
        intern("intern").value       = new LispSubr("intern", 1)       { public LispObject run (LispObject[] o) {
            if (o[0] instanceof LispString) return intern(((LispString)o[0]).toJavaString());
            if (o[0] instanceof Symbol)     return ((Symbol)o[0]).intern();
            throw new LispException(internalError, "Bad argument"); }};
        intern("+").value            = new LispSubr("+", 2)            { public LispObject run (LispObject[] o) { return ((LispNumber)o[0]).add((LispNumber)o[1]);            }};
        intern("-").value            = new LispSubr("-", 2)            { public LispObject run (LispObject[] o) { return ((LispNumber)o[0]).sub((LispNumber)o[1]);            }};
        intern("*").value            = new LispSubr("*", 2)            { public LispObject run (LispObject[] o) { return ((LispNumber)o[0]).mul((LispNumber)o[1]);            }};
        intern("/").value            = new LispSubr("/", 2)            { public LispObject run (LispObject[] o) { return ((LispNumber)o[0]).div((LispNumber)o[1]);            }};
        intern("mod").value          = new LispSubr("mod", 2)          { public LispObject run (LispObject[] o) { return ((LispInteger)o[0]).mod((LispInteger)o[1]);          }};
        intern("ash").value          = new LispSubr("ash", 2)          { public LispObject run (LispObject[] o) { return ((LispInteger)o[0]).ash((LispInteger)o[1]);          }};
        intern("neg?").value         = new LispSubr("neg?", 1)         { public LispObject run (LispObject[] o) { return ((LispNumber)o[0]).negP() ? t : null;                }}; 
        intern("eql?").value         = new LispSubr("eql?", 2)         { public LispObject run (LispObject[] o) { return eql(o[0], o[1]);                                     }};
        intern("=").value            = new LispSubr("=", 2)            { public LispObject run (LispObject[] o) { return ((LispNumber)o[0]).equals((LispNumber)o[1])?t:null;  }};
        intern("char=").value        = new LispSubr("char=", 2)        { public LispObject run (LispObject[] o) { return (((LispChar)o[0]).ch == ((LispChar)o[1]).ch)?t:null; }};
        intern("aref").value         = new LispSubr("aref", 2)         { public LispObject run (LispObject[] o) { return ((LispArray)o[0]).aref(((LispInteger)o[1]).toJavaInt()); }};
        intern("aset").value         = new LispSubr("aset", 3)         { public LispObject run (LispObject[] o) { return ((LispArray)o[0]).aset(((LispInteger)o[1]).toJavaInt(), o[2]); }};
        intern("exit").value         = new LispSubr("exit", 0, 1)      { public LispObject run (LispObject[] o) {
            System.exit((o.length < 1) ? 0 : ((LispNumber)o[0]).toJavaInt()); return null; }};
        intern("get-time").value     = new LispSubr("get-time", 0)     { public LispObject run (LispObject[] o) { return new LispFixnum(System.currentTimeMillis()); }};
        intern("read-char").value    = new LispSubr("read-char", 0, 1) { public LispObject run (LispObject[] o) {
            try                   { return readChar((o.length > 0) ? (LispStream)o[0] : null); }
            catch (IOException e) { throw new LispException(internalError, "An IOException just occured to me, " + this.toString()); }}};
        intern("write-char").value = new LispSubr("write-char", 1, 2){ public LispObject run (LispObject[] o) {
            try                   { return writeChar((LispChar)o[0], ((o.length > 1) ? (LispStream)o[1] : null));     }
            catch (IOException e) { throw new LispException(internalError, "An IOException just occured to me, " + this.toString()); }}};
        intern("read").value         = new LispSubr("read", 0, 1)      { public LispObject run (LispObject[] o) {
            try                   { return read((o.length > 0) ? (LispStream)o[0] : null); }
            catch (IOException e) { throw new LispException(internalError, "An IOException just ocurred to me, " + this.toString()); }}};
        intern("open").value = new LispSubr("open", 2) { public LispObject run (LispObject[] o) {
            try {
                if (o[1] == in)  return new LispStream(new FileReader(((LispString)o[0]).toJavaString()), null);
                if (o[1] == out) return new LispStream(null, new PrintWriter(new FileWriter(((LispString)o[0]).toJavaString())));
                throw new LispException(internalError, "You confused me, you want a stream out, or in?");
            } catch (IOException e) {
                throw new LispException(internalError, e); }}};
        intern("close").value        = new LispSubr("close", 1) { public LispObject run (LispObject[] o) {
            try                   { return ((LispStream)o[0]).close() ? t : null; }
            catch (IOException e) { throw new LispException(internalError, "An IOException just ocurred to me, " + this.toString()); }}};
        intern("eof?").value = new LispSubr("eof?", 1) { public LispObject run (LispObject[] o) { return ((LispStream)o[0]).eof() ? t : null; }};
        intern("make-listener").value = new LispSubr("make-listener", 1) { public LispObject run (final LispObject[] o) {
            final class Listener implements ActionListener, KeyListener, MouseListener, WindowListener { // TODO: Implement more interfaces
                private void handle(EventObject e) { eval(cons(o[0], cons(new JavaObject(e), null))); }
                public void actionPerformed(ActionEvent e)   { handle(e); } public void keyPressed(KeyEvent e)           { handle(e); }
                public void keyReleased(KeyEvent e)          { handle(e); } public void keyTyped(KeyEvent e)             { handle(e); }
                public void mouseClicked(MouseEvent e)       { handle(e); } public void mousePressed(MouseEvent e)       { handle(e); }
                public void mouseReleased(MouseEvent e)      { handle(e); } public void mouseEntered(MouseEvent e)       { handle(e); }
                public void mouseExited(MouseEvent e)        { handle(e); } public void windowActivated(WindowEvent e)   { handle(e); }
                public void windowClosed(WindowEvent e)      { handle(e); } public void windowClosing(WindowEvent e)     { handle(e); }
                public void windowDeactivated(WindowEvent e) { handle(e); } public void windowDeiconified(WindowEvent e) { handle(e); }
                public void windowIconified(WindowEvent e)   { handle(e); } public void windowOpened(WindowEvent e)      { handle(e); }}
            return new JavaObject(new Listener()); }};
        intern("make-runnable").value = new LispSubr("make-runnable", 1) { public LispObject run (final LispObject[] o) {
            return new JavaObject(new Runnable() { public void run() { eval(cons(o[0], null)); }}); }};
        intern("make-string-input-stream").value = new LispSubr("make-string-input-stream", 1) { public LispObject run (LispObject[] o) { 
            return new LispStream(new StringReader(((LispString)o[0]).toJavaString()), null); }}; // NOTE: copies string
        intern("make-string-output-stream").value = new LispSubr("make-string-output-stream", 0) { public LispObject run (LispObject[] o) { return new StringOutputStream(); }};
        intern("get-output-stream-string").value = new LispSubr("get-output-stream-string", 1) { public LispObject run (LispObject[] o) {
            return new LispString(((StringOutputStream)o[0]).getOutputStreamString()); }};
        intern("%try").value = new LispSubr("%try", 2) { public LispObject run (LispObject[] o) {
            try                 { return eval(cons(o[0], null)); }
            catch (Exception e) { return eval(cons(o[1], cons(new JavaObject(e), null))); }}};
        intern("throw").value = new LispSubr("throw", 1, 2) { public LispObject run (LispObject[] o) {
            if (o.length == 2) {
                if      (o[1] instanceof LispString) throw new LispException((Symbol)o[0], ((LispString)o[1]).toJavaString());
                else if (o[1] instanceof JavaObject) throw new LispException((Symbol)o[0], (Throwable)((JavaObject)o[1]).getObj());
                else                                 throw new LispException(internalError, "Throw threw a throw."); }
            if (o[0] instanceof JavaObject && ((JavaObject)o[0]).getObj() instanceof LispException) throw (LispException)((JavaObject)o[0]).getObj();
            throw new LispException((Symbol)o[0]); }};
        intern("make-array").value = new LispSubr("make-array", 1) { public LispObject run (LispObject[] o) {
            if      (o[0] instanceof Cons)        return new LispArray((Cons)o[0]);
            else if (o[0] instanceof LispInteger) return new LispArray(((LispInteger)o[0]).toJavaInt());
            else                                  throw new LispException(internalError, "make-array wants an integer or a list"); }};
        intern("make-string").value = new LispSubr("make-string", 2) { public LispObject run (LispObject[] o) { return new LispString(((LispInteger)o[0]).toJavaInt(), (LispChar)o[1]); }};
        
        // Primitive due to the overloading on type, and the fact that I would need to export getting the length of a LispArray anyhow.
        intern("length").value = new LispSubr("length", 1) { public LispObject run (LispObject[] o) {
            return new LispFixnum((o[0] == null)         ? 0 :
                                  (o[0] instanceof Cons) ? ((Cons)o[0]).length() :
                                                           ((LispArray)o[0]).length()); }};
        intern("equal?").value = new LispSubr("equal?", 2) { public LispObject run (LispObject[] o) { return ((o[0] == null) ? o[1] == null : o[0].equals(o[1])) ? t : null; }};
        intern("sxhash").value = new LispSubr("sxhash", 1) { public LispObject run (LispObject[] o) { return new LispFixnum((o[0] == null) ? 0 : o[0].hashCode()); }};

        // When called from compiled code will return true, but not here
        intern("running-compiled?").value = new LispSubr("running-compiled?", 0) { public LispObject run (LispObject[] o) { return null; }};

        intern("char->integer").value = new LispSubr("char->integer", 1) { public LispObject run (LispObject[] o) { return new LispFixnum((int)((LispChar)o[0]).ch); }};
        intern("integer->char").value = new LispSubr("integer->char", 1) { public LispObject run (LispObject[] o) {
            return new LispChar((char)((LispInteger)o[0]).toJavaInt()); }};
            

        // While this _could_ be implemented using the reflection api from within LJSP it is here due to shenigans and a
        // stronger reinforcing that this is _truly_ a part of the language and not it's library (which makes the
        // compiler-writing later easier in a way), especially since the reflection API stuff should be considered somewhat of
        // an extension of the base language to implement at will.
        intern("type?").value =
            new LispSubr("type?", 2) {
                Symbol number = intern("number"), integer = intern("integer"), fixnum = intern("fixnum"), bignum = intern("bignum"), flonum = intern("flonum"),
                    symbol = intern("symbol"), cons = intern("cons"), procedure = intern("procedure"), subr = intern("subr"), array = intern("array"),
                    string = intern("string"), javaObject = intern("java-object"), javaMethod = intern("java-method"), exception = intern("exception"),
                    charmander = intern("char"), stream = intern("stream"), list = intern("list");
                public LispObject run (LispObject[] o) {
                    boolean woot = o[0] == number     ? o[1] instanceof LispNumber :
                                   o[0] == integer    ? o[1] instanceof LispInteger :
                                   o[0] == fixnum     ? o[1] instanceof LispFixnum :
                                   o[0] == bignum     ? o[1] instanceof LispBignum :
                                   o[0] == flonum     ? o[1] instanceof LispFlonum :
                                   o[0] == symbol     ? o[1] instanceof Symbol :
                                   o[0] == cons       ? o[1] instanceof Cons :
                                   o[0] == list       ? (o[1] == null || o[1] instanceof Cons) :
                                   o[0] == procedure  ? o[1] instanceof Procedure : // what about lambdas?
                                   o[0] == subr       ? o[1] instanceof LispSubr :
                                   o[0] == array      ? o[1] instanceof LispArray :
                                   o[0] == string     ? o[1] instanceof LispString :
                                   o[0] == javaObject ? o[1] instanceof JavaObject :
                                   o[0] == javaMethod ? o[1] instanceof JavaMethod :
                                   // o[0] == exception  ? o[1] instanceof LispException : 
                                   o[0] == charmander ? o[1] instanceof LispChar :
                                   o[0] == stream     ? o[1] instanceof LispStream :
                                   false;
                    return woot ? t : null; }};
    }

    public static void main(String args[]) {
        initEnvironment();
        while (true)                                        // REPL, with some wrapping...
            try { while (true) {
                    ((LispStream)standardOutput.value).writeJavaString("\n>> ");
                    prin1(eval(read(null)), null); }
            } catch (IOException e) {
                e.printStackTrace(((LispStream)standardError.value).out);
            } catch (RuntimeException e) {
                e.printStackTrace(((LispStream)standardError.value).out); }}}

final class LispException extends RuntimeException {
    public final Symbol tag;
    public LispException(Symbol tag)                                  { this.tag = tag; }
    public LispException(Symbol tag, String message)                  { super(message); this.tag = tag; }
    public LispException(Symbol tag, String message, Throwable cause) { super(message, cause); this.tag = tag; }
    public LispException(Symbol tag, Throwable cause)                 { super(cause); this.tag = tag; }
    public String toString() { return "<" + tag + ">" + super.toString(); }}

class LispObject { public void printObject(LispStream stream) { stream.writeJavaString(this.toString()); }}

abstract class Procedure extends LispObject {
    public final String name;
    public final int minArgs, maxArgs;
    public Procedure()            { this("", 0, Integer.MAX_VALUE);   } 
    public Procedure(String name) { this(name, 0, Integer.MAX_VALUE); } // FIXME: Don't know if this constructor will see much use
    public Procedure(String name, int numArgs) { this(name, numArgs, numArgs); }
    public Procedure(String name, int minArgs, int maxArgs) { this.name = name; this.minArgs = minArgs; this.maxArgs = maxArgs; }
    public final LispObject apply(LispObject[] o) {
        if (o.length < minArgs) throw new LispException(ljsp.internalError, "Too few args when calling procedure: "  + toString());
        if (o.length > maxArgs) throw new LispException(ljsp.internalError, "Too many args when calling procedure: " + toString());
        return run(o); }
    public abstract LispObject run(LispObject[] objects); }

abstract class LispSubr extends Procedure {             // FIXME: This class does nothing but modify toString, remove in cleanup later on?
    public LispSubr(String name) { super(name); }
    public LispSubr(String name, int numArgs) { super(name, numArgs); }
    public LispSubr(String name, int minArgs, int maxArgs) { super(name, minArgs, maxArgs); }
    public String toString() { return "#<subr " + name + ">"; }}

class Cons extends Procedure {                             // Cons extends Procedure since we might want a list like a function
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


    // This could eventually become problematic for bootstrapping.
    // (hard-coded dependence on ljsp.eval which will become obsolete upon bootstrapping)
    // If so remove this and make Cons extend LispObject, not Procedure
    private static final Symbol quote = Symbol.intern("quote");
    public LispObject run(LispObject[] o) {
        Cons list = null;
        for (int i = o.length-1; i >= 0; --i) 
            list = new Cons(new Cons(quote, new Cons(o[i], null)), list);
        return ljsp.eval(new Cons(this, list));
    }
    
    private static final int hashCode(LispObject obj) {
        return (obj == null)         ? 261835505 :          // Ensures different hashes between a proper and non-proper list
               (obj instanceof Cons) ? 1 + obj.hashCode() : 
                                       obj.hashCode(); }
    public int hashCode() { return hashCode(car) + 31*hashCode(cdr); } // KLUDGE: hash implementation could be a bit better
    private static final boolean equals(LispObject a, LispObject b) { return (a == null) ? b == null : a.equals(b); }
    public boolean equals(Object obj) { return (obj instanceof Cons) ? equals(((Cons)obj).car, car) && equals(((Cons)obj).cdr, cdr) : false; }

    public int length() { int i = 0; for (Cons c = this; c != null; c = (Cons)c.cdr) ++i; return i; }}

class Symbol extends LispObject {
    private static Cons symbols = null;
    public static Cons getSymbols() { return symbols; }

    public static Symbol findSymbol(String str) { return findSymbol(str, symbols); }
    private static Symbol findSymbol(String str, Cons list) { // FIXME: Make a loop out of this recursion
        if      (list == null)                       return null;
        else if (str.equals(((Symbol)list.car).str)) return (Symbol)list.car;
        else                                         return findSymbol(str, (Cons)list.cdr); }

    public LispObject value;                                // Value field, manipulated directly most of the time
    private String str;
    private boolean interned;
    
    public Symbol(String str) { this.str = str; this.interned = false; }

    public Symbol intern() {
        if (this.interned) return this;
        Symbol sbl;
        if ((sbl = findSymbol(this.str)) == null) {
            symbols = new Cons(this, symbols);
            this.interned = true;
            return this; }
        else {
            return sbl; }}

    public static Symbol intern(String str) { return (new Symbol(str)).intern(); }

    public String getStr() { return this.str; }
    public String toString() { return this.interned ? this.str : "#:" + this.str; }}

// Numerical tower, a.k.a. royal pain to implement nicely, multimethods are not native to java so some form of double-dispatch is required
abstract class LispNumber extends LispObject {
    public static final Pattern REGEX = Pattern.compile("^[+-]?\\d*\\.?(?:\\d+e)?\\d+$"); // The regex to match them all (numbers)
    public static final boolean javaStringMatchesLispNumber(String str) { return REGEX.matcher(str).matches(); }
    
    protected static final LispBignum coerceFixnumToBignum(LispNumber nbr) { return new LispBignum(((LispFixnum)nbr).toJavaLong()); }
    protected static final LispFlonum coerceIntegerToFlonum(LispNumber nbr) { return new LispFlonum(((LispInteger)nbr).toJavaDouble()); }

    /* Take that String and make a fitting LispNumber */
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
    public abstract boolean negP();

    public abstract int        toJavaInt();                 // FIXME: Kind of redundant
    public abstract long       toJavaLong();
    public abstract float      toJavaFloat();               // FIXME: Kind of redundant
    public abstract double     toJavaDouble();
    public abstract BigInteger toJavaBigInteger(); }

final class LispFlonum extends LispNumber {
    private double n; 
    public static LispNumber parse(String str) { return new LispFlonum(Double.parseDouble(str)); }
    public LispFlonum(double nbr) { n = nbr; }
    public int hashCode() { return Double.valueOf(n).hashCode(); }

    public LispFlonum add(LispFlonum nbr) { return new LispFlonum(n + nbr.n); }
    public LispFlonum sub(LispFlonum nbr) { return new LispFlonum(n - nbr.n); }
    public LispFlonum mul(LispFlonum nbr) { return new LispFlonum(n * nbr.n); }
    public LispFlonum div(LispFlonum nbr) { return new LispFlonum(n / nbr.n); }

    public boolean negP() { return n < 0; }
    public boolean equals(Object obj) { return (obj instanceof LispFlonum)  ? n == ((LispFlonum)obj).n :
                                               (obj instanceof LispInteger) ? n == ((LispInteger)obj).toJavaDouble() :
                                                                              false; }
    public LispFlonum add(LispNumber nbr) { return (nbr instanceof LispInteger) ? add(coerceIntegerToFlonum(nbr)) : add((LispFlonum)nbr); }
    public LispFlonum sub(LispNumber nbr) { return (nbr instanceof LispInteger) ? sub(coerceIntegerToFlonum(nbr)) : sub((LispFlonum)nbr); }
    public LispFlonum mul(LispNumber nbr) { return (nbr instanceof LispInteger) ? mul(coerceIntegerToFlonum(nbr)) : mul((LispFlonum)nbr); }
    public LispFlonum div(LispNumber nbr) { return (nbr instanceof LispInteger) ? div(coerceIntegerToFlonum(nbr)) : div((LispFlonum)nbr); }

    public String     toString()         { return "" + n; }
    public int        toJavaInt()        { return (int)n; }
    public long       toJavaLong()       { return (long)n; }
    public float      toJavaFloat()      { return (float)n; }
    public double     toJavaDouble()     { return (double)n; }
    public BigInteger toJavaBigInteger() { return BigInteger.valueOf((long)n); }}

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
    public int hashCode()             { return n.hashCode(); }
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
    public LispInteger ash(LispInteger nbr) { return new LispBignum(n.shiftLeft(nbr.toJavaInt())); } // TODO: Only well-defined between -2^31 and 2^31-1 inclusive
    public boolean equals(Object obj) { return (obj instanceof LispBignum) ? n.equals(((LispBignum)obj).n) :
                                               (obj instanceof LispFixnum) ? equals(coerceFixnumToBignum((LispFixnum)obj)) :
                                               (obj instanceof LispFlonum) ? obj.equals(this) :
                                                                             null; }
                                                                             
    public boolean negP() { return n.signum() == -1; }

    public String     toString()         { return n.toString(); }
    public int        toJavaInt()        { return n.intValue(); }
    public long       toJavaLong()       { return n.longValue(); }
    public float      toJavaFloat()      { return n.floatValue(); }
    public double     toJavaDouble()     { return n.doubleValue(); }
    public BigInteger toJavaBigInteger() { return n; }}

final class LispFixnum extends LispInteger {
    private final long n;

    public static LispFixnum parse(String str) { return new LispFixnum(Long.parseLong(str)); }
    public LispFixnum(long nbr) { n = nbr; }
    public int hashCode() { return Long.valueOf(n).hashCode(); }
    
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
    public boolean equals(Object obj) {
        return (obj instanceof LispFixnum) ? n == ((LispFixnum)obj).n :
               (obj instanceof LispNumber) ? obj.equals(this) :
                                             false; }
    public boolean negP() { return n < 0; }
    
    public String     toString()         { return "" + n; }
    public int        toJavaInt()        { return (int)n;  }
    public long       toJavaLong()       { return (long)n; }
    public float      toJavaFloat()      { return (float)n; }
    public double     toJavaDouble()     { return (double)n; }
    public BigInteger toJavaBigInteger() { return BigInteger.valueOf(n); }}

// class LispRatio extends LispNumber
// {
// }

final class LispChar extends LispObject {
    public final char ch;
    public LispChar(char ch) { this.ch = ch; }
    public int hashCode() { return Character.valueOf(ch).hashCode(); }
    public boolean equals(Object obj) { return (obj instanceof LispChar) ? ((LispChar)obj).ch == ch : false; }
    public String toString() { return "#\\" + ch; }}

class LispArray extends LispObject {
    protected final LispObject[] ar;
    public LispArray(int length) { ar = new LispObject[length]; }
    public LispArray(Cons list) {
        this(list.length());
        if (length() == 0) return;
        int i = 0;
        for (Cons c = list; c != null; c = (Cons)c.cdr) ar[i++] = c.car; }
    public LispArray(LispObject[] ar) { this.ar = ar; }
    public int hashCode() { return Arrays.deepHashCode(ar); }
    public boolean equals(Object obj) { return (obj instanceof LispArray) ? Arrays.deepEquals(ar, ((LispArray)obj).ar) :  false; }
    public int length() { return ar.length; }
    public LispObject aref(int idx) { return ar[idx]; }
    public LispObject aset(int idx, LispObject obj) { LispObject res = ar[idx]; ar[idx] = obj; return res; }
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("#(");
        for (LispObject o: ar) { sb.append(ljsp.toStringOrNull(o)); sb.append(' '); }
        sb.setLength(sb.length()-1);
        sb.append(')');
        return sb.toString(); }}

final class LispString extends LispArray {
    public LispObject aset(int idx, LispObject obj) {
        if (!(obj instanceof LispChar)) throw new LispException(ljsp.internalError, "Only Char may be in a string.");
        return super.aset(idx, obj); }
    public LispString(String str) {
        super(str.length());
        for (int i = 0; i < ar.length; ++i) ar[i] = new LispChar(str.charAt(i)); }
    public LispString(int length, LispChar ch) { super(length); for (int i = 0; i < length; ++i) ar[i] = ch; }
    public String toJavaString() {
        StringBuffer sb = new StringBuffer();
        for (LispObject o: ar) sb.append(((LispChar)o).ch);
        return sb.toString(); }
    public String toString() { return '"' + toJavaString() + '"'; }}

// Java-LJSP interface
// I combined Method and Constructor, and what do I get? Monstructor! of course! (this helps 
// in treating cosntructors like methods)
final class Monstructor {
    private final Method method;
    private final Constructor ctor;
    public Monstructor(Constructor ctor) { this.ctor = ctor; this.method = null;   }
    public Monstructor(Method method)    { this.method = method; this.ctor = null; }
    public Class<?> getReturnType()       { return (ctor == null) ? method.getReturnType()     : ctor.getDeclaringClass(); }
    public Class<?>[] getParameterTypes() { return (ctor == null) ? method.getParameterTypes() : ctor.getParameterTypes(); }
    /** obj is ignored for static methods and constructors */
    public Object invoke(Object obj, Object... args) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        return (ctor == null) ? method.invoke(obj, args) : ctor.newInstance(args); }
    public String toString() { return (ctor == null) ? method.toString() : ctor.toString(); }
    public int hashCode() { return (ctor == null) ? method.hashCode() : ctor.hashCode(); }
    public boolean equals(Object obj) {
        return (obj instanceof Monstructor) ? ((ctor == null) ? method.equals(((Monstructor)obj).method) : ctor.equals(((Monstructor)obj).ctor)) :
               (obj instanceof Method)      ? ((ctor == null) ? method.equals(obj)                       : false) :
               (obj instanceof Constructor) ? ((ctor == null) ? ctor.equals(obj)                         : false) :
                                              false; }}

final class JavaMethod extends Procedure {
    // FIXME: ARRGGGHH I SEEM TO PREFER OBJECT OVER DOUBLE SOMEOFTHEMTIMES... is this bad? Is it instead maybe what we want?
    //        fix is probably in either accept (more probable) or argumentMoreSpecificThan
    
    private final Object obj;
    private final Monstructor[] methods;
    // Maps argument classes to methods (this-object considered first argument in the list)
    private final static Map<String, Map<List<Class>, Monstructor>> methodMap = new HashMap<String, Map<List<Class>, Monstructor>>(); 

    // NEWTHING: what about accpetiong when the lispArg is null? (anything can be null except int,float etc. (not Integer,Float etc.)
    // Checks if lispArg can be cast to the class javaArg represents
    // TODO: Arrays maybe, but they currently lack autoconverting and stuff since it's such a pita
    // TODO: For numbers (at least integers) only accept if a conversion won't lose precision. (in fact with this in place i
    // might just be able to, in argumentMoreSpecificThan, prefer narrower over wider for everything)
    private static boolean accept(Class javaArg, LispObject lispArg) {
        return (javaArg == Object.class || LispObject.class.isAssignableFrom(javaArg)) ? true : // Move this line? (with or without the LispObject part?)
               (javaArg == float.class  || javaArg == Float.class ||
                javaArg == double.class || javaArg == Double.class)                    ? (lispArg instanceof LispFlonum) :
               (javaArg == char.class || javaArg == Character.class)                   ? (lispArg instanceof LispChar) :
               (javaArg == short.class || javaArg == Short.class ||
                javaArg == int.class   || javaArg == Integer.class ||
                javaArg == long.class  || javaArg == Long.class ||
                javaArg == BigInteger.class)                                           ? (lispArg instanceof LispInteger) : 
               (javaArg == String.class)                                               ? (lispArg instanceof LispString) :
               (javaArg == boolean.class || javaArg == Boolean.class)                  ? true :
               (lispArg instanceof JavaObject)                                         ? javaArg.isInstance(((JavaObject)lispArg).getObj()) :
                                                                                         false;
    }

    // Returns true if a is more specific than ("prefered" over) b, false otherwise. Additionally contains special case rules to
    // prefer "wider" argument types when it comes to numeric types instead of the other way around (i.e: prefer double over
    // float). TODO: Them arrays again
    // FIXME: bool support. think of it as a bool if it is either nil or t? special symbols javatrue javafalse?
    private static boolean argumentMoreSpecificThan(Class<?> a, Class<?> b) {
        return (a == b)                                   ? false : // Can't make a judgement now can we
               // (a == Object.class)                        ? true : // BUGFISK? Object is always specificisestist
               // Special: LispObject is more specific than anything else yo, also tricky rules
               LispObject.class.isAssignableFrom(a)       ? !(LispObject.class.isAssignableFrom(b) && a.isAssignableFrom(b)) : 
               //(a == boolean.class || a == Boolean.class) ? (b != boolean.class || b != Boolean.class || !LispObject.class.isAssignableFrom(b)) : // But lispobject wins over bools
               (a == int.class || a == Integer.class)     ? (b == short.class || b == Short.class) : // For numbers we prefer wider over narrower
               (a == long.class || a == Long.class)       ? (b == int.class || b == Integer.class || b == short.class || b == Short.class) :
               (a == BigInteger.class)                    ? (b == long.class || b == Long.class || b == int.class || b == Integer.class || b == short.class || b == Short.class) :
               (a == double.class || a == Double.class)   ? (b == float.class || b == Float.class) :
               b.isAssignableFrom(a);                       // If b is assignable from a, a is more specific, "narrower", than b
    }

    // Matches a method given a list of LispObjects that this method will be called with. A three-stage rocket.
    // TODO: Split me into my three logical parts.
    private int matchMethod(LispObject[] lispArgs) {
        LinkedList<Integer> list = new LinkedList<Integer>();
        for (int i = 0; i < methods.length; ++i) list.add(i);

        // Prune all methods of insufficient length
        ListIterator<Integer> it = list.listIterator();
        while (it.hasNext())                                
            if (methods[it.next()].getParameterTypes().length != lispArgs.length)
                it.remove();
        if (list.isEmpty()) return -1;                  // No match
        if (list.size() == 1) return list.getFirst();   // We have a match here (there was but one method of this length)

        System.out.println("hej");

        // Prune all methods that don't have a chance of matching
        for (int argNumber = 0; argNumber < lispArgs.length; ++argNumber) { 
            it = list.listIterator();
            while (it.hasNext())       
                if (!accept(methods[it.next()].getParameterTypes()[argNumber], lispArgs[argNumber]))
                    it.remove();
            if (list.isEmpty()) return -1;                  // No match
            if (list.size() == 1) return list.getFirst();   // We have a potential match here
        }

        System.out.println("fisk");

        for (Integer i: list)
            System.out.print(methods[i] + " \\\\//");
        System.out.println("\n----");
        
        // Find the most specific method of the matching methods
        for (int argNumber = 0; argNumber < lispArgs.length; ++argNumber) { 
            it = list.listIterator();
            // Find the maximum (most specific) of all the argNumber'th method arguments classes
            int max = list.getFirst();
            int tmp;
            while (it.hasNext())
                max = argumentMoreSpecificThan(methods[tmp = it.next()].getParameterTypes()[argNumber], methods[max].getParameterTypes()[argNumber]) ? tmp : max;
            it = list.listIterator();
            // Remove all less specific methods.
            System.out.println("mask " + methods[max].getParameterTypes()[argNumber]);
            while (it.hasNext()) {
                int asdf = it.next();
                System.out.println("asdf " + methods[asdf].getParameterTypes()[argNumber]);
                if (methods[asdf].getParameterTypes()[argNumber] != methods[max].getParameterTypes()[argNumber])
                    it.remove();
            }
            
            if (list.size() == 1) return list.getFirst();   // We have found our match
        }

        for (Integer i: list)
            System.out.print(methods[i] + " \\\\//");
        System.out.println("\n----");

        // KLUDGE: If we still find ourselves confused and with more than a single method left. prefer those that don't return
        // an abstract type. Majorly kludgeish.... TODO: think up a scenario to make this explode or something (can there be
        // cases where we have several alternatives here yet all of them return an abstract type?)
        it = list.listIterator();
        while (it.hasNext())
            if (Modifier.isAbstract(methods[it.next()].getReturnType().getModifiers()))
                it.remove();

        for (Integer i: list)
            System.out.print(methods[i] + " \\\\//");
        System.out.println("\n----");

        if (list.size() == 1) return list.getFirst();

        
            
        throw new LispException(ljsp.internalError, "This should not happen!"); // Seriously, it shouldn't!
    }

    // Conversion helpers, does the unboxing and boxing
    private static Object[] lispToJava(LispObject[] objs, Class[] argt) {
        Object[] res = new Object[objs.length];
        for (int i = 0; i < objs.length; ++i)
            res[i] = // (objs[i] instanceof JavaObject)                         ? ((JavaObject)objs[i]).getObj() :
                     (LispObject.class.isAssignableFrom(argt[i]))             ? objs[i] : // Wants a LispObject or subclass, no unboxing needed
                     (objs[i] instanceof JavaObject)                          ? ((JavaObject)objs[i]).getObj() :
                     (argt[i] == float.class  || argt[i] == Float.class)      ? ((LispFlonum)objs[i]).toJavaFloat() :
                     (argt[i] == double.class || argt[i] == Double.class)     ? ((LispFlonum)objs[i]).toJavaDouble() :
                     (argt[i] == int.class    || argt[i] == Integer.class)    ? ((LispInteger)objs[i]).toJavaInt() :
                     (argt[i] == short.class  || argt[i] == Short.class)      ? new Short((short)((LispInteger)objs[i]).toJavaInt()) :
                     (argt[i] == BigInteger.class)                            ? ((LispInteger)objs[i]).toJavaBigInteger() :
                     (argt[i] == char.class    || argt[i] == Character.class) ? ((LispChar)objs[i]).ch :
                     (argt[i] == long.class    || argt[i] == Long.class)      ? ((LispInteger)objs[i]).toJavaLong() :
                     (argt[i] == Boolean.class || argt[i] == boolean.class)   ? objs[i] != null :
                     (argt[i] == String.class)                                ? ((LispString)objs[i]).toJavaString() :
                                                                                objs[i]; 
        return res; }
    
    private LispObject javaToLisp(Object obj) {
        return (obj == null)               ? null :
               (obj instanceof LispObject) ? (LispObject)obj :
               (obj instanceof Float)      ? new LispFlonum((Float)obj) :
               (obj instanceof Double)     ? new LispFlonum((Double)obj) :
               (obj instanceof Short)      ? new LispFixnum((Short)obj) :
               (obj instanceof Integer)    ? new LispFixnum((Integer)obj) :
               (obj instanceof Long)       ? new LispFixnum((Long)obj) :
               (obj instanceof Character)  ? new LispChar((Character)obj) : 
               (obj instanceof BigInteger) ? new LispBignum((BigInteger)obj) :
               (obj instanceof String)     ? new LispString((String)obj) :
               (obj instanceof Boolean)    ? (Boolean)obj == true ? ljsp.t : null : 
                                             new JavaObject(obj); }

    // Gives a list of classes; the argument types of the list, used as a key for memoizing in methodMap
    private List<Class> getArgumentTypes(Object[] list) {
        List<Class> res = new ArrayList<Class>(list.length + 1);
        for (int i = 0; i < list.length; ++i)
            res.add((list[i] == null)               ? null : // Don't want NullPointerExceptions
                    (list[i] instanceof JavaObject) ? ((JavaObject)list[i]).getObj().getClass() : // For JavaObject it is more useful to use the class of the object it contains
                                                      list[i].getClass());
        return res; }

    // Apply method to objects and the closed-over this object
    public LispObject run(LispObject[] objects) {
        try {
            List<Class> argumentTypes = getArgumentTypes(objects);
            Class storeKlas = (obj instanceof Class) ? (Class)obj : obj.getClass(); // We need to use obj as key when it is an instance of Class
            argumentTypes.add(0, storeKlas);                                        // prepend the class to the list of argument types
            Monstructor method = methodMap.get(name).get(argumentTypes);
            if (method == null) {
                int m = matchMethod(objects);
                if (m == -1) throw new LispException(ljsp.internalError, "No matching method found for the args: " + Arrays.toString(objects));
                method = methods[m];
                methodMap.get(name).put(argumentTypes, method); }
            return javaToLisp(method.invoke(obj, lispToJava(objects, method.getParameterTypes()))); // Wee...
        } catch (IllegalAccessException e) {
            throw new LispException(ljsp.internalError, e);
        } catch (InvocationTargetException e) {
            throw new LispException(ljsp.internalError, e);
        } catch (InstantiationException e) {
            throw new LispException(ljsp.internalError, e); }}
    
    public JavaMethod(Monstructor[] methods, String name, Object obj) {
        super(name);                                        // TODO: Send proper limits to super?
        this.methods = methods;
        this.obj = obj;
        if (!methodMap.containsKey(name))
            methodMap.put(name, new HashMap<List<Class>, Monstructor>()); }
    public String toString() { return "#<java-method " + name + " | " + obj + ">"; }
    public boolean equals(Object obj) { return (obj instanceof JavaMethod) ? this.obj == ((JavaMethod)obj).obj && Arrays.equals(methods, ((JavaMethod)obj).methods) : false; }
    public int hashCode() { return Arrays.hashCode(methods) ^ System.identityHashCode(obj); } 
}

final class JavaObject extends Procedure {
    private final static Map<Class, Map<Symbol, Monstructor[]>> methodMap = new HashMap<Class, Map<Symbol, Monstructor[]>>();
    private final Class klas;
    private final Object obj;
    private final static Symbol newInstance = ljsp.intern("newInstance");

    /* Wrap that object! */
    public JavaObject(Object obj) {
        super(obj.toString(), 1);
        this.obj = obj;
        // klas = (obj != null) ? obj.getClass() : null;
        klas = obj.getClass();
        Class storeKlas = (klas == Class.class) ? (Class)obj : klas; // We need to use obj as key when it is an instance of Class
        if (!methodMap.containsKey(storeKlas))
            methodMap.put(storeKlas, new HashMap<Symbol, Monstructor[]>());
    }

    /* Apply object to symbol generating a "closure", a.k.a. method. */
    public JavaMethod run(LispObject[] o) {
        Symbol sbl = (Symbol)o[0];
        ArrayList<Monstructor> methodList = new ArrayList<Monstructor>();
        Monstructor[] methodArray;
        Class storeKlas = (klas == Class.class) ? (Class)obj : klas; // We need to use obj as key when it is an instance of Class
        if ((methodArray = methodMap.get(storeKlas).get(sbl)) != null) return new JavaMethod(methodArray, sbl.getStr(), obj);
        if (obj != Class.class && obj instanceof Class) {
            // Special case when obj is a Class object (but not a Class object representing a Class object. That is: not Class.class):
            // Allow, in addition to accessing the methods of the object, access to static methods, and the constructors of the
            // class this object represents. If you are confused now blame Javas reflection API.
            for (Method m: ((Class)obj).getMethods())       // Find static methods
                if (Modifier.isStatic(m.getModifiers()) && m.getName().equals(sbl.getStr()))
                    methodList.add(new Monstructor(m));
            if (sbl == newInstance)                         // Yay, constructors! (Note: we do not try to fetch any more methods in  this case)
                for (Constructor c: ((Class)obj).getConstructors())
                    methodList.add(new Monstructor(c));
            else
                for (Method m: klas.getMethods())
                // for (Method m: klas.getDeclaredMethods())
                    if (m.getName().equals(sbl.getStr()))
                    // if (m.getName().equals(sbl.getStr()) && !Modifier.isAbstract(m.getReturnType().getModifiers())) // say no to methods returning abstract types
                        methodList.add(new Monstructor(m)); }
        else
            for (Method m: klas.getMethods())
            // for (Method m: klas.getDeclaredMethods())
                if (m.getName().equals(sbl.getStr()))
                // if (m.getName().equals(sbl.getStr()) && !Modifier.isAbstract(m.getReturnType().getModifiers())) // say no to methods returning abstract types
                    methodList.add(new Monstructor(m));
        if (methodList.isEmpty()) throw new LispException(ljsp.internalError, "No such method: " + sbl.getStr() + ", " + toString());
        methodArray = methodList.toArray(new Monstructor[0]);    // Umm... Not pretty API here Java...
        methodMap.get(storeKlas).put(sbl, methodArray);          // Cache the results
        return new JavaMethod(methodArray, sbl.getStr(), obj); 
    }
    
    public Object getObj() { return obj; }
    public String toString() { return "#<java-object " + obj + " | " + klas + ">"; }

    public boolean equals(Object obj) { return (obj instanceof JavaObject) ? ((JavaObject)obj).obj.equals(this.obj) : false; } // FIXME: change me?
    public int hashCode() { return obj.hashCode(); }}

// FIXME: Doesn't terminate properly on EOF when reading symbols and other stuffs
/* The stream class used throughout. Can be input or output stream, optionally at the same time but points aren't synchronized so I advice against */
// THIS READER REALLY REALLY SUCKS SERIOUSLY HOW COULD I EVEN WRITE IT THIS BAD?
class LispStream extends LispObject {
    public final Reader in;
    public final PrintWriter out;
    private static final Symbol readerError = ljsp.intern("reader-error");
    private static final Symbol eofError = ljsp.intern("eof-error");
    private Stack<Character> pushbackStack;
    private boolean open;
    private boolean eof;

    public LispStream(InputStream in, OutputStream out) throws UnsupportedEncodingException {
        this((in  != null) ? new BufferedReader(new InputStreamReader(in, "UTF-8")) : null,
             (out != null) ? new PrintWriter(out, true) : null); }

    public LispStream(Reader in, PrintWriter out) {
        this.in = in;
        this.out = out;
        if (inputStreamP()) pushbackStack = new Stack<Character>();
        open = true;
        eof = false; }

    public boolean close() throws IOException {
        if (open) {
            if (inputStreamP())  in.close();
            if (outputStreamP()) out.close();
            open = false;
            return true; }
        else return false; }

    public void writeJavaString(String str) { out.print(str); out.flush(); } // Throws NullPointerException when not output stream FIXME?
    public void terpri()                    { out.println();               } // Throws NullPointerException when not output stream FIXME?

    public boolean eof() { return eof; }
    
    public void writeJavaChar(char ch) throws IOException { out.print(ch); if (ch == '\n') out.flush(); }
    private void checkEOF() { if (eof) throw new LispException(eofError, "Hit EOF, don't read further or else... " + this); }
    private char readCheckEOF() throws IOException {
        int ch;
        checkEOF();
        if ((ch = in.read()) == -1) eof = true;
        return (char)ch; } // Throws NullPointerException 
    public char readJavaChar() throws IOException {          // Throws NullPointerException when not input stream FIXME?
        // checkEOF();
        if (pushbackStack.empty()) return readCheckEOF();
        else                       return pushbackStack.pop(); }
    public char peekJavaChar() throws IOException {           // Throws NullPointerException when not input stream FIXME?
        // checkEOF();
        if (pushbackStack.empty()) return pushbackStack.push(readCheckEOF());
        else                       return pushbackStack.peek(); }
    public void unreadJavaChar(char ch) { pushbackStack.push(ch); } // Throws NullPointerException when not inputstream FIXME?
    public void skipWhiteSpaceAndComments() throws IOException {
        char tmp = readJavaChar();
        while (Character.isWhitespace(tmp) || tmp == ';') {
            if (tmp == ';') // if we find ; discard everything to, and with, newline
                while (readJavaChar() != '\n');
            tmp = readJavaChar(); }
        unreadJavaChar(tmp); }
    
    public boolean inputStreamP()  { return (in  != null) ? true : false; }
    public boolean outputStreamP() { return (out != null) ? true : false; }

    public String toString() { return "#<" + super.toString() + ">"; }

    /* Read in a list. Messy code ahead. */
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
                if (ch == ')') throw new LispException(readerError, "You now have me confuzzled, don't you want something after the dot?");
                last.cdr = this.read();
                this.skipWhiteSpaceAndComments();
                if (this.peekJavaChar() != ')') throw new LispException(readerError, "You just might want to end the list with parentheses, even though you're a prick.");
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

    // TODO: "\n" and the likes
    private LispString readString() throws IOException {
        char ch;
        StringBuffer sb = new StringBuffer();
        this.readJavaChar();                                // Discard '"'
        while ((ch = this.readJavaChar()) != '"') sb.append(ch);
        return new LispString(sb.toString()); }

    /* Handle syntax starting on '#' */
    private LispObject dispatchFence() throws IOException {
        this.readJavaChar();                                // Discard '#'
        char ch = this.readJavaChar();
        if      (ch == ';')  { this.read(); return this.read(); }                                 // Commment out a sexp
        else if (ch == '\\') return new LispChar(this.readJavaChar());                            // Read  a character
        else if (ch =='(')   { this.unreadJavaChar('('); return new LispArray(this.readList()); } // Read an array
        else throw new LispException(readerError, "Syntax Errol: dispatchFence()"); } 

    /* Read text return lisp data structures. */
    public LispObject read() throws IOException {
        if (!this.inputStreamP()) throw new LispException(readerError, "You can't read what you can't read man, get over it.");

        this.skipWhiteSpaceAndComments();
        char ch = this.peekJavaChar();
        switch (ch) {
            case ')':
                this.readJavaChar();                        // Discard the lonely brace
                throw new LispException(readerError, "Lonely ending brace");
            case '.':
                this.readJavaChar();                        // Discard the stray .
                throw new LispException(readerError, "Stray dot");
            case '#':  return this.dispatchFence();
            case '(':  return this.readList();
            case '\'': return this.readQuote();             // Handle quote syntax. Having to type "(quote blaha)", when you can "'blaha" is such a chore.
            case '|':  return this.readQuotedSymbol();
            case '"':  return this.readString();
            default:                                        // An atom, (well ||-style symbols are atoms too)
                StringBuilder sb = new StringBuilder();
                for (ch = this.readJavaChar();
                     !Character.isWhitespace(ch) && ch != '(' && ch != ')' && !eof;
                     ch = this.readJavaChar())
                    sb.append(ch);
                this.unreadJavaChar(ch);
                String str = sb.toString();
                if (LispNumber.javaStringMatchesLispNumber(str))          // Is a number
                    return LispNumber.parse(str);
                else // Is a symbol: Funnyness since nil not separated from java null (early bad decision)
                    return str.equals("nil") ? null : new Symbol(str).intern(); }}}

class StringOutputStream extends LispStream {
    private static StringWriter tmp; // Java is being utterly stupid, but i can use a temporary static variable to redeem part of it
    private final StringWriter stringWriter;
    public StringOutputStream() {
        super(null, new PrintWriter(tmp = new StringWriter())); // Couldn't assign directly to stringWriter here, nope...
        stringWriter = tmp; }
    public String getOutputStreamString() {
        StringBuffer sb = stringWriter.getBuffer();
        String result = sb.toString();
        sb.setLength(0);                                    // Clear characters in stream/buffer/whatever
        return result; }}
