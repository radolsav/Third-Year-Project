import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.orchestrate.client.*;

import java.io.IOException;
import java.util.ArrayList;

public class SignatureCompare {


  @JsonIgnoreProperties
  public static ArrayList<HashSignature> compareSignatures(Client client, long size)
          throws IOException {
//    HashSignature signature = new HashSignature("44D88612FEA8A8F36DE82E1278ABB02F",68,"eicar.com");

//    KvMetadata signatureMeta = client.postValue("HashSignatures", signature).get();
    /*KvObject<HashSignature> signatureKvObject =
            client.kv("HashSignatures", "0db20ca56640ae01")
                    .get(HashSignature.class)
                    .get();*/

    String luceneQuery = "value.size: " + size;
    SearchResults<HashSignature> results =
            client.searchCollection("HashSignatures")
                    .limit(20)
                    .get(HashSignature.class, luceneQuery)
                    .get();

    client.close();
    ArrayList<HashSignature> hashSignature = new ArrayList<>();
    if (results.getCount() == 0) {
      System.out.println("No threat found!");
    } else {
      for (Result<HashSignature> signature : results) {
        KvObject<HashSignature> kvObject = signature.getKvObject();
        hashSignature.add(kvObject.getValue());
      }
    }
    return hashSignature;
  }
}


