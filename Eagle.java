import javax.sound.sampled.*; 
import java.io.File;

public class Eagle {

    int x= -50; //M
    int y = 40;
    int eaglewidth = 60;
    int eagleHeight = 40;
    int windowswidth = 1000;   //Fensterbreite M
    String imagePath = "images/EagleB.png"; //S
    int[][] eagleImage;
    
    boolean active = false;     //Adler startet nicht aktiv M

    int speed = 3;  //horizontale Geschwindigkeit M

    public Eagle(int startX, int startY, int width, int height) {  //M
        this.x = startX;
        this.y = startY;
        this.eaglewidth = width;
        this.eagleHeight = height;
    }

    public void draw() {
        if (eagleImage != null && x > -eaglewidth && x < windowswidth) { //S
            Draw.blendImage(x, y, eagleImage, false);
        } else if (x > -eaglewidth && x < windowswidth) {
            // Fallback: draw dark gray rectangle if image not loaded
            Draw.setColor(64, 64, 64); // Dark gray for eagle
            Draw.filledRect(x, y, eaglewidth, eagleHeight);
        }
    
    
    try {
            int[][] fullImage = Draw.loadImage(imagePath);      //S
            System.out.println("Eagle image loaded successfully!");
            System.out.println("Eagle dimensions: " + fullImage.length + "x" + fullImage[0].length);
            
            
            int sourceWidth = fullImage[0].length;
            int sourceHeight = fullImage.length;
            int startX_crop = (sourceWidth - eaglewidth) / 2;
            int startY_crop = (sourceHeight - eagleHeight) / 2;
            
            this.eagleImage = new int[eagleHeight][eaglewidth];
            for (int y = 0; y < eagleHeight; y++) {
                for (int x = 0; x < eaglewidth; x++) {
                    this.eagleImage[y][x] = fullImage[startY_crop + y][startX_crop + x];
                }
            }
            System.out.println("Eagle image cropped to: " + eagleImage.length + "x" + eagleImage[0].length);
        } catch (Exception e) {
            System.out.println("Could not load eagle image: " + e.getMessage());
            e.printStackTrace();
            this.eagleImage = null;
        }
    }

    public void eaglemovement(){   //Adler Bewegung und Wahrscheinlichkeit pro Frame M
        if(!active){

        if(Math.random() < 0.001){  //Wenn adler nicht aktiv ist, Chance das er wieder erscheint (~alle 16 sek.)M
            active = true;
            x = -eaglewidth;       //Adler startet links ausserhalb des Fensters M
            playSound();           //Adler Sound abspielen M
        }
       
        }
        else{
            x += speed;            //Wenn Adler aktiv ist, bewegt er sich nach rechts bis ans Ende vom Fenster M                           
            if(x > 1000){
            active= false;
            }
            
          }
            
        }
        public void playSound(){ //Adler Sound abspielen M
            try {
                File soundFile = new File("eaglesound.wav"); // Pfad zur Sounddatei M
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
