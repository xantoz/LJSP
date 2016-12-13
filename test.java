public class test
{

    public boolean lollerskates()
    {
        return true;
    }



    public static final LispNumber wee = LispNumber.parse("123.4");

    public static final LispChar fisk = new LispChar('<');

    public static final String asfd = "hej";

    public static final String ya = "fiskus";

    public static final Double lolf = Double.NaN;
    public static final Double rolf = 12.4;
    public static final Double kolf = 1.0;
    
    
    public static final boolean fisk()
    {
        System.out.println("fisk");
        return true;
    }
    
    public static void main(String args[]) 
    {
        // int a = Integer.MAX_VALUE;
        // int b = 3;
        // long r = (long)a*b;
        // int asdf = (int)r;
        // System.out.println(r);
        // System.out.println(r & 0xFFFFFFFF00000000L);
        // System.out.println(asdf);

        long a = Integer.MAX_VALUE;
        long b = 3;
        int a0 = (int)a;
        int a1 = (int)(a >>> 32);
        int b0 = (int)b;
        int b1 = (int)(b >>> 32);
        long r0 = (long)a0*b0;
        long r1 = (long)a1*b1;
        System.out.println(((r0 & 0x00000000FFFFFFFFL)) + (r1 & 0x00000000FFFFFFFFL));
        
        if ((r0 & 0xFFFFFFFF00000000L) != 0 || (r1 & 0xFFFFFFFF00000000L) != 0)
            System.out.println("Overflowzor");
        
        
        // int a = 23;
        // while (fisk())
        // {
        //     System.out.println("hej");
        //     if (a == 23) continue;
        //     System.out.println("detta syns inte");
        // }

        System.out.println("" + java.lang.Math.random() + " " + lolf  + " " + rolf + " " + kolf);
    }
}

