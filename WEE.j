.class WEE
.super Procedure

.field private static final t LLispObject;
.field private static final lit1 LLispObject;


.method static <clinit>()V
    .limit locals 255
    .limit stack 255

    ldc_w "t"
    invokestatic Symbol.intern(Ljava/lang/String;)LSymbol;
    putstatic WEE/t LLispObject;
    new Cons
    dup
    new LispFixnum
    dup
    ldc2_w 1
    invokenonvirtual LispFixnum.<init>(J)V
    new Cons
    dup
    ldc_w "a"
    invokestatic Symbol.intern(Ljava.lang.String;)LSymbol;
    aconst_null
    invokenonvirtual Cons.<init>(LLispObject;LLispObject;)V
    invokenonvirtual Cons.<init>(LLispObject;LLispObject;)V
    putstatic WEE/lit1 LLispObject;
    return
.end method

.method public <init>()V
    .limit stack 2
    .limit locals 1

    aload_0
    ldc "WEE"
    invokenonvirtual Procedure.<init>(Ljava/lang/String;)V
    return
.end method

.method public run([LLispObject;)LLispObject;
    .limit stack  255
    .limit locals 255
    ;; (lambda nil (quote (1 a)))
Lselftail:
    getstatic WEE/lit1 LLispObject;
    areturn
    ;; endlambda
.end method
