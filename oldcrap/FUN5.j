.class FUN5
.super Procedure

.field public static final t LLispObject;

.method static <clinit>()V
    .limit locals 0
    .limit stack 1

    ldc_w "t"
    invokestatic Symbol.intern(Ljava/lang/String;)LSymbol;
    putstatic FUN5/t LLispObject;
    
    return
.end method

.method public <init>()V
    .limit stack 2
    .limit locals 1

    aload_0
    ldc "FUN5"
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
astore_3 ; store in the temp variable
dup
ifnull L80
invokevirtual java/lang/Object.toString()Ljava/lang/String;
goto L79
L80:
pop
ldc "nil"
L79:
invokevirtual java/io/PrintStream.println(Ljava/lang/String;)V
aload_3 ; we return what we got
pop
;; (if (= n 1) nil (collatz (if (= (mod n 2) 0) (/ n 2) (+ 1 (* n 3)))))
aload 5
new LispFixnum
dup
ldc2_w 1
invokenonvirtual LispFixnum.<init>(J)V
invokevirtual java/lang/Object.equals(Ljava/lang/Object;)Z
ifeq L84
getstatic FUN5/t LLispObject;
goto L83
L84:
aconst_null
L83:
ifnonnull L82 ; branches to the true-expr
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
ifeq L88
getstatic FUN5/t LLispObject;
goto L87
L88:
aconst_null
L87:
ifnonnull L86 ; branches to the true-expr
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
goto L85 ; Don't also run the true-expr like a fool
L86:
aload 5
checkcast LispNumber
new LispFixnum
dup
ldc2_w 2
invokenonvirtual LispFixnum.<init>(J)V
checkcast LispNumber
invokevirtual LispNumber.div(LLispNumber;)LLispNumber;
L85:
;; endif
aastore
; end preparing args
invokevirtual Procedure.run([LLispObject;)LLispObject;
goto L81 ; Don't also run the true-expr like a fool
L82:
aconst_null
L81:
;; endif
areturn
;; endlambda
.end method
