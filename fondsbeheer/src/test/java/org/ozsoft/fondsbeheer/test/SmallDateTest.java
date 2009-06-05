package org.ozsoft.fondsbeheer.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.ozsoft.fondsbeheer.entities.SmallDate;

public class SmallDateTest {
    
    @Test
    public void testBasics() throws Exception {
        SmallDate date1 = new SmallDate( 1, 1, 1970);

        Assert.assertEquals(1, date1.getDay());
        Assert.assertEquals(1, date1.getMonth());
        Assert.assertEquals(1970, date1.getYear());
        
        SmallDate date2 = new SmallDate(31, 1, 1970);
        Assert.assertEquals(31, date2.getDay());
        Assert.assertEquals(1, date2.getMonth());
        Assert.assertEquals(1970, date2.getYear());

        SmallDate date3 = new SmallDate( 1, 2, 1970);
        Assert.assertEquals(1, date3.getDay());
        Assert.assertEquals(2, date3.getMonth());
        Assert.assertEquals(1970, date3.getYear());

        // Comparisons.
        Assert.assertTrue(date1.equals(date1));
        Assert.assertFalse(date1.equals(date2));
        Assert.assertFalse(date2.equals(date3));
        Assert.assertTrue(date1.compareTo(date1) == 0);
        Assert.assertTrue(date2.compareTo(date2) == 0);
        Assert.assertTrue(date3.compareTo(date3) == 0);
        Assert.assertTrue(date2.compareTo(date1) > 0);
        Assert.assertTrue(date1.compareTo(date2) < 0);
        Assert.assertTrue(date1.compareTo(date3) < 0);
        Assert.assertTrue(date3.compareTo(date2) > 0);
        Assert.assertTrue(date2.compareTo(date1) > 0);

        // Short format.
        Assert.assertEquals("01-Jan-1970", date1.toString());
        Assert.assertEquals(date1, SmallDate.parseDate("700101"));
        Assert.assertEquals("31-Jan-1970", date2.toString());
        Assert.assertEquals(date2, SmallDate.parseDate("700131"));
        Assert.assertEquals("01-Feb-1970", date3.toString());
        Assert.assertEquals(date3, SmallDate.parseDate("700201"));
        
        // ISO format.
        Assert.assertEquals("1970-01-01", date1.toIsoString());
        Assert.assertEquals(date1, SmallDate.parseIsoDate("1970-01-01"));
        Assert.assertEquals("1970-01-31", date2.toIsoString());
        Assert.assertEquals(date2, SmallDate.parseIsoDate("1970-01-31"));
        Assert.assertEquals("1970-02-01", date3.toIsoString());
        Assert.assertEquals(date3, SmallDate.parseIsoDate("1970-02-01"));
    }
    
    @Test
    public void serialization() throws IOException {
        SmallDate date1 = new SmallDate( 1,  1, 1970);
        SmallDate date2 = new SmallDate(27,  5, 1983);
        SmallDate date3 = new SmallDate( 2,  3, 2143);
        
        // Serialize to binary file.
        DataOutputStream dos = new DataOutputStream(new FileOutputStream("file.bin"));
        date1.serialize(dos);
        date2.serialize(dos);
        date3.serialize(dos);
        dos.close();
        
        // Deserialize from binary file.
        DataInputStream dis = new DataInputStream(new FileInputStream("file.bin"));
        date1.deserialize(dis);
        Assert.assertEquals("1970-01-01", date1.toIsoString());
        date2.deserialize(dis);
        Assert.assertEquals("1983-05-27", date2.toIsoString());
        date3.deserialize(dis);
        Assert.assertEquals("2143-03-02", date3.toIsoString());
        dis.close();
    }

}