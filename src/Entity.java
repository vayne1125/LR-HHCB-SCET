public interface Entity {
    void selfKeyGen();
    void tdGen();
    void signcryption(String msg, String receiverFileName,String CTFileName);
    String unSigncryption(String senderFileName,String CTFileName);
}
