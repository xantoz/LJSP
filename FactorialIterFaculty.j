.class FactorialIterFaculty
.super java/lang/Object

.field acc I

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
iconst_1
putfield FactorialIterFaculty/acc I
; while
L10:
iload_1
iconst_0
if_icmpeq L13
iconst_0
goto L14
L13:
iconst_1
L14:
ifeq L11
iconst_0
goto L12
L11:
iconst_1
L12:
ifeq L9
aload_0
iload_1
aload_0
getfield FactorialIterFaculty/acc I
imul
putfield FactorialIterFaculty/acc I
iload_1
iconst_1
isub
istore_1
goto L10
L9:
; end while
aload_0
getfield FactorialIterFaculty/acc I
ireturn
.end method


