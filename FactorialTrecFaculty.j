.class FactorialTrecFaculty
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

aload_0
iload_1
iconst_1
invokevirtual FactorialTrecFaculty.woop(II)I
ireturn
.end method

.method woop(II)I
.limit stack 255
.limit locals 255

;; if 
iload_1
iconst_0
if_icmpeq L7
iconst_0
goto L8
L7:
iconst_1
L8:
ifeq L5
iload_2
istore_3
goto L6
L5:
aload_0
iload_1
iconst_1
isub
iload_1
iload_2
imul
invokevirtual FactorialTrecFaculty.woop(II)I
istore_3
L6:
;; endif 
iload_3
ireturn
.end method


