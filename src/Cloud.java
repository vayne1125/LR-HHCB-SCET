import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.util.Base64;
import java.util.Properties;

public class Cloud {
    private String pairingParametersFileName;
    public Cloud(String pairingParametersFileName){
        this.pairingParametersFileName = pairingParametersFileName;
    }
    public boolean equalityTest(String CTAFileName, String TDAFileName, String CTBFileName, String TDBFileName){
        // pairing
        Pairing bp = PairingFactory.getPairing(pairingParametersFileName);
        Field G = bp.getG1();

        // read CTA, CTB, TDA, TDB
        Properties CTA = Tools.loadPropFromFile(CTAFileName);
        Properties CTB = Tools.loadPropFromFile(CTBFileName);
        Properties TDA = Tools.loadPropFromFile(TDAFileName);
        Properties TDB = Tools.loadPropFromFile(TDBFileName);

        // CTA
        String CTA0_str = CTA.getProperty("CT0");
        String CTA3_str = CTA.getProperty("CT3");
        String CTA4_str = CTA.getProperty("CT4");
        Element CTA0 = G.newElementFromBytes(Base64.getDecoder().decode(CTA0_str)).getImmutable();
        Element CTA3 = G.newElementFromBytes(Base64.getDecoder().decode(CTA3_str)).getImmutable();
        Element CTA4 = G.newElementFromBytes(Base64.getDecoder().decode(CTA4_str)).getImmutable();

        // CTB
        String CTB0_str = CTB.getProperty("CT0");
        String CTB3_str = CTB.getProperty("CT3");
        String CTB4_str = CTB.getProperty("CT4");
        Element CTB0 = G.newElementFromBytes(Base64.getDecoder().decode(CTB0_str)).getImmutable();
        Element CTB3 = G.newElementFromBytes(Base64.getDecoder().decode(CTB3_str)).getImmutable();
        Element CTB4 = G.newElementFromBytes(Base64.getDecoder().decode(CTB4_str)).getImmutable();

        // member
        String memberOfA = TDA.getProperty("memberOf");
        String memberOfB = TDB.getProperty("memberOf");

        // RA
        Element RA = G.newZeroElement();
        if(memberOfA.equals("PB")){
            String PBTDA_str = TDA.getProperty("TD");
            Element PBTDA = G.newElementFromBytes(Base64.getDecoder().decode(PBTDA_str)).getImmutable();
            RA = CTA4.sub(Tools.HF3(bp.pairing(CTA0,PBTDA), pairingParametersFileName)).getImmutable();
        }


        // RB
        Element RB = G.newZeroElement();
        if(memberOfB.equals("PB")){
            String PBTDB_str = TDB.getProperty("TD");
            Element PBTDB = G.newElementFromBytes(Base64.getDecoder().decode(PBTDB_str)).getImmutable();
            RB = CTB4.sub(Tools.HF3(bp.pairing(CTB0,PBTDB), pairingParametersFileName)).getImmutable();
        }


        return bp.pairing(RA,CTB3).isEqual(bp.pairing(RB,CTA3));
    }
}
