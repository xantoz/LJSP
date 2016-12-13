.class QUOTE-TEST
.super Procedure

.field private static final t LLispObject;
.field private static final literal_1 LLispObject;


.method static <clinit>()V
    .limit locals 255
    .limit stack 255

    ldc_w "t"
    invokestatic Symbol.intern(Ljava/lang/String;)LSymbol;
    putstatic QUOTE-TEST/t LLispObject;
    new Cons
dup
new LispChar
dup
bipush 87
invokenonvirtual LispChar.<init>(C)V
new Cons
dup
new Cons
dup
ldc_w "1231312312312312312312312312312313123"
invokestatic LispBignum.parse(Ljava.lang.String;)LLispBignum;
new LispFixnum
dup
ldc2_w 5343412914294967296
invokenonvirtual LispFixnum.<init>(J)V
invokenonvirtual Cons.<init>(LLispObject;LLispObject;)V
new Cons
dup
new Cons
dup
new LispFlonum
dup
;; jasmin lacks all sort of documentation on how to push a NaN double. Division by zero works as a work-around.
dconst_0
dconst_0
ddiv
invokenonvirtual LispFlonum.<init>(D)V
new Cons
dup
new LispFlonum
dup
;; hackaround for positive infinity
ldc2_w 1.0d
dconst_0
ddiv
invokenonvirtual LispFlonum.<init>(D)V
new Cons
dup
new LispFlonum
dup
;; hackaround for negative infinity
ldc2_w -1.0d
dconst_0
ddiv
invokenonvirtual LispFlonum.<init>(D)V
aconst_null
invokenonvirtual Cons.<init>(LLispObject;LLispObject;)V
invokenonvirtual Cons.<init>(LLispObject;LLispObject;)V
invokenonvirtual Cons.<init>(LLispObject;LLispObject;)V
new Cons
dup
ldc_w "b"
invokestatic Symbol.intern(Ljava.lang.String;)LSymbol;
new Cons
dup
new LispArray
dup
ldc_w 4
anewarray LispObject
dup
ldc_w 3
new Cons
dup
new LispFixnum
dup
ldc2_w 1
invokenonvirtual LispFixnum.<init>(J)V
new Cons
dup
new LispFixnum
dup
ldc2_w 2
invokenonvirtual LispFixnum.<init>(J)V
new Cons
dup
new LispFixnum
dup
ldc2_w 3
invokenonvirtual LispFixnum.<init>(J)V
aconst_null
invokenonvirtual Cons.<init>(LLispObject;LLispObject;)V
invokenonvirtual Cons.<init>(LLispObject;LLispObject;)V
invokenonvirtual Cons.<init>(LLispObject;LLispObject;)V
aastore
dup
ldc_w 2
ldc_w "fisk"
invokestatic Symbol.intern(Ljava.lang.String;)LSymbol;
aastore
dup
ldc_w 1
ldc_w "din"
invokestatic Symbol.intern(Ljava.lang.String;)LSymbol;
aastore
dup
ldc_w 0
ldc_w "hej"
invokestatic Symbol.intern(Ljava.lang.String;)LSymbol;
aastore
invokenonvirtual LispArray.<init>([LLispObject;)V
new Cons
dup
new LispString
dup
ldc_w "potatismossa"
invokenonvirtual LispString.<init>(Ljava.lang.String;)V
new LispFlonum
dup
ldc2_w 12.4d
invokenonvirtual LispFlonum.<init>(D)V
invokenonvirtual Cons.<init>(LLispObject;LLispObject;)V
invokenonvirtual Cons.<init>(LLispObject;LLispObject;)V
invokenonvirtual Cons.<init>(LLispObject;LLispObject;)V
invokenonvirtual Cons.<init>(LLispObject;LLispObject;)V
invokenonvirtual Cons.<init>(LLispObject;LLispObject;)V
invokenonvirtual Cons.<init>(LLispObject;LLispObject;)V
putstatic QUOTE-TEST/literal_1 LLispObject;
    return
.end method

.method public <init>()V
    .limit stack 2
    .limit locals 1

    aload_0
    ldc "QUOTE-TEST"
    invokenonvirtual Procedure.<init>(Ljava/lang/String;)V
    return
.end method

.method public run([LLispObject;)LLispObject;
.limit stack  255
.limit locals 255
;; (lambda (a) (cons a (quote (#\W (1231312312312312312312312312312313123 . 5343412914294967296) (NaN Infinity -Infinity) b #(hej din fisk (1 2 3)) "potatismossa" . 12.4))))
aload_1
ldc_w 0
aaload
astore 5
Lselftail:
new Cons
dup
aload 5
getstatic QUOTE-TEST/literal_1 LLispObject;
invokenonvirtual Cons.<init>(LLispObject;LLispObject;)V
areturn
;; endlambda
.end method
