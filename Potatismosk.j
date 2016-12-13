

.class public Potatismosk
.super java/lang/Object


.method public ran()LLispObject;
    .limit stack 512
    .limit locals 512

    new LispFixnum
    dup
    ldc2_w 34 
    invokenonvirtual LispFixnum.<init>(J)V
    pop
    new LispFixnum
    dup
    ldc2_w 55
    invokenonvirtual LispFixnum.<init>(J)V
    
    areturn
.end method

.method public <init>()V
   aload_0
   invokenonvirtual java/lang/Object/<init>()V
   return
.end method

