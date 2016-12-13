
.class  public synchronized ManualFoob3
.super Procedure

.field public static stringers Ljava/lang/String;

.method public toString()Ljava/lang/String;
	.limit locals 1
	.limit stack 3

	new	java/lang/StringBuilder
	dup
	invokenonvirtual	java/lang/StringBuilder.<init>()V
	getstatic	ManualFoob3/stringers Ljava/lang/String;
	invokevirtual	java/lang/StringBuilder.append(Ljava/lang/String;)Ljava/lang/StringBuilder;
	invokestatic	java/lang/Math.random()D
	invokevirtual	java/lang/StringBuilder.append(D)Ljava/lang/StringBuilder;
	invokevirtual	java/lang/StringBuilder.toString()Ljava/lang/String;
	areturn
.end method

.method public <init>()V
	.limit locals 1
	.limit stack 2

	aload_0
	ldc	"BLARGH"
	invokenonvirtual	Procedure.<init>(Ljava/lang/String;)V
	return
.end method

.method public run([LLispObject;)LLispObject;
	.limit locals 3
	.limit stack 10

	aload_1
	iconst_0
	aaload
	astore_2
	aload_0
	iconst_1
	anewarray	LispObject
	dup
	iconst_0
	aload_2
	checkcast	LispNumber
	new	LispFixnum
	dup
	lconst_1
	invokenonvirtual	LispFixnum.<init>(J)V
	invokevirtual	LispNumber.sub(LLispNumber;)LLispNumber;
	aastore
	invokevirtual	ManualFoob3.run([LLispObject;)LLispObject;
	checkcast	LispNumber
	aload_0
	iconst_1
	anewarray	LispObject
	dup
	iconst_0
	aload_2
	checkcast	LispNumber
	new	LispFixnum
	dup
	ldc2_w	2
	invokenonvirtual	LispFixnum.<init>(J)V
	invokevirtual	LispNumber.sub(LLispNumber;)LLispNumber;
	aastore
	invokevirtual	ManualFoob3.run([LLispObject;)LLispObject;
	checkcast	LispNumber
	invokevirtual	LispNumber.add(LLispNumber;)LLispNumber;
	areturn
.end method

.method public volatile printObject(LLispStream;)V
	.limit locals 2
	.limit stack 2

	aload_0
	aload_1
	invokenonvirtual	Procedure.printObject(LLispStream;)V
	return
.end method

.method static <clinit>()V
	.limit locals 0
	.limit stack 1

	ldc	"Wee a weirdo mockup yay!"
	putstatic	ManualFoob3/stringers Ljava/lang/String;
	return
.end method

