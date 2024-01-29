import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.util.Base64;
import java.util.Properties;

public abstract class Entity {
    public Element pk1, pk2;
    protected Element sk10,sk11,sk20,sk21;

    public String memberOf, id;
    protected String pairingParametersFileName, SPPFileName, entityFileName;

    public Entity(String pairingParametersFileName, String SPPFileName, String id, String entityFileName,String memberOf){
        this.pairingParametersFileName = pairingParametersFileName;
        this.SPPFileName = SPPFileName;
        this.id = id;
        this.entityFileName = entityFileName;
        this.memberOf = memberOf;
        selfKeyGen();
    }
    private void selfKeyGen(){
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

    public void TDGen(String tdFileName){
        // pairing
        Pairing bp = PairingFactory.getPairing(pairingParametersFileName);
        Field G = bp.getG1();

        // 將公開參數 SPP 讀入
        Properties SPP = Tools.loadPropFromFile(SPPFileName);

        // 還原 P (G 的 generator)
        String P_str = SPP.getProperty("P");
        Element P = G.newElementFromBytes(Base64.getDecoder().decode(P_str)).getImmutable();

        // update self sk
        updateSk(P);

        // 將 pk 以文件方式存起來
        storeTDFile(tdFileName);
    }

    protected abstract void storeTDFile(String tdFileName);
    protected abstract void updateSk(Element P);
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
        String S_str = SPP.getProperty("S");
        String T_str = SPP.getProperty("T");
        String U1_str = SPP.getProperty("U1");
        String U2_str = SPP.getProperty("U2");
        String V1_str = SPP.getProperty("V1");
        String V2_str = SPP.getProperty("V2");
        String CBCAPK_str = SPP.getProperty("CBCAPK");
        Element P = G.newElementFromBytes(Base64.getDecoder().decode(P_str)).getImmutable();
        Element U1 = G.newElementFromBytes(Base64.getDecoder().decode(U1_str)).getImmutable();
        Element U2 = G.newElementFromBytes(Base64.getDecoder().decode(U2_str)).getImmutable();
        Element V1 = G.newElementFromBytes(Base64.getDecoder().decode(V1_str)).getImmutable();
        Element V2 = G.newElementFromBytes(Base64.getDecoder().decode(V2_str)).getImmutable();
        Element S = G.newElementFromBytes(Base64.getDecoder().decode(S_str)).getImmutable();
        Element T = G.newElementFromBytes(Base64.getDecoder().decode(T_str)).getImmutable();
        Element CBCAPK = GT.newElementFromBytes(Base64.getDecoder().decode(CBCAPK_str)).getImmutable();

        // receiverInfo
        String receiverId = receiverInfo.getProperty("id");
        String receiverMemberOf = receiverInfo.getProperty("memberOf");
        String pk1_str = receiverInfo.getProperty("pk1");
        String pk2_str = receiverInfo.getProperty("pk2");
        Element receiverPk1 = GT.newElementFromBytes(Base64.getDecoder().decode(pk1_str)).getImmutable();
        Element receiverPk2 = GT.newElementFromBytes(Base64.getDecoder().decode(pk2_str)).getImmutable();
        Element receiverPk3 = G.newOneElement();

        if(receiverMemberOf.equals("CB")){
            String pk3_str = receiverInfo.getProperty("pk3");
            receiverPk3 = G.newElementFromBytes(Base64.getDecoder().decode(pk3_str)).getImmutable();
        }

        // signcryption-------------------------------------------------------------
        // CT0
        Element h = Zq.newRandomElement().getImmutable();
        Element CT0 = P.mulZn(h).getImmutable();

        // H、TT
        byte[] H = new byte[20];
        Element TT = G.newZeroElement();
        if(receiverMemberOf.equals("PB")){
            Element PBH1 = receiverPk1.powZn(h).getImmutable();
            Element PBH2 = receiverPk2.powZn(h).getImmutable();
            H = Tools.HF2(PBH1,PBH2);
            TT = Tools.HF3(PBH2,pairingParametersFileName).getImmutable();
        }else if(receiverMemberOf.equals("CB")){
            byte[] idR_Byte = Tools.HF0(receiverId,receiverPk1,receiverPk2,receiverPk3);
            Element idR = Zq.newElementFromHash(idR_Byte,0,idR_Byte.length).getImmutable();
            Element PBH1 = receiverPk1.powZn(h).getImmutable();
            Element PBH2 = receiverPk2.powZn(h).getImmutable();
            Element PBH3 = (CBCAPK.mul(bp.pairing(receiverPk3,U1.add(V1.mulZn(idR))))).powZn(h).getImmutable();
            Element PBH4 = (CBCAPK.mul(bp.pairing(receiverPk3,U2.add(V2.mulZn(idR))))).powZn(h).getImmutable();
            H = Tools.HF4(PBH1,PBH2,PBH3,PBH4);
            TT = Tools.HF5(PBH2,PBH4,pairingParametersFileName).getImmutable();
        }else{
            System.out.println("非法 Entity");
            System.exit(-1);
        }

        // update self sk
        updateSk(P);

        // TS
        Element TS = TSGen();

        // CT1
        byte[] sk = Tools.genSk();
        byte[] CT1 = Tools.SE(msg,sk);

        // CT2
        byte[] CT2 = Tools.XOR(Tools.HF6(H),sk);

        // CT3
        Element t = Zq.newRandomElement().getImmutable();
        Element CT3 = P.mulZn(t).getImmutable();

        // CT4
        Element msgHash2G = Tools.HF7(msg,pairingParametersFileName).getImmutable();
        Element CT4 = TT.add(msgHash2G.mulZn(t)).getImmutable();

        // CT5
        byte[] n_byte = Tools.HF8(msg,sk,CT0,CT1,CT2,CT3,CT4);
        Element n = Zq.newElementFromBytes(n_byte).getImmutable();
        Element SnT = S.add(T.mulZn(n)).getImmutable();
        Element hSnT = SnT.mulZn(h).getImmutable();
        Element CT5 = TS.add(hSnT).getImmutable();


        // write CT ----------------------------------------------------------------
        Properties prop = new Properties();
        prop.setProperty("CT0", Base64.getEncoder().encodeToString(CT0.toBytes()));
        prop.setProperty("CT1", Base64.getEncoder().encodeToString(CT1));
        prop.setProperty("CT2", Base64.getEncoder().encodeToString(CT2));
        prop.setProperty("CT3", Base64.getEncoder().encodeToString(CT3.toBytes()));
        prop.setProperty("CT4", Base64.getEncoder().encodeToString(CT4.toBytes()));
        prop.setProperty("CT5", Base64.getEncoder().encodeToString(CT5.toBytes()));
        Tools.storePropToFile(prop,CTFileName);
    }

    protected abstract Element TSGen();
    protected abstract byte[] H_Gen(Element CT0);
    public String unSigncryption(String senderFileName,String CTFileName){
        // pairing
        Pairing bp = PairingFactory.getPairing(pairingParametersFileName);
        Field G = bp.getG1();
        Field GT = bp.getGT();
        Field Zq = bp.getZr();

        // 將公開參數 SPP, senderInfo, CT 讀入
        Properties SPP = Tools.loadPropFromFile(SPPFileName);
        Properties receiverInfo = Tools.loadPropFromFile(senderFileName);
        Properties CT = Tools.loadPropFromFile(CTFileName);

        // SPP
        String P_str = SPP.getProperty("P");
        String S_str = SPP.getProperty("S");
        String T_str = SPP.getProperty("T");
        String U1_str = SPP.getProperty("U1");
        String U2_str = SPP.getProperty("U2");
        String V1_str = SPP.getProperty("V1");
        String V2_str = SPP.getProperty("V2");
        String CBCAPK_str = SPP.getProperty("CBCAPK");
        Element P = G.newElementFromBytes(Base64.getDecoder().decode(P_str)).getImmutable();
        Element S = G.newElementFromBytes(Base64.getDecoder().decode(S_str)).getImmutable();
        Element T = G.newElementFromBytes(Base64.getDecoder().decode(T_str)).getImmutable();
        Element U1 = G.newElementFromBytes(Base64.getDecoder().decode(U1_str)).getImmutable();
        Element U2 = G.newElementFromBytes(Base64.getDecoder().decode(U2_str)).getImmutable();
        Element V1 = G.newElementFromBytes(Base64.getDecoder().decode(V1_str)).getImmutable();
        Element V2 = G.newElementFromBytes(Base64.getDecoder().decode(V2_str)).getImmutable();
        Element CBCAPK = GT.newElementFromBytes(Base64.getDecoder().decode(CBCAPK_str)).getImmutable();

        // senderInfo
        String senderId = receiverInfo.getProperty("id");
        String senderMemberOf = receiverInfo.getProperty("memberOf");
        String pk1_str = receiverInfo.getProperty("pk1");
        String pk2_str = receiverInfo.getProperty("pk2");
        Element senderPk1 = GT.newElementFromBytes(Base64.getDecoder().decode(pk1_str)).getImmutable();
        Element senderPk2 = GT.newElementFromBytes(Base64.getDecoder().decode(pk2_str)).getImmutable();
        Element senderPk3 = G.newZeroElement();

        if(senderMemberOf.equals("CB")){
            String pk3_str = receiverInfo.getProperty("pk3");
            senderPk3 = G.newElementFromBytes(Base64.getDecoder().decode(pk3_str)).getImmutable();
        }
        // CT
        String CT0_str = CT.getProperty("CT0");
        String CT1_str = CT.getProperty("CT1");
        String CT2_str = CT.getProperty("CT2");
        String CT3_str = CT.getProperty("CT3");
        String CT4_str = CT.getProperty("CT4");
        String CT5_str = CT.getProperty("CT5");
        Element CT0 = G.newElementFromBytes(Base64.getDecoder().decode(CT0_str)).getImmutable();
        byte[] CT1 = Base64.getDecoder().decode(CT1_str);
        byte[] CT2 = Base64.getDecoder().decode(CT2_str);
        Element CT3 = G.newElementFromBytes(Base64.getDecoder().decode(CT3_str)).getImmutable();
        Element CT4 = G.newElementFromBytes(Base64.getDecoder().decode(CT4_str)).getImmutable();
        Element CT5 = G.newElementFromBytes(Base64.getDecoder().decode(CT5_str)).getImmutable();

        // unSigncryption-------------------------------------------------------------
        // update self sk
        updateSk(P);

        // compute H', sk'
        byte[] H_ = H_Gen(CT0);
        byte[] sk_ = Tools.XOR(Tools.HF6(H_),CT2);
        String msg_ = Tools.DE(CT1,sk_);

        // varify
        byte[] n_byte = Tools.HF8(msg_,sk_,CT0,CT1,CT2,CT3,CT4);
        Element n_ = Zq.newElementFromBytes(n_byte).getImmutable();

        Element testL = bp.pairing(P,CT5).getImmutable();
        Element testR = GT.newZeroElement();
        if(senderMemberOf.equals("PB")){
            testR = senderPk1.mul(senderPk2).mul(bp.pairing(CT0,S.add(T.mulZn(n_)))).getImmutable();
        } else if (senderMemberOf.equals("CB")) {
            byte[] idCB_Byte = Tools.HF1(senderId,senderPk1,senderPk2,senderPk3);
            Element idCB = Zq.newElementFromHash(idCB_Byte,0,idCB_Byte.length).getImmutable();
            testR = CBCAPK.mul(CBCAPK).mul(senderPk1).mul(senderPk2).mul(bp.pairing(senderPk3,U1.add(V1.mulZn(idCB)))).mul(bp.pairing(senderPk3,U2.add(V2.mulZn(idCB)))).mul(bp.pairing(CT0,S.add(T.mulZn(n_)))).getImmutable();
        } else {
            System.out.println("非法 Entity");
            System.exit(-1);
        }
        // System.out.println("testMsg: " + msg_);
        if(testL.isEqual(testR)) return msg_;

        return "invalid";
    }
}
