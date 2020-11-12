/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.quickchecksdcard;

import java.io.Console;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 *
 * @author erhannis
 */
public class Main {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws FileNotFoundException, IOException {
    args = new String[]{"/dev/sdb"};
    boolean syncwait = true;
    if (args.length != 1) {
      System.out.println("sdcheck DRIVE_FILE\ne.g. `sdcheck /dev/sdb`");
      return;
    }
    SecureRandom sr = new SecureRandom();
    byte[] target = new byte[8];
    //sr.nextBytes(target);
    target = new byte[]{'S','D','C','A','R','D','X','Z'};
    
    byte[] buffer = new byte[8];
    long pos = 0;
    final long INITIAL_OFF = 8;
    long off = INITIAL_OFF;
    
    System.out.println("Start");
    if (syncwait) {
      while (true) {        
        RandomAccessFile raf = new RandomAccessFile(args[0], "rws");
        
        System.out.println("Start write");
        off = INITIAL_OFF;
        while (true) {
          try {
            raf.seek(pos+off);
            raf.write(target);
            off *= 2;
            System.out.println("pos off " + pos + " " + off + " -> " + (pos+off));
          } catch (IOException e) {
            System.out.println("IOException at " + (pos+off));
            e.printStackTrace();
            if (off == INITIAL_OFF) {
              System.out.println("Final failure: " + (pos+off));
              return;
            }
            System.out.println("pos off " + pos + " " + off + " -> " + (pos+off));
            break;
          }
        }

        raf.close();
        System.out.println("Sync, eject, and reseat device, then hit enter");
        System.console().readLine();
        raf = new RandomAccessFile(args[0], "rws");
        
        System.out.println("Start read");
        off = INITIAL_OFF;
        while (true) {
          try {
            raf.seek(pos+off);
            raf.read(buffer);
            if (Arrays.compare(target, buffer) != 0) {
              System.out.println("Failed verification at " + (pos+off));
              if (off == INITIAL_OFF) {
                System.out.println("Final failure: " + (pos+off));
                return;
              }
              pos += off/2;
              break;
            } else {
              off *= 2;
            }
            System.out.println("pos off " + pos + " " + off + " -> " + (pos+off));
          } catch (IOException e) {
            System.out.println("IOException at " + (pos+off));
            e.printStackTrace();
            if (off == INITIAL_OFF) {
              System.out.println("Final failure: " + (pos+off));
              return;
            }
            System.out.println("pos off " + pos + " " + off + " -> " + (pos+off));
            pos += off/2;
            break;
          }
        }
        raf.close();
      }
    } else {
      RandomAccessFile raf = new RandomAccessFile(args[0], "rws");
      System.out.println("Start write/read");
      while (true) {
        try {
          raf.seek(pos+off);
          raf.write(target);
          raf.seek(pos+off);
          raf.read(buffer);
          if (Arrays.compare(target, buffer) != 0) {
            System.out.println("Failed verification at " + (pos+off));
            if (off == INITIAL_OFF) {
              System.out.println("Final failure: " + (pos+off));
              return;
            }
            pos += off/2;
            off = INITIAL_OFF;
          } else {
            off *= 2;
          }
          System.out.println("pos off " + pos + " " + off + " -> " + (pos+off));
        } catch (IOException e) {
          System.out.println("IOException at " + (pos+off));
          e.printStackTrace();
          if (off == INITIAL_OFF) {
            System.out.println("Final failure: " + (pos+off));
            return;
          }
          pos += off/2;
          off = INITIAL_OFF;
          System.out.println("pos off " + pos + " " + off + " -> " + (pos+off));
        }
      }
    }
  }
}
