public class Grass {    //Grass Klasse (S)
    double x, y, width, height;

    public Grass(double x, double y, double width, double height) {
        this.x = x; 
        this.y = y; 
        this.width = width; 
        this.height = height;   
    }


 // Prüft ob der Hase sich mit dem Grass überlappt (S)

   public boolean isBunnyHidden(Bunny bunny) {
        return bunny.getX() + bunny.getWidth() > x &&
               bunny.getX() < x + width &&
               bunny.getY() + bunny.getHeight() > y && 
               bunny.getY() < y + height;
    }
}
