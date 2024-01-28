import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.util.Base64;
import java.util.Properties;

public class CB_PKS{
    public Element CAPk;
    private Element CASk0,CASk1;
    private String pairingParametersFileName, SPPFileName;
    private Pairing bp;
    private Field G,Zq;
    private Properties SPP;
    private Element P,U1,U2,V1,V2;
    public CB_PKS(String pairingParametersFileName, String SPPFileName){
        this.pairingParametersFileName = pairingParametersFileName;
        this.SPPFileName = SPPFileName;

        // pairing
        this.bp = PairingFactory.getPairing(pairingParametersFileName);
        this.G = bp.getG1();
        this.Zq = bp.getZr();

        // 將公開參數 SPP 讀入
        this.SPP = Tools.loadPropFromFile(SPPFileName);
        String P_str = SPP.getProperty("P");
        String U1_str = SPP.getProperty("U1");
        String U2_str = SPP.getProperty("U2");
        String V1_str = SPP.getProperty("V1");
        String V2_str = SPP.getProperty("V2");
        this.P = G.newElementFromBytes(Base64.getDecoder().decode(P_str)).getImmutable();
        this.U1 = G.newElementFromBytes(Base64.getDecoder().decode(U1_str)).getImmutable();
        this.U2 = G.newElementFromBytes(Base64.getDecoder().decode(U2_str)).getImmutable();
        this.V1 = G.newElementFromBytes(Base64.getDecoder().decode(V1_str)).getImmutable();
        this.V2 = G.newElementFromBytes(Base64.getDecoder().decode(V2_str)).getImmutable();

        // 做 CA 的公私鑰
        Element w1 = Zq.newRandomElement().getImmutable();
        Element CASK = P.mulZn(w1).getImmutable();
        CAPk = bp.pairing(P,CASK).getImmutable();

        // 將 CASK 一拆為二
        Element b = Zq.newRandomElement().getImmutable();
        CASk0 = P.mulZn(b).getImmutable();
        CASk1 = CASK.sub(CASk0).getImmutable();

        // 將 pk 以文件方式存起來
//        Properties prop = new Properties();
//        prop.setProperty("CAPk", Base64.getEncoder().encodeToString(CAPk.toBytes()));
//        Tools.storePropToFile(prop,CAFileName);
    }

    public CB_Entity createEntity(String id,String entityFileName){
        CB_Entity entity = new CB_Entity(pairingParametersFileName,SPPFileName,id, entityFileName);
        crtGen(entity);
        return entity;
    }

    public void crtGen(CB_Entity entity) {
        // get entity pk1, pk2, id
        String entityId = entity.id;
        Element entityPk1 = entity.pk1;
        Element entityPk2 = entity.pk2;

        // 更新 sk
        Element b = Zq.newRandomElement().getImmutable();
        Element bP = P.mulZn(b).getImmutable();
        CASk0 = CASk0.add(bP).getImmutable();
        CASk1 = CASk1.sub(bP).getImmutable();

        // pk3
        Element x2 = Zq.newRandomElement().getImmutable();
        Element entityPk3 = P.mulZn(x2).getImmutable();

        // 製作 CRT10, CRT11, CRT20, CRT21
        byte[] idCB_Byte = Tools.HF1(entityId,entityPk1,entityPk2,entityPk3);
        Element idCB = Zq.newElementFromHash(idCB_Byte,0,idCB_Byte.length).getImmutable();
        Element crt1 = (CASk0.add(CASk1)).add((U1.add(V1.mulZn(idCB))).mulZn(x2)).getImmutable();
        Element crt2 = (CASk0.add(CASk1)).add((U2.add(V2.mulZn(idCB))).mulZn(x2)).getImmutable();
        Element z1 =  Zq.newRandomElement().getImmutable();
        Element z2 =  Zq.newRandomElement().getImmutable();
        Element crt10 = P.mulZn(z1).getImmutable();
        Element crt11 = crt1.sub(crt10).getImmutable();
        Element crt20 = P.mulZn(z2).getImmutable();
        Element crt21 = crt2.sub(crt20).getImmutable();
        entity.setCrt(entityPk3,crt10,crt11,crt20,crt21);
    }
}
