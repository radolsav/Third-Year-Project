import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.orchestrate.client.*;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Created by Radoslav Ralinov on 30/12/2015. All rights reserved. Created as part of the Third Year Project
 * at University of Manchester. Third-Year-Project
 */
public class SignatureCompare {

    private static final String HASH_COLLECTION = "HashSignatures";
    private static final String BYTE_COLLECTION = "ByteSignatures";

    @JsonIgnoreProperties
    public static  ArrayList<HashSignature> compareHashSignatures(Client client, long size) {
//    HashSignature signature1 = new HashSignature("275A021BBFB6489E54D471899F7DB9D1663FC695EC2FE2A2C4538AABF651FD0F",68,"eicar.com");

//    KvMetadata signatureMeta = client.postValue("HashSignatures", signature1).get();
    /*KvObject<HashSignature> signatureKvObject =
            client.kv("HashSignatures", "0db20ca56640ae01")
                    .get(HashSignature.class)
                    .get();*/
        ArrayList<HashSignature> hashSignature = new ArrayList<>();
        try {
            String luceneQuery = "value.size: " + size;
            SearchResults<HashSignature> results =
                    client.searchCollection(HASH_COLLECTION)
                            .limit(20)
                            .get(HashSignature.class, luceneQuery)
                            .get(5000L, TimeUnit.MILLISECONDS);
            if (results.getCount() == 0) {
                System.out.println("No threat found!");
            } else {
                for (Result<HashSignature> signature : results) {
                    KvObject<HashSignature> kvObject = signature.getKvObject();
                    hashSignature.add(kvObject.getValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

   /* try {
      client.close();
    }
    catch (IOException exception)
    {
      exception.printStackTrace(System.err);
    }*/

        return hashSignature;
    }

    @JsonIgnoreProperties
    public static  boolean compareByteSignatures(byte[] fileBytes, Client client) {
        /*String str = new String(fileBytes, "UTF-8"); // for UTF-8 encoding
        ByteSignature signature = new ByteSignature(str,"Eicar-test-signature2");
    KvMetadata signatureMeta = client.postValue("ByteSignatures", signature).get();*/
   /* KvList<ByteSignature> results =
            client.listCollection("ByteSignatures")
                    .limit(20)
                    .get(ByteSignature.class)
                    .get();*/
        Collection<Emit> emits;
        try {
            String stringBytes = new String(fileBytes, "UTF-8"); // for UTF-8 encoding
            KvObject<ByteSignature> signatureKvObject =
                    client.kv(BYTE_COLLECTION, "0f9b8afa4c40f9fe")
                            .get(ByteSignature.class)
                            .get(5000L, TimeUnit.MILLISECONDS);
            ByteSignature byteSignature = signatureKvObject.getValue();
            Trie trie = Trie.builder().removeOverlaps().addKeyword(byteSignature.getSignature()).build();
            emits = trie.parseText(stringBytes);
            return !emits.isEmpty();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return false;
    }
}


