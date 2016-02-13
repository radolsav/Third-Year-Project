import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.orchestrate.client.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.SynchronousQueue;

/**
 * Created by Radoslav Ralinov on 30/12/2015. All rights reserved. Created as part of the Third Year Project
 * at University of Manchester. Third-Year-Project
 */
public class SignatureCompare {

    @JsonIgnoreProperties
    public static ArrayList<HashSignature> compareHashSignatures(Client client, long size) {
//    HashSignature signature1 = new HashSignature("275A021BBFB6489E54D471899F7DB9D1663FC695EC2FE2A2C4538AABF651FD0F",68,"eicar.com");

//    KvMetadata signatureMeta = client.postValue("HashSignatures", signature1).get();
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
   /* try {
      client.close();
    }
    catch (IOException exception)
    {
      exception.printStackTrace(System.err);
    }*/
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

    public static boolean compareByteSignatures(Client client, String fileBytes) throws IOException {
//    ByteSignature signature = new ByteSignature("58354f2150254041505b345c505a58353428505e2937434329377d2445494341522d5354414e4441","Eicar-test-signature");
//    KvMetadata signatureMeta = client.postValue("ByteSignatures", signature).get();
   /* KvList<ByteSignature> results =
            client.listCollection("ByteSignatures")
                    .limit(20)
                    .get(ByteSignature.class)
                    .get();*/

        KvObject<ByteSignature> signatureKvObject =
                client.kv("ByteSignatures", "0de0645dc840c6cb")
                        .get(ByteSignature.class)
                        .get();

        AhoCorasick ahoCorasick = new AhoCorasick(1000);
    /*for (KvObject<ByteSignature> signatureKvObject : results) {
      // do something with the object*/
        ByteSignature byteSignature = signatureKvObject.getValue();
        ahoCorasick.addString(byteSignature.getSignature());
//      System.out.println(signatureKvObject);
//    }
        int node = 0;
        for (char ch : fileBytes.toCharArray()) {
            node = ahoCorasick.transition(node, ch);
        }
        System.out.println(ahoCorasick.nodes[node].leaf);
        return true;
    }
}


