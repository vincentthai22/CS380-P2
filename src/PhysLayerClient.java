import sun.misc.Signal;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Created by Vincent on 4/20/2017.
 * CS 380 Project 2
 *
 * Physical layer simulation.
 *
 */
public class PhysLayerClient {

    private final static int PORT_NUMBER = 38002;
    private final static String SERVER_NAME = "codebank.xyz";

    private String serverName;
    private int portNumber;
    int baseline;
    private byte[] bytesArray = new byte[320];

    public PhysLayerClient(String serverName, int portNumber) {
        this.serverName = serverName;
        this.portNumber = portNumber;
        baseline = 0;

        callServer();
    }

    private void callServer() {
        Integer bytes;
        try (Socket socket = new Socket(serverName, portNumber)) {
            System.out.println("Connected to server");

            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            OutputStream os = socket.getOutputStream();
            PrintStream out = new PrintStream(os, true, "UTF-8");

            int count = 0, arrayCounter = 0;

            for (count = 0; count < 64; count++)                                /*calculate baseline*/
                baseline += is.read();

            baseline /= 64;

            System.out.println("Baseline established from preamble: " + baseline);

            while (arrayCounter != bytesArray.length) {
                bytes = is.read();

                if (bytes > baseline)
                    bytesArray[arrayCounter++] = 1;          /*Build bits accordingly to preamble*/
                else
                    bytesArray[arrayCounter++] = 0;

//                System.out.println(bytes + "    " + Long.toBinaryString(bytes) + "\tcount is : " + count +
//                        "\n bytesarray[" + (arrayCounter - 1) + "] = " + bytesArray[arrayCounter - 1]);
            }

            nrziDecoder();

            os.write(bytesArray);
            if (is.read() == 1)
                System.out.println("Response good.\nDisconnected from server.");
            else
                System.out.println("Response bad yo\nDisconnected from server.");

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    /**
     * @Params NONE @ReturnType VOID
     * method: nrziDecoder
     *
     * decodes a message that was encoded by NRZI
     * builds off of first bit.
     *
     * STEPS:
     *  1. save first bit.
     *  2. compare next bit to first bit.
     *  3. if next bit is same -- decode the message as a '0'. else its a '1'.
     *
     */

    private void nrziDecoder() {

        int current, next;

        byte[] newByteArray = new byte[bytesArray.length];
        newByteArray[0] = bytesArray[0];
        for (int i = 0; i < bytesArray.length - 1; i++) {
            current = bytesArray[i];
            next = bytesArray[i + 1];

            if (current == next) {
                newByteArray[i + 1] = 0;
            } else { //flip
                newByteArray[i + 1] = 1;
            }

        }
//        for(byte b : bytesArray)
//            System.out.print(b);

//        System.out.println();

//        for(byte b : newByteArray) {
//            System.out.print(b);
//        }
        bytesArray = newByteArray;
        fiveBitFourBitConverter();

    }


    /**
     * @PARAMS NONE @RETURNTYPE VOID
     * method: fiveBitFourBitConverter()
     *
     *  STEPS:
     *  1. Use nested forloop to separate the bits to sets of five.
     *  2. Build the 5-bit data pieces using a String
     *  3. Use a switch statement as a lookup table.
     *  4. Build a 4-bit data piece corresponding to the lookup table and store into an array.
     *  5. Convert this bitArray to a byteArray by splitting it up into 8-bit pieces of data.
     *  6. Parse the 8-bit data using Integer.parse(someString, baseNumber) \ in our case base 2.
     *  7. Assign array to global array.
     *
     */
    private void fiveBitFourBitConverter() {

        byte[] newBitArray = new byte[(int) (bytesArray.length * 0.8)];
        byte[] newByteArray = new byte[32];

        // System.out.println("\n" + newBitArray.length);
        int temp = bytesArray.length / 5, newBitArrayIndex = 0, newByteArrayIndex = 0;

        for (int i = 0; i < temp; i++) {
            String fiveBitString = "";
            for (int j = 0; j < 5; j++)
                fiveBitString += bytesArray[i * 5 + j];
//            System.out.println(fiveBitString);
            switch (fiveBitString) {
                case "11110":
                    for (int index = 0; index < 4; index++)
                        newBitArray[newBitArrayIndex++] = 0;
                    break;
                case "01001":
                    for (int index = 0; index < 3; index++)
                        newBitArray[newBitArrayIndex++] = 0;
                    newBitArray[newBitArrayIndex++] = 1;
                    break;
                case "10100":
                    newBitArray[newBitArrayIndex++] = 0;
                    newBitArray[newBitArrayIndex++] = 0;
                    newBitArray[newBitArrayIndex++] = 1;
                    newBitArray[newBitArrayIndex++] = 0;
                    break;
                case "10101":
                    newBitArray[newBitArrayIndex++] = 0;
                    newBitArray[newBitArrayIndex++] = 0;
                    newBitArray[newBitArrayIndex++] = 1;
                    newBitArray[newBitArrayIndex++] = 1;
                    break;
                case "01010":
                    newBitArray[newBitArrayIndex++] = 0;
                    newBitArray[newBitArrayIndex++] = 1;
                    newBitArray[newBitArrayIndex++] = 0;
                    newBitArray[newBitArrayIndex++] = 0;
                    break;
                case "01011":
                    newBitArray[newBitArrayIndex++] = 0;
                    newBitArray[newBitArrayIndex++] = 1;
                    newBitArray[newBitArrayIndex++] = 0;
                    newBitArray[newBitArrayIndex++] = 1;
                    break;
                case "01110":
                    newBitArray[newBitArrayIndex++] = 0;
                    newBitArray[newBitArrayIndex++] = 1;
                    newBitArray[newBitArrayIndex++] = 1;
                    newBitArray[newBitArrayIndex++] = 0;
                    break;
                case "01111":
                    newBitArray[newBitArrayIndex++] = 0;
                    newBitArray[newBitArrayIndex++] = 1;
                    newBitArray[newBitArrayIndex++] = 1;
                    newBitArray[newBitArrayIndex++] = 1;
                    break;
                case "10010":
                    newBitArray[newBitArrayIndex++] = 1;
                    newBitArray[newBitArrayIndex++] = 0;
                    newBitArray[newBitArrayIndex++] = 0;
                    newBitArray[newBitArrayIndex++] = 0;
                    break;
                case "10011":
                    newBitArray[newBitArrayIndex++] = 1;
                    newBitArray[newBitArrayIndex++] = 0;
                    newBitArray[newBitArrayIndex++] = 0;
                    newBitArray[newBitArrayIndex++] = 1;
                    break;
                case "10110":
                    newBitArray[newBitArrayIndex++] = 1;
                    newBitArray[newBitArrayIndex++] = 0;
                    newBitArray[newBitArrayIndex++] = 1;
                    newBitArray[newBitArrayIndex++] = 0;
                    break;
                case "10111":
                    newBitArray[newBitArrayIndex++] = 1;
                    newBitArray[newBitArrayIndex++] = 0;
                    newBitArray[newBitArrayIndex++] = 1;
                    newBitArray[newBitArrayIndex++] = 1;
                    break;
                case "11010":
                    newBitArray[newBitArrayIndex++] = 1;
                    newBitArray[newBitArrayIndex++] = 1;
                    newBitArray[newBitArrayIndex++] = 0;
                    newBitArray[newBitArrayIndex++] = 0;
                    break;
                case "11011":
                    newBitArray[newBitArrayIndex++] = 1;
                    newBitArray[newBitArrayIndex++] = 1;
                    newBitArray[newBitArrayIndex++] = 0;
                    newBitArray[newBitArrayIndex++] = 1;
                    break;
                case "11100":
                    newBitArray[newBitArrayIndex++] = 1;
                    newBitArray[newBitArrayIndex++] = 1;
                    newBitArray[newBitArrayIndex++] = 1;
                    newBitArray[newBitArrayIndex++] = 0;
                    break;
                case "11101":
                    for (int index = 0; index < 4; index++)
                        newBitArray[newBitArrayIndex++] = 1;
                    break;
                default:
                    System.out.println("defaulted.. this shouldnt happen tho....");
                    break;
            }
        }
        int counter = 0;
        String eightBitMessage = "";
        //   System.out.println("newlength" + newBitArray.length);
        for (byte b : newBitArray) {
//            System.out.print(b);
            eightBitMessage += b;
            counter++;

            if (counter % 8 == 0) {
                // System.out.println(eightBitMessage + "\t" + Integer.decode(eightBitMessage));
                newByteArray[newByteArrayIndex++] = (byte) Integer.parseInt(eightBitMessage, 2);
                eightBitMessage = "";
            }
        }

      //  System.out.println(newByteArrayIndex);
        bytesArray = newByteArray;
        counter = 0;
        System.out.print("Received 32 bytes: ");
        for (byte b : bytesArray) {
            System.out.print(Integer.toHexString(b & 0xFF).toUpperCase());
            counter++;
        }
        System.out.println();
        //System.out.println(tempString.length());
    }

    public static void main(String[] args) {

        PhysLayerClient plc = new PhysLayerClient(SERVER_NAME, PORT_NUMBER);

    }

}
