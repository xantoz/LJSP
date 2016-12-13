.bytecode 45.0

.class DriverMock
.super Procedure

.method public toString()Ljava/lang/String;
    ldc "#<compiler driver mockery yeah>"
    areturn
.end method


.method public <init>()V
    .limit stack 2
    .limit locals 1

    aload_0
    ldc "DriverMockery"
    invokenonvirtual Procedure.<init>(Ljava/lang/String;)V
    return
.end method


.method public run([LLispObject;)LLispObject;
    .limit locals 256
    .limit stack 256
    
    ; aload_1
    ; iconst_0
    ; aaload
    ; aload_1
    ; iconst_1
    ; aaload
    ; jsr_w FUNfoo
    ; areturn

    aload_1
    iconst_0
    aaload
    ; jsr_w FUNbar
    jsr FUNfib

    areturn

    
FUNfoo:
    ;; (lambda (a b) (+ a (- b 2)))
    astore 255     ; store return address in variable 255
    astore 6
    astore 5
    aload 5
    checkcast LispNumber
    aload 6
    checkcast LispNumber
    new LispFixnum
    dup
    ldc2_w 2
    invokenonvirtual LispFixnum.<init>(J)V
    checkcast LispNumber
    invokevirtual LispNumber.sub(LLispNumber;)LLispNumber;
    checkcast LispNumber
    invokevirtual LispNumber.add(LLispNumber;)LLispNumber;
    ret 255
    ;; endlambda
    
FUNbar:
    ;; (lambda (a) (if (= 10 a) (+ a 23) (- a 23)))
    astore 255     ; store return address in variable 255
    astore 5
    ;; (if (= 10 a) (+ a 23) (- a 23))
    new LispFixnum
    dup
    ldc2_w 10
    invokenonvirtual LispFixnum.<init>(J)V
    aload 5
    invokevirtual java/lang/Object.equals(Ljava/lang/Object;)Z
    ifeq L4
    new LispFixnum
    dup
    ldc2_w 1
    invokenonvirtual LispFixnum.<init>(J)V
    goto L3
L4:
    aconst_null
L3:
    ifnonnull L2 ; branches to the true-expr
    aload 5
    checkcast LispNumber
    new LispFixnum
    dup
    ldc2_w 23
    invokenonvirtual LispFixnum.<init>(J)V
    checkcast LispNumber
    invokevirtual LispNumber.sub(LLispNumber;)LLispNumber;
    goto L1 ; Don't also run the true-expr like a fool
L2:
    aload 5
    checkcast LispNumber
    new LispFixnum
    dup
    ldc2_w 23
    invokenonvirtual LispFixnum.<init>(J)V
    checkcast LispNumber
    invokevirtual LispNumber.add(LLispNumber;)LLispNumber;
L1:
    ;; endif
    ret 255
    ;; endlambda


FUNfib:
    ;; (lambda (n) (if (= n 0) 0 (if (= n 1) 1 (+ (fib (- n 1)) (fib (- n 2))))))
    ; astore 255     ; store return address in variable 255
    swap 
    astore 5
    ;; (if (= n 0) 0 (if (= n 1) 1 (+ (fib (- n 1)) (fib (- n 2)))))
    aload 5
    new LispFixnum
    dup
    ldc2_w 0
    invokenonvirtual LispFixnum.<init>(J)V
    invokevirtual java/lang/Object.equals(Ljava/lang/Object;)Z
    ifeq L68
    new LispFixnum
    dup
    ldc2_w 1
    invokenonvirtual LispFixnum.<init>(J)V
    goto L67
L68:
    aconst_null
L67:
    ifnonnull L66 ; branches to the true-expr
    ;; (if (= n 1) 1 (+ (fib (- n 1)) (fib (- n 2))))
    aload 5
    new LispFixnum
    dup
    ldc2_w 1
    invokenonvirtual LispFixnum.<init>(J)V
    invokevirtual java/lang/Object.equals(Ljava/lang/Object;)Z
    ifeq L72
    new LispFixnum
    dup
    ldc2_w 1
    invokenonvirtual LispFixnum.<init>(J)V
    goto L71
L72:
    aconst_null
L71:
    ifnonnull L70 ; branches to the true-expr


    ;;funcallnotimpl fult manuellt implementerat foer test
    aload 5
    ; aload 255

    ; ; manuellt inpastad (- n 1)
    aload 5
    checkcast LispNumber
    new LispFixnum
    dup
    ldc2_w 1
    invokenonvirtual LispFixnum.<init>(J)V
    checkcast LispNumber
    invokevirtual LispNumber.sub(LLispNumber;)LLispNumber;

    jsr FUNfib
    astore 1                                                ; temp var
    ; astore 255
    astore 5
    aload 1

    checkcast LispNumber
    
    ;; funcallnotimpl fult manuellt
    aload 5
    aload 255

    ; manuellt inpastad (- n 2)
    aload 5
    checkcast LispNumber
    new LispFixnum
    dup
    ldc2_w 2
    invokenonvirtual LispFixnum.<init>(J)V
    checkcast LispNumber
    invokevirtual LispNumber.sub(LLispNumber;)LLispNumber;

    jsr FUNfib
    astore 1                                                ; temp var
    astore 255
    astore 5
    aload 1
    
    checkcast LispNumber
    
    invokevirtual LispNumber.add(LLispNumber;)LLispNumber;
    goto L69 ; Don't also run the true-expr like a fool
L70:
    new LispFixnum
    dup
    ldc2_w 1
    invokenonvirtual LispFixnum.<init>(J)V
L69:
    ;; endif
    goto L65 ; Don't also run the true-expr like a fool
L66:
    new LispFixnum
    dup
    ldc2_w 0
    invokenonvirtual LispFixnum.<init>(J)V
L65:
    ;; endif
    swap
    astore 255
    ret 255
    ;; endlambda
.end method


; ;; (new DriverMock()).run(new LispObject[]{new LispFixnum(argv[1])}))
; .method public static main([Ljava/lang/String;)V
;     .limit locals 255
;     .limit stack 255

;     invokestatic LispFixnum.parse(Ljava/lang/String;)LLispFixnum;
;     iconst_0
;     aaload

;     iconst_1
;     anewarray LLispObject;
;     dup

;     iconst_0
;     swap
;     aastore

;     new DriverMock
;     dup
;     invokenonvirtual DriverMock.<init>()V

;     invokevirtual DriverMock.run([LLispObject;)LLispObject;
;     invokevirtual LispObject.toString()Ljava/lang/String;

;     getstatic java/lang/System/out Ljava/io/PrintStream;
;     swap
;     invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V

;     return
; .end method

