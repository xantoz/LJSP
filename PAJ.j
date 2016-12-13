.class PAJ
.super Procedure

.field private static final t LLispObject;
.field private static final literal_1 LLispObject;


.method static <clinit>()V
    .limit locals 255
    .limit stack 255

    ldc_w "t"
    invokestatic Symbol.intern(Ljava/lang/String;)LSymbol;
    putstatic PAJ/t LLispObject;
    new LispFlonum
dup
ldc2_w 3.141592653589793d
invokenonvirtual LispFlonum.<init>(D)V
putstatic PAJ/literal_1 LLispObject;
    return
.end method

.method public <init>()V
    .limit stack 2
    .limit locals 1

    aload_0
    ldc "PAJ"
    invokenonvirtual Procedure.<init>(Ljava/lang/String;)V
    return
.end method

.method public run([LLispObject;)LLispObject;
.limit stack  255
.limit locals 255
;; (lambda (a) (* a (quote 3.141592653589793)))
aload_1
ldc_w 0
aaload
astore 5
Lselftail:
aload 5
checkcast LispNumber
getstatic PAJ/literal_1 LLispObject;
checkcast LispNumber
invokevirtual LispNumber.mul(LLispNumber;)LLispNumber;
areturn
;; endlambda
.end method
