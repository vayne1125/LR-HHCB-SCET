import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class Comparison {
    public static void main(String[] args){
        Pairing bp = PairingFactory.getPairing("data/a.properties");
        Field G = bp.getG1();
        Field GT = bp.getGT();
        Field Zq = bp.getZr();

        long startTime, endTime;
        long time = 0;
        int repNum = 1000;
        for(int i=0;i<repNum;i++){
            Element g1 = G.newRandomElement();
            Element g2 = G.newRandomElement();
            Element x = Zq.newRandomElement();
            startTime = System.currentTimeMillis();
            // bp.pairing(g1,g2);
            // g1.add(g2);
            g1.mulZn(x);
            endTime = System.currentTimeMillis();
            time += endTime- startTime;
        }
        System.out.println(time/(double)repNum);

        Element g1 = G.newRandomElement().getImmutable();
        System.out.println(g1.getLengthInBytes());

        Element gt = GT.newRandomElement().getImmutable();
        System.out.println(gt.getLengthInBytes());

        Element x = Zq.newRandomElement().getImmutable();
        System.out.println(x.getLengthInBytes());
    }
}
