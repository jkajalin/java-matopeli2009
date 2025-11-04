import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.util.Random;

/*
 * Matopeli
 * Vuoden 2009 "Alkuperäinen" Matopeli oli Java Applet versio.
 * Kehitetty mahdollisesti jo muutama vuosi aikaisemmin.
 * Toteutus: Jussi Kajalin
 * Päivitetty: 1.11.2025, swing JPanel otettu takaisin käyttöön.
 */

//JApplet korvattu JPanelilla modernisointia varten
public class Matopeli extends JPanel 
				implements MouseListener, Runnable, KeyListener{ // Liitetään tarvittavat luokat avuksi
	
	/**
	 * Matopeli
	 */
	// muuttujat
	private static final long serialVersionUID = 1L; // Eclipse pakotti laittaa ohjelmaversion, standardivaatimus
	private Thread game; 
	private int x,y, wLength, speed, pisteet; 
	private boolean gameOver; 
	private String info, direction; 
	private int[][] worm, dot; 
	private Random rdn; 
	
	
	// Appletin Alustus
	public void init(){		
		
		// Pelin muuttujat
		gameOver=true; // Peli alussa pysähtynyt		
		worm = new int [300][2]; //Madon max pituus 299
		dot= new int[1][2];
		dot[0][0]=1;
		dot[0][1]=1;
		rdn=new Random(); // Random luku generaattori
		
		// rajapinnat
		addKeyListener(this); // Yhdistää näppäimistön rajapinnan applettiin
		addMouseListener(this); // Yhdistää hiiren rajapinnan aplettiin
		this.setFocusable(true); // Asettaa fokusoitavaksi
		this.requestFocus(); // Fokusoi applettiin		
		
		game = new Thread(this);// Luodaan säe
	}
	
	//  alustetaan peli
	public void start(){
		this.restartGame();		
		if(game!=null) {
			game.setPriority(1); // Asettaa säikeen prioriteetiksi pienimmän mahdollisen
		} 		
	}
	
	// pysäyttää pelin kun apletti ei ole aktiivinen
	public void stop(){
		this.setGameOver(true); //pysäytetään peli kun apletti ikkuna ei ole käytössä
	}
	
	public void destroy(){
		if(game!=null){
			try {
				game.interrupt();
			} catch (Exception e) {
				// ignore
			}
		}
		game=null;        
	}
	
	// Juoksuttaa peliä eteenpäin ja ohjataan sen toimintaa
	public void run(){
		while(true){
			if(!this.isGameOver()){ 
				
				try {
					Thread.sleep(getSpeed()); // Määrää pelin nopeuden
				} catch (InterruptedException e) {
				}
				this.moveWorm();				
				this.eatDot();
				this.repaint();
				this.updateWorm();
			} else {
				try {
					Thread.sleep(100); // Don't burn CPU when paused
				} catch (InterruptedException e) {
					System.out.println("Game thread interrupted while paused");
					
					return;
				}
			}		
		}
	}
	
	// Alustaa pelin muuttujat
	public void restartGame(){
		this.setSpeed(150);
		this.setPoints(0);
		this.setInfo("Paina Space aloittaaksei uusi peli !!");
		
		// Aloitus kordinaatit
		x=120; 
		y=120;
		//Madon alustus	
		worm[0][0]=x;
		worm[0][1]=y;
		worm[1][0]=x;
		worm[1][1]=y-4;
		worm[2][0]=x;
		worm[2][1]=y-8;
		this.setLength(3);
		this.setDirection("Down");
		
		// Arvotaan ensimmäinen syötävä piste
		this.newRandomDot();
	}

	// Piirretään sisältö
	public void paint(Graphics g){
		super.paint(g); // Jotta saadaan piirrettyä oikein komponentit
		g.drawRect(0, 0,248,320);		
		g.drawRect(0, 300,248,20);
		if(this.isGameOver()){
			g.drawString( this.getInfo() , 30, 80 );
		}
		if(!this.isGameOver()){
			g.drawString( this.getInfo()+" "+(150-this.getSpeed()), 10, 316 );
		}		
		String pointsit= "Pisteesi: "+this.getPoints();
		g.drawString( pointsit , 170, 316 );
		this.drawWorm(g);
		this.drawDot(g);
	}
	
	// Piirtää madon
	private void drawWorm(Graphics g){
		
		for(int i=0; i < this.getLength(); i++){	
			g.setColor(Color.BLACK);
			g.drawRect(worm[i][0], worm[i][1],4,4);
			if(i==0){
				g.setColor(Color.GRAY);
				g.fillRect(this.getWx(), this.getWy(), 4, 4);	
			}			
		}		
	}
	
	// Piirtää madon syötävän
	private void drawDot(Graphics g){
		g.setColor(Color.BLACK);
		int dx=this.getDotX();
		int dy=this.getDotY();
		g.drawRect( dx, dy,4,4 );
		g.fillRect(dx, dy, 4, 4);		
	}
	
	// Liikuttaa matoa ja tarkistaa törmäykset, (getWx() ja getWy) !=null
	private void moveWorm(){
		
		// Kuljetaan asetettuun suuntaan, ja tarkisetaan että osuuko seinään		
		if( this.getDirection().equals("Up")){ 
			if(this.getWy() > 0 ){				
				this.setY(this.getWy()-4); // liikutaan ylös
	        }
			else{
				this.setGameOver(true);
	        	this.setInfo("GameOver!");
	        }			
		}
		if (getDirection().equals("Down")){
			if(this.getWy() < 296 ){
				this.setY(this.getWy()+4); // liikutaan alas
	        }
			else{
				this.setGameOver(true);
	        	this.setInfo("GameOver!");	
	        }						
		}
            
        if (getDirection().equals("Left")){
        	if(this.getWx() > 0 ){
            	this.setX(this.getWx()-4); // liikutaan vasemmale
            }
        	else{
        		this.setGameOver(true);
            	this.setInfo("GameOver!");
        	}        	
        }
            
        if (this.getDirection().equals("Right")){
        	if(this.getWx() < 244){
            	this.setX(this.getWx()+4);  //liikutaan oikealle
            }
        	else{
        		this.setGameOver(true);
            	this.setInfo("GameOver!");
        	}         	        	
        }        
               
	}
	
	// Päivittää madon sijainti kordinaatteja
	private void updateWorm(){		
		for(int i=this.getLength(); i > 0 ; i--){		
			worm[i][0]= worm[i-1][0];			
			worm[i][1]= worm[i-1][1];
			//System.out.println("w"+i);
		}
		// Tarkistetaan osuuko madon pää vartaloon
		for(int k=2; k < this.getLength(); k++ ){
			if(this.getWx()==worm[k][0] && this.getWy()==worm[k][1] && this.getSpeed()>3){
				this.setGameOver(true);
			}
		}
	}
	
	// Asetetaan Info texti, nfo!=null
	private void setInfo(String nfo){
		info=nfo;
	}
	// Palauttaa asetetun infotextin
	private String getInfo(){
		return info;
	}
	// Asettaa madon nopeuden, Spe!=null
	private void setSpeed(int Spe){
		speed= Spe;
	}
	// Palauttaa madon nopeuden
	private int getSpeed(){
		return speed;
	}
	// Kasvatetaan nopeutta;
	private void moreSpeed(){
		speed--;
	}
	// Asettaa pistemäärän, points!=null
	private void setPoints(int points){
		pisteet=points;
	}
	// Palauttaa Pistemäärän
	private int getPoints(){
		return pisteet;
	}
	
	// Asetetaan madon alkupään sijainti, cx,cy != null
	private void setX(int cx){
		worm[0][0]=cx;
	}	
	private void setY(int cy){
		worm[0][1]=cy;
	}
	// Madon alupäänsijainti
	private int getWx(){
		return worm[0][0];
	}
	private int getWy(){
		return worm[0][1];
	}
	// Asettaa madon pituuden, length !=null
	private void setLength(int length){
		wLength=length;
	}
	// Kasvatetaan piirrettävien pisteiden määrää
	private void riseLength(){
		wLength++;
	}
	// Palauttaa madon pituuden
	private int getLength(){
		return wLength;
	}
	// Syötävan pisteen kordinaatit
	private int getDotX(){
		return dot[0][0];
	}
	private int getDotY(){
		return dot[0][1];
	}
	// Palautetaan pelin tilan
	private boolean isGameOver(){
		return gameOver;
	}
	// Asettaa pelin tilan
	private void setGameOver(boolean stat){
		gameOver=stat;
	}
	
	// Tarkistetaan osuuko mato syötävään pisteeseen
	private void eatDot(){
		if( this.getWx()==this.getDotX() && this.getWy()==this.getDotY() ){
			if( getLength() < 299 ){
				this.riseLength();
			}
			if(this.getSpeed() >0){
				this.moreSpeed();
			}			
			this.setPoints(getPoints()+4); // Kasvatetaan pisteitä			
			this.newRandomDot();	//Arvotaan uusi sijainti pisteelle
		}
	}
	
	// Arpoo pisteelle uuden pseudo satunnaisen sijainnin syötävälle
	private void newRandomDot(){
		dot[0][0]=rdn.nextInt(60)*4;
		dot[0][1]=rdn.nextInt(74)*4;
	}
	
	// Hiiren painikkeiden monitorointi	// Paussaa pelin
	public void mouseClicked(MouseEvent event) {

		if(!this.isGameOver()){
			this.setGameOver(true);
			this.setInfo("Napsauta jatkaaksesi !");			
		}
		else{			
			this.setGameOver(false);
			this.setInfo("Nopeutesi: ");
			
		}
	}	
	public void mouseEntered(MouseEvent event) {		
	}
	public void mouseExited(MouseEvent event) {
	}
	public void mousePressed(MouseEvent event) {
	}
	public void mouseReleased(MouseEvent event) {
	}
	
	/*
	 * Asettaa liikkumis suunnan
	 * AE: (d=="Up" or d=="Down" or d=="Right" or d=="Left") && d==null
	 */
	private void setDirection(String d){
		direction=d;
	}
	// Palauttaa liikkumis suunnan
	private String getDirection(){
		return direction;
	}
	
	
	// Nappuloiden monitorointi
	public void keyPressed (KeyEvent e){
		@SuppressWarnings("static-access")
		String button=e.getKeyText(e.getKeyCode()); //Haetaan painetun näppäimen merkkijonoesitys
		if( button.equals("Up") && getDirection()!="Down"){
			this.setDirection(button);
		}
		if (button.equals("Down")&& getDirection()!="Up"){
			this.setDirection(button);
		}
            
        if (button.equals("Left") && getDirection()!="Right"){
        	this.setDirection(button);
        }
            
        if (button.equals("Right")&& getDirection()!="Left"){
        	this.setDirection(button);
        }
        if (button.equals("Space")){
        	
        	if(this.isGameOver()){
        		this.setGameOver(false);
        		// Startataan threadi jollei vielä juokse
        		if(!game.isAlive()){
        			game.start();
        			start();
        		}
        		
        	}        	
        	this.restartGame();
        	this.setInfo("Nopeus: ");
        }
            
    }
     
    public void keyTyped (KeyEvent e) {}
    public void keyReleased (KeyEvent e) {}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Matopeli");			
		Matopeli matopeli = new Matopeli();
		matopeli.init();
		frame.add(matopeli);		
		frame.setSize(256, 360);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		matopeli.start();		
	}
}
