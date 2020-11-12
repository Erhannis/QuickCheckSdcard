Program to check how much stuff a disk can actually hold.

Writes salted SHA256 hashes of position into the target file (1st argument) (e.g. "/dev/sdb") until an error occurs.  Then it waits for you to sync, remove, and reseat the device.  Then it checks the disk until it encounters a discrepancy.  The position of the error, to within 32 bytes, is reported in bytes.

Released under the Apache License 2.0

-Erhannis