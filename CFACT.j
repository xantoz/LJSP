.class CFACT
.super Procedure

.field private static final t LLispObject;


.method static <clinit>()V
    .limit locals 255
    .limit stack 255

    ldc_w "t"
    invokestatic Symbol.intern(Ljava/lang/String;)LSymbol;
    putstatic CFACT/t LLispObject;
    
    return
.end method

.method public <init>()V
    .limit stack 2
    .limit locals 1

    aload_0
    ldc "CFACT"
    invokenonvirtual Procedure.<init>(Ljava/lang/String;)V
    return
.end method

.method public run([LLispObject;)LLispObject;
.limit stack  255
.limit locals 255
;; (lambda (n acc) (if (= 0 n) acc (fact (- n 1) (* n acc))))
aload_1
ldc_w 0
aaload
astore 5
aload_1
ldc_w 1
aaload
astore 6
Lselftail:
;; (if (= 0 n) acc (fact (- n 1) (* n acc)))
new LispFixnum
dup
ldc2_w 0
invokenonvirtual LispFixnum.<init>(J)V
aload 5
invokevirtual java/lang/Object.equals(Ljava/lang/Object;)Z
ifeq L78
getstatic CFACT/t LLispObject;
goto L77
L78:
aconst_null
L77:
ifnonnull L76 ; branches to the true-expr
;; self-recursive tail-call args: ((- n 1) (* n acc))
aload 5
checkcast LispNumber
new LispFixnum
dup
ldc2_w 1
invokenonvirtual LispFixnum.<init>(J)V
checkcast LispNumber
invokevirtual LispNumber.sub(LLispNumber;)LLispNumber;
aload 5
checkcast LispNumber
aload 6
checkcast LispNumber
invokevirtual LispNumber.mul(LLispNumber;)LLispNumber;
astore 6
astore 5
goto Lselftail
goto L75 ; Don't also run the true-expr like a fool
L76:
aload 6
L75:
;; endif
areturn
;; endlambda
.end method
