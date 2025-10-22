package org.example.client.components;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

// reference: https://howtodoinjava.com/java/java-security/how-to-generate-secure-password-hash-md5-sha-pbkdf2-bcrypt-examples/
public class Enc {
    private final String hashed;
    //get hash on construction
    public Enc(String pass) throws NoSuchAlgorithmException {
        hashed = hashpassword512(pass,getpwsalt());
    }

    //sha-512 since secure but not too complex, not entirely broken unlike sha1 md5 etc..
    private static String hashpassword512 (String password, String psalt){
        String hashedpw; //initialise
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512"); //setup
            md.update(psalt.getBytes());
            byte[] hashbytes = md.digest(password.getBytes()); //hash password
            StringBuilder sb = new StringBuilder(); //rebuild
            for (int i=1; i < hashbytes.length; i++){ //iterate
                sb.append(Integer.toString((hashbytes[i] & 0xff) + 0x100,16).substring((1))); //construct string as hex
            }
            hashedpw = sb.toString(); //convert to hexstring


        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return hashedpw; //return!
    }

    //generate salt for randomness
    private static String getpwsalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt.toString();
    }

    public String getres(){
        return hashed;
    }

}
