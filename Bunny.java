public class Bunny {
        int x;  // Start-Position (Hase) M
        int y;

        int windowswidth;  //Fensterbreite M 

        int bunnyWidth = 60;       // Grösse Hase M
        int bunnyHeight = 100;

        int speed = 6;  //horizontale Geschwindigkeit M

        boolean hidden = false;  //Hase ist nicht versteckt M

        double fallspeed = 0;           
        double gravitation = 0.8;

        int groundY;   //Bodenhöhe M

        String imagePath = "images/Bunny.png"; //S
        int[][] bunnyImage; 
        
        public Bunny(int startX, int groundY, int windowswidth){      //M
            this.x = startX;
            this.groundY = groundY;
            this.windowswidth = windowswidth;
            this.y = groundY - bunnyHeight;
            
            try{
                int[][] fullImage = Draw.loadImage(imagePath);
                int size = 120;
                int cropX = (fullImage.length - size) / 2;
                int cropY = (fullImage[0].length - size) / 2;
                this.bunnyImage = new int[size][size];
                for (int y = 0; y < size; y++){
                    for (int x = 0; x < size; x++){
                        this.bunnyImage[y][x] = fullImage[cropY + y][cropX + x];
                    }
                }
            } catch (Exception e) {
                System.out.println("Error loading bunny image: " + e.getMessage());
            }
        }


        public void moveLeft(){
            x -= speed;
            if(x < 0){
                x = 0;
            }
        }
        public void moveRight(){
            x += speed;
            if(x > windowswidth - bunnyWidth){     //bunny kann sich aus dem Fenster nicht rausbewegen M
                x = windowswidth - bunnyWidth;
            }
        }
        public void Jump(){     //springen M
            if (isOnGround()){
                fallspeed = -15;
            }
        }
        public void applyPhysics(){   //Physik anwenden M
            fallspeed += gravitation;
            y += fallspeed;
        
            
            if ( y>= groundY - bunnyHeight){
                y = groundY - bunnyHeight;    //M
                fallspeed = 0;
            }
        }
            
            public void draw(){  //Hase zeichnen M + S
              if (bunnyImage != null) {
                Draw.blendImage(x, y, bunnyImage, false);
              }
              else {
              Draw.filledRect(x, y, bunnyWidth, bunnyHeight);
              }
            }
       


            private boolean isOnGround(){
                return y >= groundY - bunnyHeight; //M
            }
        
        
    public double getX() {return x;}       // Standort vom Hasen (S)
    public double getY() {return y;}
    public double getWidth() {return bunnyWidth;}     //Größe vom Hasen (S)
    public double getHeight() {return bunnyHeight;}

    public void setHidden(boolean hidden) {       //Hase versteckt oder nicht (S)
        this.hidden = hidden;
    }
    public boolean isHidden() {   //S
         return hidden;}
    }

            
            

        
       
       
       
        




        


