package com.jaspergoes.colorland.filters;

import java.awt.image.BufferedImage;
import java.awt.image.Kernel;

/*
** Copyright 2005 Huxtable.com. All rights reserved.
*/

/**
 * A filter which applies Gaussian blur to an image. This is a subclass of
 * ConvolveFilter which simply creates a kernel with a Gaussian distribution for
 * blurring.
 * 
 * @author Jerry Huxtable
 */
public class GaussianFilter extends AbstractBufferedImageOp
{

	static final long serialVersionUID = 1L;

	protected Kernel kernel;

	public static int ZERO_EDGES = 0;
	public static int CLAMP_EDGES = 1;
	public static int WRAP_EDGES = 2;

	/**
	 * Construct a Gaussian filter
	 * 
	 * @param radius
	 *            blur radius in pixels
	 */
	public GaussianFilter(float radius)
	{
		setRadius(radius);
	}

	/**
	 * Set the radius of the kernel, and hence the amount of blur. The bigger
	 * the radius, the longer this filter will take.
	 * 
	 * @param radius
	 *            the radius of the blur in pixels.
	 */
	public void setRadius(float radius)
	{
		kernel = makeKernel(radius);
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dst)
	{
		int width = src.getWidth();
		int height = src.getHeight();

		if (dst == null) dst = createCompatibleDestImage(src, null);

		int[] inPixels = new int[width * height];
		int[] outPixels = new int[width * height];
		src.getRGB(0, 0, width, height, inPixels, 0, width);

		convolveAndTranspose(kernel, inPixels, outPixels, width, height, CLAMP_EDGES);
		convolveAndTranspose(kernel, outPixels, inPixels, height, width, CLAMP_EDGES);

		dst.setRGB(0, 0, width, height, inPixels, 0, width);
		return dst;
	}

	public static void convolveAndTranspose(Kernel kernel, int[] inPixels, int[] outPixels, int width, int height, int edgeAction)
	{
		float[] matrix = kernel.getKernelData(null);
		int cols = kernel.getWidth();
		int cols2 = cols / 2;

		for (int y = 0; y < height; y++)
		{
			int index = y;
			int ioffset = y * width;
			for (int x = 0; x < width; x++)
			{
				float r = 0, g = 0, b = 0;
				int moffset = cols2;
				for (int col = -cols2; col <= cols2; col++)
				{
					float f = matrix[moffset + col];

					if (f != 0)
					{
						int ix = x + col;
						if (ix < 0)
						{
							if (edgeAction == CLAMP_EDGES)
								ix = 0;
							else if (edgeAction == WRAP_EDGES)
								ix = (x + width) % width;
						}
						else if (ix >= width)
						{
							if (edgeAction == CLAMP_EDGES)
								ix = width - 1;
							else if (edgeAction == WRAP_EDGES)
								ix = (x + width) % width;
						}
						int rgb = inPixels[ioffset + ix];
						r += f * ((rgb >> 16) & 0xff);
						g += f * ((rgb >> 8) & 0xff);
						b += f * (rgb & 0xff);
					}
				}
				int ir = (ir = (int) (r + 0.5)) < 0 ? 0 : (ir > 255 ? 255 : ir);
				int ig = (ig = (int) (g + 0.5)) < 0 ? 0 : (ig > 255 ? 255 : ig);
				int ib = (ib = (int) (b + 0.5)) < 0 ? 0 : (ib > 255 ? 255 : ib);
				outPixels[index] = (ir << 16) | (ig << 8) | ib;
				index += height;
			}
		}
	}

	/**
	 * Make a Gaussian blur kernel.
	 */
	public static Kernel makeKernel(float radius)
	{
		int r = (int) Math.ceil(radius);
		int rows = r * 2 + 1;
		float[] matrix = new float[rows];
		float sigma = radius / 3;
		float sigma22 = 2 * sigma * sigma;
		float sigmaPi2 = 2 * ((float) Math.PI) * sigma;
		float sqrtSigmaPi2 = (float) Math.sqrt(sigmaPi2);
		float radius2 = radius * radius;
		float total = 0;
		int index = 0;
		for (int row = -r; row <= r; row++)
		{
			float distance = row * row;
			if (distance > radius2)
				matrix[index] = 0;
			else matrix[index] = (float) Math.exp(-(distance) / sigma22) / sqrtSigmaPi2;
			total += matrix[index];
			index++;
		}
		for (int i = 0; i < rows; i++)
			matrix[i] /= total;

		return new Kernel(rows, 1, matrix);
	}

	@Override
	public String toString()
	{
		return "Blur/Gaussian Blur...";
	}
}