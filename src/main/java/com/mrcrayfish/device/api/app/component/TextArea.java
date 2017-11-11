package com.mrcrayfish.device.api.app.component;

import com.mrcrayfish.device.api.app.Component;
import com.mrcrayfish.device.core.Laptop;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.input.Keyboard;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class TextArea extends Component
{
	protected FontRenderer fontRendererObj;

	protected List<String> lines = new ArrayList<>();
	protected String placeholder = null;
	protected int width, height;
	protected int visibleLines;
	protected int lineScrollOffset;
	protected int padding = 4;
	protected int cursorTick = 0;
	protected int cursorX;
	protected int cursorY;
	protected int relativeCursorY;
	protected boolean isFocused = false;
	protected boolean editable = true;
	protected boolean wrapText = false;

	/* Personalisation */
	protected int placeholderColour = new Color(1.0F, 1.0F, 1.0F, 0.35F).getRGB();
	protected int textColour = Color.WHITE.getRGB();
	protected int backgroundColour = Color.DARK_GRAY.getRGB();
	protected int borderColour = Color.BLACK.getRGB();

	/**
	 * Default text area constructor
	 *
	 * @param left how many pixels from the left
	 * @param top how many pixels from the top
	 * @param width the width of the text area
	 * @param height the height of the text area
	 */
	public TextArea(int left, int top, int width, int height)
	{
		super(left, top);
		this.fontRendererObj = Laptop.fontRenderer;
		this.width = width;
		this.height = height;
		this.visibleLines = (int) Math.floor((height - padding * 2) / fontRendererObj.FONT_HEIGHT);
		this.lines.add("");
	}

	@Override
	public void handleTick()
	{
		cursorTick++;
	}

	@Override
	public void render(Laptop laptop, Minecraft mc, int x, int y, int mouseX, int mouseY, boolean windowActive, float partialTicks)
	{
		if (this.visible)
		{
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			Gui.drawRect(xPosition, yPosition, xPosition + width, yPosition + height, borderColour);
			Gui.drawRect(xPosition + 1, yPosition + 1, xPosition + width - 1, yPosition + height - 1, backgroundColour);

			if(!isFocused && placeholder != null && (lines.isEmpty() || (lines.size() == 1 && lines.get(0).isEmpty())))
			{
				GlStateManager.enableBlend();
				mc.fontRendererObj.drawSplitString(placeholder, x + padding + 1, y + padding + 2, width - padding * 2 - 2, placeholderColour);
			}

			//Word wrapping
			/*int remainingLines = visibleLines;
			for(int i = 0; i < visibleLines && i < lines.size() && i < remainingLines; i++)
			{
				List<String> subLines = fontRendererObj.listFormattedStringToWidth(lines.get(lineScrollOffset + i), width - padding * 2);
				int posX = x + padding;
				for(int j = 0; j < subLines.size() && j < remainingLines; j++)
				{
					int posY = y + padding + i * fontRendererObj.FONT_HEIGHT + j * fontRendererObj.FONT_HEIGHT + (visibleLines - remainingLines) * fontRendererObj.FONT_HEIGHT;
					fontRendererObj.drawSplitString(subLines.get(j), posX, posY, width - padding * 2, textColour);
				}
				if(subLines.size() > 1) remainingLines -= subLines.size();
			}*/

			//original
			for(int i = 0; i < visibleLines && i + lineScrollOffset < lines.size(); i++)
			{
				fontRendererObj.drawString(lines.get(lineScrollOffset + i), x + padding, y + padding + i * fontRendererObj.FONT_HEIGHT, textColour, false);
			}

			if(this.isFocused && cursorY >= lineScrollOffset && cursorY < lineScrollOffset + visibleLines)
			{
				if ((this.cursorTick / 10) % 2 == 0)
				{
					String subString = getActiveLine().substring(0, cursorX);
					int width = fontRendererObj.getStringWidth(subString);
					int posX = x + padding + width;
					int posY = y + padding + (cursorY - lineScrollOffset) * fontRendererObj.FONT_HEIGHT;
					Gui.drawRect(posX, posY - 1, posX + 1, posY + fontRendererObj.FONT_HEIGHT, Color.WHITE.getRGB());
				}
			}
		}
	}

	@Override
	public void handleMouseClick(int mouseX, int mouseY, int mouseButton)
	{
		if(!this.visible || !this.enabled || !this.editable)
			return;

		this.isFocused = mouseX >= this.xPosition && mouseX < this.xPosition + this.width && mouseY >= this.yPosition && mouseY < this.yPosition + this.height;
	}

	@Override
	public void handleKeyTyped(char character, int code)
	{
		if(!this.visible || !this.enabled || !this.isFocused || !this.editable)
			return;

		if (GuiScreen.isKeyComboCtrlV(code))
		{
			insertAtCursor(GuiScreen.getClipboardString());
		}
		else
		{
			switch (code)
			{
				case Keyboard.KEY_BACK:
					handleBackspace();
					return;
				case Keyboard.KEY_RETURN:
					handleReturn();
					return;
				case Keyboard.KEY_TAB:
					insertAtCursor("    ");
					return;
				case Keyboard.KEY_LEFT:
					moveCursorLeft(1);
					return;
				case Keyboard.KEY_RIGHT:
					moveCursorRight(1);
					return;
				case Keyboard.KEY_UP:
					moveCursorUp();
					return;
				case Keyboard.KEY_DOWN:
					moveCursorDown();
					return;
				default:
					if (ChatAllowedCharacters.isAllowedCharacter(character))
					{
						insertAtCursor(Character.toString(character));
					}
			}
		}
	}

	private String getActiveLine()
	{
		return lines.get(lineScrollOffset + relativeCursorY);
	}

	private void handleBackspace()
	{
		int lineIndex = lineScrollOffset + relativeCursorY;

		if(lineIndex == 0 && cursorX == 0)
			return;

		removeCharAtCursor();
		if(wrapText)
		{
			if(lineScrollOffset + relativeCursorY + 1 < lines.size())
			{
				String activeLine = getActiveLine();
				if(activeLine.contains("\n"))
					return;

				String result = activeLine;
				String old = lines.remove(lineScrollOffset + relativeCursorY + 1);
				if(!old.equals("\n"))
				{
					result += old;
				}
				if(fontRendererObj.getStringWidth(result) > width - padding * 2)
				{
					String trimmed = fontRendererObj.trimStringToWidth(result, width - padding * 2);
					lines.set(lineScrollOffset + relativeCursorY, trimmed);
					if(trimmed.charAt(trimmed.length() - 1) != '\n')
					{
						prependToLine(lineScrollOffset + relativeCursorY + 1, result.substring(trimmed.length()));
					}
					else if(lineScrollOffset + relativeCursorY + 1 < lines.size())
					{
						lines.add(lineScrollOffset + relativeCursorY + 1, trimmed);
					}
					else
					{
						lines.add(trimmed);
					}
				}
				else
				{
					lines.set(lineScrollOffset + relativeCursorY, result);
				}
			}
		}
	}

	private void handleReturn()
	{
		int lineIndex = lineScrollOffset + relativeCursorY;
		String activeLine = getActiveLine();

		lines.set(lineIndex, activeLine.substring(0, cursorX) + "\n");
		if(!(wrapText && cursorX == activeLine.length()))
		{
			lines.add(lineIndex + 1, activeLine.substring(cursorX));
		}

		cursorX = 0;
		if(relativeCursorY + 1 >= visibleLines)
		{
			scroll(1);
		}
		else
		{
			moveYCursor(1);
		}
	}

	private void removeCharAtCursor()
	{
		int lineIndex = lineScrollOffset + relativeCursorY;
		String activeLine = getActiveLine();
		if(cursorX > 0)
		{
			String head = activeLine.substring(0, cursorX - 1);
			String tail = activeLine.substring(cursorX);
			lines.set(lineIndex, head + tail);
			moveCursorLeft(1);
			return;
		}

		if(activeLine.isEmpty() || (activeLine.length() == 1 && activeLine.charAt(0) == '\n'))
		{
			if(lineScrollOffset > 0)
			{
				scroll(-1);
				moveYCursor(1);
			}
		}

		if(wrapText)
		{
			if(activeLine.isEmpty())
			{
				lines.remove(lineIndex);
			}
			String previousLine = lines.get(lineIndex - 1);
			lines.set(lineIndex - 1, previousLine.substring(0, Math.max(previousLine.length() - 1, 0)));
			moveCursorLeft(1);
		}
		else
		{
			moveCursorLeft(1);
			String previousLine = lines.get(lineIndex - 1);
			if(!activeLine.isEmpty())
			{
				lines.set(lineIndex - 1, previousLine.substring(0, Math.max(previousLine.length() - 1, 0)) + activeLine);
			}
			else
			{
				lines.set(lineIndex - 1, previousLine.substring(0, Math.max(previousLine.length() - 1, 0)));
			}
			lines.remove(lineIndex);
			if(lineScrollOffset + visibleLines == lines.size() - 1)
			{
				scroll(-1);
			}
		}
	}

	private void insertAtCursor(String text)
	{
		text = text.replace("\r", "").replace("\t", "    ");
		int lineIndex = lineScrollOffset + relativeCursorY;
		String activeLine = getActiveLine();
		String head = activeLine.substring(0, cursorX);
		String tail = activeLine.substring(cursorX);
		if(wrapText)
		{
			String result = head + text + tail;
			if(fontRendererObj.getStringWidth(result) > width - padding * 2)
			{
				String trimmed = fontRendererObj.trimStringToWidth(result, width - padding * 2);
				lines.set(lineIndex, trimmed);
				prependToLine(lineIndex + 1, result.substring(trimmed.length()));
			}
			else
			{
				lines.set(lineIndex, result);
			}
		}
		else
		{
			lines.set(lineIndex, head + text + tail);
		}

		moveCursorRight(text.length());

		if(wrapText && lineIndex < lineScrollOffset + relativeCursorY)
		{
			moveCursorRight(1);
		}
	}

	private void prependToLine(int lineIndex, String text)
	{
		if(lineIndex == lines.size())
			lines.add("");
		if(lineIndex < lines.size())
		{
			if(text.charAt(text.length() - 1) == '\n')
			{
				lines.add(lineIndex, text);
				return;
			}
			String result = text + lines.get(lineIndex);
			if(fontRendererObj.getStringWidth(result) > width - padding * 2)
			{
				String trimmed = fontRendererObj.trimStringToWidth(result, width - padding * 2);
				lines.set(lineIndex, trimmed);
				prependToLine(lineIndex + 1, result.substring(trimmed.length()));
			}
			else
			{
				lines.set(lineIndex, result);
			}
		}
	}

	private void moveCursorRight(int amount)
	{
		if(amount <= 0)
			return;

		int lineIndex = lineScrollOffset + relativeCursorY;
		String activeLine = getActiveLine();

		if(lineIndex == lines.size() - 1 && cursorX == activeLine.length() || (cursorX > 0 && activeLine.charAt(cursorX - 1) == '\n'))
			return;

		cursorTick = 0;

		if(cursorX < activeLine.length() && activeLine.charAt(cursorX) != '\n')
		{
			cursorX++;
		}
		else if(lineScrollOffset + relativeCursorY + 1 < lines.size())
		{
			cursorX = 0;
			if(relativeCursorY + 1 >= visibleLines)
			{
				scroll(1);
			}
			else
			{
				moveYCursor(1);
			}
		}

		moveCursorRight(amount - 1);
	}

	private void moveCursorLeft(int amount)
	{
		if(amount <= 0)
			return;

		int lineIndex = lineScrollOffset + relativeCursorY;

		if(lineIndex == 0 && cursorX == 0)
			return;

		cursorTick = 0;
		if(cursorX > 0)
		{
			cursorX--;
		}
		else
		{
			cursorX = lines.get(lineIndex - 1).length();

			if(cursorX > 0 && lines.get(lineIndex - 1).charAt(cursorX - 1) == '\n')
			{
				cursorX--;
			}

			if(relativeCursorY - 1 < 0)
			{
				scroll(-1);
			}
			else
			{
				moveYCursor(-1);
			}
		}

		moveCursorLeft(amount - 1);
	}

	private void moveCursorUp()
	{
		int lineIndex = lineScrollOffset + relativeCursorY;

		if(lineIndex == 0)
			return;

		cursorTick = 0;
		String previousLine = lines.get(lineIndex - 1);
		if(cursorX > previousLine.length())
		{
			cursorX = previousLine.length();
			if(previousLine.contains("\n"))
			{
				cursorX--;
			}
		}
		if(relativeCursorY - 1 < 0)
		{
			scroll(-1);
		}
		else
		{
			moveYCursor(-1);
		}
	}

	private void moveCursorDown()
	{
		int lineIndex = lineScrollOffset + relativeCursorY;

		if(lineIndex == lines.size() - 1)
			return;

		cursorTick = 0;
		String nextLine = lines.get(lineIndex + 1);
		if(cursorX > nextLine.length())
		{
			cursorX = nextLine.length();
			if(nextLine.contains("\n"))
			{
				cursorX--;
			}
		}
		if(relativeCursorY + 1 >= visibleLines)
		{
			scroll(1);
		}
		else
		{
			moveYCursor(1);
		}
	}

	private void moveYCursor(int amount)
	{
		relativeCursorY += amount;
		cursorY = lineScrollOffset + relativeCursorY;
	}

	private void scroll(int amount)
	{
		lineScrollOffset += amount;
		if(lineScrollOffset < 0)
		{
			lineScrollOffset = 0;
		}
		else if(lineScrollOffset > lines.size() - visibleLines)
		{
			lineScrollOffset = Math.max(0, lines.size() - visibleLines - 1);
		}
		cursorY = lineScrollOffset + relativeCursorY;
	}

	private void updateText()
	{
		List<String> updatedLines = new ArrayList<>();
		if(wrapText)
		{
			for(int i = 0; i < lines.size() - 1; i++)
			{
				List<String> split = fontRendererObj.listFormattedStringToWidth(lines.get(i), width - padding * 2);
				for(int j = 0; j < split.size() - 1; j++)
				{
					updatedLines.add(split.get(j));
				}
				if(split.size() > 0)
				{
					updatedLines.add(split.get(split.size() - 1) + "\n");
				}
			}

			List<String> split = fontRendererObj.listFormattedStringToWidth(lines.get(lines.size() - 1), width - padding * 2);
			for(int i = 0; i < split.size() - 1; i++)
			{
				updatedLines.add(split.get(i));
			}
			updatedLines.add(split.get(split.size() - 1));
		}
		else
		{
			int lineIndex = 0;
			StringBuilder builder = new StringBuilder();
			do
			{
				String line = lines.get(lineIndex);
				builder.append(line);
				if(!line.endsWith("\n"))
				{
					if(lineIndex == lines.size() - 1)
					{
						updatedLines.add(builder.toString());
						break;
					}
				}
				else
				{
					updatedLines.add(builder.toString());
					builder.setLength(0);
				}
			}
			while(++lineIndex < lines.size());
		}
		lines = updatedLines;
	}

	/**
	 * Appends text to the text area
	 *
	 * @param text the text to append
	 */
	public void writeText(String text)
	{
		String activeLine = getActiveLine();
		String head = activeLine.substring(0, cursorX);
		String tail = activeLine.substring(cursorX);
		String[] splitText = text.split("\n");
		if(splitText.length > 0)
		{
			lines.set(lineScrollOffset + relativeCursorY, head + splitText[0]);
		}
		if(splitText.length > 1)
		{
			for(int i = splitText.length - 2; i >= 1; i--)
			{
				lines.add(lineScrollOffset + relativeCursorY + 1, splitText[i]);
			}
			lines.add(lineScrollOffset + relativeCursorY + splitText.length - 1, splitText[splitText.length - 1] + tail);
		}
	}

	/**
	 * Clears the text
	 */
	public void clear()
	{
		lines.clear();
	}

	/**
	 * Sets the text for this component 
	 *
	 * @param text the text
	 */
	public void setText(String text)
	{
		lines.clear();
		String[] splitText = text.replace("\r", "").split("\n");
		for(int i = 0; i < splitText.length - 1; i++)
		{
			lines.add(splitText[i] + "\n");
		}
		lines.add(splitText[splitText.length - 1]);
	}

	/**
	 * Gets the text in the box
	 *
	 * @return the text
	 */
	public String getText()
	{
		StringBuilder builder = new StringBuilder();
		lines.forEach(s -> {
			builder.append(s);
			builder.append("\n");
		});
		return builder.toString();
	}

	public void setPlaceholder(String placeholder)
	{
		this.placeholder = placeholder;
	}

	public void setWrapText(boolean wrapText)
	{
		this.wrapText = wrapText;
		updateText();
	}

	/**
	 * Sets this text area focused. Makes it available for typing.
	 *
	 * @param isFocused whether the text area should be focused
	 */
	public void setFocused(boolean isFocused)
	{
		this.isFocused = isFocused;
	}

	/**
	 * Sets the padding for the text area
	 *
	 * @param padding the padding size
	 */
	public void setPadding(int padding)
	{
		this.padding = padding;
		this.visibleLines = (int) Math.floor((height - padding * 2) / fontRendererObj.FONT_HEIGHT);
	}

	/**
	 * Sets the text colour for this component
	 *
	 * @param color the text colour
	 */
	public void setTextColour(Color color)
	{
		this.textColour = color.getRGB();
	}

	/**
	 * Sets the background colour for this component
	 *
	 * @param color the background colour
	 */
	public void setBackgroundColour(Color color)
	{
		this.backgroundColour = color.getRGB();
	}

	/**
	 * Sets the border colour for this component
	 *
	 * @param color the border colour
	 */
	public void setBorderColour(Color color)
	{
		this.borderColour = color.getRGB();
	}

	/**
	 * Sets whether the user can edit the text
	 *
	 * @param editable is this component editable
	 */
	public void setEditable(boolean editable)
	{
		this.editable = editable;
	}

}
