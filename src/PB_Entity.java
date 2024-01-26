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
    public Element crt0, crt1;
    public String memberOf, id;

    private String pairingParametersFileName, SPPFileName, entityFileName;
    public PB_Entity(String pairingParametersFileName, String SPPFileName, String id, String entityFileName){
        this.pairingParametersFileName = pairingParametersFileName;
        this.SPPFileName = SPPFileName;
        this.id = id;
        this.entityFileName = entityFileName;
        this.memberOf = "PB";

        selfKeyGen();
    }

    public void setCrt(Element crt0, Element crt1){
        this.crt0 = crt0;
        this.crt1 = crt1;
    }
    @Override
    public void selfKeyGen(){
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
        Element sk1 = P.mulZn(y1).getImmutable();
        Element sk2 = P.mulZn(y2).getImmutable();
        pk1 = bp.pairing(P,sk1).getImmutable();
        pk2 = bp.pairing(P,sk2).getImmutable();

        // 將 SK1, SK2 一拆為二
        Element r1 = Zq.newRandomElement().getImmutable();
        Element r2 = Zq.newRandomElement().getImmutable();
        sk10 = P.mulZn(r1).getImmutable();
        sk11 = sk1.sub(sk10).getImmutable();
        sk20 = P.mulZn(r2).getImmutable();
        sk21 = sk2.sub(sk20).getImmutable();

        // 將 pk 以文件方式存起來
        Properties prop = new Properties();
        prop.setProperty("pk1", Base64.getEncoder().encodeToString(pk1.toBytes()));
        prop.setProperty("pk2", Base64.getEncoder().encodeToString(pk2.toBytes()));
        prop.setProperty("id", id);
        prop.setProperty("memberOf", memberOf);
        Tools.storePropToFile(prop,entityFileName);
    }
    @Override
    public void tdGen(){

    }
    @Override
    public void signcryption(String msg, String receiverFileName,String CTFileName){
        // pairing
        Pairing bp = PairingFactory.getPairing(pairingParametersFileName);
        Field G = bp.getG1();
        Field GT = bp.getGT();
        Field Zq = bp.getZr();

        // 將公開參數 SPP 、receiverInfo 讀入
        Properties SPP = Tools.loadPropFromFile(SPPFileName);
        Properties receiverInfo = Tools.loadPropFromFile(receiverFileName);

        // SPP
        String P_str = SPP.getProperty("P");
        Element P = G.newElementFromBytes(Base64.getDecoder().decode(P_str)).getImmutable();

        // receiverInfo
        String receiverId = receiverInfo.getProperty("id");
        String receiverMemberOf = receiverInfo.getProperty("memberOf");
        String pk1_str = SPP.getProperty("pk1");
        String pk2_str = SPP.getProperty("pk2");
        Element receiverPk1 = GT.newElementFromBytes(Base64.getDecoder().decode(pk1_str)).getImmutable();
        Element receiverPk2 = GT.newElementFromBytes(Base64.getDecoder().decode(pk2_str)).getImmutable();

        // signcryption-------------------------------------------------------------
        // CT0
        Element h = Zq.newRandomElement().getImmutable();
        Element CT0 = P.mulZn(h).getImmutable();

        // H、TT
        byte[] H;
        Element TT;
        if(receiverMemberOf.equals("PB")){
            Element PBH1 = receiverPk1.powZn(h).getImmutable();
            Element PBH2 = receiverPk2.powZn(h).getImmutable();
            H = Tools.HF2(PBH1,PBH2);
            TT = Tools.HF3(PBH2,pairingParametersFileName).getImmutable();
        }else if(receiverMemberOf.equals("CB")){

        }else{
            System.out.println("非法 Entity");
            System.exit(-1);
        }
        // update self sk
        Element r1 = Zq.newRandomElement().getImmutable();
        Element r2 = Zq.newRandomElement().getImmutable();
        Element r1P = P.mulZn(r1).getImmutable();
        Element r2P = P.mulZn(r2).getImmutable();
        sk10 = sk10.add(r1P).getImmutable();
        sk11 = sk10.sub(r1P).getImmutable();
        sk20 = sk20.add(r2P).getImmutable();
        sk21 = sk20.sub(r2P).getImmutable();

        // TS
        Element TS = sk10.add(sk10.add(sk20.add(sk21))).getImmutable();

        // CT1
        byte[] sk = Tools.genSk();

        // write CT ----------------------------------------------------------------
        Properties prop = new Properties();
        prop.setProperty("CT0", Base64.getEncoder().encodeToString(CT0.toBytes()));
        Tools.storePropToFile(prop,CTFileName);
    }
    @Override
    public void unSigncryption(){

    }

}
