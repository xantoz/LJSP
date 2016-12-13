.class EKANS
.super Procedure

.field public static final t LLispObject;

.method static <clinit>()V
    .limit locals 0
    .limit stack 1

    ldc_w "t"
    invokestatic Symbol.intern(Ljava/lang/String;)LSymbol;
    putstatic EKANS/t LLispObject;
    
    return
.end method

.method public <init>()V
    .limit stack 2
    .limit locals 1

    aload_0
    ldc "EKANS"
    invokenonvirtual Procedure.<init>(Ljava/lang/String;)V
    return
.end method

.method public run([LLispObject;)LLispObject;
.limit stack  255
.limit locals 255
;; (lambda (key value alist) (cons (cons key value) alist))
aload_1
ldc_w 0
aaload
astore 5
aload_1
ldc_w 1
aaload
astore 6
aload_1
ldc_w 2
aaload
astore 7
Lselftail:
new Cons
dup
new Cons
dup
aload 5
aload 6
invokenonvirtual Cons.<init>(LLispObject;LLispObject;)V
aload 7
invokenonvirtual Cons.<init>(LLispObject;LLispObject;)V
areturn
;; endlambda
.end method
