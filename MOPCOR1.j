.class MOPCOR1
.super Procedure

.field public static final t LLispObject;

.method static <clinit>()V
    .limit locals 0
    .limit stack 1

    ldc_w "t"
    invokestatic Symbol.intern(Ljava/lang/String;)LSymbol;
    putstatic MOPCOR1/t LLispObject;
    
    return
.end method

.method public <init>()V
    .limit stack 2
    .limit locals 1

    aload_0
    ldc "MOPCOR1"
    invokenonvirtual Procedure.<init>(Ljava/lang/String;)V
    return
.end method

.method public run([LLispObject;)LLispObject;
.limit stack  255
.limit locals 255
;; (lambda (fnx lstx) (if lstx (cons (fnx (car lstx)) (mapcar1 fnx (cdr lstx))) nil))
aload_1
ldc_w 0
aaload
astore 5
aload_1
ldc_w 1
aaload
astore 6
Lselftail:
;; (if lstx (cons (fnx (car lstx)) (mapcar1 fnx (cdr lstx))) nil)
aload 6
ifnonnull L31 ; branches to the true-expr
aconst_null
goto L30 ; Don't also run the true-expr like a fool
L31:
new Cons
dup
;; (fnx (car lstx))
aload 5
checkcast Procedure
; preparing args
ldc_w 1
anewarray LispObject
dup
ldc_w 0
aload 6
dup
ifnull L32
checkcast Cons
getfield Cons/car LLispObject;
L32:
aastore
; end preparing args
invokevirtual Procedure.run([LLispObject;)LLispObject;
;; (mapcar1 fnx (cdr lstx))
aload 0
checkcast Procedure
; preparing args
ldc_w 2
anewarray LispObject
dup
ldc_w 0
aload 5
aastore
dup
ldc_w 1
aload 6
dup
ifnull L33
checkcast Cons
getfield Cons/cdr LLispObject;
L33:
aastore
; end preparing args
invokevirtual Procedure.run([LLispObject;)LLispObject;
invokenonvirtual Cons.<init>(LLispObject;LLispObject;)V
L30:
;; endif
areturn
;; endlambda
.end method
