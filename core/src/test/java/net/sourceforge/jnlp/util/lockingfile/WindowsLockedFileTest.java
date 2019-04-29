/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.jnlp.util.lockingfile;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.OS_NAME;

/**
 *
 * @author jvanek
 */
public class WindowsLockedFileTest {

    private static String os;

    @BeforeClass
    public static void smuggleOs() {
        os = System.getProperty(OS_NAME);
        System.setProperty(OS_NAME, "Windows for itw");
    }

    @AfterClass
    public static void restoreOs() {
        System.setProperty(OS_NAME, os);
    }

    @Test
    public void testLockUnlockOkExists() throws IOException {
        File f = File.createTempFile("itw", "lockingFile");
        f.deleteOnExit();
        LockedFile lf = LockedFile.getInstance(f);
        lf.lock();
        lf.unlock();
    }

    @Test
    public void testLockUnlockOkNotExists() throws IOException {
        File f = File.createTempFile("itw", "lockingFile");
        f.delete();
        LockedFile lf = LockedFile.getInstance(f);
        lf.lock();
        lf.unlock();
    }

    @Test
    public void testLockUnlockNoOkNotExists() throws IOException {
        File parent = File.createTempFile("itw", "lockingFile");
        parent.deleteOnExit();
        File f = new File(parent, "itwLcokingRelict");
        f.delete();
        parent.setReadOnly();
        LockedFile lf = LockedFile.getInstance(f);
        lf.lock();
        lf.unlock();;
    }

    @Test
    public void testLockUnlockNotOkExists() throws IOException {
        File f = new File("/some/definitely/not/existing/file.itw");
        LockedFile lf = LockedFile.getInstance(f);
        lf.lock();
        lf.unlock();
    }

}
