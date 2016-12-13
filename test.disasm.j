.class  public synchronized fest
.super java/lang/Object

.field public static final wee LLispNumber;
.field public static final fisk LLispChar;
.field public static final asfd Ljava/lang/String; = "hej"
.field public static final ya Ljava/lang/String; = "fiskus"
.field public static final lolf Ljava/lang/Double;
.field public static final rolf Ljava/lang/Double;
.field public static final kolf Ljava/lang/Double;

.method public <init>()V
	.limit locals 1
	.limit stack 1

	aload_0
	invokenonvirtual	java/lang/Object.<init>()V
	return
.end method

.method public lollerskates()Z
	.limit locals 1
	.limit stack 1

	iconst_1
	ireturn
.end method

.method public static final fisk()Z
	.limit locals 0
	.limit stack 2

	getstatic	java/lang/System/out Ljava/io/PrintStream;
	ldc	"fisk"
	invokevirtual	java/io/PrintStream.println(Ljava/lang/String;)V
	iconst_1
	ireturn
.end method

.method public static main([Ljava/lang/String;)V
	.limit locals 13
	.limit stack 7

	ldc2_w	2147483647
	lstore_1
	ldc2_w	3
	lstore_3
	lload_1
	l2i
	istore	5
	lload_1
	bipush	32
	lushr
	l2i
	istore	6
	lload_3
	l2i
	istore	7
	lload_3
	bipush	32
	lushr
	l2i
	istore	8
	iload	5
	i2l
	iload	7
	i2l
	lmul
	lstore	9
	iload	6
	i2l
	iload	8
	i2l
	lmul
	lstore	11
	getstatic	java/lang/System/out Ljava/io/PrintStream;
	lload	9
	ldc2_w	-1
	land
	lload	11
	ldc2_w	-1
	land
	ladd
	invokevirtual	java/io/PrintStream.println(J)V
	lload	9
	ldc2_w	0
	land
	lconst_0
	lcmp
	ifne	L89
	lload	11
	ldc2_w	0
	land
	lconst_0
	lcmp
	ifeq	L97
L89:
	getstatic	java/lang/System/out Ljava/io/PrintStream;
	ldc	"Overflowzor"
	invokevirtual	java/io/PrintStream.println(Ljava/lang/String;)V
L97:
	getstatic	java/lang/System/out Ljava/io/PrintStream;
	new	java/lang/StringBuilder
	dup
	invokenonvirtual	java/lang/StringBuilder.<init>()V
	ldc	""
	invokevirtual	java/lang/StringBuilder.append(Ljava/lang/String;)Ljava/lang/StringBuilder;
	invokestatic	java/lang/Math.random()D
	invokevirtual	java/lang/StringBuilder.append(D)Ljava/lang/StringBuilder;
	ldc	" "
	invokevirtual	java/lang/StringBuilder.append(Ljava/lang/String;)Ljava/lang/StringBuilder;
	getstatic	fest/lolf Ljava/lang/Double;
	invokevirtual	java/lang/StringBuilder.append(Ljava/lang/Object;)Ljava/lang/StringBuilder;
	ldc	" "
	invokevirtual	java/lang/StringBuilder.append(Ljava/lang/String;)Ljava/lang/StringBuilder;
	getstatic	fest/rolf Ljava/lang/Double;
	invokevirtual	java/lang/StringBuilder.append(Ljava/lang/Object;)Ljava/lang/StringBuilder;
	ldc	" "
	invokevirtual	java/lang/StringBuilder.append(Ljava/lang/String;)Ljava/lang/StringBuilder;
	getstatic	fest/kolf Ljava/lang/Double;
	invokevirtual	java/lang/StringBuilder.append(Ljava/lang/Object;)Ljava/lang/StringBuilder;
	invokevirtual	java/lang/StringBuilder.toString()Ljava/lang/String;
	invokevirtual	java/io/PrintStream.println(Ljava/lang/String;)V
	return
.end method

.method static <clinit>()V
	.limit locals 0
	.limit stack 123

	ldc	"123.4"
	invokestatic	LispNumber.parse(Ljava/lang/String;)LLispNumber;
	putstatic	fest/wee LLispNumber;
	new	LispChar
	dup
	bipush	60
	invokenonvirtual	LispChar.<init>(C)V
    putstatic	fest/fisk LLispChar;
    ; ldc2_w nand.0
     ; ldc2_w	NaNd
    ; ldc2_w 0.2d
    dconst_0
    dconst_0
    ddiv
	invokestatic	java/lang/Double.valueOf(D)Ljava/lang/Double;
	putstatic	fest/lolf Ljava/lang/Double;
	ldc2_w	12.4d
	invokestatic	java/lang/Double.valueOf(D)Ljava/lang/Double;
	putstatic	fest/rolf Ljava/lang/Double;
	dconst_1
	invokestatic	java/lang/Double.valueOf(D)Ljava/lang/Double;
	putstatic	fest/kolf Ljava/lang/Double;
	return
.end method


