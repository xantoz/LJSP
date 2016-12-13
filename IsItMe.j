.class IsItMe
.super Procedure

.field public static final t LLispObject;

.method static <clinit>()V
    .limit locals 0
    .limit stack 1

    ldc_w "t"
    invokestatic Symbol.intern(Ljava/lang/String;)LSymbol;
    putstatic IsItMe/t LLispObject;
    
    return
.end method

.method public <init>()V
    .limit stack 2
    .limit locals 1

    aload_0
    ldc "IsItMe"
    invokenonvirtual Procedure.<init>(Ljava/lang/String;)V
    return
.end method

.method public run([LLispObject;)LLispObject;
.limit stack  255
.limit locals 255
;; (lambda (b) (eq? a b))
aload_1
ldc_w 0
aaload
astore 5
Lselftail:
aload 0
aload 5
if_acmpne L51
getstatic IsItMe/t LLispObject;
goto L50
L51:
aconst_null
L50:
areturn
;; endlambda
.end method
