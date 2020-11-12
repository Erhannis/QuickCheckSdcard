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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author erhannis
 */
public class Main {
  private static enum Mode {
    WRITE_READ,
    WRITE_SYNC_READ,
    WRITE_FULL
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws FileNotFoundException, NoSuchAlgorithmException {
    //args = new String[]{"/dev/sdb"};
    Mode mode = Mode.WRITE_FULL;
    if (args.length != 1) {
      System.out.println("sdcheck DRIVE_FILE\ne.g. `sdcheck /dev/sdb`");
      return;
    }
    SecureRandom sr = new SecureRandom();
    
    System.out.println("Start");
    switch (mode) {
      case WRITE_SYNC_READ:
        {
          byte[] target = new byte[8];
          //sr.nextBytes(target);
          target = new byte[]{'S','D','C','A','R','D','X','Z'};
          byte[] buffer = new byte[8];

          long pos = 0;
          final long INITIAL_OFF = 8;
          long off = INITIAL_OFF;
          
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

            try {
              raf.close();
            } catch (IOException ex) {
              Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
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
            try {
              raf.close();
            } catch (IOException ex) {
              Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
          }
          //break;
        }
      case WRITE_READ:
        {
          byte[] target = new byte[8];
          //sr.nextBytes(target);
          target = new byte[]{'S','D','C','A','R','D','X','Z'};
          byte[] buffer = new byte[8];

          long pos = 0;
          final long INITIAL_OFF = 8;
          long off = INITIAL_OFF;

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
          //break;
        }
      case WRITE_FULL:
        {
          MessageDigest digest = MessageDigest.getInstance("SHA-256");
          byte[] salt = new byte[8];
          sr.nextBytes(salt);
          
          System.out.println("Start write full");
          RandomAccessFile raf = new RandomAccessFile(args[0], "rw");
          try {
            while (true) {
              long pos = raf.getFilePointer();
              digest.reset();
              digest.update(salt);
              byte[] hash = digest.digest(longToBytes(pos));
              raf.write(hash);
            }
          } catch (IOException e) {
            try {
              long pos = raf.getFilePointer();
              System.out.println("(Probably) hit end at " + pos);
            } catch (IOException ex) {
              Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
          }
          try {
            raf.close();
          } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
          }
          System.out.println("Sync, eject, and reseat device, then hit enter");
          System.console().readLine();
          
          System.out.println("Start read full");
          raf = new RandomAccessFile(args[0], "rw");
          byte[] buffer = new byte[32];
          try {
            while (true) {
              long pos = raf.getFilePointer();
              digest.reset();
              digest.update(salt);
              byte[] hash = digest.digest(longToBytes(pos));
              raf.read(buffer);
              if (Arrays.compare(hash, buffer) != 0) {
                System.out.println("Failed verification at " + pos);
                System.out.println("(If right near the reported end, it may have just got cut off, and is actually fine.)");
                raf.close();
                return;
              }
            }
          } catch (IOException e) {
            try {
              long pos = raf.getFilePointer();
              System.out.println("(Probably) hit end at " + pos);
              System.out.println("Which means the device is probably fine!");
            } catch (IOException ex) {
              Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
          }
          try {
            raf.close();
          } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
          }

          break;
        }
      default:
        throw new RuntimeException("Unhandled mode: " + mode);
    }
  }
  
  // https://stackoverflow.com/a/29132118
  public static byte[] longToBytes(long l) {
    byte[] result = new byte[8];
    for (int i = 7; i >= 0; i--) {
        result[i] = (byte)(l & 0xFF);
        l >>= 8;
    }
    return result;
  }
}
