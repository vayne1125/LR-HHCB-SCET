import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.util.Base64;
import java.util.Properties;

public class CB_Entity extends Entity{
    public Element pk3;
    private Element crt10, crt11, crt20, crt21;

    public CB_Entity(String pairingParametersFileName, String SPPFileName, String id, String entityFileName){
        super(pairingParametersFileName,SPPFileName,id,entityFileName,"CB");
        selfKeyGen();
    }

    public void setCrt(Element pk3, Element crt10, Element crt11, Element crt20, Element crt21) {
        this.pk3 = pk3;
        this.crt10 = crt10;
        this.crt11 = crt11;
        this.crt20 = crt20;
        this.crt21 = crt21;

        // 將 pk3 放進文件中
        Properties prop = Tools.loadPropFromFile(entityFileName);
        prop.setProperty("pk3", Base64.getEncoder().encodeToString(pk3.toBytes()));
        Tools.storePropToFile(prop,entityFileName);
    }

    @Override
    protected void updateSk(Element P){
        Pairing bp = PairingFactory.getPairing(pairingParametersFileName);
        Field Zq = bp.getZr();
        Element z1 = Zq.newRandomElement().getImmutable();
        Element z2 = Zq.newRandomElement().getImmutable();
        Element d1 = Zq.newRandomElement().getImmutable();
        Element d2 = Zq.newRandomElement().getImmutable();
        Element z1P = P.mulZn(z1).getImmutable();
        Element z2P = P.mulZn(z2).getImmutable();
        Element d1P = P.mulZn(d1).getImmutable();
        Element d2P = P.mulZn(d2).getImmutable();
        sk10 = sk10.add(z1P).getImmutable();
        sk11 = sk11.sub(z1P).getImmutable();
        sk20 = sk20.add(z2P).getImmutable();
        sk21 = sk21.sub(z2P).getImmutable();
        crt10 = crt10.add(d1P).getImmutable();
        crt11 = crt11.sub(d1P).getImmutable();
        crt20 = crt20.add(d2P).getImmutable();
        crt21 = crt21.sub(d2P).getImmutable();
    }

    @Override
    protected Element TSGen(){
        Element tp = sk10.add(sk11.add(sk20.add(sk21))).getImmutable();
        Element tp2 = crt10.add(crt11.add(crt20.add(crt21))).getImmutable();
        return tp.add(tp2).getImmutable();
    }

    @Override
    protected byte[] H_Gen(Element CT0){
        Pairing bp = PairingFactory.getPairing(pairingParametersFileName);
        Element PBH1_ = bp.pairing(CT0,sk10.add(sk11)).getImmutable();
        Element PBH2_ = bp.pairing(CT0,sk20.add(sk21)).getImmutable();
        Element PBH3_ = bp.pairing(CT0,crt10.add(crt11)).getImmutable();
        Element PBH4_ = bp.pairing(CT0,crt20.add(crt21)).getImmutable();
        return Tools.HF4(PBH1_,PBH2_,PBH3_,PBH4_);
    }

    @Override
    protected void storeTDFile(String tdFileName){
        Properties prop = new Properties();
        prop.setProperty("TD1", Base64.getEncoder().encodeToString((sk20.add(sk21)).toBytes()));
        prop.setProperty("TD2", Base64.getEncoder().encodeToString((crt20.add(crt21)).toBytes()));
        prop.setProperty("memberOf", memberOf);
        Tools.storePropToFile(prop,tdFileName);
    }

}
