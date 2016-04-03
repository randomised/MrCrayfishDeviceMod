package com.mrcrayfish.device.object;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;

public class Picture 
{
	private String name;
	private String author;
	public int[][] pixels;
	public Size size;
	
	public Picture(String name, String author, Size size) 
	{
		this.name = name;
		this.author = author;
		this.pixels = new int[size.width][size.height];
		this.size = size;
	}

	public String getName() 
	{
		return name;
	}
	
	public String getAuthor() 
	{
		return author;
	}
	
	public int[][] getPixels() 
	{
		return pixels;
	}
	
	public int getWidth()
	{
		return size.width;
	}
	
	public int getHeight()
	{
		return size.height;
	}
	
	public int getPixelWidth()
	{
		return size.pixelWidth;
	}
	
	public int getPixelHeight()
	{
		return size.pixelHeight;
	}
	
	@Override
	public String toString() 
	{
		return name;
	}
	
	public void writeToNBT(NBTTagCompound tagCompound)
	{
		tagCompound.setString("Name", getName());
		tagCompound.setString("Author", getAuthor());
		
		NBTTagList pixelList = new NBTTagList();
		for(int i = 0; i < getHeight(); i++) {
			pixelList.appendTag(new NBTTagIntArray(pixels[i]));
		}
		tagCompound.setTag("Pixels", pixelList);
	}
	
	public static Picture readFromNBT(NBTTagCompound tagCompound)
	{
		NBTTagList pixelList = (NBTTagList) tagCompound.getTag("Pixels");
		Size size = Size.getFromSize(pixelList.tagCount());
		Picture picture = new Picture(tagCompound.getString("Name"), tagCompound.getString("Author"), size);
		
		picture.pixels = new int[size.width][size.height];
		for(int i = 0; i < pixelList.tagCount(); i++)
		{
			picture.pixels[i] = pixelList.getIntArrayAt(i);
		}
		
		return picture;
	}
	
	public static enum Size 
	{
		X16(16, 16, 8, 8), X32(32, 32, 4, 4);
		
		public int width, height;
		public int pixelWidth, pixelHeight;
		
		Size(int width, int height, int pixelWidth, int pixelHeight)
		{
			this.width = width;
			this.height = height;
			this.pixelWidth = pixelWidth;
			this.pixelHeight = pixelHeight;
		}
		
		public static Size getFromSize(int size)
		{
			if(size == 16) return X16;
			return X32;
		}
	}
}