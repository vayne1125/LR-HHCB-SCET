import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.util.Base64;
import java.util.Properties;

public class PB_PKS{
    public Element CAPk;
    private Element CASk0,CASk1;
    private String pairingParametersFileName, SPPFileName;
    private Pairing bp;
    private Field G,Zq;
    private Properties SPP;
    private Element P,S,T;
    public PB_PKS(String pairingParametersFileName, String SPPFileName){
        this.pairingParametersFileName = pairingParametersFileName;
        this.SPPFileName = SPPFileName;

        // pairing
        this.bp = PairingFactory.getPairing(pairingParametersFileName);
        this.G = bp.getG1();
        this.Zq = bp.getZr();

        // 將公開參數 SPP 讀入
        this.SPP = Tools.loadPropFromFile(SPPFileName);

        // 還原 P(G 的 generator), S, T
        String P_str = SPP.getProperty("P");
        String S_str = SPP.getProperty("S");
        String T_str = SPP.getProperty("T");
        this.P = G.newElementFromBytes(Base64.getDecoder().decode(P_str)).getImmutable();
        this.S = G.newElementFromBytes(Base64.getDecoder().decode(S_str)).getImmutable();
        this.T = G.newElementFromBytes(Base64.getDecoder().decode(T_str)).getImmutable();

        // 做 CA 的公私鑰
        Element w1 = Zq.newRandomElement().getImmutable();
        Element CASK = P.mulZn(w1).getImmutable();
        CAPk = bp.pairing(P,CASK).getImmutable();

        // 將 CASK 一拆為二
        Element a = Zq.newRandomElement().getImmutable();
        CASk0 = P.mulZn(a).getImmutable();
        CASk1 = CASK.sub(CASk0).getImmutable();
    }

    public PB_Entity createEntity(String id,String entityFileName){
        PB_Entity entity = new PB_Entity(pairingParametersFileName,SPPFileName,id, entityFileName);
        crtGen(entity);
        return entity;
    }

    public void crtGen(PB_Entity entity) {
        // get entity pk1, pk2, id
        String entityId = entity.id;
        Element entityPk1 = entity.pk1;
        Element entityPk2 = entity.pk2;

        // 更新 sk
        Element a = Zq.newRandomElement().getImmutable();
        Element aP = P.mulZn(a).getImmutable();
        CASk0 = CASk0.add(aP).getImmutable();
        CASk1 = CASk1.sub(aP).getImmutable();

        // 製作 CRT0, CRT1
        a = Zq.newRandomElement().getImmutable();
        Element crt0 = P.mulZn(a).getImmutable();
        byte[] idPB_Byte = Tools.HF0(entityId,entityPk1,entityPk2,crt0);
        Element idPB = Zq.newElementFromHash(idPB_Byte,0,idPB_Byte.length).getImmutable();
        Element SidPBT = S.add(T.mulZn(idPB)).getImmutable();
        Element TC = CASk1.add(SidPBT.mulZn(a)).getImmutable();
        Element crt1 = CASk0.add(TC).getImmutable();
        entity.setCrt(crt0,crt1);
    }
}
