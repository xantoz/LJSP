.class FactorialFaculty
.super java/lang/Object


.method public <init>()V
.limit locals 1
.limit stack 1

aload_0
invokenonvirtual java/lang/Object.<init>()V
return
.end method



.method run(I)I
.limit stack 255
.limit locals 255

;; if 
iload_1
iconst_0
if_icmpeq L3
iconst_0
goto L4
L3:
iconst_1
L4:
ifeq L1
iconst_1
istore_2
goto L2
L1:
iload_1
aload_0
iload_1
iconst_1
isub
invokevirtual FactorialFaculty.run(I)I
imul
istore_2
L2:
;; endif 
iload_2
ireturn
.end method


