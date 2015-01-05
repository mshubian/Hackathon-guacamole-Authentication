package com.hackathon.guacamole;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import org.apache.log4j.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class SignatureVerifier {
    private final SecretKeySpec secretKey;

    private Logger logger = Logger.getLogger(SignatureVerifier.class);

    public SignatureVerifier(String secretKey) {
        this.secretKey = new SecretKeySpec(secretKey.getBytes(), "HmacSHA1");
    }

    private Mac createMac() throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(secretKey);
        return mac;
    }
    
    public boolean verifySignature(String signature, String message) {
        try {
            Mac mac = createMac();
            String expected = Base64.encode(mac.doFinal(message.getBytes()));
            logger.info("signature from server bring up is:  " + expected );
            return signature.equals(expected);
        } catch (InvalidKeyException e) {
            return false;
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }
}

