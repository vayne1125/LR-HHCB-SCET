
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.util.Base64;
import java.util.Properties;

public class LR_HHCB_SCET {
    public static PB_PKS pb_pks;
    public static CB_PKS cb_pks;
    public static Tools tools;
    public static void setUp(String pairingParametersFileName, String SPPFileName, String PBFileName, String CBFileName){
        // Pairing
        Pairing bp = PairingFactory.getPairing(pairingParametersFileName);
        Field G = bp.getG1();

        // 建立系統公開參數 SPP，並以文件方式存起來
        Element P = G.newRandomElement().getImmutable();  // G 的 generator
        Element S = G.newRandomElement().getImmutable();
        Element T = G.newRandomElement().getImmutable();
        Element U1 = G.newRandomElement().getImmutable();
        Element V1 = G.newRandomElement().getImmutable();
        Element U2 = G.newRandomElement().getImmutable();
        Element V2 = G.newRandomElement().getImmutable();

        Properties SPP = new Properties();
        SPP.setProperty("P", Base64.getEncoder().encodeToString(P.toBytes()));
        SPP.setProperty("S", Base64.getEncoder().encodeToString(S.toBytes()));
        SPP.setProperty("T", Base64.getEncoder().encodeToString(T.toBytes()));
        SPP.setProperty("U1", Base64.getEncoder().encodeToString(U1.toBytes()));
        SPP.setProperty("U2", Base64.getEncoder().encodeToString(V1.toBytes()));
        SPP.setProperty("V1", Base64.getEncoder().encodeToString(U2.toBytes()));
        SPP.setProperty("V2", Base64.getEncoder().encodeToString(V2.toBytes()));
        Tools.storePropToFile(SPP,SPPFileName);

        pb_pks = new PB_PKS(pairingParametersFileName,SPPFileName, PBFileName);
        cb_pks = new CB_PKS(pairingParametersFileName,SPPFileName, CBFileName);
    }
    // 建立 pb_pks, cb_pks
    public static void main(String[] args){
        String dir = "data/";
        String pairingParametersFileName = dir + "a.properties";
        String SPPFileName = dir + "spp.properties";
        String PBFileName = dir + "pbpks.properties";
        String CBFileName = dir + "cbpks.properties";
        setUp(pairingParametersFileName, SPPFileName, PBFileName, CBFileName);

        String PBSenderFileName = dir + "pbsender.properties";
        PB_Entity entity = pb_pks.createEntity(PBSenderFileName);


    }
}
