; This is slightly less manual. Only driver like stuff is manual here.
.class ManualFoob4
.super Procedure

.field public static t LLispObject;

.method public toString()Ljava/lang/String;
    ldc "#<a knockup of very nearly complete (nlambda fib (n) (if (= n 0) 0 (if (= n 1) 1 (+ (fib (- n 1)) (fib (- n 2))))))>"
    areturn
.end method



.method static <clinit>()V
    .limit locals 0
    .limit stack 1

    ldc_w "t"
    invokestatic Symbol.intern(Ljava/lang/String;)LSymbol;
    putstatic	ManualFoob4/t LLispObject;
    
    return
.end method

.method public <init>()V
    .limit stack 2
    .limit locals 1

    aload_0
    ldc "WASFFG"
    invokenonvirtual Procedure.<init>(Ljava/lang/String;)V
    return
.end method

.method public run([LLispObject;)LLispObject;
    .limit locals 255
    .limit stack 255

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
    ifeq L12
    getstatic ManualFoob4/t LLispObject;  ; This here was truly manual
    goto L11
L12:
    aconst_null
L11:
    ifnonnull L10 ; branches to the true-expr
    ;; (if (= n 1) 1 (+ (fib (- n 1)) (fib (- n 2))))
    aload 5
    new LispFixnum
    dup
    ldc2_w 1
    invokenonvirtual LispFixnum.<init>(J)V
    invokevirtual java/lang/Object.equals(Ljava/lang/Object;)Z
    ifeq L16
    getstatic ManualFoob4/t LLispObject;  ; This too was truly manual

    
    ; dup
    ; invokevirtual java/lang/String.toString()Ljava/lang/String;
    ; getstatic java/lang/System/out Ljava/io/PrintStream;
    ; swap
    ; invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
    
    goto L15
L16:
    aconst_null
L15:
    ifnonnull L14 ; branches to the true-expr
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
    goto L13 ; Don't also run the true-expr like a fool
L14:
    new LispFixnum
    dup
    ldc2_w 1
    invokenonvirtual LispFixnum.<init>(J)V
L13:
    ;; endif
    goto L9 ; Don't also run the true-expr like a fool
L10:
    new LispFixnum
    dup
    ldc2_w 0
    invokenonvirtual LispFixnum.<init>(J)V
L9:
    ;; endif
    areturn
    ;; endlambda

.end method


