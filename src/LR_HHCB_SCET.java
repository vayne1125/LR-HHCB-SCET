
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.util.Base64;
import java.util.Properties;

public class LR_HHCB_SCET {
    public static PB_PKS pb_pks;
    public static CB_PKS cb_pks;
    public static Cloud cloud;
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

        pb_pks = new PB_PKS(pairingParametersFileName,SPPFileName);
        cb_pks = new CB_PKS(pairingParametersFileName,SPPFileName);

        SPP.setProperty("PBCAPK", Base64.getEncoder().encodeToString(pb_pks.CAPk.toBytes()));
        SPP.setProperty("CBCAPK", Base64.getEncoder().encodeToString(cb_pks.CAPk.toBytes()));
        Tools.storePropToFile(SPP,SPPFileName);

        cloud = new Cloud(pairingParametersFileName);
    }
    // 建立 pb_pks, cb_pks
    public static void main(String[] args){
        // setup-------------------------------------------------------------------
        String dir = "data/";
        String pairingParametersFileName = dir + "a.properties";
        String SPPFileName = dir + "spp.properties";
        String PBFileName = dir + "pbpks.properties";
        String CBFileName = dir + "cbpks.properties";
        setUp(pairingParametersFileName, SPPFileName, PBFileName, CBFileName);

        // 加解密測試-----------------------------------------------------------------
        // 創造 Entity
        String entityDir = "data/entityinfo/";
        String PBSenderFileName = entityDir + "pbsender.properties";
        String PBSenderId = "pbsender@gmail.com";
        PB_Entity PBSender = pb_pks.createEntity(PBSenderId,PBSenderFileName);

        String PBReceiverFileName = entityDir + "pbreceiver.properties";
        String PBReceiverId = "pbreceiver@gmail.com";
        PB_Entity PBReceiver = pb_pks.createEntity(PBReceiverId,PBReceiverFileName);

        String CBSenderFileName = entityDir + "cbsender.properties";
        String CBSenderId = "cbsender@gmail.com";
        CB_Entity CBSender = cb_pks.createEntity(CBSenderId,CBSenderFileName);

        String CBReceiverFileName = entityDir + "cbreceiver.properties";
        String CBReceiverId = "cbreceiver@gmail.com";
        CB_Entity CBReceiver = cb_pks.createEntity(CBReceiverId,CBReceiverFileName);

        // -------------------------test PB PB ------------------------------- //
        // PBSender 加密訊息給 PBReceiver
        String CTDir = "data/ct/";
        String CT_PB2PB_FileName = CTDir + "pb2pb.properties";
        String msg = "Hello, I'm PBSender! How are you PBReceiver? 我來自 PB 系統歐~";
        PBSender.signcryption(msg,PBReceiverFileName,CT_PB2PB_FileName);

        // PBReceiver 解密 PBSender 的訊息
        String msg_ = PBReceiver.unSigncryption(PBSenderFileName,CT_PB2PB_FileName);
        System.out.println("PBReceiver 解 PBSender 的密文: " + msg_);

        // -------------------------test CB CB ------------------------------- //
        // CBSender 加密訊息給 CBReceiver
        String CT_CB2CB_FileName = CTDir + "cb2cb.properties";
        msg = "Hello, I'm CBSender! How are you CBReceiver? 我來自 CB 系統歐~";
        CBSender.signcryption(msg,CBReceiverFileName,CT_CB2CB_FileName);

        // CBReceiver 解密 CBSender 的訊息
        msg_ = CBReceiver.unSigncryption(CBSenderFileName,CT_CB2CB_FileName);
        System.out.println("CBReceiver 解 CBSender 的密文: " + msg_);

        // -------------------------test PB CB ------------------------------- //
        // PBSender 加密訊息給 CBReceiver
        String CT_PB2CB_FileName = CTDir + "pb2cb.properties";
        msg = "Hello, I'm PBSender! How are you CBReceiver? 我來自 PB 系統歐~";
        PBSender.signcryption(msg,CBReceiverFileName,CT_PB2CB_FileName);

        // CBReceiver 解密 PBSender 的訊息
        msg_ = CBReceiver.unSigncryption(PBSenderFileName,CT_PB2CB_FileName);
        System.out.println("CBReceiver 解 PBSender 的密文: " + msg_);

        // -------------------------test CB PB ------------------------------- //
        // CBSender 加密訊息給 PBReceiver
        String CT_CB2PB_FileName = CTDir + "cb2pb.properties";
        msg = "Hello, I'm CBSender! How are you PBReceiver? 我來自 PB 系統歐~";
        CBSender.signcryption(msg,PBReceiverFileName,CT_CB2PB_FileName);

        // PBReceiver 解密 CBSender 的訊息
        msg_ = PBReceiver.unSigncryption(CBSenderFileName,CT_CB2PB_FileName);
        System.out.println("PBReceiver 解 CBSender 的密文: " + msg_);

        // 相等性測試-----------------------------------------------------------------
        // 創造 Entity;
        String PBAFileName = entityDir + "pbA.properties";
        String PBAId = "pbA@gmail.com";
        PB_Entity PBA = pb_pks.createEntity(PBAId,PBAFileName);

        String PBBFileName = entityDir + "pbB.properties";
        String PBBrId = "pbB@gmail.com";
        PB_Entity PBB = pb_pks.createEntity(PBBrId,PBBFileName);

        String CBAFileName = entityDir + "cbA.properties";
        String CBAId = "cbA@gmail.com";
        CB_Entity CBA = cb_pks.createEntity(CBAId,CBAFileName);

        String CBBFileName = entityDir + "cbB.properties";
        String CBBrId = "cbB@gmail.com";
        CB_Entity CBB = cb_pks.createEntity(CBBrId,CBBFileName);

        String TDDir = "data/td/";
        String PBATDFileName = TDDir + "pbA.properties";
        PBA.TDGen(PBATDFileName);

        String PBBTDFileName = TDDir + "pbB.properties";
        PBB.TDGen(PBBTDFileName);

        String CBATDFileName = TDDir + "cbA.properties";
        CBA.TDGen(CBATDFileName);

        String CBBTDFileName = TDDir + "cbB.properties";
        CBB.TDGen(CBBTDFileName);

        // PB <-> PB
        String CTAFileName = CTDir + "pbA.properties";
        String CTBFileName = CTDir + "pbB.properties";
        PBSender.signcryption("Hello",PBAFileName,CTAFileName);
        PBSender.signcryption("Hello",PBBFileName,CTBFileName);
        System.out.println(cloud.equalityTest(CTAFileName,PBATDFileName,CTBFileName ,PBBTDFileName));

        // CB <-> CB
        CTAFileName = CTDir + "cbA.properties";
        CTBFileName = CTDir + "cbB.properties";
        PBSender.signcryption("Hello",CBAFileName,CTAFileName);
        PBSender.signcryption("Hello",CBBFileName,CTBFileName);
        System.out.println(cloud.equalityTest(CTAFileName,CBATDFileName,CTBFileName ,CBBTDFileName));

        // PB <-> CB
        CTAFileName = CTDir + "pbA.properties";
        CTBFileName = CTDir + "cbB.properties";
        PBSender.signcryption("Hello",PBAFileName,CTAFileName);
        PBSender.signcryption("Hello",CBBFileName,CTBFileName);
        System.out.println(cloud.equalityTest(CTAFileName,PBATDFileName,CTBFileName ,CBBTDFileName));

        // CB <-> PB
        CTAFileName = CTDir + "cbA.properties";
        CTBFileName = CTDir + "pbB.properties";
        PBSender.signcryption("Hello",CBAFileName,CTAFileName);
        PBSender.signcryption("Hello",PBBFileName,CTBFileName);
        System.out.println(cloud.equalityTest(CTAFileName,CBATDFileName,CTBFileName ,PBBTDFileName));

    }
}
