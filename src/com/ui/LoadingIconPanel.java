package com.ui;


import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;


public class LoadingIconPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	private BufferedImage loadingIcon;
	private Timer paintTimer;
	private int angle ;
	
	public LoadingIconPanel(int containerSizeX, int containerSizeY){

        setLayout(null);
        
		try {
			/*String filePath = new File(".").getCanonicalPath()+"/bin/loader.png";
			loadingIcon = ImageIO.read(new File(filePath));*/
			String filePath = "images/loader.png";
			InputStream fileInputStream = getClass().getResourceAsStream(filePath);
			loadingIcon = ImageIO.read(fileInputStream);
        } catch (IOException e) {
			e.printStackTrace();
		}			
		
		setBounds(containerSizeX/2 - loadingIcon.getWidth()/2, 
				containerSizeY/2 - loadingIcon.getHeight()/2 ,
				loadingIcon.getWidth(), loadingIcon.getHeight());
		
		angle = 0;
		paintTimer = new Timer(15, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

    			angle +=5;
    			if (angle > 360)
    				angle = 0;
                repaint();
            }
        });
        paintTimer.setRepeats(true);
        setVisible(false);
	}

    public void setRuning(boolean running) {
        if (running) {
        	setVisible(true);
            paintTimer.start();
        } else {
            paintTimer.stop();
            setVisible(false);
        }
    }

    public boolean isRunning() {
        return paintTimer.isRunning();
    }
	
	private double getAngle() {
        return Math.toRadians(angle);

    }

	@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        
        int x = (getWidth() - loadingIcon.getWidth()) / 2;
        int y = (getHeight() - loadingIcon.getHeight()) / 2;
        AffineTransform at = new AffineTransform();
        at.setToRotation(getAngle(), x + (loadingIcon.getWidth() / 2), y + (loadingIcon.getHeight() / 2));
        at.translate(x, y);
        
        g2d.setTransform(at);
        g2d.drawImage(loadingIcon, 0, 0, null);
        g2d.dispose();
        
    }



}

