jVault - Simple Password Encryption
The emphasis here is on simple, but not simple encryption, simple in format. This program grew out of the need to manage my passwords once I moved to a Linux workstation. My previous method was using a memory stick with built in encryption, that would only work on Windows. 

After researching and trying a number of open source solutions out, I realized that none of them did exactly what I wanted. Mostly they forced you into a structure where you had to enter userid, server, and password all in separate fields. I was used to a using free form text file, where I could put things and search for things however I wanted. This is jVault.</p>

jVault is not really a password encryption tool. It allows you to encrypt and store text files. You can have whatever you want in the text file. I use it to store server userid and passwords and database usernames and passwords typically in a loose structure by server name or function.

A password is required to encrypt the text file when saving. The textfile is stored as encrypted text that looks like an SSL certificate. The same password must be used to decrypt the file. If you forget the password you're out of luck. Don't call me, there's nothing I can do. (Here's an idea, stick the password on your monitor!). 

Different files can have different passwords. You can change your password by SaveAs to a different file. To put all your passwords into jVault, create a New document and copy and paste them in, then Save. You'll be prompted for a password.

This program was more 'assembled' than written. It definately <a href="http://en.wikipedia.org/wiki/Standing_on_the_shoulders_of_giants">stands on the shoulders of giants.</a> It uses the Java Swing TextArea demo as its starting point. (<a href="http://java.sun.com/docs/books/tutorial/uiswing/examples/components/index.html">See Swing Components Examples.</a>) Most of the Swing functionality comes from there. jVault also depends heavily on the JASYPT (Java Simple Encryption) library. Without JASYPT, jVault would not be possible. See the <a href="http://www.jasypt.org/">JASYPT website</a> for information on security. It's pretty good for being simple. Thanks to those contributions that made jVault possible.

The useful part (for me) of jVault is that you can put it on a memory stick(I now have it on the unencrypted part of mine), and run it on anything. Works on Windows, Linux and Mac, and probably anything that's Java enabled. All librarys that are needed are included in the distribution. You'll need to provide your own Java VM. Works on 1.6 and probably on some lower versions as well.

Bob Walker
bwalker99@gmail.com
