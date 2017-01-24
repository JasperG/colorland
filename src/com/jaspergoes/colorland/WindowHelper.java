package com.jaspergoes.colorland;

import java.awt.AWTException;
import java.awt.Frame;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

public final class WindowHelper
{

	public static final String appVersion = "0.0.8 ( January 22nd 2017 )";

	public static final String appTitle = "Jasper Goes' to Colorland";

	/* Boolean indicating whether we have shown the "App is still running" hint yet */
	private static boolean hintShown = false;

	/* Surprisingly, this is supposed to set app title and icon */
	public static void setTitleAndIcon(Frame window)
	{
		window.setTitle(appTitle);
		window.setIconImage(getIcon());
	}

	public static Image getIcon()
	{
		/* Identifying application icon */
		return Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("icon.png"));
	}

	public static void setLookAndFeel(final JFrame window)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
		{
			/* Whatever, is this really ever going to fail? :|
			 * Don't care bout anything but Windows, really. */
		}

		setTitleAndIcon(window);

		/* If SystemTray is not supported, we are done here */
		if (!SystemTray.isSupported()) return;

		final SystemTray systemTray = SystemTray.getSystemTray();

		PopupMenu popup = new PopupMenu();

		final TrayIcon systemTrayIcon = new TrayIcon(getIcon(), appTitle, popup);

		systemTrayIcon.setImageAutoSize(true);

		systemTrayIcon.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e)
			{
				if (e.getClickCount() >= 2)
				{
					window.setVisible(true);
					window.setExtendedState(Frame.NORMAL);
					systemTray.remove(systemTrayIcon);
				}
			}

		});

		/* Prevent closing app from the platform UI */
		window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		window.addWindowListener(new java.awt.event.WindowAdapter() {

			/* Window minimized. Send to tray */
			@Override
			public void windowIconified(WindowEvent winEvt)
			{
				String OS = System.getProperty("os.name").toLowerCase();
				if (!(OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0))
				{

					try
					{
						systemTray.add(systemTrayIcon);
					}
					catch (AWTException e)
					{
					}

					if (!hintShown)
					{
						systemTrayIcon.displayMessage(WindowHelper.appTitle, "The application is still running!", TrayIcon.MessageType.INFO);
						hintShown = true;
					}

					window.setVisible(false);
				}
			}

			/* Window closing from platform UI */
			@Override
			public void windowClosing(WindowEvent winEvt)
			{
				if (window.getToolkit().isFrameStateSupported(Frame.ICONIFIED))
				{
					window.setExtendedState(Frame.ICONIFIED);
				}
				else
				{
					/* Terminate */
					System.exit(0);
				}
			}

		});

		/* Simply pad this menuitem with extra spaces to achieve minimum width */
		MenuItem menuItem = new MenuItem("Open       ");

		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				window.setVisible(true);
				window.setExtendedState(Frame.NORMAL);
				systemTray.remove(systemTrayIcon);
			}

		});

		popup.add(menuItem);

		popup.addSeparator();

		menuItem = new MenuItem("Quit");

		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				System.exit(0);
			}

		});

		popup.add(menuItem);

	}

}
