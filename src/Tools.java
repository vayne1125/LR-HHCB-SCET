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

    public static byte[] HF4( Element a, Element b, Element c, Element d) {
        String a_str = Base64.getEncoder().encodeToString(a.toBytes());
        String b_str = Base64.getEncoder().encodeToString(b.toBytes());
        String c_str = Base64.getEncoder().encodeToString(c.toBytes());
        String d_str = Base64.getEncoder().encodeToString(d.toBytes());
        String hash_str = a_str + b_str + c_str + d_str;
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

    public static Element HF5(Element a,Element b, String pairingParametersFileName) {
        String a_str = Base64.getEncoder().encodeToString(a.toBytes());
        String b_str = Base64.getEncoder().encodeToString(b.toBytes());
        String hash_str = a_str + b_str;
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
    public static byte[] HF6(byte[] a){
        try {
            return sha1(Base64.getEncoder().encodeToString(a));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.out.println("HF6: error!");
            System.exit(-1);
        }
        return null;
    }

    public static Element HF7(String msg,String pairingParametersFileName){
        Pairing bp = PairingFactory.getPairing(pairingParametersFileName);
        Field G = bp.getG1();
        try {
            byte[] hashByte =  sha1(msg);
            Element rt = G.newElementFromHash(hashByte,0,hashByte.length).getImmutable();
            return rt;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.out.println("HF7: error!");
            System.exit(-1);
        }
        return null;
    }

    public static byte[] HF8(String msg,byte[] sk, Element CT0, byte[] CT1, byte[] CT2,Element CT3, Element CT4){
        String sk_str = Base64.getEncoder().encodeToString(sk);
        String CT0_str = Base64.getEncoder().encodeToString(CT0.toBytes());
        String CT1_str = Base64.getEncoder().encodeToString(CT1);
        String CT2_str = Base64.getEncoder().encodeToString(CT2);
        String CT3_str = Base64.getEncoder().encodeToString(CT3.toBytes());
        String CT4_str = Base64.getEncoder().encodeToString(CT4.toBytes());
        String hash_str = msg + sk_str + CT0_str + CT1_str + CT2_str + CT3_str + CT4_str;
        try {
            return sha1(hash_str);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.out.println("HF8: error!");
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
        byte[] msgByte = msg.getBytes();
        int skLen = sk.length;
        byte[] rt = new byte[msgByte.length];
        for(int i=0;i<msgByte.length;i++){
            rt[i] = (byte) (msgByte[i] ^ sk[i%skLen]);
        }
        return rt;
    }

    public static String SD(byte[] CT1, byte[] sk_){
        int skLen = sk_.length;
        byte[] rt = new byte[CT1.length];
        for(int i=0;i<CT1.length;i++){
            rt[i] = (byte) (CT1[i] ^ sk_[i%skLen]);
        }
        return new String(rt);
    }

    public static byte[] XOR(byte[] x, byte[] y){

        byte[] rt = new byte[x.length];
        for(int i=0;i<x.length;i++){
            rt[i] = (byte) (x[i] ^ y[i]);
        }
        // System.out.println(new String(msgByte));
        return rt;
    }

    public static byte[] sha1(String content) throws NoSuchAlgorithmException{
        MessageDigest instance = MessageDigest.getInstance("SHA-1");
        instance.update(content.getBytes());
        return instance.digest();
    }
}
