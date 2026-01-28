/**
 * MIT License
 *
 * Copyright (c) 2024 Arnulph Fuhrmann (TH Koeln)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;

/*
  * Draw version 0.44
 */
public class Draw {
    
    public static char CHAR_UNDEFINED = KeyEvent.CHAR_UNDEFINED;

    private static Color       backGroundColor = new Color(228, 228, 228);

    private static boolean     useDoubleBuffering = false;
    private static boolean     useAntiAliasing = true;
    //private static boolean   useXorMode = false;
    
    private static boolean     leftMouseButtonPressed = false;
    private static boolean     rightMouseButtonPressed = false;
    
    private static SwingCanvas drawingCanvas;
    
    private static Random random = new Random();

    private static class SwingCanvas extends JComponent implements KeyListener, MouseListener, MouseMotionListener {

        private int     width, height;
        private int     realPixelWidth, realPixelHeight;

        private boolean showFPS = false;
        private int     fps = 60;        
        private double  fpsLastTime = System.nanoTime();        
        private LinkedList<Double> fpsGraph = new LinkedList<Double>();
        private Color   fpsColor = new Color(0, 0, 0);

        private BufferedImage frontBuffer, backBuffer;
        private Graphics frontBufferGraphics, backBufferGraphics;

        private Font currentFont;
        
        private int mouseX, mouseY;

        private boolean hideMouseCursor = false;

        private boolean backBufferReady;

        private double fpsDiff;

        private boolean fullScreen = false;

        private double high_DPI_scale;
        
        public SwingCanvas(int width, int height, String title) {
            this.realPixelHeight = height;
            this.realPixelWidth = width;
            
            frontBuffer = createBuffer(realPixelWidth, realPixelHeight);
            backBuffer = createBuffer(realPixelWidth, realPixelHeight);
            
            
            // Create and set up the window.
            JFrame frame = new JFrame(title);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(this);
            
            // Exit on escape
            String key = "ESCAPE";
            KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(key);
            Action escapeAction = new AbstractAction() {

                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            };
            frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, key);
            frame.getRootPane().getActionMap().put(key, escapeAction);

            // Listeners
            frame.addKeyListener(this);
            addMouseListener(this);
            addMouseMotionListener(this);
            
            setDoubleBuffered(true);
            
            // HighDPI stuff
            {
                double scale = getGraphicsConfiguration().getDefaultTransform().getScaleX();
                //System.out.println("High DPI scale: " + scale);
                this.high_DPI_scale = scale;
                this.width = (int)(realPixelWidth / scale);
                this.height = (int)(realPixelHeight / scale);
            }
            
            
            if(fullScreen) {
                frame.setUndecorated(true);
                frame.setIgnoreRepaint(true);
            }
            
            // display the window.
            frame.pack();
            frame.setVisible(true);
            
            // center frame
            frame.setLocationRelativeTo(null);
            
            frontBufferGraphics = frontBuffer.getGraphics();
            backBufferGraphics = backBuffer.getGraphics();

            currentFont = frontBufferGraphics.getFont();

            initFrontBuffer();
        }

        private void initFrontBuffer() {
            frontBufferGraphics.setColor(backBufferGraphics.getColor());            
            frontBufferPixels = ((DataBufferInt) frontBuffer.getRaster().getDataBuffer()).getData();
        }

        private BufferedImage createBuffer(int width, int height) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        public Dimension getPreferredSize() {
            return new Dimension(width, height);
        }

        private void measureFPS() {
            double time = System.nanoTime();
            fpsDiff = time - fpsLastTime;
            fpsLastTime = time;
        }
        
        private void renderFPS() {
            
            if(showFPS) {
                Graphics g = getFronfBufferGraphics();
                double fps = (double)((int)(1.0 / (fpsDiff / 1.0E9 ) * 10.0)) / 10;
                Color color = g.getColor();
                g.setColor(fpsColor);
                fpsGraph.add(fps);
                
                text(10,20, ""+fps);
                
                if(fpsGraph.size() > realPixelWidth) {
                    fpsGraph.removeFirst();
                }
                int x = 0;
                for(double value : fpsGraph) {
                    g.drawLine(x, realPixelHeight-1, x, realPixelHeight-(int)value);
                    x++;
                }                
                g.setColor(color);
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            synchronized(keyMonitor)
            {
                keyMonitor.notifyAll();
                lastPressedKey = e.getKeyCode();
                pressedKeys.add(e.getKeyCode());
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            synchronized(keyMonitor)
            {
                if(lastPressedKey == e.getKeyCode())
                {
                    lastPressedKey = -1;                    
                }
                lastTypedKey = e.getKeyCode();
                lastTypedKeyChar = e.getKeyChar();
                pressedKeys.remove(e.getKeyCode());
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if(e.getButton() == MouseEvent.BUTTON1) {
                leftMouseButtonPressed = true;
            }
            if(e.getButton() == MouseEvent.BUTTON3) {
                rightMouseButtonPressed = true;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if(e.getButton() == MouseEvent.BUTTON1) {
                leftMouseButtonPressed = false;                
            }   
            if(e.getButton() == MouseEvent.BUTTON3) {
                rightMouseButtonPressed = false;                
            } 
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if(hideMouseCursor) {
                drawingCanvas.setCursor(drawingCanvas.getToolkit().createCustomCursor(new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB), 
                                                                                      new Point(0, 0), "null"));
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            setMouseCoords(e);
        }

        public Graphics getFronfBufferGraphics() {
            return frontBufferGraphics;
        }

    
        @Override
        public void mouseDragged(MouseEvent e) {
            setMouseCoords(e);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            setMouseCoords(e);
        }
        
        private void setMouseCoords(MouseEvent e)
        {
            mouseX = (int)(e.getX() * high_DPI_scale);
            mouseY = (int)(e.getY() * high_DPI_scale);
        }
        
        public void swapBuffers() {           
            if(useDoubleBuffering) {
                synchronized(drawingCanvas) {
                    backBufferReady = true;

                    BufferedImage tmp = backBuffer;
                    backBuffer = frontBuffer;
                    frontBuffer = tmp;

                    Graphics gTmp = backBufferGraphics;
                    backBufferGraphics = frontBufferGraphics;
                    frontBufferGraphics = gTmp;
                    
                    initFrontBuffer();
                }
            }
        }
        
        int[] frontBufferPixels;
        
        @Override
        protected void paintComponent(Graphics g) {
            if(fullScreen || drawingCanvas == null ) return;                
            synchronized(drawingCanvas)
            {   
                // code block for removing HighDPI scaling
                {   
                    final Graphics2D g2d = (Graphics2D) g;
                    final AffineTransform t = g2d.getTransform();
                    t.setToScale(1, 1);
                    g2d.setTransform(t);
                }
                if(useDoubleBuffering) {
                    if(backBufferReady) {
                        g.drawImage(backBuffer, 0, 0, null);
                        measureFPS();
                        backBufferReady = false;
                    }
                }
                else {
                    g.drawImage(frontBuffer, 0, 0, null);
                }
            }
        }

    }
    
    public static void setFpsColor(int r, int g, int b) {
        drawingCanvas.fpsColor = new Color(r, g, b);
    }
    
    /**
     * Clears the screen with the background color.
     * This color can be set using {@link #setBackgroundColor(int, int, int) setBackgroundColor}.
     */
    public static void clearScreen() {
        Graphics g = drawingCanvas.getFronfBufferGraphics();
        Color color = g.getColor();
        g.setColor(backGroundColor);
        //g.setColor(new Color(0,0,0,0)); // use for saving images with alpha values
        g.fillRect(0, 0, drawingCanvas.realPixelWidth, drawingCanvas.realPixelHeight);
        g.setColor(color);
    }

    private static double lastTime = System.nanoTime();
    
    /**
     * Suspends the program until a new frame can be displayed and swaps the front and 
     * back buffers to avoid artifacts.
     * A call to {@link #clearScreen() clearScreen} can be performed safely afterwards.  
     * Double buffering has to be activated (see  {@link #enableDoubleBuffering(boolean) enableDoubleBuffering}) 
     * before calling syncToFrameRate.
     */
    public static void syncToFrameRate() {
        
        if(drawingCanvas.showFPS) drawingCanvas.renderFPS();
            
        syncWithLongSleep(drawingCanvas.fps, 4);
        lastTime = System.nanoTime();
    
        drawingCanvas.swapBuffers();                    
        drawingCanvas.repaint();
    }
    
    /**
     * Checks how many nanoseconds are left until the next frame must be displayed.
     * Then the thread is set to sleep. Since the Java sleep mechanism has a large 
     * variance the thread is awakened burnMillis earlier. The remaining time is spent
     * by active waiting which causes a precise frame time at the cost of some lost CPU time.   
     * @param fps the targeted frames per second 
     * @param burnMillis the number of milliseconds which are spent in active waiting.
     */
    private static void syncWithLongSleep(int fps, int burnMillis) {

        boolean sleepOnce = true;
        while(true) {
            
            double currentTime = System.nanoTime();
            double deltaTime = currentTime - lastTime;
            double nanosPerFrame = 1d/fps * 1E9; 
            
            if( deltaTime > nanosPerFrame)
                return;
            
            if(sleepOnce) {
                
                sleepOnce = false;                
                int sleepTime = (int)((nanosPerFrame - deltaTime) / 1E6) - burnMillis;                
                if(sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }        
    }
    
    /**
     * Opens a window with a dimension of width x height.
     * The origin is at the upper left corner. The y-axis points downwards.
     * @param width width of the window in pixels 
     * @param height height of the window in pixels
     */
    public static void init(int width, int height) {
        init(width, height, "Mini Draw");
    }

    /**
     * Opens a window with a dimension of width x height.
     * The origin is at the upper left corner. The y-axis points downwards.
     * @param width width of the window in pixels 
     * @param height height of the window in pixels
     * @param title title of the window
     */
    public static void init(int width, int height, String title) {
        if (drawingCanvas == null) {
            drawingCanvas = new SwingCanvas(width, height, title);
            clearScreen();
            enableAntiAliasing(useAntiAliasing);            
        }
        else {
            synchronized (drawingCanvas) {
                drawingCanvas.width = width;
                drawingCanvas.height = height;
            }
        }
    }

    public static void setFps(int fps) {
        drawingCanvas.fps = fps;
    }
    
    /**
     * Sets the active color to the given values. 
     * Each parameter may be in the range [0,255].
     * @param red the red color channel
     * @param green the green color channel
     * @param blue the blue color channel
     */
    public static void setColor(int red, int green, int blue) {
        Graphics g = drawingCanvas.getFronfBufferGraphics();
        g.setColor(new Color(red, green, blue));                
    }

    /**
     * Sets the active color to the given values. 
     * Each parameter may be in the range [0,255].
     * @param red the red color channel
     * @param green the green color channel
     * @param blue the blue color channel
     * @param alpha the alpha channel (used for transparency)
     */
    public static void setColor(int red, int green, int blue, int alpha) {
        Graphics g = drawingCanvas.getFronfBufferGraphics();
        g.setColor(new Color(red, green, blue, alpha));
    }
    
    /**
     * Sets the background color to the given values. 
     * Each parameter may be in the range [0,255].
     * @param red the red color channel
     * @param green the green color channel
     * @param blue the blue color channel
     */
    public static void setBackgroundColor(int red, int green, int blue) {
        backGroundColor = new Color(red, green, blue);
    }
    
    /**
     * Draws a filled rectangular shape using the active color.
     * @param x x-coordinate of the upper left corner. 
     * @param y y-coordinate of the upper left corner.
     * @param width width of the rectangle.
     * @param height height of the rectangle.
     */
    public static void filledRect(int x, int y, int width, int height) {
        Graphics g = drawingCanvas.getFronfBufferGraphics();
        g.fillRect(x, y, width, height);
        if(!useDoubleBuffering) drawingCanvas.repaint();
    }
    
    public static void filledEllipse(int x, int y, int width, int height) {
        Graphics g = drawingCanvas.getFronfBufferGraphics();
        g.fillOval(x, y, width, height);
        if(!useDoubleBuffering) drawingCanvas.repaint();
    }
    
    public static void ellipse(int x, int y, int width, int height) {
        Graphics g = drawingCanvas.getFronfBufferGraphics();
        g.drawOval(x, y, width, height);
        if(!useDoubleBuffering) drawingCanvas.repaint();
    }
    
    public static void polyLine(int[] xCoords, int[] yCoords) {
        Graphics g = drawingCanvas.getFronfBufferGraphics();
        g.drawPolyline(xCoords, yCoords, xCoords.length);
        if(!useDoubleBuffering) drawingCanvas.repaint();
    }
    
    public static void polygon(int[] xCoords, int[] yCoords) {
        Graphics g = drawingCanvas.getFronfBufferGraphics();
        g.drawPolygon(xCoords, yCoords, xCoords.length);
        if(!useDoubleBuffering) drawingCanvas.repaint();
    }
    
    public static void filledPolygon(int[] xCoords, int[] yCoords) {
        Graphics g = drawingCanvas.getFronfBufferGraphics();
        g.fillPolygon(xCoords, yCoords, xCoords.length);
        if(!useDoubleBuffering) drawingCanvas.repaint();
    }

    /**
     * Draws a rectangular shape.
     * @param x x-coordinate of the upper left corner. 
     * @param y y-coordinate of the upper left corner.
     * @param width width of the rectangle.
     * @param height height of the rectangle.
     */
    public static void rect(int x, int y, int width, int height) {
        Graphics g = drawingCanvas.getFronfBufferGraphics();
        g.drawRect(x, y, width, height);
        if(!useDoubleBuffering) drawingCanvas.repaint();
    }
    
    /**
     * Draws a line from (x1, y1) to (x2, y2) using the active color.
     * @param x1 x-coordinate of the start of the line. 
     * @param y1 y-coordinate of the start of the line.
     * @param x2 x-coordinate of the end of the line.
     * @param y2 y-coordinate of the end of the line.
     */
    public static void line(int x1, int y1, int x2, int y2) {
        Graphics g = drawingCanvas.getFronfBufferGraphics();       
        g.drawLine(x1, y1, x2, y2);
        if(!useDoubleBuffering) drawingCanvas.repaint();
    }
    
    private static Stroke thickLine = new BasicStroke(3);
    
    public static void lineFat(int x1, int y1, int x2, int y2) {
        Graphics2D g = (Graphics2D) drawingCanvas.getFronfBufferGraphics();       
        g.setStroke(thickLine);
        g.drawLine(x1, y1, x2, y2);
        if(!useDoubleBuffering) drawingCanvas.repaint();
    }
    
    /**
     * Draws a horizontal line from (x, y) to (x + width - 1, y) using the active color.
     * @param x x-coordinate of the start of the line.
     * @param y y-coordinate of the start of the line.
     * @param width width of the line in pixels.
     */
    public static void horizontalLine(int x, int y, int width) {
    	
    	int color = drawingCanvas.getFronfBufferGraphics().getColor().getRGB();
    	for(int i=0; i<width; i++) {
    		drawingCanvas.frontBufferPixels[y*drawingCanvas.realPixelWidth + x + i] = color;	
    	}    	 
        if(!useDoubleBuffering) drawingCanvas.repaint();
    }

    /**
     * Draws the given text in the default font size.
     * The first character starts at x and the baseline of the text is defined by y. 
     * @param x position of the first character
     * @param y baseline of the text
     * @param text the text which is drawn
     */
    public static void text(int x, int y, String text) {
        text(x, y, text, 12);        
    }

    /**
     * Draws the given character in the default font size.
     * The character starts at x and the baseline of the text is defined by y. 
     * @param x position of the character
     * @param y baseline of the text
     * @param text the character which is drawn
     */
    public static void text(int x, int y, char text) {
        text(x, y, ""+text);       
    }
    
    /**
     * Draws the given text in the given font size.
     * The first character starts at x and the baseline of the text is defined by y. 
     * @param x position of the first character
     * @param y baseline of the text
     * @param text the text which is drawn
     * @param size the size of the font.
     */
    public static void text(int x, int y, String text, int size) {
        text(x, y, text, size, -1);        
    }
    
    /**
     * @param text the text for which the width is computed
     * @param size the size of the font.
     * @return Returns the width of the given text in pixeln.
     */
    public static int getTextWidth(String text, int size) {
        Graphics g = drawingCanvas.getFronfBufferGraphics();        
        Font currentFont = drawingCanvas.currentFont;
        g.setFont( new Font(currentFont.getFontName(), currentFont.getStyle(), size) );
        int width = g.getFontMetrics().stringWidth(text);
        return width;
    }
    
    /**
     * Draws the given text in the given font size centered in a box.
     * The box starts at x and is width pixel wide.
     * If width is smaller than 1 the text is drawn at x.
     */
    public static void text(int x, int y, String text, int size, int width) {        
        Graphics g = drawingCanvas.getFronfBufferGraphics();
        Font currentFont = drawingCanvas.currentFont;

        g.setFont( new Font(currentFont.getFontName(), currentFont.getStyle(), size) );
        
        if(width > 0) {
            int stringWidth = g.getFontMetrics().stringWidth(text);
            g.drawString(text, x + (width - stringWidth)/2, y);
        }
        else {
            g.drawString(text, x, y);
        }
        if(!useDoubleBuffering) drawingCanvas.repaint();
    }

    /**
     * Limitations: The Draw class only works with fonts in plain style in order to keep the interface simpel.  
     * @param fontfamilyName The name of the font family. Note, that this can be different from the file name.
     */    
    public static void setFont(String fontfamilyName) {
        drawingCanvas.currentFont = new Font(fontfamilyName, Font.PLAIN, 36);   
        Font font = (new Font(fontfamilyName, Font.PLAIN, 36)); 
        if(!font.getFamily().equals(fontfamilyName)) {
            throw new RuntimeException("Font family not found: " + fontfamilyName);
        }
    }

    /**
     * Loads a font from hard drive and registers it. Java supports TTF and OTF fonts.
     * 
     * @param fileName The file name of the font
     */
    public static void registerFont(String fileName) {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File(fileName))
            
            );
        } catch (IOException|FontFormatException e) {
                System.out.println(e);       
        }   
    }
    
    /**
     * Draws a pixel at (x, y) using the active color.
     * @param x x-coordinate of the pixel. 
     * @param y y-coordinate of the pixel.
     */
    public static void setPixel(int x, int y) {
        // drawingCanvas.frontBuffer.setRGB() is much slower due to checks for color space conversion      
        drawingCanvas.frontBufferPixels[y*drawingCanvas.realPixelWidth + x] = drawingCanvas.getFronfBufferGraphics().getColor().getRGB();
        if(!useDoubleBuffering) drawingCanvas.repaint();
    }
    
    /**
     * Draws a pixel at (x, y) using the given color. This method is more efficient 
     * than using the active color since the given color is directly set.
     * @param x x-coordinate of the pixel. 
     * @param y y-coordinate of the pixel.
     * @param col the color in the format ARGB
     */
    public static void setPixel(int x, int y, int col) {
        drawingCanvas.frontBufferPixels[y*drawingCanvas.realPixelWidth + x] = col;
        if(!useDoubleBuffering) drawingCanvas.repaint();
    }
    
    public static void setPixelUsingAlpha(int x, int y, int col) {
        int index = y*drawingCanvas.realPixelWidth + x;
        int currentCol = drawingCanvas.frontBufferPixels[index];
        drawingCanvas.frontBufferPixels[index] = applyAlphaBlending(currentCol, col);
    }

    public static void setPixelUsingAlpha(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scanSize)  {

        int[] frontBufferPixel = ((DataBufferInt) drawingCanvas.frontBuffer.getRaster().getDataBuffer()).getData();
        int width = drawingCanvas.frontBuffer.getWidth();
        int yOff  = offset;
        for (int y = startY; y < startY+h; y++, yOff+=scanSize) {
            
            int off = yOff;
            for (int x = startX; x < startX+w; x++) {
                
                int newColor = rgbArray[off++];
                int oldColor = frontBufferPixel[y*width + x];
                frontBufferPixel[y*width + x] = applyAlphaBlending(oldColor, newColor);
            }
        }
    }
    
    private static int applyAlphaBlending(int oldColor, int newColor) {
        
        int alpha = newColor>>>24;
    
        // early outs for two important and frequent cases:
        if(alpha == 255) {
            return newColor;
        }
        else if(alpha == 0) {
            return oldColor;
        }

        // ok, now we have to do the actual blending
        int r_old = oldColor >> 16 & 0xff;
        int g_old = oldColor >> 8  & 0xff;
        int b_old = oldColor       & 0xff;
        
        long r = newColor >> 16 & 0xff;
        long g = newColor >> 8  & 0xff;
        long b = newColor       & 0xff;

        int oneMinusAlpha = 255 - alpha;        
        r = r * alpha + r_old * oneMinusAlpha;
        g = g * alpha + g_old * oneMinusAlpha;
        b = b * alpha + b_old * oneMinusAlpha;
        
        r = r / 255;
        g = g / 255;
        b = b / 255;
        
        return (int)(0xff000000 | (r << 16) | (g << 8) | ( b ));
    }
    
    
    public static void setPixel(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scanSize)  {

        int[] frontBufferPixel = drawingCanvas.frontBufferPixels;
        int width = drawingCanvas.frontBuffer.getWidth();
        int yOff  = offset;
        for (int y = startY; y < startY+h; y++, yOff+=scanSize) {
            
            int off = yOff;
            for (int x = startX; x < startX+w; x++) {
                frontBufferPixel[y*width + x] = rgbArray[off++];
            }
        }
    }

    /**
     * Returns the color of the pixel at the given coordinates. The color is returned as 
     * an array containing r, g and b. Each returned value will be in the range [0,255].
     * @param x x-coordinate of the pixel. 
     * @param y y-coordinate of the pixel.
     * @return the color as an array containing r, g and b.
     */
    public static int[] getPixelColor(int x, int y) {
        return intToRgba(getPixel(x, y));
    }
    
    /**
     * Return the color of the pixel at the given coordinates. The color is returned as 
     * a single packed integer in the format ARGB.     
     * @param x x-coordinate of the pixel. 
     * @param y y-coordinate of the pixel.
     * @return the color as a single packed integer.
     */
    public static int getPixel(int x, int y) {
        return drawingCanvas.frontBuffer.getRGB(x, y);
    }

    /**
     * When enabled the shapes are drawn into the frontBuffer and a call to {@link #syncToFrameRate} 
     * will swap the buffers. Otherwise all shapes are directly rendered into the frontBuffer
     * and a call to {@link #syncToFrameRate} is not necessary. 
     * @param enabled true enables double buffering, false disables it
     */
    public static void enableDoubleBuffering(boolean enabled) {
        synchronized (drawingCanvas) {
            useDoubleBuffering = enabled;           
        }        
    }
    
    public static void enableAntiAliasing(boolean enabled) {
        synchronized (drawingCanvas) {
            useAntiAliasing = enabled;
            Object doAA = useAntiAliasing ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF; 
            ((Graphics2D)drawingCanvas.frontBufferGraphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING, doAA);
            ((Graphics2D)drawingCanvas.backBufferGraphics ).setRenderingHint(RenderingHints.KEY_ANTIALIASING, doAA);
        }
    }
    
    public static void enableShowFPS(boolean enabled) {
        synchronized (drawingCanvas) {
            drawingCanvas.showFPS = enabled;
        }        
    }
    
    public static void hideMouseCursor(boolean hidden) {
        drawingCanvas.hideMouseCursor = hidden;
    }
    
    private static int lastPressedKey = -1;
    private static int lastTypedKey = -1;
    private static char lastTypedKeyChar = KeyEvent.CHAR_UNDEFINED;
    
    private static Object keyMonitor = new Object();
    
    private static HashSet<Integer> pressedKeys = new HashSet<>();
    
    public static boolean isKeyDown(int keyCode) {
        return pressedKeys.contains(keyCode);
    }
    
    /**
     * Suspends the program until a key is pressed. 
     * @return An integer code for the key which was pressed. See {@link KeyEvent} class or <a href="https://docs.oracle.com/en/java/javase/15/docs/api/java.desktop/java/awt/event/KeyEvent.html">KeyEvent Documentation</a> for a list of key codes. 
     */
    public static int waitForKeyboard() {   
        drawingCanvas.repaint();
        
        synchronized(keyMonitor) {
            try {
                keyMonitor.wait();                
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }        
        return lastPressedKey;
    }
    
    /**
     * Returns the key code of the key which was pressed last. If this key is released later the value
     * -1 is returned. Note, that previously pressed and not released keys, will not be reported.
     * 
     * @return -1 if no key is pressed or an integer code for the key which was pressed most recently. 
     *         See {@link KeyEvent} class or <a href="https://docs.oracle.com/en/java/javase/15/docs/api/java.desktop/java/awt/event/KeyEvent.html">KeyEvent Documentation</a> for a list of key codes. 
     */
    public static int getLastPressedKey() {
        return lastPressedKey;
    }
    
    public static int getLastTypedKeyCode() {
        int tmp = lastTypedKey;
        lastTypedKey = -1;
        return tmp;
    }
    
    public static char getLastTypedKeyChar() {
        char tmp = lastTypedKeyChar;
        lastTypedKeyChar = KeyEvent.CHAR_UNDEFINED;
        return tmp;
    }
    
    /**
     * @return x-coordinate of the mouse cursor within the drawable area.
     */
    public static int getMouseX() {
        return drawingCanvas.mouseX;
    }

    /**
     * @return y-coordinate of the mouse cursor within the drawable area.
     */
    public static int getMouseY() {
        return drawingCanvas.mouseY;
    }
    
    /**
     * @return true, if the left mouse button is pressed (i.e. it is down)
     */
    public static boolean isLeftMouseButtonPressed() {
        return leftMouseButtonPressed;
    }
    
    /**
     * @return true, if the right mouse button is pressed (i.e. it is down)
     */
    public static boolean isRightMouseButtonPressed() {
        return rightMouseButtonPressed;
    }

    
    // ------------------------------------------------------------------------------------
    // - UI element button                                                                -
    // - ----------------------------------------------------------------------------------
    private static int buttonWidth = 100;
    private static int buttonHeight = 25;
    
    public static void setButtonWidth(int width) {
        buttonWidth = width;
    }
    
    public static void setButtonHeight(int height) {
        buttonWidth = height;
    }
    
    /**
     * Draws a button with default width and height.
     * 
     * @param name centered label of the button
     * @param x x-coordinate of upper left corner
     * @param y y-coordinate of upper left corner
     * @return true, if the button is pressed
     */
    public static boolean button(String name, int x, int y) {
        
        int width  = buttonWidth;
        int height = buttonHeight;
        int mouseX = drawingCanvas.mouseX;
        int mouseY = drawingCanvas.mouseY;
        
        boolean doHighlight = false;
        boolean pressed     = false;
        
        if(mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y+height) {        
            doHighlight = true;            
            if(leftMouseButtonPressed) {
                pressed = true;
            }
        }

        if(doHighlight && !pressed) {
            setColor(150, 150, 255);
        }
        else if(pressed) {
            setColor(120, 120, 255);
        }            
        else {
            setColor(190, 190, 190);
        }
        filledRect(x, y, width, height);
        
        setColor(30, 30, 30);
        text(x, y + 17, name, 12, buttonWidth);
        
        return pressed;
    }
    
    private static boolean sliderDragged = false;
    
    public static int slider(int x, int y, int value) {
        
        int width  = buttonWidth;
        int height = buttonHeight;
        int mouseX = drawingCanvas.mouseX;
        int mouseY = drawingCanvas.mouseY;
        
        boolean doHighlight = false;
        boolean pressed     = false;
        
        if(sliderDragged) {
            if(leftMouseButtonPressed) {
                pressed = true;
            }
            else {
                sliderDragged = false;
            }
        }
        
        if(mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y+height) {        
            doHighlight = true;            
            if(leftMouseButtonPressed) {
                pressed = true;
                sliderDragged = true;
            }
        }
        
        if(doHighlight && !pressed) {
            setColor(150, 150, 255);
        }
        else if(pressed) {
            setColor(120, 120, 255);            
        }            
        else {
            setColor(190, 190, 190);
        }
        filledRect(x, y, width, height);
        
        int sliderPos;
        
        if(pressed) {
            if(mouseX < x) mouseX = x;
            if(mouseX > x+width) mouseX = x+ width;
            sliderPos = mouseX - x;
            value = sliderPos/10;            
        } 
        else {
            sliderPos = 10*value;
        }
        setColor(60, 60, 160);
        filledRect(x + sliderPos - 3, y + 2, 7, buttonHeight-4);
        return value;
    }
    
    /**
     * Loads an image from hard drive. 
     * @param fileName The file name of the image
     * @return An 2D array containing the pixels of the image. Each pixel has the format ARGB.
     *         The array addresses first the rows, then the columns.
     */
    public static int[][] loadImage(String fileName) {
        
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(fileName));
        }
        catch (IOException e) {
            System.out.println(e);
            System.out.println("File: " + fileName);
            System.out.println("Working Directory: " + System.getProperty("user.dir"));
            
        }
        
        int image[][] = new int[img.getHeight()][img.getWidth()];
        
        for(int y=0; y < image.length; y++) {
            for(int x=0; x < image[0].length; x++) {
                image[y][x] = img.getRGB(x, y);
            }
        }
        return image;        
    }
    
    /**
     * Draws the given image into the front buffer. The alpha values are used to smoothly 
     * blend the image over the current content of the front buffer.
     * 
     * @param x x-coordinate of the upper left corner where the image will be drawn
     * @param y y-coordinate of the upper left corner where the image will be drawn
     * @param image the image date as an 2D array (first rows, then columns) containing the pixel data
     * @param mirror indicates if the image will be mirrored on the x-axis
     */
    public static void blendImage(int x, int y, int[][] image, boolean mirror) {

        int yDst = y;
        int xOffset = 0;
        if(x < 0) {
            xOffset = -x;
            x = 0;
        }        
        for(int yImg=0; yImg < image.length; yImg++, yDst++) {

            int xDst = x;
            for(int xImg=xOffset; xImg < image[0].length && xDst < drawingCanvas.realPixelWidth; xImg++) {
                
                int col;
                if(mirror)col = image[yImg][image[0].length-1-xImg];
                else      col = image[yImg][xImg];
                    
                setPixelUsingAlpha(xDst++, yDst, col);                
            }            
        }
    }
    
    /**
     * Saves the content of the front buffer to a PNG file.
     * 
     * @param fileName the name of the file
     */
    public static void saveScreenshot(String fileName) {
        
        BufferedImage img = drawingCanvas.frontBuffer;
        try {
            boolean success = ImageIO.write(img, "png", new File(fileName));
            if(!success) {
                System.out.println("Writing of image failed");
            }
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }
    
    /**
     * Loads an audio clip from the harddrive. Java supported several formats. Tested only WAV yet.
     * @param fileName Path and filenname of the file.
     * @return A clip which allows to play the sound later.
     */
    public static Clip loadSound(final String fileName) {
        try {
            Clip clip = AudioSystem.getClip();
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(fileName));
            clip.open(audioInputStream);
            {
                // dummy calls to avoid lag when played for the first time
                clip.setFramePosition(clip.getFrameLength() - 1);
                clip.start();
            }
            return clip;
        }
        catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            System.out.println("File: " + fileName);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Plays the given audio clip.
     * @param clip The clip to play.
     */    
    public static void playSound(Clip clip) {
        clip.setFramePosition(0);
        clip.start();
    }
    
    public static int[] hsvToRgb(float hue, float saturation, float v) {
        
        float r=0, g=0, b=0;        
        
        float huePrime = hue / 60.0f;
        int   integerPart = (int)huePrime;
        float fractionalPart = huePrime - integerPart;
        
        float tmp = - v * saturation;
        float a0 = tmp;
        float a1 = tmp * fractionalPart;
        float a2 = tmp * (1 - fractionalPart);
        
        switch(integerPart) {
            case 0:
                        g = a2; b = a0;
                break;
            case 1:
                r = a1;         b = a0;
                break;
            case 2:
                r = a0;         b = a2;
                break;
            case 3:
                r = a0; g = a1; 
                break;
            case 4:
                r = a2; g = a0; 
                break;
            case 5:
                        g = a0; b = a1;
                break;
            default:
                System.out.println("Hue out of range. Hue = " + hue);
                break;
        }
        
        int[] colors = new int[3];        
        colors[0] = (int)(255*(r + v) + 0.5);
        colors[1] = (int)(255*(g + v) + 0.5);
        colors[2] = (int)(255*(b + v) + 0.5);
        return colors;
    }

    /**
     * Unpacks the parameter color in the format ARGB into an array of integers 
     * containing r, g, b. Each returned value will be in the range [0,255].
     * 
     * @param color the color to be unpacked
     * @return An array containing R, G and B
     */
    public static int[] intToRgb(int color) {

        int[] rgb = new int[3];
        rgb[0] = color >> 16 & 0xff;
        rgb[1] = color >> 8  & 0xff;
        rgb[2] = color       & 0xff;
        return rgb;
    }    

    /**
     * Packs the parameters r, g, b into a single integer in the format ARGB.
     * Each parameter may be in the range [0,255].
     * 
     * @param r red color channel
     * @param g green color channel
     * @param b blue color channel
     * @return the packed integer.
     */
    public static int rgbToInt(int r, int g, int b) {
        return (int)(0xff000000 | (r << 16) | (g << 8) | ( b ));
    }

    /**
     * Unpacks the parameter color in the format ARGB into an array of integers 
     * containing r, g, b, a. Each returned value will be in the range [0,255].
     * 
     * @param color the color to be unpacked
     *   @return An array containing R, G and B
     */
    public static int[] intToRgba(int color) {

        int[] rgba = new int[4];        
        rgba[0] = color >>  16 & 0xff;
        rgba[1] = color >>   8 & 0xff;
        rgba[2] = color        & 0xff;
        rgba[3] = color >>> 24;        
        return rgba;
    }    
        
    /**
     * Packs the parameters r, g, b, a into a single integer in the format ARGB.
     * Each parameter may be in the range [0,255].
     * 
     * @param r red color channel
     * @param g green color channel
     * @param b blue color channel
     * @param a alpha channel (used for transparency)
     * @return the packed integer.
     */
    public static int rgbaToInt(int r, int g, int b, int a) {        
        return (int)( a << 24 | (r << 16) | (g << 8) | ( b ));
    }

    /*
     * Returns the next pseudorandom, uniformly distributed between {@code 0.0} and {@code 1.0}.
     */
    public static double getNextRandom() {
        return random.nextDouble();
    }
    
    /**
     * Set the seed value for the random number generator.
     */
    public static void setRandomSeed(long seed) {
        random = new Random(seed);
    }
}