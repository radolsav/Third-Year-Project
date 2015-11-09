/**
 * Created by User on 04/11/2015.
 */
public class Signature {
  private String signatureHash;
  private int size;
  private String malwareName;

  public Signature(String signatureHash, int size, String malwareName) {
    this.signatureHash = signatureHash;
    this.size = size;
    this.malwareName = malwareName;
  }

  public Signature() {

  }

  public String getSignatureHash() {
    return signatureHash;
  }

  public int getSize() {
    return size;
  }

  public String getMalwareName() {
    return malwareName;
  }

  public void setMalwareName(String malwareName) {
    this.malwareName = malwareName;
  }

  public void setSignatureHash(String signatureHash) {
    this.signatureHash = signatureHash;
  }

  public void setSize(int size) {
    this.size = size;
  }
}
