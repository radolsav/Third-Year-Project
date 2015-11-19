/**
 * Created by User on 04/11/2015.
 */
public class HashSignature extends Signature{
  private long size;

  public HashSignature(String signatureHash,long size, String malwareName) {
    super(signatureHash,malwareName);
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
