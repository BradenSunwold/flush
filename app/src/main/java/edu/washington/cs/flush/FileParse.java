package edu.washington.cs.flush;
import java.io.*;
import java.util.*;

public class FileParse {
   private File inputFile;
   private Scanner input;
   private String[] info;
   private String buildingName;
   private String buildingCode;
   private String latitude;
   private String longitude;
   private String floor;
   private String stalls;
   private String handicap;
   private String unisex;
   private String changingStation;
   private String rating;
   
   public FileParse (String file) throws FileNotFoundException {
      inputFile = new File(file);
      input = new Scanner(inputFile);
      info = new String[10];
      //findData();
   }
   
   public void findData(String line) {
      //boolean hasNotCreatedInfo = true;
      //while(input.hasNextLine() && hasNotCreatedInfo) {
         //String line = input.nextLine();
         Scanner lineScan = new Scanner(line);
         int index = -1;
         while (lineScan.hasNext()) {
            index++;
            String token = lineScan.next();
            boolean findingVerticalLine = true;
            while (lineScan.hasNext() && findingVerticalLine) {
               String token2 = lineScan.next();
               if (token2.startsWith("|")) {
                  findingVerticalLine = false;
               } else {
                  token += " " + token2;
               }
            }
            info[index] = token;
         }
         //hasNotCreatedInfo = false;
      //}
   }   
   
   public String getBuildingCode() {
      buildingCode = info[0];
      return buildingCode;
   }
   
   public String getBuildingName() {
      buildingName = info[1];
      return buildingName;
   }
   
   public String getLat() {
      latitude = info[2];
      return latitude;
   }
   
   public String getLong() {
      longitude = info[3];
      return longitude;
   }
   
   public String getFloor() {
      floor = info[4];
      return floor;
   }
   
   public String getStalls() {
      stalls = info[5];
      return stalls;
   }
   
   public String getHandicap() {
      handicap = info[6];
      return handicap;
   }
   
   public String getUnisex() {
      unisex = info[7];
      return unisex;
   }
   
   public String getChangingStation() {
      changingStation = info[8];
      return changingStation;
   }
   
   public String getRating() {
      rating = info[9];
      return rating;
   }
   
}