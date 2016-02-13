/**
 * Created by Radoslav Ralinov on 30/12/2015. All rights reserved. Created as part of the Third Year Project
 * at University of Manchester. Third-Year-Project
 */
public class HashSignature extends Signature {
    private long size;

    public HashSignature(String signatureHash, long size, String malwareName) {
        super(signatureHash, malwareName);
        this.size = size;
    }

    public HashSignature() {

    }

    public long getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
