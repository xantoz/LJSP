.class  public synchronized HelloPrint
.super LispSubr


.method public <init>()V
	.limit locals 1
	.limit stack 4

	aload_0
	ldc	"helloprintenlainen"
	iconst_0
	iconst_0
	invokenonvirtual	LispSubr.<init>(Ljava/lang/String;II)V
	return
.end method

.method public run([LLispObject;)LLispObject;
	.limit locals 2
	.limit stack 2

	getstatic	java/lang/System/out Ljava/io/PrintStream;
	ldc	"hej"
	invokevirtual	java/io/PrintStream.println(Ljava/lang/String;)V
	aconst_null
	areturn
.end method

.method public volatile toString()Ljava/lang/String;
	.limit locals 1
	.limit stack 1

	aload_0
	invokenonvirtual	LispSubr.toString()Ljava/lang/String;
	areturn
.end method

.method public volatile printObject(LLispStream;)V
	.limit locals 2
	.limit stack 2

	aload_0
	aload_1
	invokenonvirtual	LispSubr.printObject(LLispStream;)V
	return
.end method

