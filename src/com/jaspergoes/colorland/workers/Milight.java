package com.jaspergoes.colorland.workers;

import java.awt.Toolkit;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import com.jaspergoes.colorland.MainWindow;
import com.jaspergoes.colorland.WindowHelper;
import com.jaspergoes.colorland.objects.MilightDevice;

public class Milight
{

	/* Note: I'll be using many static variables since we'll be accessing them from loops mainly.
	 * By using statics, we'll avoid synthetic accessors, and so, improve performance a tiny bit.
	 * Thought through; Stop whining and simply change it if you don't like it.
	 */

	private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

	/* Socket, used entire app session long */
	private static DatagramSocket socket;

	/* Milight device addr */
	private static InetAddress milightAddress;

	/* Milight device port - declared final to help compiler inline port number (I expect this port number to never change for v6 devices) */
	private static final int milightPort = 5987;

	/* Local port to bind to - declared final to help compiler inline port number */
	private static final int localPort = 52123;

	/* Session bytes */
	private static byte milightSessionByte1;
	private static byte milightSessionByte2;

	/* Password bytes */
	private static byte milightPasswordByte1;
	private static byte milightPasswordByte2;

	/* Incremental value for each frame sent ( 0 - 255 ) */
	private static int noOnce = 2;

	/* Last submitted values */
	private static int lastColor = Integer.MAX_VALUE;
	private static int lastBrightness = Integer.MAX_VALUE;
	private static int lastSaturation = Integer.MAX_VALUE;

	/* Index of currently selected wifi bridge */
	private int milightDeviceSelected;

	public static Milight INSTANCE;

	/* String formatted password */
	public static String milightPassword = "";

	/* List of all found wifi bridges */
	public static ArrayList<MilightDevice> milightDevices = new ArrayList<MilightDevice>();

	/* Whether we are connecting */
	public volatile static boolean isConnecting;

	/* Whether we are connected */
	public volatile static boolean isConnected;

	/* Group of devices to control */
	public static int milightGroup = 7; /* 0 - wifi box, 7; RGBW lamps, 8; RGBWW (dual white) lamps */

	/* Zone to of devices to control */
	public static int milightZone = 0; /* 0 - all, or 1,2,3,4 ( after testing, can be anything in 0-255 range, but doubt anyone has assigned lamps to zone 255 :P ) */

	/* Whether or not to always set bridge brightness to 100% */
	public static boolean bridgeBrightnessLock = true;

	/* Whether we want to doColor, doBrightness, doSaturation and whether or not we want to send saturation commands that may result in different colors, compared to RGBW lamps */
	public static final int MILIGHT_DOCOLOR = 1;
	public static final int MILIGHT_DOBRGHT = 2;
	public static final int MILIGHT_DOSATUR = 4;
	public static final int MILIGHT_EMLRGBW = 8;
	public static int controlFlags = MILIGHT_DOCOLOR | MILIGHT_DOBRGHT;

	/* Minimal difference between last submitted color, brightness or saturation before sending new value */
	public static int offsetFactor = 2;

	/* New color and brightness values to be submitted */
	public volatile static int newColor;
	public volatile static int newBrightness;
	public volatile static int newSaturation;

	/* Name of the network interface in use */
	public volatile static String networkInterfaceName = "";
	public volatile static boolean networkInterfaceBound = false;

	public Milight()
	{
		INSTANCE = this;

		int triedInterfaces = 0;

		try
		{

			for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces()))
			{

				/* Skip loopback and disabled interfaces */
				if (networkInterface.isLoopback() || !networkInterface.isUp())
					continue;

				for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses())
				{

					InetAddress localAddress = interfaceAddress.getAddress();
					InetAddress broadcastAddress = interfaceAddress.getBroadcast();

					/* Skip any non-Inet4Address, and skip any interface without broadcast address */
					if (!localAddress.getClass().getSimpleName().equals("Inet4Address") || broadcastAddress == null)
						continue;

					networkInterfaceBound = false;
					networkInterfaceName = networkInterface.getDisplayName();
					triedInterfaces++;

					discoverDevices(localAddress, broadcastAddress);

					if (milightDevices.size() > 0)
					{

						/* We have lift-off!
						 * Sort by IP address, connect to the first device on the list.
						 * Agh gut, hoe laat is het eigenlijk? Tijd voor slaap? Nee? Heeft de cavia al eten? Ja? Ok. Keep going. Excuse my Dutch. */

						Collections.sort(milightDevices);

						setDevice(milightDevices.get(0).addrIP);

						/* Stop this discovery cycle */
						return;

					}

				}

			}

		}
		catch (SocketException e)
		{
			e.printStackTrace();
		}

		if (triedInterfaces == 0)
		{
			JOptionPane.showMessageDialog(null, "No suitable IPv4 network interfaces found.\n\nTerminating.", WindowHelper.appTitle, JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		else if (milightDevices.size() == 0)
		{

			/* No devices found, or, no devices picked up the phone (lol). Quit, and stuff. */

			if (socket != null) socket.close();

			ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("ibox.png")));
			JOptionPane.showMessageDialog(null, "No WiFi iBox1 ( v6 ) could be found on your network, or, the device did not respond within 15 seconds.\n\nNote: You need a WiFi iBox1 ( v6 ) ( aka. \"WiFi Bridge v6\" ) from Milight / Applamp / LimitlessLED\nconnected to your network to use this application.\n\nRestart the application to retry discovery.\n\nTerminating.", WindowHelper.appTitle, JOptionPane.ERROR_MESSAGE, icon);
			System.exit(0);

		}

	}

	private void discoverDevices(InetAddress localAddress, InetAddress broadcastAddress)
	{

		/* Close any previously opened socket */
		if (socket != null) socket.close();

		/* Bind new socket to given localAddress - thus - attaching to specific interface */
		try
		{
			socket = new DatagramSocket(localPort, localAddress);
		}
		catch (SocketException e)
		{
			JOptionPane.showMessageDialog(null, "Could not bind to port " + Integer.toString(localPort) + " at " + localAddress.getHostAddress() + ".\nIs another instance of the application already running?\n\n" + e.toString() + "\n\nTerminating.", WindowHelper.appTitle, JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}

		networkInterfaceBound = true;

		// byte[] payload = "HF-A11ASSISTHREAD".getBytes(Charset.forName("UTF-8"));
		byte[] payload = new byte[] { (byte) 72, (byte) 70, (byte) 45, (byte) 65, (byte) 49, (byte) 49, (byte) 65, (byte) 83, (byte) 83, (byte) 73, (byte) 83, (byte) 84, (byte) 72, (byte) 82, (byte) 69, (byte) 65, (byte) 68 };

		byte[] buffer = new byte[64];
		DatagramPacket packet = new DatagramPacket(buffer, 64);

		int attempts = 0;

		do
		{

			try
			{
				socket.send(new DatagramPacket(payload, payload.length, broadcastAddress, 48899));
			}
			catch (UnknownHostException e)
			{
				/* This should never happen */
			}
			catch (IOException e)
			{
				/* This should never happen */
			}

			/* Note:
			 * We'll spend eight seconds waiting for the first device to answer.
			 * We'll spend no more than four seconds once the first device has been found.
			 * If there's been no answer after eight seconds, we'll retry the whole thing, once.
			 * Max 16 seconds for discovery, or, show an error dialog afterwards. */

			/* Keep listening for incoming packets for at least four seconds, or eight while no devices are found */
			long timeout = System.currentTimeMillis();

			discoveryLoop: do
			{

				try
				{
					socket.setSoTimeout(1000);

					socket.receive(packet);

					/* We could have received literally anything from literally anywhere, so, check if this packet is actually something we're interested in */
					String[] discovery = new String(buffer, 0, packet.getLength(), Charset.forName("UTF-8")).split(",");

					/* Keep it simple stupid. What is the chance, another device would present us with an UTF-8
					 * formatted string, with two ',' chars and a 12 char String after it's first comma?
					 * Close to zero, I suppose. So, this check will do. */
					/* Also: discovery[2] always contains 'HF-LPB100' as in 'Low Power WiFi Module HF-LPB100' */
					/* title (3rd returned value) is always 'HF-LPB100' as 'Low Power WiFi Module HF-LPB100' */
					if (discovery.length == 3 && discovery[1].length() == 12)
					{

						/* Avoid duplicate entries. */
						for (int i = milightDevices.size() - 1; i >= 0; i--)
						{
							if (milightDevices.get(i).addrIP.equals(discovery[0]))
							{
								continue discoveryLoop;
							}
						}

						milightDevices.add(new MilightDevice(discovery[0], discovery[1]));

					}
				}
				catch (SocketTimeoutException e)
				{
				}
				catch (SocketException e)
				{
				}
				catch (IOException e)
				{
				}

			}
			while (System.currentTimeMillis() - timeout < (milightDevices.size() == 0 ? 8000 : 4000));

		}
		while (++attempts < 2 && milightDevices.size() == 0);

	}

	private void setDevice(String address)
	{

		/* TODO: Make sure we can change connection - packets sent by threads may destroy our chance to connect. Duh, okee, later dan maar. (IOW, geen zin in). */
		/* if (isConnected)
		{
			isConnected = false;
		
			 Wait a second for old packets to become stale 
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
			}
		} */

		isConnecting = true;

		try
		{
			milightAddress = InetAddress.getByName(address);
		}
		catch (UnknownHostException e)
		{
			/* This should never happen. Nasty empty catch blocks, I should know better. Oh, right, I do. */
		}

		byte[] payload = new byte[] { (byte) 32, (byte) 0, (byte) 0, (byte) 0, (byte) 22, (byte) 2, (byte) 98, (byte) 58, (byte) 213, (byte) 237, (byte) 163, (byte) 1, (byte) 174, (byte) 8, (byte) 45, (byte) 70, (byte) 97, (byte) 65, (byte) 167, (byte) 246, (byte) 220, (byte) 175, (byte) 211, (byte) 230, (byte) 0, (byte) 0, (byte) 30 };

		try
		{
			socket.send(new DatagramPacket(payload, 27, milightAddress, milightPort));
		}
		catch (UnknownHostException e)
		{
		}
		catch (IOException e)
		{
		}

		byte[] buffer = new byte[64];
		DatagramPacket packet = new DatagramPacket(buffer, 64);

		/* Keep listening for incoming packets for ten seconds */
		long timeout = System.currentTimeMillis() + 10000;

		do
		{

			try
			{
				socket.setSoTimeout(1000);

				socket.receive(packet);

				/* Check if the packet came from the selected device, and the received response is as expected */
				if (packet.getAddress().equals(milightAddress) && bytesToHex(buffer, packet.getLength()).indexOf("2800000011") == 0)
				{
					milightSessionByte1 = buffer[19];
					milightSessionByte2 = buffer[20];

					/* Discover password bytes before setting isConnected to true */
					passwordDiscovery();

					isConnected = true;
				}

			}
			catch (SocketTimeoutException e)
			{
			}
			catch (SocketException e)
			{
			}
			catch (IOException e)
			{
			}

		}
		while (!isConnected && timeout - System.currentTimeMillis() > 0);

		if (!isConnected)
		{

			socket.close();

			ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("ibox.png")));
			JOptionPane.showMessageDialog(null, "Could not establish a connection to the WiFi iBox1 ( v6 ) on your network within 10 seconds.\n\nRestart the application to retry.\n\nTerminating.", WindowHelper.appTitle, JOptionPane.ERROR_MESSAGE, icon);
			System.exit(0);

		}

		/* We are connected now!; The splash screen thread will die right about now it's seen our volatile boolean 'isConnected' change. */

	}

	private void passwordDiscovery()
	{

		/* Set an incorrect pair of password-bytes, so we'll receive a packet with the correct password-bytes as response */
		milightPasswordByte1 = -1;
		milightPasswordByte2 = -1;

		byte[] buffer = new byte[64];
		DatagramPacket packet = new DatagramPacket(buffer, 64);

		try
		{

			int sendAttempts = 0;

			do
			{

				/* Send a useless color payload with an incorrect set of password-bytes */
				socket.send(new DatagramPacket(buildColorPayload(0), 22, milightAddress, milightPort));

				/* The second packet received will contain the correct password bytes */
				int receiveAttempts = 0;

				do
				{

					try
					{

						socket.receive(packet);

						if (packet.getAddress().equals(milightAddress))
						{

							String response = bytesToHex(buffer, packet.getLength());

							if (response.indexOf("8000000015") == 0 || response.indexOf("8000000021") == 0)
							{

								milightPassword = bytesToHex(new byte[] { buffer[16], buffer[17] }, 2);

								milightPasswordByte1 = buffer[16];
								milightPasswordByte2 = buffer[17];

								/* Update password displayed in main window */
								if (MainWindow.INSTANCE != null)
									MainWindow.INSTANCE.updatePassword();

								return;

							}

						}
						else
						{
							receiveAttempts--;
						}

					}
					catch (SocketTimeoutException e)
					{
					}

				}
				while (++receiveAttempts < 2);

			}
			while (++sendAttempts < 2);

		}
		catch (IOException e)
		{
			/* This should never happen */
		}

		System.out.println("Could not retrieve password bytes.");

	}

	public void startWorkerThread()
	{

		new Thread() {

			@Override
			public void run()
			{

				/* Keep initializations out of the loop */
				int controlFlags, color, brightness, saturation, group;
				boolean doColor, doBrightness, doSaturation;

				boolean keepAlive = false;
				boolean dataSent;
				long keepAliveTime = 0;
				int factor;

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				StringBuilder output = new StringBuilder();

				ArrayList<byte[]> payloads = new ArrayList<byte[]>();

				/* Yes, you've read that right. while(true); We wont ever have this thread die unless the app is explicitly terminated. */
				while (true)
				{

					dataSent = false;

					while (true)
					{

						/* Hoist color, brightness and group in only once per cycle of the loop */
						factor = offsetFactor;
						color = newColor;
						brightness = newBrightness;
						saturation = newSaturation;
						group = milightGroup;
						controlFlags = Milight.controlFlags;

						doColor = (controlFlags & Milight.MILIGHT_DOCOLOR) == Milight.MILIGHT_DOCOLOR && Math.abs(lastColor - color) >= factor;
						doBrightness = (controlFlags & Milight.MILIGHT_DOBRGHT) == Milight.MILIGHT_DOBRGHT && Math.abs(lastBrightness - brightness) >= factor;
						doSaturation = (controlFlags & Milight.MILIGHT_DOSATUR) == Milight.MILIGHT_DOSATUR && Math.abs(lastSaturation - ((controlFlags & Milight.MILIGHT_EMLRGBW) == Milight.MILIGHT_EMLRGBW ? 0 : saturation)) >= factor;

						/* Break out, into synchronized wait, if no changes are to be submitted */
						if (!(isConnected && (doColor || doBrightness || doSaturation)))
						{

							if (isConnected && keepAlive)
							{

								/* Send keep-alive packet */
								try
								{
									socket.send(new DatagramPacket(new byte[] { (byte) 208, (byte) 0, (byte) 0, (byte) 0, (byte) 2, milightSessionByte1, milightSessionByte2 }, 7, milightAddress, milightPort));
								}
								catch (IOException e)
								{
									/* This should never happen */
								}

								keepAlive = false;
								dataSent = true;

							}

							break;

						}
						else
						{

							output.append("Frame at ").append(sdf.format(new Date())).append('\n');

							keepAlive = false;
							dataSent = true;

						}

						if (doColor)
						{
							lastColor = color;
							output.append("Set -> Color ").append(color).append('\n');
						}

						if (doBrightness)
						{
							lastBrightness = brightness;
							output.append("Set -> Brightness ").append(brightness).append('\n');
						}

						if (doSaturation)
						{
							lastSaturation = (controlFlags & Milight.MILIGHT_EMLRGBW) == Milight.MILIGHT_EMLRGBW ? 0 : saturation;
							output.append("Set -> Saturation ").append(saturation).append('\n');
						}

						System.out.println(output.toString());
						output.setLength(0);

						/* Multiple device groups selected in UI? Or just a single one? */
						if (group < 0)
						{

							/* Note: Set colors once per lamp, for each lamp, send brightnesses later(!) This gives
							 * the lamp time to execute the first RF-signal before the second one comes in;
							 * 
							 * Sidenote; We'll probably need to add at least 10 millisecond delays sending out the payload frames.
							 * */

							/* Color */
							if (doColor)
							{

								/* RGBWW dual white lamps */
								if (group <= -2)
									payloads.add(buildColorPayload(8));

								/* WiFi bridge */
								payloads.add(buildColorPayload(0));

								/* RGBW lamps */
								if (group == -3 || group == -1)
									payloads.add(buildColorPayload(7));

							}

							/* Brightness */
							if (doBrightness)
							{

								/* RGBWW dual white lamps */
								if (group <= -2)
									payloads.add(buildBrightnessPayload(8));

								/* WiFi bridge */
								payloads.add(buildBrightnessPayload(0));

								/* RGBW lamps */
								if (group == -3 || group == -1)
									payloads.add(buildBrightnessPayload(7));

							}

							/* Saturation */
							if (doSaturation)
							{

								/* RGBWW dual white lamps */
								payloads.add(buildSaturationPayload());

							}

							/* Send command series */
							sendFrames(payloads);

						}
						else
						{

							/* Single device group selected treat as such. */

							if (doColor)
							{
								payloads.add(buildColorPayload(group));
							}

							if (doBrightness)
							{
								payloads.add(buildBrightnessPayload(group));
							}

							if (doSaturation)
							{
								/* RGBWW dual white lamps */
								payloads.add(buildSaturationPayload());
							}

							/* Send command series */
							sendFrames(payloads);

						}

						/* Clear payloads arraylist for re-use in next cycle */
						payloads.clear();

					}

					/* Entering wait, set keepalive time */
					if (dataSent)
						keepAliveTime = System.currentTimeMillis() + 5000;

					try
					{
						synchronized (Milight.INSTANCE)
						{
							Milight.INSTANCE.wait(keepAliveTime - System.currentTimeMillis());
						}
					}
					catch (InterruptedException e)
					{
					}

					/* Came out of wait, check if 5 seconds have passed */
					if (keepAliveTime - System.currentTimeMillis() <= 0)
						keepAlive = true;

				}

			}

		}.start();

	}

	private void sendFrames(ArrayList<byte[]> payloads)
	{

		for (byte[] payload : payloads)
		{

			try
			{
				socket.send(new DatagramPacket(payload, 22, milightAddress, milightPort));
				
				/* Wait 50 milliseconds for previous RF command to propagate from iBox */
				Thread.sleep(50);
			}
			catch (IOException e)
			{
				/* This should never happen, but, if it does, for whatever reason; There would be no need for
				 * a followup Thread.sleep call, which will not be performed once this catch block has been reached */
			}
			catch (InterruptedException e)
			{
				/* This may happen, and I wouldn't care one byte */
			}

		}

	}

	private byte[] buildColorPayload(int deviceGroup)
	{
		/* Note: We intentionally take the color value from newColor here.
		 * We do not nescessarily want to set all lamps to the *SAME* color,
		 * we just want to set it to the very, very LATEST calculated color.
		 */

		/* Each type of bulb has a different offset for the same position in the color spectrum. 
		 * List might be incomplete or (slightly) inaccurate. Improvements? Please pull.
		 * 
		 * Full RED values: 
		 *	group 0: 0
		 *	group 8: 10
		 *	group 7: 26
		*/
		int color = -128 + ((newColor + (deviceGroup == 7 ? 26 : (deviceGroup == 8 ? 10 : 0))) % 256);

		/* payload[4]; Length byte (Total amount of bytes - 5 header bytes) */
		/* payload[10]; First byte of lamp command (0x31) */
		/* payload[11] and payload[12]; Password bytes. */
		/* Rest should be self-explanatory */

		byte[] payload = new byte[] { (byte) 128, (byte) 0, (byte) 0, (byte) 0, (byte) 17, milightSessionByte1, milightSessionByte2, (byte) 0, (byte) (-128 + noOnce), (byte) 0, (byte) 49, milightPasswordByte1, milightPasswordByte2, (byte) deviceGroup, (byte) 1, (byte) color, (byte) color, (byte) color, (byte) color, (byte) milightZone, (byte) 0, (byte) 0 };

		/* Checksum */
		payload[21] = (byte) ((char) (0xFF & payload[10]) + (char) (0xFF & payload[11]) + (char) (0xFF & payload[12]) + (char) (0xFF & payload[13]) + (char) (0xFF & payload[14]) + (char) (0xFF & payload[15]) + (char) (0xFF & payload[16]) + (char) (0xFF & payload[17]) + (char) (0xFF & payload[18]) + (char) (0xFF & payload[19]) + (char) (0xFF & payload[20]));

		/* Increment sequential number */
		noOnce = (noOnce + 1) % 256;

		return payload;
	}

	private byte[] buildBrightnessPayload(int deviceGroup)
	{
		/* Note: We intentionally take the brightness value from newBrightness here.
		 * We do not nescessarily want to set all lamps to the *SAME* brightness,
		 * we just want to set it to the very, very LATEST calculated brightness.
		 */

		/* Always set wifi bridge brightness to 100? Nice for me personally,
		 * since I've placed my iBox right behind my TV. As others might. Whatever. */

		byte[] payload = new byte[] { (byte) 128, (byte) 0, (byte) 0, (byte) 0, (byte) 17, milightSessionByte1, milightSessionByte2, (byte) 0, (byte) (-128 + noOnce), (byte) 0, (byte) 49, milightPasswordByte1, milightPasswordByte2, (byte) deviceGroup, (byte) (deviceGroup == 8 ? 3 : 2), (byte) (deviceGroup == 0 && bridgeBrightnessLock ? 100 : (deviceGroup == 8 ? Math.min(newBrightness + 6, 100) : newBrightness)), (byte) 0, (byte) 0, (byte) 0, (byte) milightZone, (byte) 0, (byte) 0 };

		/* Checksum */
		payload[21] = (byte) ((char) (0xFF & payload[10]) + (char) (0xFF & payload[11]) + (char) (0xFF & payload[12]) + (char) (0xFF & payload[13]) + (char) (0xFF & payload[14]) + (char) (0xFF & payload[15]) + (char) (0xFF & payload[16]) + (char) (0xFF & payload[17]) + (char) (0xFF & payload[18]) + (char) (0xFF & payload[19]) + (char) (0xFF & payload[20]));

		/* Increment sequential number */
		noOnce = (noOnce + 1) % 256;

		return payload;
	}

	private byte[] buildSaturationPayload()
	{
		/* Note: This only works for deviceGroup 8; Lamps with saturation control. RGBWW */
		byte[] payload = new byte[] { (byte) 128, (byte) 0, (byte) 0, (byte) 0, (byte) 17, milightSessionByte1, milightSessionByte2, (byte) 0, (byte) (-128 + noOnce), (byte) 0, (byte) 49, milightPasswordByte1, milightPasswordByte2, (byte) 8, (byte) 2, (byte) ((controlFlags & Milight.MILIGHT_EMLRGBW) == Milight.MILIGHT_EMLRGBW ? 0 : newSaturation), (byte) 0, (byte) 0, (byte) 0, (byte) milightZone, (byte) 0, (byte) 0 };

		/* Checksum */
		payload[21] = (byte) ((char) (0xFF & payload[10]) + (char) (0xFF & payload[11]) + (char) (0xFF & payload[12]) + (char) (0xFF & payload[13]) + (char) (0xFF & payload[14]) + (char) (0xFF & payload[15]) + (char) (0xFF & payload[16]) + (char) (0xFF & payload[17]) + (char) (0xFF & payload[18]) + (char) (0xFF & payload[19]) + (char) (0xFF & payload[20]));

		/* Increment sequential number */
		noOnce = (noOnce + 1) % 256;

		return payload;
	}

	public void switchOnOff(boolean onOff)
	{

		synchronized (Milight.INSTANCE)
		{

			/* We've got a synchronized lock on the Milight instance. Now; Block for 50 milliseconds so all
			 * previously sent commands have had time to get processed before sending an on/off command */
			try
			{
				Thread.sleep(50);
			}
			catch (InterruptedException e)
			{
				/* No one would care if this happens; But it won't. */
			}

			ArrayList<byte[]> payloads = new ArrayList<byte[]>();

			int group = Milight.milightGroup;
			if (group < 0)
			{
				/* WiFi bridge */
				payloads.add(buildSwitchPayload(0, onOff));

				/* RGBWW dual white lamps */
				if (group <= -2) payloads.add(buildSwitchPayload(8, onOff));

				/* RGBW lamps */
				if (group == -3 || group == -1)
					payloads.add(buildSwitchPayload(7, onOff));
			}
			else
			{
				payloads.add(buildSwitchPayload(group, onOff));
			}

			sendFrames(payloads);

		}

		/* Force (re-)sending color, brightness and saturation commands after a selection of bulbs has been switched 'On' */
		if (onOff) forceUpdate(true);

	}

	private byte[] buildSwitchPayload(int group, boolean onOff)
	{
		byte[] payload = new byte[] { (byte) 128, (byte) 0, (byte) 0, (byte) 0, (byte) 17, milightSessionByte1, milightSessionByte2, (byte) 0, (byte) (-128 + noOnce), (byte) 0, (byte) 49, milightPasswordByte1, milightPasswordByte2, (byte) group, (byte) (group == 8 ? 4 : 3), (byte) (group == 0 ? (onOff ? 3 : 4) : (onOff ? 1 : 2)), (byte) 0, (byte) 0, (byte) 0, (byte) (group == 0 ? 0 : milightZone), (byte) 0, (byte) 0 };

		/* Checksum */
		payload[21] = (byte) ((char) (0xFF & payload[10]) + (char) (0xFF & payload[11]) + (char) (0xFF & payload[12]) + (char) (0xFF & payload[13]) + (char) (0xFF & payload[14]) + (char) (0xFF & payload[15]) + (char) (0xFF & payload[16]) + (char) (0xFF & payload[17]) + (char) (0xFF & payload[18]) + (char) (0xFF & payload[19]) + (char) (0xFF & payload[20]));

		/* Increment sequential number */
		noOnce = (noOnce + 1) % 256;

		return payload;
	}

	public void setDevice(int index)
	{
		if (milightDeviceSelected != index)
		{
			milightDeviceSelected = index;
			setDevice(milightDevices.get(index).addrIP);
		}
	}

	public static void forceUpdate(boolean notify)
	{
		lastColor = Integer.MAX_VALUE;
		lastBrightness = Integer.MAX_VALUE;
		lastSaturation = Integer.MAX_VALUE;

		if (notify) synchronized (Milight.INSTANCE)
		{
			Milight.INSTANCE.notify();
		}
	}

	public static void setRandomColor()
	{
		int c;
		do
		{
			c = (int) (Math.random() * 256D);
		}
		while ((newColor >= c - 12) && (newColor <= c + 12));
		newColor = c;
		forceUpdate(true);
	}

	private static String bytesToHex(byte[] bytes, int length)
	{
		char[] hexChars = new char[length * 2];
		int j = 0, v;
		while (j < length)
		{
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
			j++;
		}
		return new String(hexChars);
	}

	public static void setDoColor(boolean doColor)
	{
		if (doColor)
		{
			controlFlags |= MILIGHT_DOCOLOR;

			forceUpdate(true);
		}
		else
		{
			controlFlags &= ~MILIGHT_DOCOLOR;
		}
	}

	public static void setDoBrightness(boolean doBrightness)
	{
		if (doBrightness)
		{
			controlFlags |= MILIGHT_DOBRGHT;

			forceUpdate(true);
		}
		else
		{
			controlFlags &= ~MILIGHT_DOBRGHT;
		}
	}

	public static void setDoSaturation(boolean doSaturation, boolean forceUpdate)
	{
		if (doSaturation && (Milight.milightGroup <= -2 || Milight.milightGroup == 8))
		{
			controlFlags |= MILIGHT_DOSATUR;

			if (forceUpdate) forceUpdate(true);
		}
		else
		{
			controlFlags &= ~MILIGHT_DOSATUR;
		}
	}

	public static void setEmulateRGBWSaturation(boolean rgbwSaturation)
	{
		if (rgbwSaturation)
		{
			controlFlags |= MILIGHT_EMLRGBW;
		}
		else
		{
			controlFlags &= ~MILIGHT_EMLRGBW;
		}

		/* This option can only be changed while MILIGHT_DOSATUR is true, no need to check, just force update */
		forceUpdate(true);
	}

}