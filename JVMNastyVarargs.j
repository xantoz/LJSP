; --- Copyright Jonathan Meyer 1996. All rights reserved. -----------------
; File:      jasmin/examples/NewArray.j
; Author:    Jonathan Meyer, 10 July 1996
; Purpose:   Example of newarray
; -------------------------------------------------------------------------
;
; Example showing how to allocate an array using
; newarray.
;

.class public JVMNastyVarargs
.super java/lang/Object

.method public <init>()V
    aload_0
    invokenonvirtual java/lang/Object/<init>()V
    return
.end method

.method public static wee()V;
    
.end method


.method public static main([Ljava/lang/String;)V
    .limit stack 4
    .limit locals 2


    
    invokestatic JVMNastyVarargs.wee()V
    

    ; ; create an array like:
    ; ;
    ; ;     boolean b[] = new boolean[2]
    ; ;
    ; ; (stores it in local var 1)

    ; iconst_2
    ; newarray boolean
    ; astore_1

    ; ; b[0] = true;
    ; aload_1
    ; iconst_0
    ; iconst_1
    ; bastore

    return
.end method
