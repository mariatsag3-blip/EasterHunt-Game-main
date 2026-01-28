import java.awt.event.KeyEvent;



public class EasterHunt {

      static final int width = 1000;
      static final int height = 800;
      static Bunny bunny;
      static Eagle eagle;
      static Grass grass;
      static boolean gameover = false;

     // Bilder laden (S) 
       static int[][] sun = null;
       static  int[][] cloud1 = null;
       static  int[][] cloud2 = null;
       static  int[][] grass1 = null;
       static int[][] grass2 = null;
       static int[][] tree = null;
       static int[][] tree2 = null;
       static int[][] pinkEgg = null;
       static int[][] purpleEgg = null;
       static int[][] yellowEgg = null;
       static int tree2Width;
       static int tree2Height;

       static int[] eggX = {250, 450, 700};
       static int[] eggY = {520, 500, 400}; 
       static boolean[] eggCollected = {false, false, false};
       static int score = 0; // Gelbes Ei über den Baum (S)


    public static void main(String[] args){

        Draw.init(1000, 800);         
        Draw.enableDoubleBuffering(true);   

        int groundY = height - 160; //Höhe des Bodens M

        eagle = new Eagle(-60, 40, 60, 40); // Eagle Objekt erzeugt M

        bunny = new Bunny(0, groundY, width); // Bunny Objekt erzeugt M

        grass = new Grass(200, groundY, 200, 40); //Grass Objekt erzeugt (S)

            // Sonne Bild (S)
            try {
          
            int[][] sunFull = Draw.loadImage("images/Sun.png");
            if (sunFull != null) {
                System.out.println("Sun.png loaded: height=" + sunFull.length + ", width=" + sunFull[0].length);
                int sunSize = 100;
                int startX = (sunFull[0].length - sunSize) / 2;
                int startY = (sunFull.length - sunSize) / 2;
                sun = new int[sunSize][sunSize];
                for (int y = 0; y < sunSize; y++) {
                    for (int x = 0; x < sunSize; x++) {
                        sun[y][x] = sunFull[startY + y][startX + x];
                    }
                }
                System.out.println("Sun loaded and cropped to 100x100");
            } else {
                System.out.println("Sun.png failed to load or is empty");
            }
            
            // Wolken Bilder (S)
            int[][] cloudFull = Draw.loadImage("images/CloudX.png");
            if (cloudFull != null) {
                System.out.println("CloudX.png loaded: height=" + cloudFull.length + ", width=" + cloudFull[0].length);
                if (cloudFull.length >= 250 && cloudFull[0].length >= 250) {
                    int cloudSize = 250;
                    int startX = (cloudFull[0].length - cloudSize) / 2;
                    int startY = (cloudFull.length - cloudSize) / 2;
                    cloud1 = new int[cloudSize][cloudSize];
                    cloud2 = new int[cloudSize][cloudSize];
                    for (int y = 0; y < cloudSize; y++) {
                        for (int x = 0; x < cloudSize; x++) {
                            cloud1[y][x] = cloudFull[startY + y][startX + x];
                            cloud2[y][x] = cloudFull[startY + y][startX + x];
                        }
                    }
                    System.out.println("Clouds loaded and cropped to 250x250");
                } else {
                    System.out.println("CloudX.png too small for 250x250 crop");
                }
            } else {
                System.out.println("CloudX.png failed to load or is empty");
            }
            
            // Baum größe einstellen (S)
            int[][] treeFull = Draw.loadImage("images/treeT.png");
            if (treeFull != null) {
                System.out.println("treeT.png loaded: height=" + treeFull.length + ", width=" + treeFull[0].length);
                
                int treeWidth = Math.min(200, treeFull[0].length);
                int treeHeight = Math.min(250, treeFull.length);
                int startX = (treeFull[0].length - treeWidth) / 2;
                int startY = (treeFull.length - treeHeight) / 2;
                tree = new int[treeHeight][treeWidth];
                for (int y = 0; y < treeHeight; y++) {
                    for (int x = 0; x < treeWidth; x++) {
                        tree[y][x] = treeFull[startY + y][startX + x];
                    }
                }
                System.out.println("Tree loaded and cropped to " + treeWidth + "x" + treeHeight);

                
                int tree2Width = Math.min(400, treeFull[0].length);
                int tree2Height = Math.min(500, treeFull.length);
                int startX2 = (treeFull[0].length - tree2Width) / 2;
                int startY2 = (treeFull.length - tree2Height) / 2;
                tree2 = new int[tree2Height][tree2Width];
                for (int y = 0; y < tree2Height; y++) {
                    for (int x = 0; x < tree2Width; x++) {
                        tree2[y][x] = treeFull[startY2 + y][startX2 + x];
                    }
                }
                System.out.println("Tree2 loaded and cropped to " + tree2Width + "x" + tree2Height);
            }
            
            //Grass größe einstellen (S)
            int[][] grassFull = Draw.loadImage("images/GrassA.png");
            if (grassFull != null) {
                System.out.println("GrassA.png loaded: height=" + grassFull.length + ", width=" + grassFull[0].length);
                int grassWidth = 200;
                int grassHeight = 150;
                int startX = (grassFull[0].length - grassWidth) / 2;
                int startY = (grassFull.length - grassHeight) / 2;
                grass1 = new int[grassHeight][grassWidth];
                grass2 = new int[grassHeight][grassWidth];
                for (int y = 0; y < grassHeight; y++) {
                    for (int x = 0; x < grassWidth; x++) {
                        grass1[y][x] = grassFull[startY + y][startX + x];
                        grass2[y][x] = grassFull[startY + y][startX + x];
                    }
                }
                System.out.println("Grass loaded and cropped to 200x150");
            } else {
                System.out.println("GrassA.png failed to load or is empty");
            }
            
            // Eier größe einstellen (S)
            int[][] pinkFull = Draw.loadImage("images/PinkEgg.1.png");
            if (pinkFull != null) {
                System.out.println("PinkEgg.1.png loaded: height=" + pinkFull.length + ", width=" + pinkFull[0].length);
                int eggSize = 80;
                int startX = (pinkFull[0].length - eggSize) / 2;
                int startY = (pinkFull.length - eggSize) / 2;
                pinkEgg = new int[eggSize][eggSize];
                for (int y = 0; y < eggSize; y++) {
                    for (int x = 0; x < eggSize; x++) {
                        pinkEgg[y][x] = pinkFull[startY + y][startX + x];
                    }
                }
            } else {
                System.out.println("PinkEgg.1.png failed to load or is empty");
            }

            int[][] purpleFull = Draw.loadImage("images/PurpleEgg.png");
            if (purpleFull != null) {
                System.out.println("PurpleEgg.png loaded: height=" + purpleFull.length + ", width=" + purpleFull[0].length);
                int eggSize = 80;
                int startX = (purpleFull[0].length - eggSize) / 2;
                int startY = (purpleFull.length - eggSize) / 2;
                purpleEgg = new int[eggSize][eggSize];
                for (int y = 0; y < eggSize; y++) {
                    for (int x = 0; x < eggSize; x++) {
                        purpleEgg[y][x] = purpleFull[startY + y][startX + x];
                    }
                }
            } else {
                System.out.println("PurpleEgg.png failed to load or is empty");
            }

            int[][] yellowFull = Draw.loadImage("images/YellowEgg.2.png");
            if (yellowFull != null) {
                System.out.println("YellowEgg.2.png loaded: height=" + yellowFull.length + ", width=" + yellowFull[0].length);
                int eggSize = 80;
                int startX = (yellowFull[0].length - eggSize) / 2;
                int startY = (yellowFull.length - eggSize) / 2;
                yellowEgg = new int[eggSize][eggSize];
                for (int y = 0; y < eggSize; y++) {
                    for (int x = 0; x < eggSize; x++) {
                        yellowEgg[y][x] = yellowFull[startY + y][startX + x];
                    }
                }
            } else {
                System.out.println("YellowEgg.2.png failed to load or is empty");
            }
            
            System.out.println("All images loaded and cropped!");
            } catch (Exception e) {
            System.out.println("Could not load images: " + e.getMessage());
            e.printStackTrace();
            }
    

        
        System.out.println("Game starting... Bunny at position: " + bunny.x + ", " + bunny.y);

                    
        int tree2Width = (tree2 != null) ? tree2[0].length : 400;  
        int tree2Height = (tree2 != null) ? tree2.length : 500;
        eggX[2] = Math.max(0, width - 100 - tree2Width + tree2Width/2 - 35); 
        eggY[2] = 200 + tree2Height/2 - 75; 
        int eggWidth = 80;                    // Eier (S)
        int eggHeight = 80;


        while(true){

             if(!gameover){     //Solange kein Game Over ist M
            Draw.clearScreen(); //Bildschirm löschen M

            bunny.applyPhysics();
            eagle.eaglemovement();
            gameOver(bunny.isHidden()); //Game Over Funktion M

            Draw.setColor(255, 0, 0);
            Draw.filledRect(10, 10, 100, 100);
            
            // Background - Sky blue
            Draw.setColor(135, 206, 235);
            Draw.filledRect(0, 0, width, height);
            
            // Sonne oben rechts (S)
            if (sun != null) {
                Draw.blendImage(800, 20, sun, false);
            } else {
                Draw.setColor(255, 255, 0);
                Draw.filledEllipse(800, 20, 100, 100);
            }


             boolean hidingBehindGrass = false;  // Prüfen ob Bunny hinter Gras versteckt ist (S)
            int[] grassPositions = {0, 500};
            int grassWidth = 200;                 
            for (int grassX : grassPositions) {
                 boolean xOverlap =
                 bunny.x < grassX + grassWidth &&
                 bunny.x + bunny.bunnyWidth > grassX;

                 boolean yOverlap =
                 bunny.y + bunny.bunnyHeight > groundY - 60; 

                 if (xOverlap && yOverlap) {
                 hidingBehindGrass = true;
                  break;
                 }
                }
                 bunny.setHidden(hidingBehindGrass);
            
            
            // Eagle greift an nur wenn Bunny nicht versteckt ist und Eagle direkt darüber ist (S)
            int eagleCenterX = eagle.x + eagle.eaglewidth / 2;
            int bunnyCenterX = bunny.x + bunny.bunnyWidth / 2;
            int horizontalDistance = Math.abs(eagleCenterX - bunnyCenterX);
        
            
            for (int i = 0; i < eggX.length; i++) {   //Eier werden bei Kontakt mit dem Bunny gesammelt (S)
                if (!eggCollected[i]) {
                   
                    int margin = 40; // Toleranzbereich für Kollisionserkennung M
                    boolean xOverlap = bunny.x + margin < eggX[i] + eggWidth && bunny.x + bunny.bunnyWidth > eggX[i];
                    boolean yOverlap = bunny.y + margin< eggY[i] + eggHeight && bunny.y + bunny.bunnyHeight > eggY[i];
                    
                    if (xOverlap && yOverlap) {
                        eggCollected[i] = true;
                        score++;
                    }
                }
            }
            
            // Clouds
            if (cloud1 != null && cloud2 != null) {
                // Wolke positionieren (S)
                Draw.blendImage(200, 200, cloud1, false);
                Draw.blendImage(500, Math.max(0, -30), cloud2, false);
            } else {
                Draw.setColor(255, 255, 255);
                Draw.filledEllipse(110, 20, 150, 150);
                Draw.filledEllipse(550, 90, 150, 150);
            }
            
            // Baum im Hintergrund (S)
            if (tree != null) {
             
            } else {
            
                Draw.setColor(139, 69, 19);  // Brown trunk
                Draw.filledRect(830, 450, 40, 100);
                Draw.setColor(34, 139, 34); // Green leaves
                Draw.filledEllipse(750, 300, 150, 180);
            }
            
            // Boden (S)
            Draw.setColor(50, 150, 50);
            Draw.filledRect(0, 500, width, 300);

            // Baum einfügen (S)
            if (tree != null) {
                int treeY = 350;
                Draw.blendImage(50, treeY, tree, false);
            }
            
            if (Draw.isKeyDown(KeyEvent.VK_LEFT)){  //Taste Bewegung nach Links M
                bunny.moveLeft();
             }

            if (Draw.isKeyDown(KeyEvent.VK_RIGHT)){  //Taste Bewegung nach Rechts M
                bunny.moveRight();
            }
            if (Draw.isKeyDown(KeyEvent.VK_SPACE)){  //Taste Sprung M
                bunny.Jump();
            }
    
    
            // Eier gesammelt - "LEVEL COMPLETE" (S)
            if (score >= 3) {
                
                Draw.setColor(0, 0, 0, 150); 
                Draw.filledRect(0, 0, width, height);

                
                Draw.setColor(255, 215, 0); // Gold color
                Draw.text(width/2 - 300, height/2 - 50, "LEVEL COMPLETE!", 70, 0);
                Draw.setColor(0, 255, 0); // Green
                Draw.text(width/2 - 210, height/2 + 50, "All Eggs Collected!", 40, 0);
                Draw.setColor(255, 255, 255); // White
                Draw.text(width/2 - 100, height/2 + 150, "Score: " + score, 35, 0);
                
                Draw.syncToFrameRate();
                try {
                    Thread.sleep(3000); // "LEVEL COMPLETE" läuft 3 sek (S)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.exit(0); 
            }
        
            
            if (!eggCollected[0]) {
                if (pinkEgg != null) {
                    Draw.blendImage(eggX[0], eggY[0], pinkEgg, false);
                } else {
                    Draw.setColor(255, 192, 203);
                    Draw.filledEllipse(eggX[0], eggY[0], 80, 80);
                }
            }
            
            if (!eggCollected[1]) {
                if (purpleEgg != null) {
                    Draw.blendImage(eggX[1], eggY[1], purpleEgg, false);
                } else {
                    Draw.setColor(128, 0, 128);
                    Draw.filledEllipse(eggX[1], eggY[1], 80, 80);
                }
            }
   
            Draw.setColor(0, 0, 0);                       // Punktestand (S)
            Draw.text(10, 30, "Score: " + score, 30, 0);
            
            eagle.draw();
            bunny.draw();
            
            
            if (tree2 != null) {
                int tree2X = Math.max(0, width - 100 - tree2Width);
                Draw.blendImage(tree2X, 200, tree2, false);      // Baum rechts (S)
                
                if (!eggCollected[2]) {           // Gelbes Ei vor dem Baum einfügen (S)
                    if (yellowEgg != null) {
                        Draw.blendImage(eggX[2], eggY[2], yellowEgg, false);
                    } else {
                        Draw.setColor(255, 255, 0);
                        Draw.filledEllipse(eggX[2], eggY[2], 80, 80);
                    }
                }
            } else {
                        // Baum (S)
                Draw.setColor(139, 69, 19);                 
                Draw.filledRect(220, 500, 80, 200);
                Draw.setColor(34, 139, 34); 
                Draw.filledEllipse(100, 200, 300, 360);
            }
            
            // Grass im Vordergrund stellen damit Bunny sich verstecken kann (S)
            int grassY = 550;                // Position vom Grass (S)
            if (grass1 != null && grass2 != null) {
                Draw.blendImage(0, grassY, grass1, false);
                Draw.blendImage(500, grassY, grass2, false);
            } else {
                
                Draw.setColor(34, 139, 34);         // Farbe
                Draw.filledRect(0, grassY, 200, 150);
                Draw.filledRect(500, grassY, 200, 150);
            }
        
             }
             else{                    //Wenn Game Over ist M
            Draw.clearScreen();
            Draw.setColor(0, 0, 0); //Schwarzer Hintergrund M
            Draw.filledRect(0, 0, width, height);
            Draw.setColor(255, 0, 0); //Roter Text M
            Draw.text(width / 2 , height / 2 + 30, "GAME OVER"); //Game Over Text M
            Draw.text(width / 2, height / 2 - 30, "Press R to restart");
            if ( Draw.isKeyDown(KeyEvent.VK_R)){  //R Taste zum Neustarten M
                gameover = false;
                bunny = new Bunny(0, groundY, width); // Neues Bunny Objekt erzeugt M
                eagle = new Eagle(-60, 40, 60, 40); // Neues Eagle Objekt erzeugt M
                score = 0;
                for (int i = 0; i < eggCollected.length; i++) {
                    eggCollected[i] = false;
            }
        }

    }

            Draw.syncToFrameRate();
    }

}
   
    
            static void gameOver(boolean hidingBehindGrass){ //Game Over Funktion M
                if(Math.abs(eagle.x- bunny.x) < 20 && !hidingBehindGrass){   //Wenn der Abstand zwischen Adler und Hase kleiner als 30 ist M
                    System.out.println("Game Over");
                    gameover = true;
                }
            } 
}      



    
    

