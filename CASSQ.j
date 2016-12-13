.class CASSQ
.super Procedure

.field private static final t LLispObject;


.method static <clinit>()V
    .limit locals 255
    .limit stack 255

    ldc_w "t"
    invokestatic Symbol.intern(Ljava/lang/String;)LSymbol;
    putstatic CASSQ/t LLispObject;
    
    return
.end method

.method public <init>()V
    .limit stack 2
    .limit locals 1

    aload_0
    ldc "CASSQ"
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
if_acmpne L16
getstatic CASSQ/t LLispObject;
goto L15
L16:
aconst_null
L15:
ifnonnull L14 ; branches to the true-expr
;; (if (eq? key (car (car alist))) (car alist) (assq key (cdr alist)))
aload 5
aload 6
dup
ifnull L22
checkcast Cons
getfield Cons/car LLispObject;
L22:
dup
ifnull L21
checkcast Cons
getfield Cons/car LLispObject;
L21:
if_acmpne L20
getstatic CASSQ/t LLispObject;
goto L19
L20:
aconst_null
L19:
ifnonnull L18 ; branches to the true-expr
;; self-recursive tail-call args: (key (cdr alist))
aload 5
aload 6
dup
ifnull L23
checkcast Cons
getfield Cons/cdr LLispObject;
L23:
astore 6
astore 5
goto Lselftail
goto L17 ; Don't also run the true-expr like a fool
L18:
aload 6
dup
ifnull L24
checkcast Cons
getfield Cons/car LLispObject;
L24:
L17:
;; endif
goto L13 ; Don't also run the true-expr like a fool
L14:
;; ((lambda nil nil))
new FUN1608662707
dup
invokenonvirtual FUN1608662707.<init>()V
checkcast Procedure
; preparing args
aconst_null
; end preparing args
invokevirtual Procedure.run([LLispObject;)LLispObject;
L13:
;; endif
areturn
;; endlambda
.end method
