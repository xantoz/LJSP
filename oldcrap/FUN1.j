.class FUN1
.super Procedure

.field public static final t LLispObject;

.method static <clinit>()V
    .limit locals 0
    .limit stack 1

    ldc_w "t"
    invokestatic Symbol.intern(Ljava/lang/String;)LSymbol;
    putstatic FUN1/t LLispObject;
    
    return
.end method

.method public <init>()V
    .limit stack 2
    .limit locals 1

    aload_0
    ldc "FUN1"
    invokenonvirtual Procedure.<init>(Ljava/lang/String;)V
    return
.end method

.method public run([LLispObject;)LLispObject;
.limit stack  255
.limit locals 255
;; (lambda (n) (print n) (if (= n 1) nil (collatz (if (= (mod n 2) 0) (/ n 2) (+ 1 (* n 3))))))
aload_1
ldc_w 0
aaload
astore 5
;; (print n)
getstatic java/lang/System/out Ljava/io/PrintStream;
aload 5
dup
ifnull L48
invokevirtual java/lang/Object.toString()Ljava/lang/String;
goto L47
L48:
pop
ldc "nil"
L47:
invokevirtual java/io/PrintStream.println(Ljava/lang/String;)V
pop
;; (if (= n 1) nil (collatz (if (= (mod n 2) 0) (/ n 2) (+ 1 (* n 3)))))
aload 5
new LispFixnum
dup
ldc2_w 1
invokenonvirtual LispFixnum.<init>(J)V
invokevirtual java/lang/Object.equals(Ljava/lang/Object;)Z
ifeq L52
getstatic FUN1/t LLispObject;
goto L51
L52:
aconst_null
L51:
ifnonnull L50 ; branches to the true-expr
;; (collatz (if (= (mod n 2) 0) (/ n 2) (+ 1 (* n 3))))
aload 0
; preparing args
ldc_w 1
anewarray LispObject
dup
ldc_w 0
;; (if (= (mod n 2) 0) (/ n 2) (+ 1 (* n 3)))
aload 5
checkcast LispInteger
new LispFixnum
dup
ldc2_w 2
invokenonvirtual LispFixnum.<init>(J)V
checkcast LispInteger
invokevirtual LispInteger.mod(LLispInteger;)LLispInteger;
new LispFixnum
dup
ldc2_w 0
invokenonvirtual LispFixnum.<init>(J)V
invokevirtual java/lang/Object.equals(Ljava/lang/Object;)Z
ifeq L56
getstatic FUN1/t LLispObject;
goto L55
L56:
aconst_null
L55:
ifnonnull L54 ; branches to the true-expr
new LispFixnum
dup
ldc2_w 1
invokenonvirtual LispFixnum.<init>(J)V
checkcast LispNumber
aload 5
checkcast LispNumber
new LispFixnum
dup
ldc2_w 3
invokenonvirtual LispFixnum.<init>(J)V
checkcast LispNumber
invokevirtual LispNumber.mul(LLispNumber;)LLispNumber;
checkcast LispNumber
invokevirtual LispNumber.add(LLispNumber;)LLispNumber;
goto L53 ; Don't also run the true-expr like a fool
L54:
aload 5
checkcast LispNumber
new LispFixnum
dup
ldc2_w 2
invokenonvirtual LispFixnum.<init>(J)V
checkcast LispNumber
invokevirtual LispNumber.div(LLispNumber;)LLispNumber;
L53:
;; endif
aastore
; end preparing args
invokevirtual Procedure.run([LLispObject;)LLispObject;
goto L49 ; Don't also run the true-expr like a fool
L50:
aconst_null
L49:
;; endif
areturn
;; endlambda
.end method
