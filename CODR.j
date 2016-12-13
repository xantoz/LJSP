.class CODR
.super Procedure

.field public static final t LLispObject;

.method static <clinit>()V
    .limit locals 0
    .limit stack 1

    ldc_w "t"
    invokestatic Symbol.intern(Ljava/lang/String;)LSymbol;
    putstatic CODR/t LLispObject;
    
    return
.end method

.method public <init>()V
    .limit stack 2
    .limit locals 1

    aload_0
    ldc "CODR"
    invokenonvirtual Procedure.<init>(Ljava/lang/String;)V
    return
.end method

.method public run([LLispObject;)LLispObject;
.limit stack  255
.limit locals 255
;; (lambda (a) (car (cdr a)))
aload_1
ldc_w 0
aaload
astore 5
Lselftail:
aload 5
dup
ifnull L25
checkcast Cons
getfield Cons/cdr LLispObject;
L25:
dup
ifnull L24
checkcast Cons
getfield Cons/car LLispObject;
L24:
areturn
;; endlambda
.end method
