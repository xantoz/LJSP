.class FUN3
.super Procedure

.field private static final t LLispObject;


.method static <clinit>()V
    .limit locals 255
    .limit stack 255

    ldc_w "t"
    invokestatic Symbol.intern(Ljava/lang/String;)LSymbol;
    putstatic FUN3/t LLispObject;
    
    return
.end method

.method public <init>()V
    .limit stack 2
    .limit locals 1

    aload_0
    ldc "FUN3"
    invokenonvirtual Procedure.<init>(Ljava/lang/String;)V
    return
.end method

.method public run([LLispObject;)LLispObject;
.limit stack  255
.limit locals 255
;; (lambda (n a b) (if (= n 0) a (calc-fib (- n 1) b (+ a b))))
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
;; (if (= n 0) a (calc-fib (- n 1) b (+ a b)))
aload 5
new LispFixnum
dup
ldc2_w 0
invokenonvirtual LispFixnum.<init>(J)V
invokevirtual java/lang/Object.equals(Ljava/lang/Object;)Z
ifeq L12
getstatic FUN3/t LLispObject;
goto L11
L12:
aconst_null
L11:
ifnonnull L10 ; branches to the true-expr
;; self-recursive tail-call args: ((- n 1) b (+ a b))
aload 5
checkcast LispNumber
new LispFixnum
dup
ldc2_w 1
invokenonvirtual LispFixnum.<init>(J)V
checkcast LispNumber
invokevirtual LispNumber.sub(LLispNumber;)LLispNumber;
aload 7
aload 6
checkcast LispNumber
aload 7
checkcast LispNumber
invokevirtual LispNumber.add(LLispNumber;)LLispNumber;
astore 7
astore 6
astore 5
goto Lselftail
goto L9 ; Don't also run the true-expr like a fool
L10:
aload 6
L9:
;; endif
areturn
;; endlambda
.end method