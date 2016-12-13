;; Manually fixed fib mockup (the funcalls are manually added)

.class ManualFoob2
.super Procedure

.method public toString()Ljava/lang/String;
    ldc "#<a mazy mockup of (nlambda fib (n) (if (= n 0) 0 (if (= n 1) 1 (+ (fib (- n 1)) (fib (- n 2))))))>"
    areturn
.end method


.method public <init>()V
    .limit stack 2
    .limit locals 1

    aload_0
    ldc "ManualFoob2"
    invokenonvirtual Procedure.<init>(Ljava/lang/String;)V
    return
.end method

;; Written not entirely manually...
.method public run([LLispObject;)LLispObject;
    .limit locals 255
    .limit stack 255

    ;; Manually coded unpackage of array. In a "real" version we'd probably astore to 5 instead so we can keep the array for shits and giggles... (actually we might keep it for closure purposes! gosh this idea just came to me! although we would probably need to always access the array directly). The checkcast might or mighn't be in a real version.
    aload 1
    iconst_0
    aaload
    ; checkcast LispFixnum  ; This could be done using SBCL-style optional declarations that also are assertions
    astore 2
    
    ;; (lambda (n) (if (= n 0) 0 (if (= n 1) 1 (+ (fib (- n 1)) (fib (- n 2))))))
    ;; (if (= n 0) 0 (if (= n 1) 1 (+ (fib (- n 1)) (fib (- n 2)))))
    aload 2
    new LispFixnum
    dup
    ldc2_w 0
    invokenonvirtual LispFixnum.<init>(J)V
    invokevirtual java/lang/Object.equals(Ljava/lang/Object;)Z
    ifeq L4
    new LispFixnum
    dup
    ldc2_w 123
    invokenonvirtual LispFixnum.<init>(J)V
    goto L3
L4:
    aconst_null
L3:
    ifnonnull L2 ; branches to the true-expr
    ;; (if (= n 1) 1 (+ (fib (- n 1)) (fib (- n 2))))
    aload 2
    new LispFixnum
    dup
    ldc2_w 1
    invokenonvirtual LispFixnum.<init>(J)V
    invokevirtual java/lang/Object.equals(Ljava/lang/Object;)Z
    ifeq L8
    new LispFixnum
    dup
    ldc2_w 123
    invokenonvirtual LispFixnum.<init>(J)V
    goto L7
L8:
    aconst_null
L7:
    ifnonnull L6 ; branches to the true-expr


    ; Obtained from disasm of ManualFoob3, with some changes
    aload 0
    iconst_1
    anewarray	LispObject
    dup
    iconst_0
    aload 2
    checkcast	LispNumber
    new	LispFixnum
    dup
    ldc2_w 1
    invokenonvirtual	LispFixnum.<init>(J)V
    invokevirtual	LispNumber.sub(LLispNumber;)LLispNumber;
    aastore
    invokevirtual	Procedure.run([LLispObject;)LLispObject;
    checkcast	LispNumber
    aload 0
    iconst_1
    anewarray	LispObject
    dup
    iconst_0
    aload 2
    checkcast	LispNumber
    new	LispFixnum
    dup
    ldc2_w	2
    invokenonvirtual	LispFixnum.<init>(J)V
    invokevirtual	LispNumber.sub(LLispNumber;)LLispNumber;
    aastore
    invokevirtual	Procedure.run([LLispObject;)LLispObject;
    checkcast	LispNumber
    invokevirtual	LispNumber.add(LLispNumber;)LLispNumber;

    ; aload 0
    ; aload 2
    ; checkcast LispNumber
    ; new LispFixnum
    ; dup
    ; ldc2_w 1
    ; invokenonvirtual LispFixnum.<init>(J)V
    ; checkcast LispNumber
    ; invokevirtual LispNumber.sub(LLispNumber;)LLispNumber;
    ; invokevirtual ManualFoob.run(LLispObject;)LLispObject;
    ; checkcast LispNumber
    ; aload 0
    ; aload 2
    ; checkcast LispNumber
    ; new LispFixnum
    ; dup
    ; ldc2_w 2
    ; invokenonvirtual LispFixnum.<init>(J)V
    ; checkcast LispNumber
    ; invokevirtual LispNumber.sub(LLispNumber;)LLispNumber;
    ; invokevirtual ManualFoob.run(LLispObject;)LLispObject;
    ; checkcast LispNumber
    ; invokevirtual LispNumber.add(LLispNumber;)LLispNumber;

    
    goto L5 ; Don't also run the true-expr like a fool
L6:
    new LispFixnum
    dup
    ldc2_w 1
    invokenonvirtual LispFixnum.<init>(J)V
L5:
    ;; endif
    goto L1 ; Don't also run the true-expr like a fool
L2:
    new LispFixnum
    dup
    ldc2_w 0
    invokenonvirtual LispFixnum.<init>(J)V
L1:
    ;; endif
    areturn
    ;; endlambda
.end method


