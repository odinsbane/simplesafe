package simplecrypt;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;

/**
 * For encrypting and decrypting strings to a file. The salt is fixed, the keysize can change depending on
 * weather or not your platform supports the larger key.
 *
 * User: melkor
 * Date: 2/17/13
 * Time: 9:45 AM
 */
public class SafeCrypt {
    final String encryption_provider = "AES/CBC/PKCS5Padding";
    char[] password;
    byte[] salt;
    ArrayList<String> errors = new ArrayList<String>();
    SecretKey secret;
    int key_size;
    public SafeCrypt(String salt, int key_size){
        this.key_size = key_size;
        try {
            this.salt = salt.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            errors.add("UTF-8 not supported using platform default");
            this.salt = salt.getBytes();
            e.printStackTrace();
        }
    }

    public void setPassword(String password){
        this.password = password.toCharArray();

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(this.password, salt, 65536, key_size);
            SecretKey tmp = factory.generateSecret(spec);
            secret = new SecretKeySpec(tmp.getEncoded(), "AES");


        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }


    }

    public void encryptAndSave(File outfile, String data){

        /* Encrypt the message. */
        byte[] bytes_to_write = null;
        try {
            Cipher cipher = Cipher.getInstance(encryption_provider);
            cipher.init(Cipher.ENCRYPT_MODE, secret);
            AlgorithmParameters params = cipher.getParameters();
            byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
            byte[] ciphertext = cipher.doFinal(getBytes(data));
            bytes_to_write= new byte[iv.length+ciphertext.length + 1];
            bytes_to_write[0] = (byte)iv.length;
            System.arraycopy(iv,0,bytes_to_write,1,iv.length);
            System.arraycopy(ciphertext, 0, bytes_to_write, 1+iv.length, ciphertext.length);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidParameterSpecException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }


        try {
            FileOutputStream fos = new FileOutputStream(outfile);
            fos.write(bytes_to_write);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    final int chunk_size = 2048;
    public String decryptFile(File infile){
        byte[] cipher_text, iv;
        try {

            FileInputStream fis = new FileInputStream(infile);
            int reading = 1;
            int total = 0;
            ArrayList<byte[]> chunks = new ArrayList<byte[]>();
            while(reading>0){
                byte[] chunk = new byte[chunk_size];
                int read = fis.read(chunk);
                if(read>0){
                    total += read;
                    chunks.add(chunk);
                } else{
                    //the file size lined up with the chunk_size.
                    reading = 0;
                }
                if(read<chunk_size){
                    reading=0;
                }

            }
            byte[] l_iv_text = new byte[total];
            int mapped = 0;
            for(byte[] chunk: chunks){
                int remaining = total-mapped;
                int map_size = (remaining>chunk_size)?chunk_size:remaining;
                System.arraycopy(chunk, 0, l_iv_text, mapped,map_size );
                mapped += map_size;
            }

            int iv_length = l_iv_text[0];
            iv = new byte[iv_length];
            System.arraycopy(l_iv_text,1,iv,0,iv_length);
            int text_length = total-1-iv_length;
            cipher_text = new byte[text_length];
            System.arraycopy(l_iv_text,1+iv_length, cipher_text, 0, text_length);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }

        byte[] output = null;
        /* Decrypt the message, given derived key and initialization vector. */
        try {
            Cipher cipher = Cipher.getInstance(encryption_provider);

            cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
            output = cipher.doFinal(cipher_text);


        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }  catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        if(output!=null){
            return getString(output);
        } else{
            return null;
        }
    }

    /**
     * Tries to get the bytes of this string assuming UTF-8, if that fails it
     * uses the default.
     *
     * @param s
     * @return s's bytes.
     */
    byte[] getBytes(String s){
        try{
            return s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return s.getBytes();
        }
    }

    String getString(byte[] b){
        try {
            return new String(b,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return new String(b);
        }

    }

}
 