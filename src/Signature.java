/**
 * Created by User on 12/11/2015.
 */
public class Signature {
  private String signature;
  private String malwareName;


  public Signature(String signatureHash, String malwareName) {
    this.signature = signatureHash;
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
