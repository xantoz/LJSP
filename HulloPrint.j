.class HulloPrint
.super LispSubr


.method public run([LLispObject;)LLispObject;
    .limit stack 512
    .limit locals 512

    ; Push java.lang.System.out on stack
    getstatic java/lang/System/out Ljava/io/PrintStream; 
    ldc "kkorejhej"
    invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V

    getstatic java/lang/System/out Ljava/io/PrintStream; 
    aload_1
    iconst_0
    aaload
    invokevirtual java/lang/Object.toString()Ljava/lang/String;
    invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
    
    aload_0
    ; aconst_null
    areturn
.end method


;
; standard initializer
.method public <init>()V
    .limit stack 100
    .limit locals 100
    
    aload_0
    ldc "bullkorv"
    ; invokenonvirtual LispSubr/<init>()V
    invokenonvirtual LispSubr/<init>(Ljava/lang/String;)V
    return
.end method
