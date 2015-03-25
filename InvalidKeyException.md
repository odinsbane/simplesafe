#Describes how to avoid an "Illegal key size or default parameters" exception.

# Introduction #

As outlined here: http://stackoverflow.com/a/992413 a java.security.InvalidKeyException occurs with the message "Illegal key size or default parameters". This is because the keysize is too large and cannot be shipped with the JVM by default. So either change the keysize or ...

Instead you should get no errors. The stackoverflow thread points to the answer. You need to download the Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files 7 Download, for java 7 it is at http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html .

I replaced the files found in ${jdk}/jre/lib/security with the files from the JCE and it worked fine.