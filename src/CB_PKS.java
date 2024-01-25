import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.util.Base64;
import java.util.Properties;

public class CB_PKS{
    public Element CAPk;
    private Element CASk0,CASk1;
    protected String pairingParametersFileName, SPPFileName;
    protected Pairing bp;
    protected Field G,Zq;
    protected Properties SPP;
    protected Element P;
    public CB_PKS(String pairingParametersFileName, String SPPFileName, String CAFileName){
        this.pairingParametersFileName = pairingParametersFileName;
        this.SPPFileName = SPPFileName;

        // pairing
        this.bp = PairingFactory.getPairing(pairingParametersFileName);
        this.G = bp.getG1();
        this.Zq = bp.getZr();

        // 將公開參數 SPP 讀入
        this.SPP = Tools.loadPropFromFile(SPPFileName);

        // 還原 P (G 的 generator)
        String P_str = SPP.getProperty("P");
        this.P = G.newElementFromBytes(Base64.getDecoder().decode(P_str)).getImmutable();

        // 做 CA 的公私鑰
        Element w1 = Zq.newRandomElement().getImmutable();
        Element CASK = P.mulZn(w1).getImmutable();
        CAPk = bp.pairing(P,CASK).getImmutable();

        // 將 CASK 一拆為二
        Element b = Zq.newRandomElement().getImmutable();
        CASk0 = P.mulZn(b).getImmutable();
        CASk1 = CASK.sub(CASk0).getImmutable();

        // 將 pk 以文件方式存起來
        Properties prop = new Properties();
        prop.setProperty("CAPk", Base64.getEncoder().encodeToString(CAPk.toBytes()));
        Tools.storePropToFile(prop,CAFileName);
    }
}
