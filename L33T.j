.class L33T
.super Procedure

.field public static final t LLispObject;

.method static <clinit>()V
    .limit locals 0
    .limit stack 1

    ldc_w "t"
    invokestatic Symbol.intern(Ljava/lang/String;)LSymbol;
    putstatic L33T/t LLispObject;
    
    return
.end method

.method public <init>()V
    .limit stack 2
    .limit locals 1

    aload_0
    ldc "L33T"
    invokenonvirtual Procedure.<init>(Ljava/lang/String;)V
    return
.end method

.method public run([LLispObject;)LLispObject;
.limit stack  255
.limit locals 255
;; (lambda nil (cons 13 37))
Lselftail:
new Cons
dup
new LispFixnum
dup
ldc2_w 13
invokenonvirtual LispFixnum.<init>(J)V
new LispFixnum
dup
ldc2_w 37
invokenonvirtual LispFixnum.<init>(J)V
invokenonvirtual Cons.<init>(LLispObject;LLispObject;)V
areturn
;; endlambda
.end method
