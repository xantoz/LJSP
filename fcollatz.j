.class fcollatz
.super Procedure

.field public static final t LLispObject;

.method static <clinit>()V
    .limit locals 0
    .limit stack 1

    ldc_w "t"
    invokestatic Symbol.intern(Ljava/lang/String;)LSymbol;
    putstatic fcollatz/t LLispObject;
    
    return
.end method

.method public <init>()V
    .limit stack 2
    .limit locals 1

    aload_0
    ldc "fcollatz"
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
Lselftail:
;; (print n)
getstatic java/lang/System/out Ljava/io/PrintStream;
aload 5
dup
astore_3 ; store in the temp variable
dup
ifnull L22
invokevirtual java/lang/Object.toString()Ljava/lang/String;
goto L21
L22:
pop
ldc "nil"
L21:
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
ifeq L26
getstatic fcollatz/t LLispObject;
goto L25
L26:
aconst_null
L25:
ifnonnull L24 ; branches to the true-expr
;; self-recursive tail-call args: ((if (= (mod n 2) 0) (/ n 2) (+ 1 (* n 3))))
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
ifeq L30
getstatic fcollatz/t LLispObject;
goto L29
L30:
aconst_null
L29:
ifnonnull L28 ; branches to the true-expr
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
goto L27 ; Don't also run the true-expr like a fool
L28:
aload 5
checkcast LispNumber
new LispFixnum
dup
ldc2_w 2
invokenonvirtual LispFixnum.<init>(J)V
checkcast LispNumber
invokevirtual LispNumber.div(LLispNumber;)LLispNumber;
L27:
;; endif
astore 5
goto Lselftail
goto L23 ; Don't also run the true-expr like a fool
L24:
aconst_null
L23:
;; endif
areturn
;; endlambda
.end method
