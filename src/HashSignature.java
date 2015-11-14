/**
 * Created by User on 04/11/2015.
 */
public class HashSignature extends Signature{
  private int size;

  public HashSignature(String signatureHash,int size, String malwareName) {
    super(signatureHash,malwareName);
    this.size = size;
  }

  public HashSignature() {

  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }
}
