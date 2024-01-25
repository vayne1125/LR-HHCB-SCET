import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.util.Base64;
import java.util.Properties;
import java.util.Random;

public class PB_Entity implements Entity{
    public Element pk1, pk2;
    private Element sk10,sk11,sk20,sk21;
    private Element crt0, crt1;
    public String id;
    private String pairingParametersFileName, SPPFileName;
    public PB_Entity(String pairingParametersFileName, String SPPFileName, String entityFileName){
        this.pairingParametersFileName = pairingParametersFileName;
        this.SPPFileName = SPPFileName;

        // 隨機生成 id，用於辨識此 Entity 為 PKI-Based
        Random random = new Random();
        byte[] randomBytes = new byte[20];
        random.nextBytes(randomBytes);
        id = "PB-" + Base64.getEncoder().encodeToString(randomBytes);
        selfKeyGen(id,entityFileName);
    }

    public void setCrt(Element crt0, Element crt1){
        this.crt0 = crt0;
        this.crt1 = crt1;
    }
    @Override
    public void selfKeyGen(String id, String entityFileName){
        // pairing
        Pairing bp = PairingFactory.getPairing(pairingParametersFileName);
        Field G = bp.getG1();
        Field Zq = bp.getZr();

        // 將公開參數 SPP 讀入
        Properties SPP = Tools.loadPropFromFile(SPPFileName);

        // 還原 P (G 的 generator)
        String P_str = SPP.getProperty("P");
        Element P = G.newElementFromBytes(Base64.getDecoder().decode(P_str)).getImmutable();

        // 做 Entity 的公私鑰
        Element y1 = Zq.newRandomElement().getImmutable();
        Element y2 = Zq.newRandomElement().getImmutable();
        Element SK1 = P.mulZn(y1).getImmutable();
        Element SK2 = P.mulZn(y2).getImmutable();
        pk1 = bp.pairing(P,SK1).getImmutable();
        pk2 = bp.pairing(P,SK2).getImmutable();

        // 將 SK1, SK2 一拆為二
        Element r1 = Zq.newRandomElement().getImmutable();
        Element r2 = Zq.newRandomElement().getImmutable();
        sk10 = P.mulZn(r1).getImmutable();
        sk11 = SK1.sub(sk10).getImmutable();
        sk20 = P.mulZn(r2).getImmutable();
        sk21 = SK2.sub(sk20).getImmutable();

        // 將 pk 以文件方式存起來
        Properties prop = new Properties();
        prop.setProperty("pk1", Base64.getEncoder().encodeToString(pk1.toBytes()));
        prop.setProperty("pk2", Base64.getEncoder().encodeToString(pk2.toBytes()));
        prop.setProperty("id", id);
        Tools.storePropToFile(prop,entityFileName);
    }
    @Override
    public void tdGen(){

    }
    @Override
    public void signcryption(){

    }
    @Override
    public void unSigncryption(){

    }

}
