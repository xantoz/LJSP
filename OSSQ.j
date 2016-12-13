.class OSSQ
.super Procedure

.field public static final t LLispObject;

.method static <clinit>()V
    .limit locals 0
    .limit stack 1

    ldc_w "t"
    invokestatic Symbol.intern(Ljava/lang/String;)LSymbol;
    putstatic OSSQ/t LLispObject;
    
    return
.end method

.method public <init>()V
    .limit stack 2
    .limit locals 1

    aload_0
    ldc "OSSQ"
    invokenonvirtual Procedure.<init>(Ljava/lang/String;)V
    return
.end method

.method public run([LLispObject;)LLispObject;
.limit stack  255
.limit locals 255
;; (lambda (key alist) (if (eq? alist nil) ((lambda nil nil)) (if (eq? key (car (car alist))) (car alist) (assq key (cdr alist)))))
aload_1
ldc_w 0
aaload
astore 5
aload_1
ldc_w 1
aaload
astore 6
Lselftail:
;; (if (eq? alist nil) ((lambda nil nil)) (if (eq? key (car (car alist))) (car alist) (assq key (cdr alist))))
aload 6
aconst_null
if_acmpne L67
getstatic OSSQ/t LLispObject;
goto L66
L67:
aconst_null
L66:
ifnonnull L65 ; branches to the true-expr
;; (if (eq? key (car (car alist))) (car alist) (assq key (cdr alist)))
aload 5
aload 6
dup
ifnull L73
checkcast Cons
getfield Cons/car LLispObject;
L73:
dup
ifnull L72
checkcast Cons
getfield Cons/car LLispObject;
L72:
if_acmpne L71
getstatic OSSQ/t LLispObject;
goto L70
L71:
aconst_null
L70:
ifnonnull L69 ; branches to the true-expr
;; self-recursive tail-call args: (key (cdr alist))
aload 5
aload 6
dup
ifnull L74
checkcast Cons
getfield Cons/cdr LLispObject;
L74:
astore 6
astore 5
goto Lselftail
goto L68 ; Don't also run the true-expr like a fool
L69:
aload 6
dup
ifnull L75
checkcast Cons
getfield Cons/car LLispObject;
L75:
L68:
;; endif
goto L64 ; Don't also run the true-expr like a fool
L65:
;; ((lambda nil nil))
new FUN1158867388
dup
invokenonvirtual FUN1158867388.<init>()V
checkcast Procedure
; preparing args
ldc_w 0
anewarray LispObject
; end preparing args
invokevirtual Procedure.run([LLispObject;)LLispObject;
L64:
;; endif
areturn
;; endlambda
.end method
