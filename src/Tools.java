import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Properties;
import java.util.Random;

public class Tools {
//    private Pairing bp;
//    private Field G,Zq;
//    private Properties SPP;
//    private Element P;
//    public Tools(String pairingParametersFileName, String SPPFileName){
//        // pairing
//        this.bp = PairingFactory.getPairing(pairingParametersFileName);
//        this.G = bp.getG1();
//        this.Zq = bp.getZr();
//
//        // 將公開參數 SPP 讀入
//        this.SPP = Tools.loadPropFromFile(SPPFileName);
//
//        // 還原 P(G 的 generator)
//        String P_str = SPP.getProperty("P");
//        this.P = G.newElementFromBytes(Base64.getDecoder().decode(P_str)).getImmutable();
//    }
    public static void storePropToFile(Properties prop, String fileName){
        try(FileOutputStream out = new FileOutputStream(fileName)){
            prop.store(out,null);
        }
        catch (IOException e){
            e.printStackTrace();
            System.out.println(fileName + " save failed!");
            System.exit(-1);
        }
    }

    public static Properties loadPropFromFile(String fileName){
        Properties prop = new Properties();
        try(FileInputStream in = new FileInputStream(fileName)){
            prop.load(in);
        }
        catch(IOException e){
            e.printStackTrace();
            System.out.println(fileName + " load failed!");
            System.exit(-1);
        }
        return prop;
    }

    public static byte[] HF0(String id, Element a, Element b, Element c) {
        String a_str = Base64.getEncoder().encodeToString(a.toBytes());
        String b_str = Base64.getEncoder().encodeToString(b.toBytes());
        String c_str = Base64.getEncoder().encodeToString(c.toBytes());
        String hash_str = id + a_str + b_str + c_str;
        // byte[] rt;
        try {
            return sha1(hash_str);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.out.println("HF0、HF1: error!");
            System.exit(-1);
        }
        return null;
    }

    public static byte[] HF1(String id, Element a, Element b, Element c) {
        return HF0(id,a,b,c);
    }

    public static byte[] HF2( Element a, Element b) {
        String a_str = Base64.getEncoder().encodeToString(a.toBytes());
        String b_str = Base64.getEncoder().encodeToString(b.toBytes());
        String hash_str = a_str + b_str;
        // byte[] rt;
        try {
            return sha1(hash_str);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.out.println("HF2: error!");
            System.exit(-1);
        }
        return null;
    }

    public static Element HF3(Element a,String pairingParametersFileName) {
        String hash_str = Base64.getEncoder().encodeToString(a.toBytes());
        Pairing bp = PairingFactory.getPairing(pairingParametersFileName);
        Field G = bp.getG1();
        try {
            byte[] hashByte =  sha1(hash_str);
            Element rt = G.newElementFromHash(hashByte,0,hashByte.length).getImmutable();
            return rt;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.out.println("HF3: error!");
            System.exit(-1);
        }
        return null;
    }

    public  static byte[] genSk(){
        Random random = new Random();
        byte[] randomBytes = new byte[20];
        random.nextBytes(randomBytes);
        return randomBytes;
    }

    public static byte[] SE(String msg, byte[] sk){
        byte[] msgByte = Base64.getDecoder().decode(msg);

    }
    public static byte[] sha1(String content) throws NoSuchAlgorithmException{
        MessageDigest instance = MessageDigest.getInstance("SHA-1");
        instance.update(content.getBytes());
        return instance.digest();
    }
}
