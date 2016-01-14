/**
 * Signer.java 
 * Copyright (C) 2016 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package jloda.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.NoSuchElementException;

/**
 * methods for signing licenses
 * Daniel Huson, 4.2013
 */
public class Signer {
    private PrivateKey privateKey;
    private PublicKey publicKey;

    /**
     * constructor
     */
    public Signer() {
    }

    /**
     * Construct a signer and generate a private key and public key
     *
     * @param seed
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     */
    public Signer(int seed) throws NoSuchProviderException, NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
        keyGen.initialize(1024, random);
        //Note: The SecureRandom implementation attempts to completely randomize the internal state of the generator itself unless the caller
        // follows the call to the getInstance method with a call to the setSeed method.
        // So if you had a specific seed value that you wanted used, you would call the following prior to the initialize call:
        random.setSeed(seed);

        KeyPair pair = keyGen.generateKeyPair();
        privateKey = pair.getPrivate();
        publicKey = pair.getPublic();
    }

    /**
     * load public key from a file
     *
     * @param fileName
     * @return public key
     * @throws IOException
     * @throws InvalidKeySpecException
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     */
    public void loadPublicKey(String fileName) throws IOException, InvalidKeySpecException, NoSuchProviderException, NoSuchAlgorithmException {
        InputStream fs = ResourceManager.getFileAsStream(fileName);
        byte[] encKey = new byte[fs.available()];
        int total = fs.available();
        int count = 0;
        while (count < total) {
            count += fs.read(encKey, count, total - count);
        }
        fs.close();

        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
        KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
        publicKey = keyFactory.generatePublic(pubKeySpec);
    }

    /**
     * save the public key
     *
     * @param fileName
     * @throws IOException
     */
    public void savePublicKey(String fileName) throws IOException {
        if (publicKey == null)
            throw new NoSuchElementException("publicKey");

        byte[] bytes = publicKey.getEncoded();
        FileOutputStream fs = new FileOutputStream(fileName);
        fs.write(bytes);
        fs.close();

    }

    /**
     * save the public key
     *
     * @param fileName
     * @throws IOException
     */
    public void savePrivateKey(String fileName) throws IOException {
        if (privateKey == null)
            throw new NoSuchElementException("privateKey");
        byte[] bytes = privateKey.getEncoded();
        FileOutputStream fs = new FileOutputStream(fileName);
        fs.write(bytes);
        fs.close();

    }

    /**
     * generate a signature
     *
     * @param data
     * @return signature
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    public byte[] generateSignature(String data) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        return generateSignature(data.getBytes());
    }

    /**
     * generate a signature
     *
     * @param data
     * @return signature
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    public byte[] generateSignature(byte[] data) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        if (privateKey == null)
            throw new NoSuchElementException("privateKey");
        Signature dsa = Signature.getInstance("SHA1withDSA", "SUN");
        dsa.initSign(privateKey);
        dsa.update(data);
        return dsa.sign();
    }

    /**
     * verify that signature matches data
     *
     * @param data
     * @param signature
     * @return true, if signature is valid for data
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    public boolean verifySignedData(byte[] data, byte[] signature) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        if (publicKey == null)
            throw new NoSuchElementException("publicKey");
        Signature sig = Signature.getInstance("SHA1withDSA", "SUN");
        sig.initVerify(publicKey);
        sig.update(data);
        return sig.verify(signature);
    }

    /**
     * convert signature bytes to hex string
     *
     * @param signature
     * @return hex string
     */
    public static String signatureBytesToHexString(byte[] signature) {
        StringBuilder sb = new StringBuilder();
        for (byte b : signature) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    /**
     * converts signature hex string to bytes
     *
     * @param signature
     * @return bytes
     */
    public static byte[] signatureHexStringToBytes(String signature) {
        byte[] buf = new byte[signature.length() / 2];
        int i = 0;
        for (char c : signature.toCharArray()) {
            byte b = Byte.parseByte(String.valueOf(c), 16);
            buf[i / 2] |= (b << (((i % 2) == 0) ? 4 : 0));
            i++;
        }

        return buf;
    }

    /**
     * get public key
     *
     * @return public key
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * set public key
     *
     * @param publicKey
     */
    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * get private key
     *
     * @return private key
     */
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * set private key
     *
     * @param privateKey
     */
    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }
}
