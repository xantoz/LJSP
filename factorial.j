.class factorial
.super java/lang/Object

.method public <init>()V
.limit locals 1
.limit stack 1

aload_0
invokenonvirtual java/lang/Object.<init>()V
return
.end method



.method public static main([Ljava/lang/String;)V
.limit locals 255
.limit stack  255

getstatic java/lang/System/out Ljava/io/PrintStream;
new FactorialFaculty
dup
invokenonvirtual FactorialFaculty.<init>()V
bipush 10
invokevirtual FactorialFaculty.run(I)I
invokevirtual java/io/PrintStream/println(I)V
getstatic java/lang/System/out Ljava/io/PrintStream;
new FactorialTrecFaculty
dup
invokenonvirtual FactorialTrecFaculty.<init>()V
bipush 10
invokevirtual FactorialTrecFaculty.run(I)I
invokevirtual java/io/PrintStream/println(I)V
getstatic java/lang/System/out Ljava/io/PrintStream;
new FactorialIterFaculty
dup
invokenonvirtual FactorialIterFaculty.<init>()V
bipush 10
invokevirtual FactorialIterFaculty.run(I)I
invokevirtual java/io/PrintStream/println(I)V

return
.end method
