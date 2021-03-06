package org.ozsoft.encryption;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test suite for the <code>Encryptor</code> class.
 * 
 * @author Oscar Stigter
 */
public class EncryptorTest {

    private static final String CLEARTEXT = "This is a very secret message.";

    private static final String PASSWORD = "aBc#123!";

    private static final int BLOCK_SIZE = 10 * 1024 * 1024; // 10MB

    @Test
    public void encryption() throws Exception {
        Random random = new Random();
        byte[] cleardata = null;
        byte[] cipherdata = null;
        String cleartext = null;
        String ciphertext = null;
        long startTime = 0L;
        long duration = 0L;

        startTime = System.currentTimeMillis();
        Encryptor encryptor = new Encryptor();
        duration = System.currentTimeMillis() - startTime;
        Assert.assertNotNull(encryptor);
        System.out.format("Encryptor created in %d ms\n", duration);

        // Set shared key based on password.
        startTime = System.currentTimeMillis();
        encryptor.setKey(PASSWORD);
        duration = System.currentTimeMillis() - startTime;
        System.out.format("Key generated in %d ms\n", duration);

        // Encrypt and decrypt a byte array.
        cleardata = null;
        cipherdata = encryptor.encrypt(cleardata);
        assertNull(cipherdata);
        cleardata = encryptor.decrypt(cipherdata);
        assertNull(cleardata);
        cleardata = new byte[0];
        cipherdata = encryptor.encrypt(cleardata);
        Assert.assertNotNull(cipherdata);
        assertEquals(0, cipherdata.length);
        cleardata = encryptor.decrypt(cipherdata);
        Assert.assertNotNull(cleardata);
        assertEquals(0, cleardata.length);
        startTime = System.currentTimeMillis();
        cleardata = CLEARTEXT.getBytes();
        cipherdata = encryptor.encrypt(cleardata);
        duration = System.currentTimeMillis() - startTime;
        System.out.format("Byte array encrypted in %d ms\n", duration);
        startTime = System.currentTimeMillis();
        cleardata = encryptor.decrypt(cipherdata);
        cleartext = new String(cleardata);
        duration = System.currentTimeMillis() - startTime;
        assertEquals(CLEARTEXT, cleartext);
        System.out.format("Byte array decrypted in %d ms\n", duration);

        // Encrypt and decrypt large data block.
        cleardata = new byte[BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; i++) {
            cleardata[i] = (byte) random.nextInt(256);
        }
        startTime = System.currentTimeMillis();
        cipherdata = encryptor.encrypt(cleardata);
        duration = System.currentTimeMillis() - startTime;
        System.out.println("Large text block encrypted in  " + duration + " ms.");
        // Decrypt large data block.
        startTime = System.currentTimeMillis();
        cleardata = encryptor.decrypt(cipherdata);
        duration = System.currentTimeMillis() - startTime;
        System.out.format("Large data block decrypted in %d ms\n", duration);

        // Encypt and decrypt String.
        cleartext = null;
        ciphertext = encryptor.encrypt(cleartext);
        assertEquals(null, ciphertext);
        cleartext = encryptor.decrypt(ciphertext);
        assertEquals(null, cleartext);
        cleartext = CLEARTEXT;
        ciphertext = encryptor.encrypt(cleartext);
        cleartext = encryptor.decrypt(ciphertext);
        assertEquals(CLEARTEXT, cleartext);

        // Encrypt and decrypt stream.
        cleartext = "This is a test with streaming encryption and decryption.";
        InputStream clearIn = IOUtils.toInputStream(cleartext);
        ByteArrayOutputStream cipherOut = new ByteArrayOutputStream();
        encryptor.encrypt(clearIn, cipherOut);
        cipherdata = cipherOut.toByteArray();
        cipherOut.close();
        clearIn.close();
        InputStream cipherIn = new ByteArrayInputStream(cipherdata);
        ByteArrayOutputStream clearOut = new ByteArrayOutputStream();
        encryptor.decrypt(cipherIn, clearOut);
        assertEquals("Streaming encryption/decryption failed", cleartext, clearOut.toString());
    }
}
