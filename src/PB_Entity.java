import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.util.Base64;
import java.util.Properties;
import java.util.Random;

public class PB_Entity extends Entity{
    public Element crt0, crt1;
    public PB_Entity(String pairingParametersFileName, String SPPFileName, String id, String entityFileName){
        super(pairingParametersFileName,SPPFileName,id,entityFileName,"PB");
        selfKeyGen();
    }

    public void setCrt(Element crt0, Element crt1){
        this.crt0 = crt0;
        this.crt1 = crt1;
    }

    @Override
    protected void updateSk(Element P){
        Pairing bp = PairingFactory.getPairing(pairingParametersFileName);
        Field Zq = bp.getZr();
        Element r1 = Zq.newRandomElement().getImmutable();
        Element r2 = Zq.newRandomElement().getImmutable();
        Element r1P = P.mulZn(r1).getImmutable();
        Element r2P = P.mulZn(r2).getImmutable();
        sk10 = sk10.add(r1P).getImmutable();
        sk11 = sk11.sub(r1P).getImmutable();
        sk20 = sk20.add(r2P).getImmutable();
        sk21 = sk21.sub(r2P).getImmutable();
    }

    @Override
    protected Element TSGen(){
        return sk10.add(sk11.add(sk20.add(sk21))).getImmutable();
    }

    @Override
    protected byte[] H_Gen(Element CT0){
        Pairing bp = PairingFactory.getPairing(pairingParametersFileName);
        Element PBH1_ = bp.pairing(CT0,sk10.add(sk11)).getImmutable();
        Element PBH2_ = bp.pairing(CT0,sk20.add(sk21)).getImmutable();
        return Tools.HF2(PBH1_,PBH2_);
    }

    @Override
    protected void storeTDFile(String tdFileName){
        Properties prop = new Properties();
        prop.setProperty("TD", Base64.getEncoder().encodeToString((sk20.add(sk21)).toBytes()));
        prop.setProperty("memberOf", memberOf);
        Tools.storePropToFile(prop,tdFileName);
    }

}
