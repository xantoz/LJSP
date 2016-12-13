.class FUN2
.super Procedure

.field public static final t LLispObject;

.method static <clinit>()V
    .limit locals 0
    .limit stack 1

    ldc_w "t"
    invokestatic Symbol.intern(Ljava/lang/String;)LSymbol;
    putstatic FUN2/t LLispObject;
    
    return
.end method

.method public <init>()V
    .limit stack 2
    .limit locals 1

    aload_0
    ldc "FUN2"
    invokenonvirtual Procedure.<init>(Ljava/lang/String;)V
    return
.end method

.method public run([LLispObject;)LLispObject;
.limit stack  255
.limit locals 255
;; (lambda (n) (if (= n 0) 0 (if (= n 1) 1 (+ (fib (- n 1)) (fib (- n 2))))))
aload_1
ldc_w 0
aaload
astore 5
;; (if (= n 0) 0 (if (= n 1) 1 (+ (fib (- n 1)) (fib (- n 2)))))
aload 5
new LispFixnum
dup
ldc2_w 0
invokenonvirtual LispFixnum.<init>(J)V
invokevirtual java/lang/Object.equals(Ljava/lang/Object;)Z
ifeq L60
getstatic FUN2/t LLispObject;
goto L59
L60:
aconst_null
L59:
ifnonnull L58 ; branches to the true-expr
;; (if (= n 1) 1 (+ (fib (- n 1)) (fib (- n 2))))
aload 5
new LispFixnum
dup
ldc2_w 1
invokenonvirtual LispFixnum.<init>(J)V
invokevirtual java/lang/Object.equals(Ljava/lang/Object;)Z
ifeq L64
getstatic FUN2/t LLispObject;
goto L63
L64:
aconst_null
L63:
ifnonnull L62 ; branches to the true-expr
;; (fib (- n 1))
aload 0
; preparing args
ldc_w 1
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
; end preparing args
invokevirtual Procedure.run([LLispObject;)LLispObject;
checkcast LispNumber
;; (fib (- n 2))
aload 0
; preparing args
ldc_w 1
anewarray LispObject
dup
ldc_w 0
aload 5
checkcast LispNumber
new LispFixnum
dup
ldc2_w 2
invokenonvirtual LispFixnum.<init>(J)V
checkcast LispNumber
invokevirtual LispNumber.sub(LLispNumber;)LLispNumber;
aastore
; end preparing args
invokevirtual Procedure.run([LLispObject;)LLispObject;
checkcast LispNumber
invokevirtual LispNumber.add(LLispNumber;)LLispNumber;
goto L61 ; Don't also run the true-expr like a fool
L62:
new LispFixnum
dup
ldc2_w 1
invokenonvirtual LispFixnum.<init>(J)V
L61:
;; endif
goto L57 ; Don't also run the true-expr like a fool
L58:
new LispFixnum
dup
ldc2_w 0
invokenonvirtual LispFixnum.<init>(J)V
L57:
;; endif
areturn
;; endlambda
.end method
