public interface Entity {
    void selfKeyGen();
    void TDGen(String tdFileName);
    void signcryption(String msg, String receiverFileName,String CTFileName);
    String unSigncryption(String senderFileName,String CTFileName);
}
