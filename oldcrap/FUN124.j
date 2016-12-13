.class FUN124
.super Procedure

.field public static final t LLispObject;

.method static <clinit>()V
    .limit locals 0
    .limit stack 1

    ldc_w "t"
    invokestatic Symbol.intern(Ljava/lang/String;)LSymbol;
    putstatic FUN124/t LLispObject;
    
    return
.end method

.method public <init>()V
    .limit stack 2
    .limit locals 1

    aload_0
    ldc "FUN124"
    invokenonvirtual Procedure.<init>(Ljava/lang/String;)V
    return
.end method

.method public run([LLispObject;)LLispObject;
.limit stack  255
.limit locals 255
;; (lambda nil (print 123.4))
;; (print 123.4)
getstatic java/lang/System/out Ljava/io/PrintStream;
new LispFlonum
dup
ldc2_w 123.4
invokenonvirtual LispFlonum.<init>(D)V;
dup
astore_3 ; store in the temp variable
dup
ifnull L2
invokevirtual java/lang/Object.toString()Ljava/lang/String;
goto L1
L2:
pop
ldc "nil"
L1:
invokevirtual java/io/PrintStream.println(Ljava/lang/String;)V
aload_3 ; we return what we got
areturn
;; endlambda
.end method
