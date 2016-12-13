.class FUN26
.super Procedure

.field public static final t LLispObject;

.method static <clinit>()V
    .limit locals 0
    .limit stack 1

    ldc_w "t"
    invokestatic Symbol.intern(Ljava/lang/String;)LSymbol;
    putstatic FUN26/t LLispObject;
    
    return
.end method

.method public <init>()V
    .limit stack 2
    .limit locals 1

    aload_0
    ldc "FUN26"
    invokenonvirtual Procedure.<init>(Ljava/lang/String;)V
    return
.end method

.method public run([LLispObject;)LLispObject;
.limit stack  255
.limit locals 255
;; (lambda (n) ((nlambda calc-fib (n a b) (if (= n 0) a (calc-fib (- n 1) b (+ a b)))) n 0 1))
aload_1
ldc_w 0
aaload
astore 5
;; ((nlambda calc-fib (n a b) (if (= n 0) a (calc-fib (- n 1) b (+ a b)))) n 0 1)
new FUN27
dup
invokenonvirtual FUN27.<init>()V
; preparing args
ldc_w 3
anewarray LispObject
dup
ldc_w 0
aload 5
aastore
dup
ldc_w 1
new LispFixnum
dup
ldc2_w 0
invokenonvirtual LispFixnum.<init>(J)V
aastore
dup
ldc_w 2
new LispFixnum
dup
ldc2_w 1
invokenonvirtual LispFixnum.<init>(J)V
aastore
; end preparing args
invokevirtual Procedure.run([LLispObject;)LLispObject;
areturn
;; endlambda
.end method
