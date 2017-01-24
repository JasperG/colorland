package com.jaspergoes.colorland;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import com.jaspergoes.colorland.workers.Screen;
import com.jaspergoes.colorland.workers.Milight;

public class SplashWindow extends JDialog
{

	private static final long serialVersionUID = 1L;

	public static void main(String[] args)
	{
		new SplashWindow();
	}

	public SplashWindow()
	{

		WindowHelper.setTitleAndIcon((java.awt.Frame) getOwner());

		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(620, 300));

		JLayeredPane lPane = new JLayeredPane();
		lPane.setBounds(0, 0, 620, 300);

		JLabel label = new JLabel();
		label.setBounds(0, 0, 620, 300);
		label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		label.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("splash.png"))));
		lPane.add(label, 0, 0);

		JPanel panel = new JPanel();
		panel.setBounds(175, 160, 440, 100);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setOpaque(false);

		Font fontBold = new Font("Arial", Font.BOLD, 12);

		JLabel textV = new JLabel();
		textV.setFont(fontBold);
		textV.setOpaque(false);
		textV.setText("v" + WindowHelper.appVersion);
		panel.add(textV);

		JLabel textSpacer = new JLabel();
		textSpacer.setFont(fontBold);
		textSpacer.setOpaque(false);
		textSpacer.setText(" ");
		panel.add(textSpacer);

		final JLabel textIf = new JLabel();
		textIf.setFont(fontBold);
		textIf.setOpaque(false);
		textIf.setText("");
		panel.add(textIf);

		textSpacer = new JLabel();
		textSpacer.setFont(fontBold);
		textSpacer.setOpaque(false);
		textSpacer.setText(" ");
		panel.add(textSpacer);

		final JLabel text = new JLabel();
		text.setFont(fontBold);
		text.setOpaque(false);
		text.setText("Enumerating network interfaces.");
		panel.add(text);

		lPane.add(panel, 1, 0);

		add(lPane, BorderLayout.CENTER);

		setUndecorated(true);
		pack();
		setLocationRelativeTo(null);

		setVisible(true);

		/* Initialize the milight worker ( .. in a new thread as to not block the splash screen ) */
		new Thread() {

			@Override
			public void run()
			{

				/* This thread will die as soon as a Milight instance has been formed */
				new Milight();

			}

		}.start();

		/* New thread to monitor status, and initialize MainWindow if everything succeeds */
		new Thread() {

			@Override
			public void run()
			{
				int i = 0;
				long done = -1;

				while (true)
				{

					textIf.setText("Network Interface: " + Milight.networkInterfaceName);

					try
					{
						Thread.sleep(5);
					}
					catch (InterruptedException e)
					{

					}

					if (done == -1)
					{

						if (Milight.isConnected)
						{

							/* Done, we are connected! */
							text.setText("Connected!");
							done = System.currentTimeMillis();

							/* Initialize the screen worker ( .. in a new thread as to not block the splash screen ) */
							new Thread() {

								@Override
								public void run()
								{

									/* This thread will die as soon as a Screen instance has been formed */
									new Screen();

								}

							}.start();

						}
						else
						{

							String t;

							if (Milight.isConnecting)
							{
								/* Connecting */
								t = "Connecting to " + Milight.milightDevices.get(0).addrIP + '.';
							}
							else
							{
								/* Disovering */
								if (Milight.networkInterfaceBound)
								{
									int s = Milight.milightDevices.size();
									t = "Found " + s + " Milight device" + (s == 1 ? "" : "s") + " on the network.";
								}
								else
								{
									t = "Attempt binding to network interface.";
								}
							}

							t += new String(new char[i / 67]).replace("\0", ".");

							text.setText(t);

							i = (i + 1) % 200;

						}

					}
					else if (System.currentTimeMillis() - done > 1000 && Screen.initialized)
					{

						/* Destroy splash window */
						setVisible(false);
						dispose();

						/* Initialize main window */
						new MainWindow();

						/* Break out of 'while(true)' loop; Effectively terminating this thread. */
						break;

					}

				}

			}

		}.start();

	}

}