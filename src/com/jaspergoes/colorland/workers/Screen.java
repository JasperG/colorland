package com.jaspergoes.colorland.workers;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import com.jaspergoes.colorland.MainWindow;
import com.jaspergoes.colorland.WindowHelper;
import com.jaspergoes.colorland.filters.GaussianFilter;
import com.jaspergoes.colorland.objects.DisplayDevice;

public class Screen
{
	/* Display id-string */
	private static String displayDeviceSelected;

	/* Screen rectangle */
	private static Rectangle rect;

	/* Prevent calls to bufferedImage.getWidth() and getHeight() by caching width and height of rect */
	private static int captureWidth;
	private static int captureHeight;

	/* Blur filter */
	private static GaussianFilter blurFilter = new GaussianFilter(4);

	/* Setting useGammaCorrection */
	private static boolean useGammaCorrection;

	/* Gamma color offset lookup table */
	private static int[] gamma_table;

	/* Robot object */
	private static Robot robot;

	/* Color offset for lamps */
	public static int colorOffset = 128;

	/* Sampling type used for scaling */
	public static Object interpolationType = RenderingHints.VALUE_INTERPOLATION_BILINEAR;

	/* Size of the sample */
	public static int pixelCountSample = 60;

	/* Minimum chromatic offset */
	public volatile static int chromaOffset = 4;

	/* Attempted milliseconds per frame */
	public volatile static int fpsInterval = 50;

	/* Setting useBlur */
	public static boolean useBlur;

	/* Setting showCorrectedSample */
	public static boolean showCorrectedSample;

	/* List of available display devices */
	public static ArrayList<DisplayDevice> displayDevices = new ArrayList<DisplayDevice>();

	/* Whether we are done initializing */
	public volatile static boolean initialized;

	public Screen()
	{

		try
		{
			robot = new Robot();
		}
		catch (AWTException e)
		{
			JOptionPane.showMessageDialog(null, "Could not get system permission to capture screen.\n\n" + e.toString() + "\n\nTerminating.", WindowHelper.appTitle, JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}

		setGammaTable(4d);

		setRectangle(0, 140);

		/* When starting up, make sure we have a color and brightness set */
		Milight.newColor = (int) (Math.random() * 256);
		Milight.newBrightness = 100;
		Milight.newSaturation = 0;

		/* Start worker thread for milight */
		Milight.INSTANCE.startWorkerThread();

		/* Start worker thread for screenworker */
		new Thread() {

			@Override
			public void run()
			{

				long intervalTime;

				/* Work variables */
				int pixelCountSample;
				int pixelCountSampleTotal;
				int pixelCountSampled;

				BufferedImage imgOrig;
				BufferedImage imgWork;

				boolean useGammaCorrection;
				int[] gamma_table = Screen.gamma_table;

				boolean showSample;
				boolean showCorrectedSample;

				int milightControlFlags;

				int brightnessRed;
				int brightnessGreen;
				int brightnessBlue;

				int colorRed;
				int colorGreen;
				int colorBlue;

				int x, y;

				int color;

				int red;
				int green;
				int blue;

				float[] hsb_color = new float[3];
				float[] hsb_satbr = new float[3];

				boolean isChromaBiggerThanOffset;
				/* End work variables */

				while (true)
				{

					/* Set time that would be the upper limit to keep configured x-frames per second */
					intervalTime = System.currentTimeMillis() + fpsInterval;

					/* Start work */
					pixelCountSample = Screen.pixelCountSample;
					pixelCountSampleTotal = pixelCountSample * pixelCountSample;
					pixelCountSampled = pixelCountSampleTotal;

					imgOrig = scaleImage(robot.createScreenCapture(rect), pixelCountSample, pixelCountSample, interpolationType, true);

					/* Create a copy of the BufferedImage to work with */
					imgWork = useBlur ? blurFilter.filter(imgOrig, new BufferedImage(pixelCountSample, pixelCountSample, BufferedImage.TYPE_INT_RGB)) : new BufferedImage(imgOrig.getColorModel(), imgOrig.copyData(null), false, null);

					/* Hoist class variable into method */
					useGammaCorrection = Screen.useGammaCorrection;

					/* Hoist class variable into method, if needed */
					if (useGammaCorrection) gamma_table = Screen.gamma_table;

					/* Hoist static variable from MainWindow into method */
					showSample = MainWindow.VISIBLE;

					/* Hoist class variable into method */
					showCorrectedSample = showSample && Screen.showCorrectedSample;

					milightControlFlags = Milight.controlFlags;

					brightnessRed = 0;
					brightnessGreen = 0;
					brightnessBlue = 0;

					colorRed = 0;
					colorGreen = 0;
					colorBlue = 0;

					/* Cycle through image pixels */
					for (x = 0; x < pixelCountSample; x++)
					{

						for (y = 0; y < pixelCountSample; y++)
						{

							/* Get pixel color */
							color = imgWork.getRGB(x, y);

							/* Get red green and blue values from pixel color */
							red = (color & 0x00ff0000) >> 16;
							green = (color & 0x0000ff00) >> 8;
							blue = color & 0x000000ff;

							/* Add unmodified red green and blue values for brightness */
							brightnessRed += red;
							brightnessGreen += green;
							brightnessBlue += blue;

							/* Further processing determines how to apply for tint */
							if (color == 0xff000000) /* Black pixel */
							{
								pixelCountSampled--;
							}
							else if ((int) ((0.2126 * red) + (0.7152 * green) + (0.0722 * blue)) <= 8) /* Check for low luma (ITU-R BT.709) */
							{
								pixelCountSampled--;

								if (showCorrectedSample)
								{
									imgWork.setRGB(x, y, 0xff000000);
								}
							}
							else if (useGammaCorrection)
							{
								if (showCorrectedSample)
								{
									red = gamma_table[red];
									green = gamma_table[green];
									blue = gamma_table[blue];

									colorRed += red;
									colorGreen += green;
									colorBlue += blue;

									imgWork.setRGB(x, y, (red << 16 | green << 8 | blue));
								}
								else
								{
									colorRed += gamma_table[red];
									colorGreen += gamma_table[green];
									colorBlue += gamma_table[blue];
								}
							}
							else
							{
								colorRed += red;
								colorGreen += green;
								colorBlue += blue;
							}

						}

					}

					if (pixelCountSampled == 0)
					{
						hsb_satbr[1] = 0;
						hsb_satbr[2] = 0;

						red = 0;
						green = 0;
						blue = 0;

						isChromaBiggerThanOffset = false;
					}
					else
					{
						Color.RGBtoHSB(brightnessRed / pixelCountSampleTotal, brightnessGreen / pixelCountSampleTotal, brightnessBlue / pixelCountSampleTotal, hsb_satbr);

						red = colorRed / pixelCountSampled;
						green = colorGreen / pixelCountSampled;
						blue = colorBlue / pixelCountSampled;

						if ((milightControlFlags & Milight.MILIGHT_DOCOLOR) == Milight.MILIGHT_DOCOLOR)
						{
							isChromaBiggerThanOffset = (red > green ? (red > blue ? red : blue) : (green > blue ? green : blue)) - (red < green ? (red < blue ? red : blue) : (green < blue ? green : blue)) > chromaOffset;

							if (isChromaBiggerThanOffset)
							{
								Color.RGBtoHSB(red, green, blue, hsb_color);
							}
						}
						else
						{
							isChromaBiggerThanOffset = false;
						}
					}

					if (isChromaBiggerThanOffset && Milight.newColor != (Milight.newColor = ((colorOffset + ((int) (hsb_color[0] * 256))) % 256)))
					{
						Milight.newBrightness = (int) (hsb_satbr[2] * 100);
						Milight.newSaturation = 100 - (int) (hsb_satbr[1] * 100);

						synchronized (Milight.INSTANCE)
						{
							Milight.INSTANCE.notify();
						}
					}
					else if (Milight.newBrightness != (Milight.newBrightness = (int) (hsb_satbr[2] * 100)) && (milightControlFlags & Milight.MILIGHT_DOBRGHT) == Milight.MILIGHT_DOBRGHT)
					{
						Milight.newSaturation = 100 - (int) (hsb_satbr[1] * 100);

						synchronized (Milight.INSTANCE)
						{
							Milight.INSTANCE.notify();
						}
					}
					else if (Milight.newSaturation != (Milight.newSaturation = 100 - (int) (hsb_satbr[1] * 100)) && (milightControlFlags & Milight.MILIGHT_DOSATUR) == Milight.MILIGHT_DOSATUR) /* RGBWW */
					{
						synchronized (Milight.INSTANCE)
						{
							Milight.INSTANCE.notify();
						}
					}

					if (showSample)
					{
						MainWindow.INSTANCE.setSample(red, green, blue, new ImageIcon(scaleImage(showCorrectedSample ? imgWork : imgOrig, 200, 200, null, false)), isChromaBiggerThanOffset);
					}

					/* Flush both BufferedImage instances */
					imgWork.flush();
					imgOrig.flush();

					/* End work */

					/* Sleep until time to next frame is reached; Based on x-frames per sec, OR, just continue if demand can't be met */
					if ((intervalTime = intervalTime - System.currentTimeMillis()) > 0)
					{

						try
						{
							Thread.sleep(intervalTime);
						}
						catch (InterruptedException e)
						{

						}

					}

				}

			}

		}.start();

		initialized = true;

	}

	public static void setRectangle(int rectOffsetX, int rectOffsetY)
	{
		Rectangle rect = new Rectangle(0, 0, 0, 0);

		boolean found = false;
		// int foundindex = 0;

		while (!found)
		{
			displayDevices.clear();
			int i = 1;

			for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices())
			{
				/* Get screen ID */
				String id = gd.getIDstring();

				/* Get current resolution of screen */
				DisplayMode dm = gd.getDisplayMode();

				if (displayDeviceSelected == null || id.equals(displayDeviceSelected))
				{
					displayDeviceSelected = id;
					found = true;
					// foundindex = i - 1;

					/* Bounds of screen */
					rect = gd.getDefaultConfiguration().getBounds();

					/* Width and height to current resolution */
					int w = dm.getWidth(), h = dm.getHeight();

					/* Subtract offsets */
					int l = rect.x + Math.max(0, Math.min(rectOffsetX, (w - 2) / 2));
					int t = rect.y + Math.max(0, Math.min(rectOffsetY, (h - 2) / 2));

					/* New width and height based on offsets */
					w -= 2 * (l - rect.x);
					h -= 2 * (t - rect.y);

					captureWidth = w;
					captureHeight = h;

					/* Update rectangle */
					rect.setBounds(l, t, w, h);
				}

				displayDevices.add(new DisplayDevice(id, i++ + ": " + dm.getWidth() + "x" + dm.getHeight()));

			}

			if (!found) displayDeviceSelected = null;
		}

		Screen.rect = rect;

		/* TODO: Set display devices in main window */

		/* Note to self; this is fugly. */
		/*		MainWindow.displayDevicesComboBox.removeAllItems();
				for (int i = 0; i < devices.size(); i++)
				{
					MainWindow.displayDevicesComboBox.addItem(devices.get(i).humanReadable);
				}
				MainWindow.displayDevicesComboBox.setSelectedIndex(foundindex);*/

	}

	public static void setDisplay(int index, int rectOffsetX, int rectOffsetY)
	{
		displayDeviceSelected = displayDevices.get(index).id;
		setRectangle(rectOffsetX, rectOffsetY);
	}

	public static void setGammaTable(double gamma)
	{
		if (gamma == 1)
		{
			useGammaCorrection = false;
		}
		else
		{
			int[] g_table = new int[256];

			gamma = 1 / gamma;

			for (int i = 0; i <= 255; i++)
			{
				g_table[i] = (int) (255 * (Math.pow((double) i / (double) 255, gamma)));
			}

			gamma_table = g_table;

			useGammaCorrection = true;
		}
	}

	private BufferedImage scaleImage(BufferedImage img, int targetWidth, int targetHeight, Object hint, boolean higherQuality)
	{
		BufferedImage tmp;
		int w, h;

		if (higherQuality)
		{
			// Use multi-step technique: start with original size, then
			// scale down in multiple passes with drawImage()
			// until the target size is reached
			w = captureWidth; //img.getWidth();
			h = captureHeight; //img.getHeight();
		}
		else
		{
			// Use one-step technique: scale directly from original
			// size to target size with a single drawImage() call
			w = targetWidth;
			h = targetHeight;
		}

		do
		{
			if (higherQuality)
			{

				if (w > targetWidth)
				{
					w /= 2;
					if (w < targetWidth) w = targetWidth;
				}

				if (h > targetHeight)
				{
					h /= 2;
					if (h < targetHeight) h = targetHeight;
				}

			}

			tmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = tmp.createGraphics();
			if (hint != null)
			{
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
			}
			g2.drawImage(img, 0, 0, w, h, null);
			g2.dispose();
			img.flush();
			img = tmp;
		}
		while (w != targetWidth || h != targetHeight);

		return img;
	}

}