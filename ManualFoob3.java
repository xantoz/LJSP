// This is a mockup of part of the mockup in ManualFoob2 (calling using array and the regular old Procedure.run([[LLispObject;) using
// plain Java since it all (arrays that is) baffles me right now...
// It is for disassembly only (or blowing up the stack)

public class ManualFoob3 extends Procedure
{
    public static String stringers = "Wee a weirdo mockup yay!";
    
    public String toString()
    {
        return stringers + java.lang.Math.random();
    }
    
    
    public ManualFoob3()
    {
        super("BLARGH");
    }

    public LispObject run(LispObject[] o)
    {
        LispObject a = o[0];

        return ((LispNumber)this.run(new LispObject[]{((LispNumber)a).sub(new LispFixnum(1))}))
            .add((LispNumber)this.run(new LispObject[]{((LispNumber)a).sub(new LispFixnum(2))}));
        
    }
    
    
}

