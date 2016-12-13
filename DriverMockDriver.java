public class DriverMockDriver 
{
    public static void main(String args[]) 
    {
        LispObject a[] = new LispObject[1];
        a[0] = LispFixnum.parse(args[0]);
        DriverMock b = new DriverMock();
        System.out.println(b.run(a));
    }
}
