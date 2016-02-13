/**
 * Created by Radoslav Ralinov on 30/12/2015. All rights reserved. Created as part of the Third Year Project
 * at University of Manchester. Third-Year-Project
 */
public class Signature {
    private String signature;
    private String malwareName;


    public Signature(String signature, String malwareName) {
        this.signature = signature;
        this.malwareName = malwareName;
    }

    public Signature() {

    }

    public String getSignature() {
        return signature;
    }


    public String getMalwareName() {
        return malwareName;
    }

    public void setMalwareName(String malwareName) {
        this.malwareName = malwareName;
    }

    public void setSignatureHash(String signature) {
        this.signature = signature;
    }
}
