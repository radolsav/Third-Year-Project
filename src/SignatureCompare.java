import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.orchestrate.client.*;

import java.io.IOException;
import java.util.ArrayList;

public class SignatureCompare {
  @JsonIgnoreProperties
  public static void compareSignatures() throws IOException {
    Client client = new OrchestrateClient("e4f5cbf3-991a-41ab-aa6e-346d53c0ac2b");
    Signature signature = new Signature("49D165883CCACF53621145BAADFEC095",58,"Test.java.txt");

    ArrayList<Signature> signatureList = new ArrayList<>(100);
    for(Signature signature1 : signatureList)
    {
      signature1.setSignatureHash();
      signature1.setSize();
      signature1.setMalwareName();
    }
    KvMetadata signatureMeta = client.postValue("Signatures", signature).get();

/*    KvObject<Signature> signatureKvObject =
            client.kv("Signatures", "0d94530b7140bccc")
                    .get(Signature.class)
                    .get();

    if (signatureKvObject == null) {
      System.out.println("User 'test@email.com' does not exist.");
    } else {
      Signature sgn = signatureKvObject.getValue();
      System.out.println(sgn.getSignatureHash());
      System.out.println("Hash " + sgn.getSignatureHash() + " Size " + sgn.getSize() + "Name: " + sgn.getMalwareName());
      System.out.println(signatureKvObject.getRawValue());
    }*/
  }
}
