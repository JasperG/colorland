package com.jaspergoes.colorland;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.Font;
import java.awt.Frame;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.FlowLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.Box;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JSpinner;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.SpringLayout;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import com.jaspergoes.colorland.workers.Screen;
import com.jaspergoes.colorland.objects.DisplayDevice;
import com.jaspergoes.colorland.workers.Milight;

public class MainWindow extends JFrame
{

	private static final long serialVersionUID = 1L;

	private static JPanel samplePanel;

	private static JLabel sampleImage;
	private static JLabel sampleRed;
	private static JLabel sampleGreen;
	private static JLabel sampleBlue;
	private static JLabel sampleColor;
	private static JLabel sampleBrightness;
	private static JLabel sampleSaturation;
	private static JLabel lblBr;
	private static JLabel lblR;
	private static JLabel lblG;
	private static JLabel lblB;
	private static JLabel lblColor;
	private static JLabel lblSt;

	private static JCheckBox chckbxShowCorrectedSample;

	private static JButton btnRandom;

	private static JLabel labelPassword;

	public static MainWindow INSTANCE;

	public static boolean VISIBLE;

	public static JComboBox<String> displayDevicesComboBox;

	public static void main(String[] args)
	{
		new SplashWindow();
	}

	public MainWindow()
	{

		INSTANCE = this;

		WindowHelper.setLookAndFeel(this);
		setResizable(false);
		setMenu();

		getContentPane().setLayout(new BorderLayout());

		Font fontNorm = new Font("Arial", Font.PLAIN, 12);
		Font fontBold = new Font("Arial", Font.BOLD, 12);

		JPanel controlPanel = new JPanel();

		getContentPane().add(controlPanel, BorderLayout.NORTH);
		GridBagLayout gbl_controlPanel = new GridBagLayout();
		gbl_controlPanel.columnWidths = new int[] { 0, 0, 0, 76, 0, 27, 6, 31, 0, 0 };
		gbl_controlPanel.rowHeights = new int[] { 0, 21, 23, 0, 22, 22, 22, 23, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_controlPanel.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_controlPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		controlPanel.setLayout(gbl_controlPanel);

		String[] bridgeDevices = new String[Milight.milightDevices.size()];
		for (int i = 0; i < Milight.milightDevices.size(); i++)
		{
			bridgeDevices[i] = Milight.milightDevices.get(i).addrIP + " ( " + Milight.milightDevices.get(i).addrMAC.replaceAll("(.{2})", "$1" + ':').substring(0, 17) + " )";
		}

		final JLabel sampleSquarePixelsLabel = new JLabel("10000 pixels");
		final JComboBox bridgeDevicesComboBox = new JComboBox(bridgeDevices);
		final JComboBox comboBox_1 = new JComboBox(new String[] { "RGBWW + RGBW + WiFi", "RGBWW + WiFi", "RGBW + WiFi", "RGBW Lamp(s)", "RGBWW Lamp(s)", "WiFi Bridge" });
		final JSpinner sampleSquare_spinner = new JSpinner(new SpinnerNumberModel(60, 20, 100, 1));
		final JSpinner spinnerColorOffset = new JSpinner(new SpinnerNumberModel(0, -128, 127, 1));
		final JSpinner spinnerGamma = new JSpinner(new SpinnerNumberModel(3, 0, 3, 0.25));
		final JLabel lblOff = new JLabel("Off");
		final JSpinner offset_left_right_spinner = new JSpinner(new SpinnerNumberModel(0, 0, 400, 2));
		final JSpinner offset_top_bottom_spinner = new JSpinner(new SpinnerNumberModel(140, 0, 400, 2));
		final JComboBox comboBox = new JComboBox(new String[] { "Bicubic", "Bilinear", "None (Slow CPU)" });
		final JComboBox comboBox_2 = new JComboBox(new String[] { "All Zones", "Zone 1", "Zone 2", "Zone 3", "Zone 4" });
		final JCheckBox checkBoxSaturation = new JCheckBox("Saturation");

		comboBox_1.setSelectedIndex(3);
		comboBox_1.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0)
			{

				int oldGroup = Milight.milightGroup;

				switch (comboBox_1.getSelectedIndex())
				{

					case 0:
						Milight.milightGroup = -3;
						break;

					case 1:
						Milight.milightGroup = -2;
						break;

					case 2:
						Milight.milightGroup = -1;
						break;

					case 3:
						Milight.milightGroup = 7;
						break;

					case 4:
						Milight.milightGroup = 8;
						break;

					case 5:
						Milight.milightGroup = 0;
						comboBox_2.setSelectedIndex(0);
						break;

				}

				if (Milight.milightGroup != oldGroup)
				{
					Milight.setDoSaturation(checkBoxSaturation.isSelected(), false);

					if (btnRandom.isEnabled())
					{
						btnRandom.doClick();
					}
					else
					{
						Milight.forceUpdate(true);
					}
				}

			}

		});

		Component verticalStrut = Box.createVerticalStrut(5);
		GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
		gbc_verticalStrut.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut.gridx = 0;
		gbc_verticalStrut.gridy = 0;
		controlPanel.add(verticalStrut, gbc_verticalStrut);

		Component horizontalStrut = Box.createHorizontalStrut(5);
		GridBagConstraints gbc_horizontalStrut = new GridBagConstraints();
		gbc_horizontalStrut.insets = new Insets(0, 0, 5, 5);
		gbc_horizontalStrut.gridx = 0;
		gbc_horizontalStrut.gridy = 1;
		controlPanel.add(horizontalStrut, gbc_horizontalStrut);

		JLabel lblNewLabel = new JLabel("WiFi Bridge");
		lblNewLabel.setFont(fontBold);
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = 1;
		controlPanel.add(lblNewLabel, gbc_lblNewLabel);

		bridgeDevicesComboBox.setSelectedIndex(0);
		bridgeDevicesComboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				Milight.INSTANCE.setDevice(bridgeDevicesComboBox.getSelectedIndex());
			}

		});
		bridgeDevicesComboBox.setFont(fontNorm);
		GridBagConstraints gbc_bridgeDevicesComboBox = new GridBagConstraints();
		gbc_bridgeDevicesComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_bridgeDevicesComboBox.gridwidth = 4;
		gbc_bridgeDevicesComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_bridgeDevicesComboBox.gridx = 2;
		gbc_bridgeDevicesComboBox.gridy = 1;
		controlPanel.add(bridgeDevicesComboBox, gbc_bridgeDevicesComboBox);

		labelPassword = new JLabel("Password: " + Milight.milightPassword);
		labelPassword.setFont(fontNorm);
		labelPassword.setToolTipText("iBox Password");
		labelPassword.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblPassword = new GridBagConstraints();
		gbc_lblPassword.gridwidth = 2;
		gbc_lblPassword.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblPassword.insets = new Insets(0, 0, 5, 5);
		gbc_lblPassword.gridx = 6;
		gbc_lblPassword.gridy = 1;
		controlPanel.add(labelPassword, gbc_lblPassword);

		JLabel lblDevice = new JLabel("Device");
		lblDevice.setFont(fontNorm);
		GridBagConstraints gbc_lblDevice = new GridBagConstraints();
		gbc_lblDevice.anchor = GridBagConstraints.WEST;
		gbc_lblDevice.insets = new Insets(0, 0, 5, 5);
		gbc_lblDevice.gridx = 1;
		gbc_lblDevice.gridy = 2;
		controlPanel.add(lblDevice, gbc_lblDevice);
		comboBox_1.setFont(fontNorm);
		GridBagConstraints gbc_comboBox_1 = new GridBagConstraints();
		gbc_comboBox_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox_1.gridwidth = 3;
		gbc_comboBox_1.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox_1.gridx = 2;
		gbc_comboBox_1.gridy = 2;
		controlPanel.add(comboBox_1, gbc_comboBox_1);

		comboBox_2.setSelectedIndex(0);
		comboBox_2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				int oldZone = Milight.milightZone;

				switch (comboBox_2.getSelectedIndex())
				{

					case 0:
						Milight.milightZone = 0;
						break;

					case 1:
						Milight.milightZone = 1;
						break;

					case 2:
						Milight.milightZone = 2;
						break;

					case 3:
						Milight.milightZone = 3;
						break;

					case 4:
						Milight.milightZone = 4;
						break;

				}

				if (comboBox_2.hasFocus() && Milight.milightZone != oldZone)
				{

					if (btnRandom.isEnabled())
					{
						btnRandom.doClick();
					}
					else
					{
						Milight.forceUpdate(true);
					}

				}

			}

		});
		comboBox_2.setFont(fontNorm);
		GridBagConstraints gbc_comboBox_2 = new GridBagConstraints();
		gbc_comboBox_2.anchor = GridBagConstraints.WEST;
		gbc_comboBox_2.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox_2.gridx = 5;
		gbc_comboBox_2.gridy = 2;
		controlPanel.add(comboBox_2, gbc_comboBox_2);

		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridwidth = 2;
		gbc_panel.insets = new Insets(0, 0, 5, 5);
		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel.gridx = 6;
		gbc_panel.gridy = 2;
		controlPanel.add(panel, gbc_panel);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

		JButton btnOn = new JButton("On");
		btnOn.setFont(fontNorm);
		btnOn.setToolTipText("Switch selected groups On");
		panel.add(btnOn);
		btnOn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				Milight.INSTANCE.switchOnOff(true);
			}
		});

		JButton btnOff = new JButton("Off");
		btnOff.setFont(fontNorm);
		btnOff.setToolTipText("Switch selected groups Off");
		panel.add(btnOff);
		btnOff.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Milight.INSTANCE.switchOnOff(false);
			}
		});

		Component verticalStrut_1 = Box.createVerticalStrut(5);
		GridBagConstraints gbc_verticalStrut_1 = new GridBagConstraints();
		gbc_verticalStrut_1.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut_1.gridx = 1;
		gbc_verticalStrut_1.gridy = 3;
		controlPanel.add(verticalStrut_1, gbc_verticalStrut_1);

		JLabel lblMonitor = new JLabel("Source Display");
		lblMonitor.setFont(fontBold);
		GridBagConstraints gbc_lblMonitor = new GridBagConstraints();
		gbc_lblMonitor.anchor = GridBagConstraints.WEST;
		gbc_lblMonitor.insets = new Insets(0, 0, 5, 5);
		gbc_lblMonitor.gridx = 1;
		gbc_lblMonitor.gridy = 4;
		controlPanel.add(lblMonitor, gbc_lblMonitor);
		lblMonitor.requestFocusInWindow();

		displayDevicesComboBox = new JComboBox<String>();
		displayDevicesComboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				Screen.setDisplay(displayDevicesComboBox.getSelectedIndex());
			}

		});
		displayDevicesComboBox.setFont(fontNorm);
		GridBagConstraints gbc_displayDevicesComboBox = new GridBagConstraints();
		gbc_displayDevicesComboBox.anchor = GridBagConstraints.WEST;
		gbc_displayDevicesComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_displayDevicesComboBox.gridwidth = 2;
		gbc_displayDevicesComboBox.gridx = 2;
		gbc_displayDevicesComboBox.gridy = 4;
		controlPanel.add(displayDevicesComboBox, gbc_displayDevicesComboBox);

		/* Populate list of displaydevices (displayDevicesComboBox), and set selected item */
		updateDisplayDevices();
		
		sampleSquare_spinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e)
			{
				int value = (int) sampleSquare_spinner.getValue();
				value = Math.min(100, Math.max(value, 20));
				sampleSquarePixelsLabel.setText((value * value) + " pixels");
				Screen.pixelCountSample = value;
			}
		});

		JLabel lblPixelsSampled = new JLabel("Sample Size");
		lblPixelsSampled.setToolTipText("Amount of pixels used in sample");
		lblPixelsSampled.setFont(fontNorm);
		GridBagConstraints gbc_lblPixelsSampled = new GridBagConstraints();
		gbc_lblPixelsSampled.anchor = GridBagConstraints.WEST;
		gbc_lblPixelsSampled.insets = new Insets(0, 0, 5, 5);
		gbc_lblPixelsSampled.gridx = 5;
		gbc_lblPixelsSampled.gridy = 4;
		controlPanel.add(lblPixelsSampled, gbc_lblPixelsSampled);
		sampleSquare_spinner.setFont(fontNorm);
		GridBagConstraints gbc_sampleSquare_spinner = new GridBagConstraints();
		gbc_sampleSquare_spinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_sampleSquare_spinner.insets = new Insets(0, 0, 5, 5);
		gbc_sampleSquare_spinner.gridx = 6;
		gbc_sampleSquare_spinner.gridy = 4;
		controlPanel.add(sampleSquare_spinner, gbc_sampleSquare_spinner);

		/* Fix width of sampleSquarePixelsLabel at maximum (containing String "10000 pixels") */
		sampleSquarePixelsLabel.setFont(fontBold);
		sampleSquarePixelsLabel.setPreferredSize(sampleSquarePixelsLabel.getPreferredSize());
		sampleSquarePixelsLabel.setText("3600 pixels");

		GridBagConstraints gbc_sampleSquarePixelsLabel = new GridBagConstraints();
		gbc_sampleSquarePixelsLabel.anchor = GridBagConstraints.WEST;
		gbc_sampleSquarePixelsLabel.insets = new Insets(0, 0, 5, 5);
		gbc_sampleSquarePixelsLabel.gridx = 7;
		gbc_sampleSquarePixelsLabel.gridy = 4;
		controlPanel.add(sampleSquarePixelsLabel, gbc_sampleSquarePixelsLabel);

		JLabel lblTopbottom = new JLabel("Top / Bottom Offset");
		lblTopbottom.setToolTipText("Amount of pixels ignored at top and bottom of the screen");
		lblTopbottom.setFont(fontNorm);
		GridBagConstraints gbc_lblTopbottom = new GridBagConstraints();
		gbc_lblTopbottom.anchor = GridBagConstraints.WEST;
		gbc_lblTopbottom.insets = new Insets(0, 0, 5, 5);
		gbc_lblTopbottom.gridx = 1;
		gbc_lblTopbottom.gridy = 5;
		controlPanel.add(lblTopbottom, gbc_lblTopbottom);
		offset_top_bottom_spinner.setFont(fontNorm);
		offset_top_bottom_spinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e)
			{
				int offset = (int) offset_top_bottom_spinner.getValue();
				offset = Math.min(400, Math.max(0, offset));
				Screen.setRectangleOffsets((int) offset_left_right_spinner.getValue(), offset);
			}
		});

		GridBagConstraints gbc_offset_top_bottom_spinner = new GridBagConstraints();
		gbc_offset_top_bottom_spinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_offset_top_bottom_spinner.insets = new Insets(0, 0, 5, 5);
		gbc_offset_top_bottom_spinner.gridx = 2;
		gbc_offset_top_bottom_spinner.gridy = 5;
		controlPanel.add(offset_top_bottom_spinner, gbc_offset_top_bottom_spinner);

		JLabel lblPixels = new JLabel("pixels");
		lblPixels.setFont(fontNorm);
		GridBagConstraints gbc_lblPixels = new GridBagConstraints();
		gbc_lblPixels.anchor = GridBagConstraints.WEST;
		gbc_lblPixels.insets = new Insets(0, 0, 5, 5);
		gbc_lblPixels.gridx = 3;
		gbc_lblPixels.gridy = 5;
		controlPanel.add(lblPixels, gbc_lblPixels);

		comboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				switch (comboBox.getSelectedIndex())
				{
					case 0:
						Screen.interpolationType = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
						break;
					case 1:
						Screen.interpolationType = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
						break;
					case 2:
						Screen.interpolationType = null;
						break;

				}
			}

		});

		JLabel lblInterpolationMethod = new JLabel("Interpolation");
		lblInterpolationMethod.setToolTipText("Mathematical method used to improve calculation of average color");
		lblInterpolationMethod.setFont(fontNorm);
		GridBagConstraints gbc_lblInterpolationMethod = new GridBagConstraints();
		gbc_lblInterpolationMethod.anchor = GridBagConstraints.WEST;
		gbc_lblInterpolationMethod.insets = new Insets(0, 0, 5, 5);
		gbc_lblInterpolationMethod.gridx = 5;
		gbc_lblInterpolationMethod.gridy = 5;
		controlPanel.add(lblInterpolationMethod, gbc_lblInterpolationMethod);
		comboBox.setFont(fontNorm);
		comboBox.setSelectedIndex(1);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.gridwidth = 2;
		gbc_comboBox.anchor = GridBagConstraints.WEST;
		gbc_comboBox.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox.gridx = 6;
		gbc_comboBox.gridy = 5;
		controlPanel.add(comboBox, gbc_comboBox);

		JLabel lblLeftright = new JLabel("Left / Right Offset");
		lblLeftright.setToolTipText("Amount of pixels ignored at left and right of the screen");
		lblLeftright.setFont(fontNorm);
		GridBagConstraints gbc_lblLeftright = new GridBagConstraints();
		gbc_lblLeftright.anchor = GridBagConstraints.WEST;
		gbc_lblLeftright.insets = new Insets(0, 0, 5, 5);
		gbc_lblLeftright.gridx = 1;
		gbc_lblLeftright.gridy = 6;
		controlPanel.add(lblLeftright, gbc_lblLeftright);
		offset_left_right_spinner.setFont(fontNorm);
		offset_left_right_spinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e)
			{
				int offset = (int) offset_left_right_spinner.getValue();
				offset = Math.min(400, Math.max(0, offset));
				Screen.setRectangleOffsets(offset, (int) offset_top_bottom_spinner.getValue());
			}
		});

		GridBagConstraints gbc_offset_left_right_spinner = new GridBagConstraints();
		gbc_offset_left_right_spinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_offset_left_right_spinner.insets = new Insets(0, 0, 5, 5);
		gbc_offset_left_right_spinner.gridx = 2;
		gbc_offset_left_right_spinner.gridy = 6;
		controlPanel.add(offset_left_right_spinner, gbc_offset_left_right_spinner);

		JLabel label = new JLabel("pixels");
		label.setFont(fontNorm);
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.WEST;
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 3;
		gbc_label.gridy = 6;
		controlPanel.add(label, gbc_label);

		lblOff.setVisible(false);

		spinnerGamma.setFont(fontNorm);
		spinnerGamma.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0)
			{
				double gammaValue = (double) spinnerGamma.getValue();
				gammaValue = Math.min(3, Math.max(0, gammaValue)) + 1;
				lblOff.setVisible(gammaValue == 1);
				Screen.setGammaTable(gammaValue);
			}

		});

		JLabel lblGamma = new JLabel("Gamma Correction");
		lblGamma.setToolTipText("Gamma correction factor applied to sample");
		lblGamma.setFont(fontNorm);
		GridBagConstraints gbc_lblGamma = new GridBagConstraints();
		gbc_lblGamma.anchor = GridBagConstraints.WEST;
		gbc_lblGamma.insets = new Insets(0, 0, 5, 5);
		gbc_lblGamma.gridx = 5;
		gbc_lblGamma.gridy = 6;
		controlPanel.add(lblGamma, gbc_lblGamma);
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner.insets = new Insets(0, 0, 5, 5);
		gbc_spinner.gridx = 6;
		gbc_spinner.gridy = 6;
		controlPanel.add(spinnerGamma, gbc_spinner);
		lblOff.setFont(fontBold);
		GridBagConstraints gbc_lblOff = new GridBagConstraints();
		gbc_lblOff.anchor = GridBagConstraints.WEST;
		gbc_lblOff.insets = new Insets(0, 0, 5, 5);
		gbc_lblOff.gridx = 7;
		gbc_lblOff.gridy = 6;
		controlPanel.add(lblOff, gbc_lblOff);

		JLabel lblColorOffset = new JLabel("Color Offset");
		lblColorOffset.setToolTipText("Shift colortones");
		lblColorOffset.setFont(fontNorm);
		GridBagConstraints gbc_lblColorOffset = new GridBagConstraints();
		gbc_lblColorOffset.anchor = GridBagConstraints.WEST;
		gbc_lblColorOffset.insets = new Insets(0, 0, 5, 5);
		gbc_lblColorOffset.gridx = 1;
		gbc_lblColorOffset.gridy = 7;
		controlPanel.add(lblColorOffset, gbc_lblColorOffset);

		spinnerColorOffset.setFont(fontNorm);
		spinnerColorOffset.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e)
			{
				int colorOffset = 128 + (int) spinnerColorOffset.getValue();
				colorOffset = Math.min(256, Math.max(0, colorOffset));
				Screen.colorOffset = colorOffset;
				Milight.forceUpdate(false);
			}
		});
		GridBagConstraints gbc_spinner_1 = new GridBagConstraints();
		gbc_spinner_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner_1.insets = new Insets(0, 0, 5, 5);
		gbc_spinner_1.gridx = 2;
		gbc_spinner_1.gridy = 7;
		controlPanel.add(spinnerColorOffset, gbc_spinner_1);

		JLabel lblUseSampleBlurring = new JLabel("Sample Blurring");
		lblUseSampleBlurring.setToolTipText("Blur sampled image for color averaging");
		lblUseSampleBlurring.setFont(fontNorm);
		GridBagConstraints gbc_lblUseSampleBlurring = new GridBagConstraints();
		gbc_lblUseSampleBlurring.anchor = GridBagConstraints.WEST;
		gbc_lblUseSampleBlurring.insets = new Insets(0, 0, 5, 5);
		gbc_lblUseSampleBlurring.gridx = 5;
		gbc_lblUseSampleBlurring.gridy = 7;
		controlPanel.add(lblUseSampleBlurring, gbc_lblUseSampleBlurring);

		final JCheckBox checkBoxSampleBlur = new JCheckBox();
		GridBagConstraints gbc_checkBox_1 = new GridBagConstraints();
		gbc_checkBox_1.anchor = GridBagConstraints.WEST;
		gbc_checkBox_1.insets = new Insets(0, 0, 5, 5);
		gbc_checkBox_1.gridx = 6;
		gbc_checkBox_1.gridy = 7;
		controlPanel.add(checkBoxSampleBlur, gbc_checkBox_1);
		checkBoxSampleBlur.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Screen.useBlur = checkBoxSampleBlur.isSelected();
			}
		});

		JLabel lblSampledFramesPer = new JLabel("Frames per Second");
		lblSampledFramesPer.setToolTipText("Maximum amount of frames per second to analyze");
		lblSampledFramesPer.setFont(fontNorm);
		GridBagConstraints gbc_lblSampledFramesPer = new GridBagConstraints();
		gbc_lblSampledFramesPer.anchor = GridBagConstraints.WEST;
		gbc_lblSampledFramesPer.insets = new Insets(0, 0, 5, 5);
		gbc_lblSampledFramesPer.gridx = 1;
		gbc_lblSampledFramesPer.gridy = 8;
		controlPanel.add(lblSampledFramesPer, gbc_lblSampledFramesPer);

		final JSpinner spinnerFramesPerSecond = new JSpinner(new SpinnerNumberModel(20, 1, 30, 1));
		spinnerFramesPerSecond.setFont(fontNorm);
		GridBagConstraints gbc_spinner_2 = new GridBagConstraints();
		gbc_spinner_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner_2.insets = new Insets(0, 0, 5, 5);
		gbc_spinner_2.gridx = 2;
		gbc_spinner_2.gridy = 8;
		controlPanel.add(spinnerFramesPerSecond, gbc_spinner_2);
		spinnerFramesPerSecond.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e)
			{
				Screen.fpsInterval = 1000 / (int) spinnerFramesPerSecond.getValue();
			}
		});

		JLabel lblChromaOffset = new JLabel("Min. Chroma Offset");
		lblChromaOffset.setToolTipText("Minimum difference between Red, Green and Blue color values (Avoids dramatic color changes on near-achromatic colors)");
		lblChromaOffset.setFont(fontNorm);
		GridBagConstraints gbc_lblChromaOffset = new GridBagConstraints();
		gbc_lblChromaOffset.anchor = GridBagConstraints.WEST;
		gbc_lblChromaOffset.insets = new Insets(0, 0, 5, 5);
		gbc_lblChromaOffset.gridx = 5;
		gbc_lblChromaOffset.gridy = 8;
		controlPanel.add(lblChromaOffset, gbc_lblChromaOffset);

		final JSpinner spinnerChroma = new JSpinner(new SpinnerNumberModel(4, 1, 20, 1));
		spinnerChroma.setFont(fontNorm);
		GridBagConstraints gbc_spinner_chroma = new GridBagConstraints();
		gbc_spinner_chroma.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner_chroma.insets = new Insets(0, 0, 5, 5);
		gbc_spinner_chroma.gridx = 6;
		gbc_spinner_chroma.gridy = 8;
		controlPanel.add(spinnerChroma, gbc_spinner_chroma);
		spinnerChroma.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0)
			{
				Screen.chromaOffset = (int) spinnerChroma.getValue();
			}
		});

		JLabel lblMinCommandOffset = new JLabel("Min. Cmd. Offset");
		lblMinCommandOffset.setToolTipText("Minimum difference between last and new command to be submitted to bulbs (Avoids flickering of near-identical color, brightness and saturation values)");
		lblMinCommandOffset.setFont(fontNorm);
		GridBagConstraints gbc_lblMinCommandOffset = new GridBagConstraints();
		gbc_lblMinCommandOffset.anchor = GridBagConstraints.WEST;
		gbc_lblMinCommandOffset.insets = new Insets(0, 0, 5, 5);
		gbc_lblMinCommandOffset.gridx = 1;
		gbc_lblMinCommandOffset.gridy = 9;
		controlPanel.add(lblMinCommandOffset, gbc_lblMinCommandOffset);

		final JSpinner spinnerCommandOffset = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));
		spinnerCommandOffset.setFont(fontNorm);
		GridBagConstraints gbc_spinnerLampOffset = new GridBagConstraints();
		gbc_spinnerLampOffset.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinnerLampOffset.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerLampOffset.gridx = 2;
		gbc_spinnerLampOffset.gridy = 9;
		controlPanel.add(spinnerCommandOffset, gbc_spinnerLampOffset);
		spinnerCommandOffset.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e)
			{
				Milight.offsetFactor = (int) spinnerCommandOffset.getValue();
			}
		});

		Component verticalStrut_2 = Box.createVerticalStrut(5);
		GridBagConstraints gbc_verticalStrut_2 = new GridBagConstraints();
		gbc_verticalStrut_2.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut_2.gridx = 1;
		gbc_verticalStrut_2.gridy = 10;
		controlPanel.add(verticalStrut_2, gbc_verticalStrut_2);

		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel_1.gridwidth = 3;
		gbc_panel_1.insets = new Insets(0, 0, 5, 5);
		gbc_panel_1.gridx = 1;
		gbc_panel_1.gridy = 11;

		final JCheckBox checkBoxBrightnessLock = new JCheckBox("WiFi Bridge Brightness always 100%");
		checkBoxBrightnessLock.setFont(fontNorm);
		checkBoxBrightnessLock.setSelected(true);
		checkBoxBrightnessLock.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Milight.bridgeBrightnessLock = checkBoxBrightnessLock.isSelected();
				if ((Milight.controlFlags & Milight.MILIGHT_DOBRGHT) == Milight.MILIGHT_DOBRGHT)
					Milight.forceUpdate(true);
			}
		});
		controlPanel.add(checkBoxBrightnessLock, gbc_panel_1);

		final JCheckBox checkBoxColor = new JCheckBox("Color");
		checkBoxColor.setFont(fontNorm);
		checkBoxColor.setSelected(true);
		checkBoxColor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				boolean selected = checkBoxColor.isSelected();
				Milight.setDoColor(selected);
				btnRandom.setEnabled(!fRandom && selected);
			}
		});

		GridBagConstraints gbc_chckbxColor = new GridBagConstraints();
		gbc_chckbxColor.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxColor.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxColor.gridx = 5;
		gbc_chckbxColor.gridy = 11;
		controlPanel.add(checkBoxColor, gbc_chckbxColor);

		Component horizontalStrut_1 = Box.createHorizontalStrut(2);
		GridBagConstraints gbc_horizontalStrut_1 = new GridBagConstraints();
		gbc_horizontalStrut_1.insets = new Insets(0, 0, 5, 5);
		gbc_horizontalStrut_1.gridx = 4;
		gbc_horizontalStrut_1.gridy = 12;
		controlPanel.add(horizontalStrut_1, gbc_horizontalStrut_1);

		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel_2.insets = new Insets(0, 0, 5, 5);
		gbc_panel_2.gridx = 5;
		gbc_panel_2.gridy = 12;

		final JCheckBox checkBoxBrightness = new JCheckBox("Brightness");
		checkBoxBrightness.setFont(fontNorm);
		checkBoxBrightness.setSelected(true);
		checkBoxBrightness.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Milight.setDoBrightness(checkBoxBrightness.isSelected());
			}
		});
		controlPanel.add(checkBoxBrightness, gbc_panel_2);

		Component horizontalStrut_2 = Box.createHorizontalStrut(5);
		GridBagConstraints gbc_horizontalStrut_2 = new GridBagConstraints();
		gbc_horizontalStrut_2.insets = new Insets(0, 0, 5, 0);
		gbc_horizontalStrut_2.gridx = 8;
		gbc_horizontalStrut_2.gridy = 12;
		controlPanel.add(horizontalStrut_2, gbc_horizontalStrut_2);

		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.insets = new Insets(0, 0, 5, 5);
		gbc_panel_3.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel_3.gridx = 5;
		gbc_panel_3.gridy = 13;

		final JCheckBox checkBoxEmulateRGBW = new JCheckBox("Saturate like RGBW");
		checkBoxSaturation.setFont(fontNorm);
		checkBoxSaturation.setSelected(true);
		checkBoxSaturation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				boolean selected = checkBoxSaturation.isSelected();
				Milight.setDoSaturation(selected, true);
				checkBoxEmulateRGBW.setEnabled(selected);
			}
		});

		JLabel lblSaturationControlFor = new JLabel("RGBWW bulbs only:");
		lblSaturationControlFor.setFont(fontNorm);
		GridBagConstraints gbc_lblSaturationControlFor = new GridBagConstraints();
		gbc_lblSaturationControlFor.anchor = GridBagConstraints.EAST;
		gbc_lblSaturationControlFor.gridwidth = 4;
		gbc_lblSaturationControlFor.insets = new Insets(0, 0, 5, 5);
		gbc_lblSaturationControlFor.gridx = 1;
		gbc_lblSaturationControlFor.gridy = 13;
		controlPanel.add(lblSaturationControlFor, gbc_lblSaturationControlFor);
		controlPanel.add(checkBoxSaturation, gbc_panel_3);

		checkBoxEmulateRGBW.setFont(fontNorm);
		checkBoxEmulateRGBW.setToolTipText("Keeps colors equal in an RGBW + RGBWW setup");
		GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
		gbc_chckbxNewCheckBox.anchor = GridBagConstraints.WEST;
		gbc_chckbxNewCheckBox.gridwidth = 2;
		gbc_chckbxNewCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxNewCheckBox.gridx = 6;
		gbc_chckbxNewCheckBox.gridy = 13;
		checkBoxEmulateRGBW.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				Milight.setEmulateRGBWSaturation(checkBoxEmulateRGBW.isSelected());
			}
		});
		controlPanel.add(checkBoxEmulateRGBW, gbc_chckbxNewCheckBox);

		Component verticalStrut_3 = Box.createVerticalStrut(5);
		GridBagConstraints gbc_verticalStrut_3 = new GridBagConstraints();
		gbc_verticalStrut_3.insets = new Insets(0, 0, 0, 5);
		gbc_verticalStrut_3.gridx = 1;
		gbc_verticalStrut_3.gridy = 14;
		controlPanel.add(verticalStrut_3, gbc_verticalStrut_3);

		samplePanel = new JPanel();
		getContentPane().add(samplePanel, BorderLayout.SOUTH);
		samplePanel.setLayout(new BoxLayout(samplePanel, BoxLayout.X_AXIS));

		JPanel panelLeft = new JPanel();
		panelLeft.setOpaque(false);
		samplePanel.add(panelLeft);
		SpringLayout sl_panelLeft = new SpringLayout();
		panelLeft.setLayout(sl_panelLeft);

		lblR = new JLabel("R");
		lblR.setFont(fontBold);
		sl_panelLeft.putConstraint(SpringLayout.NORTH, lblR, 10, SpringLayout.NORTH, panelLeft);
		sl_panelLeft.putConstraint(SpringLayout.WEST, lblR, 10, SpringLayout.WEST, panelLeft);
		panelLeft.add(lblR);

		lblG = new JLabel("G");
		lblG.setFont(fontBold);
		sl_panelLeft.putConstraint(SpringLayout.NORTH, lblG, 6, SpringLayout.SOUTH, lblR);
		sl_panelLeft.putConstraint(SpringLayout.WEST, lblG, 10, SpringLayout.WEST, panelLeft);
		panelLeft.add(lblG);

		lblB = new JLabel("B");
		lblB.setFont(fontBold);
		sl_panelLeft.putConstraint(SpringLayout.NORTH, lblB, 6, SpringLayout.SOUTH, lblG);
		sl_panelLeft.putConstraint(SpringLayout.WEST, lblB, 10, SpringLayout.WEST, panelLeft);
		panelLeft.add(lblB);

		lblColor = new JLabel("Color");
		lblColor.setFont(fontBold);
		sl_panelLeft.putConstraint(SpringLayout.NORTH, lblColor, 6, SpringLayout.SOUTH, lblB);
		sl_panelLeft.putConstraint(SpringLayout.WEST, lblColor, 10, SpringLayout.WEST, panelLeft);
		panelLeft.add(lblColor);

		sampleColor = new JLabel(Integer.toString(Milight.newColor));
		sl_panelLeft.putConstraint(SpringLayout.NORTH, sampleColor, 0, SpringLayout.NORTH, lblColor);
		sampleColor.setFont(fontNorm);
		panelLeft.add(sampleColor);

		sampleBlue = new JLabel();
		sl_panelLeft.putConstraint(SpringLayout.NORTH, sampleBlue, 0, SpringLayout.NORTH, lblB);
		sl_panelLeft.putConstraint(SpringLayout.WEST, sampleBlue, 0, SpringLayout.WEST, sampleColor);
		sampleBlue.setFont(fontNorm);
		panelLeft.add(sampleBlue);

		sampleGreen = new JLabel();
		sl_panelLeft.putConstraint(SpringLayout.NORTH, sampleGreen, 0, SpringLayout.NORTH, lblG);
		sl_panelLeft.putConstraint(SpringLayout.WEST, sampleGreen, 0, SpringLayout.WEST, sampleColor);
		sampleGreen.setFont(fontNorm);
		panelLeft.add(sampleGreen);

		sampleRed = new JLabel();
		sl_panelLeft.putConstraint(SpringLayout.NORTH, sampleRed, 0, SpringLayout.NORTH, lblR);
		sl_panelLeft.putConstraint(SpringLayout.WEST, sampleRed, 0, SpringLayout.WEST, sampleColor);
		sampleRed.setFont(fontNorm);
		panelLeft.add(sampleRed);

		lblBr = new JLabel("Brightness");
		sl_panelLeft.putConstraint(SpringLayout.NORTH, lblBr, 6, SpringLayout.SOUTH, lblColor);
		sl_panelLeft.putConstraint(SpringLayout.WEST, lblBr, 0, SpringLayout.WEST, lblR);
		lblBr.setFont(fontBold);
		panelLeft.add(lblBr);

		sampleBrightness = new JLabel(Integer.toString(Milight.newBrightness));
		sl_panelLeft.putConstraint(SpringLayout.WEST, sampleBrightness, 16, SpringLayout.EAST, lblBr);
		sl_panelLeft.putConstraint(SpringLayout.WEST, sampleColor, 0, SpringLayout.WEST, sampleBrightness);
		sl_panelLeft.putConstraint(SpringLayout.NORTH, sampleBrightness, 0, SpringLayout.NORTH, lblBr);
		sampleBrightness.setFont(fontNorm);
		panelLeft.add(sampleBrightness);

		btnRandom = new JButton("Random");
		sl_panelLeft.putConstraint(SpringLayout.WEST, btnRandom, 0, SpringLayout.WEST, lblR);
		panelLeft.add(btnRandom);
		btnRandom.setOpaque(false);
		btnRandom.setToolTipText("Set a random color; Only available when sampled Red Green and Blue values are identical and therefore no specific non-achromatic color value can be assigned.");
		btnRandom.setFont(fontNorm);

		chckbxShowCorrectedSample = new JCheckBox("Show corrected sample");
		sl_panelLeft.putConstraint(SpringLayout.NORTH, chckbxShowCorrectedSample, 6, SpringLayout.SOUTH, btnRandom);
		sl_panelLeft.putConstraint(SpringLayout.WEST, chckbxShowCorrectedSample, 0, SpringLayout.WEST, lblR);
		chckbxShowCorrectedSample.setFont(fontNorm);
		chckbxShowCorrectedSample.setOpaque(false);
		panelLeft.add(chckbxShowCorrectedSample);

		lblSt = new JLabel("Saturation");
		sl_panelLeft.putConstraint(SpringLayout.NORTH, btnRandom, 6, SpringLayout.SOUTH, lblSt);
		sl_panelLeft.putConstraint(SpringLayout.NORTH, lblSt, 6, SpringLayout.SOUTH, lblBr);
		sl_panelLeft.putConstraint(SpringLayout.WEST, lblSt, 0, SpringLayout.WEST, lblR);
		lblSt.setFont(fontBold);
		panelLeft.add(lblSt);

		sampleSaturation = new JLabel("0");
		sl_panelLeft.putConstraint(SpringLayout.NORTH, sampleSaturation, 0, SpringLayout.NORTH, lblSt);
		sl_panelLeft.putConstraint(SpringLayout.WEST, sampleSaturation, 0, SpringLayout.WEST, sampleColor);
		sampleSaturation.setFont(fontNorm);
		panelLeft.add(sampleSaturation);

		chckbxShowCorrectedSample.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Screen.showCorrectedSample = chckbxShowCorrectedSample.isSelected();
			}
		});

		btnRandom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				Milight.setRandomColor();
			}
		});

		Box sampleDisplayBox = Box.createVerticalBox();
		samplePanel.add(sampleDisplayBox);

		Component verticalSpacerOne = Box.createVerticalStrut(5);
		sampleDisplayBox.add(verticalSpacerOne);

		sampleImage = new JLabel();
		sampleDisplayBox.add(sampleImage);
		sampleImage.setPreferredSize(new Dimension(200, 200));

		Component verticalSpacerTwo = Box.createVerticalStrut(5);
		sampleDisplayBox.add(verticalSpacerTwo);

		JPanel panelRight = new JPanel();
		panelRight.setOpaque(false);
		samplePanel.add(panelRight);
		panelRight.setLayout(new SpringLayout());

		pack();

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);

		setVisible(true);

	}

	public void updateDisplayDevices()
	{
		int selectedIndex = 0;

		String selectedDevice = Screen.getSelectedDisplayDevice();
		
		/* We'll just assume one single actionlistener here, as we've only added one in the constructor */
		ActionListener[] listeners = displayDevicesComboBox.getActionListeners();
		displayDevicesComboBox.removeActionListener(listeners[0]);
		
		displayDevicesComboBox.removeAllItems();

		for (int i = 0; i < Screen.displayDevices.size(); i++)
		{
			DisplayDevice device = Screen.displayDevices.get(i);

			displayDevicesComboBox.addItem(device.humanReadable);

			if (selectedDevice != null && device.id.equals(selectedDevice))
			{
				selectedIndex = i;
			}

		}

		displayDevicesComboBox.setSelectedIndex(selectedIndex);
		
		displayDevicesComboBox.addActionListener(listeners[0]);
		
	}

	private void setMenu()
	{
		JMenuBar menuBar = new JMenuBar();

		JMenu menu = new JMenu("Menu");
		menu.setMnemonic(KeyEvent.VK_M);
		menu.getAccessibleContext().setAccessibleDescription("Menu");

		JMenuItem menuItem;

		String OS = System.getProperty("os.name").toLowerCase();
		if (!(OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0) && getToolkit().isFrameStateSupported(Frame.ICONIFIED))
		{

			menuItem = new JMenuItem("Send to Tray", KeyEvent.VK_T) {

				private static final long serialVersionUID = 1L;

				@Override
				public Dimension getPreferredSize()
				{
					Dimension d = super.getPreferredSize();
					d.width = Math.max(d.width, 120); // set minimum
					return d;
				}

			};
			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, 0));
			menuItem.getAccessibleContext().setAccessibleDescription("Send to Tray");
			menuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent event)
				{
					setExtendedState(Frame.ICONIFIED);
				}

			});

			menu.add(menuItem);

			menu.addSeparator();
		}

		menuItem = new JMenuItem("Quit", KeyEvent.VK_Q);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0));
		menuItem.getAccessibleContext().setAccessibleDescription("Quit");
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event)
			{
				System.exit(0);
			}

		});

		menu.add(menuItem);

		menuBar.add(menu);

		menuBar.add(Box.createHorizontalGlue());

		menu = new JMenu("Help");
		menu.setMnemonic(KeyEvent.VK_H);
		menu.getAccessibleContext().setAccessibleDescription("Help");

		menuItem = new JMenuItem("Help", KeyEvent.VK_H) {

			private static final long serialVersionUID = 1L;

			@Override
			public Dimension getPreferredSize()
			{
				Dimension d = super.getPreferredSize();
				d.width = Math.max(d.width, 120); // set minimum
				return d;
			}

		};
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, 0));
		menuItem.getAccessibleContext().setAccessibleDescription("Help");
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event)
			{

				JOptionPane.showMessageDialog(null, "Controller for Milight / Applamp / LimitlessLED v6\n\nRequires a Milight WiFi iBox1 (v6)\n\nMatches screen color to lamp(s).\n\nVersion " + WindowHelper.appVersion, WindowHelper.appTitle, JOptionPane.INFORMATION_MESSAGE);

			}

		});

		menu.add(menuItem);

		menuBar.add(menu);

		setJMenuBar(menuBar);
	}

	/* Note: 05.40: Yes yes, I am not using a viewholder, and I am lazily passing bufferedimages into setSample . Could obviously be improved.
	 * Even, if it's just a little bit.
	 */

	/* Declared static; Avoid synthetic accessor methods */
	private static boolean fGround;
	private static boolean fRandom;

	public void setSample(int r, int g, int b, ImageIcon img, boolean chromatic)
	{

		if (fGround != (int) ((0.2126 * r) + (0.7152 * g) + (0.0722 * b)) <= 85)
		{
			fGround = !fGround;

			Color foreground = new Color(fGround ? 0xffaeaeae : 0xff000000);

			lblR.setForeground(foreground);
			sampleRed.setForeground(foreground);
			lblG.setForeground(foreground);
			sampleGreen.setForeground(foreground);
			lblB.setForeground(foreground);
			sampleBlue.setForeground(foreground);
			lblColor.setForeground(foreground);
			sampleColor.setForeground(foreground);
			lblBr.setForeground(foreground);
			sampleBrightness.setForeground(foreground);
			lblSt.setForeground(foreground);
			sampleSaturation.setForeground(foreground);
			chckbxShowCorrectedSample.setForeground(foreground);
		}

		sampleImage.setIcon(img);
		samplePanel.setBackground(new Color(r, g, b));
		sampleRed.setText(Integer.toString(r));
		sampleGreen.setText(Integer.toString(g));
		sampleBlue.setText(Integer.toString(b));
		sampleColor.setText(Integer.toString(Milight.newColor));
		sampleBrightness.setText(Integer.toString(Milight.newBrightness));
		sampleSaturation.setText(Integer.toString(Milight.newSaturation));

		if (fRandom != chromatic)
		{
			fRandom = chromatic;
			btnRandom.setEnabled(!chromatic && (Milight.controlFlags & Milight.MILIGHT_DOCOLOR) == Milight.MILIGHT_DOCOLOR);
		}

	}

	public void updatePassword()
	{
		labelPassword.setText("Password: " + Milight.milightPassword);
	}

	@Override
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		/* Store current visibility state in a static variable, avoiding calls to 'isVisible' method from Screen worker;
		 * That is, assuming accessing statics is faster -- even if only by a tiny bit. */
		VISIBLE = visible;
	}
}