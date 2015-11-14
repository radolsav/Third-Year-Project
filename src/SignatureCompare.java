import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.orchestrate.client.*;

import java.io.IOException;

public class SignatureCompare {

  private static final Client client = new OrchestrateClient("e4f5cbf3-991a-41ab-aa6e-346d53c0ac2b");

  @JsonIgnoreProperties
  public static void compareSignatures(String md5hash) throws IOException {
//    HashSignature signature = new HashSignature("44D88612FEA8A8F36DE82E1278ABB02F",68,"eicar.com");

//    KvMetadata signatureMeta = client.postValue("HashSignatures", signature).get();
    /*KvObject<HashSignature> signatureKvObject =
            client.kv("HashSignatures", "0db20ca56640ae01")
                    .get(HashSignature.class)
                    .get();*/

    String luceneQuery = "value.signature: " + md5hash;
    SearchResults<HashSignature> results =
            client.searchCollection("HashSignatures")
                    .limit(20)
                    .get(HashSignature.class, luceneQuery)
                    .get();

    for (Result<HashSignature> signature : results) {
      KvObject<HashSignature> kvObject = signature.getKvObject();
      if (kvObject == null) {
        System.out.println("No threat found!");
      } else {
        HashSignature hashSignature = kvObject.getValue();
        System.out.println("Threat found!");
        ViradoGUI viradoGUI = new ViradoGUI();
        viradoGUI.setInfectedFiles();
        System.out.println("Malware name: " + hashSignature.getMalwareName());
        System.out.println("Malware size: " + hashSignature.getSize());
        System.out.println("Malware signature: " + hashSignature.getSignature());
      }
    }

    /*if (signatureKvObject == null) {
      System.out.println("User 'test@email.com' does not exist.");
    } else {
      HashSignature sgn = signatureKvObject.getValue();
      System.out.println(sgn.getSignature());
      System.out.println("Hash " + sgn.getSignature() + " Size " + sgn.getSize() + "Name: " + sgn.getMalwareName());
      System.out.println(signatureKvObject.getRawValue());
    }*/
  }

}
