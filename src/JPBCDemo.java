import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;

public class JPBCDemo {
    public static void main(String[] args){
        Pairing bp = PairingFactory.getPairing("a.properties");

//        int rBits = 160;
//        int qBits = 512;

//        TypeACurveGenerator pg = new TypeACurveGenerator(rBits,qBits);
//        PairingParameters pp = pg.generate();
//        System.out.println(pp);
//
//        Pairing bp = PairingFactory.getPairing(pp);
        Field G = bp.getG1();
        Field GT = bp.getGT();
        Field Zr = bp.getZr();

        Element g = G.newRandomElement(); //.getImmutable();
        Element a = Zr.newRandomElement();
        Element b = Zr.newRandomElement();

        Element g_a = g.duplicate().powZn(a);
        Element g_b = g.duplicate().powZn(b);
        Element egg_ab = bp.pairing(g_a,g_b);

        Element egg = bp.pairing(g,g);
        Element ab = a.duplicate().mul(b);
        Element egg_ab_p = egg.duplicate().powZn(ab);

        if(egg_ab.isEqual(egg_ab_p))
            System.out.println("YES\n");
        else
            System.out.println("NO\n");
    }
}