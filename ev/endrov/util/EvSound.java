package endrov.util;

import java.io.*;
import java.net.URL;
import javax.sound.sampled.*;
import javax.swing.*;

/**
 * Quick routines for playing sound
 * @author Johan Henriksson
 *
 */
public class EvSound
	{
	public static Clip loadSound(URL url)
		{
		try 
			{
			// Open an audio input stream.
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
			// Get a sound clip resource.
			Clip clip = AudioSystem.getClip();
			// Open audio clip and load samples from the audio input stream.
			clip.open(audioIn);
			//    clip.start();
			return clip;
			} 
		catch (UnsupportedAudioFileException e) 
			{
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Problem loading "+url);
			System.exit(0);
			} 
		catch (IOException e) 
			{
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Problem loading "+url);
			System.exit(0);
			} 
		catch (LineUnavailableException e) 
			{
			//
			e.printStackTrace();
			}
		return null;
		}
	
	/**
	 * Play sound clip. Ignore if it is null.
	 */
	public static void play(Clip clip)
		{
		if(clip!=null)
			clip.start();
		}
	
	
	public static void main(String[] arg)
		{
		Clip snd=EvSound.loadSound(EvSound.class.getResource("bang.wav"));
		play(snd);
		}
	
	}