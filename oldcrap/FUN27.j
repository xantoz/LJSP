.class FUN27
.super Procedure

.field public static final t LLispObject;

.method static <clinit>()V
    .limit locals 0
    .limit stack 1

    ldc_w "t"
    invokestatic Symbol.intern(Ljava/lang/String;)LSymbol;
    putstatic FUN27/t LLispObject;
    
    return
.end method

.method public <init>()V
    .limit stack 2
    .limit locals 1

    aload_0
    ldc "FUN27"
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
;; (if (= n 0) a (calc-fib (- n 1) b (+ a b)))
aload 5
new LispFixnum
dup
ldc2_w 0
invokenonvirtual LispFixnum.<init>(J)V
invokevirtual java/lang/Object.equals(Ljava/lang/Object;)Z
ifeq L188
getstatic FUN27/t LLispObject;
goto L187
L188:
aconst_null
L187:
ifnonnull L186 ; branches to the true-expr
;; (calc-fib (- n 1) b (+ a b))
aload 0
; preparing args
ldc_w 3
anewarray LispObject
dup
ldc_w 0
aload 5
checkcast LispNumber
new LispFixnum
dup
ldc2_w 1
invokenonvirtual LispFixnum.<init>(J)V
checkcast LispNumber
invokevirtual LispNumber.sub(LLispNumber;)LLispNumber;
aastore
dup
ldc_w 1
aload 7
aastore
dup
ldc_w 2
aload 6
checkcast LispNumber
aload 7
checkcast LispNumber
invokevirtual LispNumber.add(LLispNumber;)LLispNumber;
aastore
; end preparing args
invokevirtual Procedure.run([LLispObject;)LLispObject;
goto L185 ; Don't also run the true-expr like a fool
L186:
aload 6
L185:
;; endif
areturn
;; endlambda
.end method
