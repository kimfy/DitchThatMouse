package net.kimfy.ditchthatmouse.client.ui;

import junit.framework.TestCase;
import net.minecraft.client.gui.GuiButton;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class GuiUtilTest extends TestCase
{
    private List<GuiButton> buttonList;
    private GuiButton       singlePlayer, multiPlayer, realms, mods, options, quitGame, web;
    private int id = 0;

    public void setUp()
    {
        buttonList = new LinkedList<>();
        buttonList.add(singlePlayer = new GuiButton(id, 113, 108, "Singleplayer"));
        buttonList.add(multiPlayer = new GuiButton(++id, 113, 132, "Multiplayer"));
        buttonList.add(realms = new GuiButton(++id, 215, 156, "Realms"));
        buttonList.add(mods = new GuiButton(++id, 113, 156, "Mods"));
        buttonList.add(options = new GuiButton(++id, 113, 192, "Options..."));
        buttonList.add(quitGame = new GuiButton(++id, 113, 192, "Quit Game"));
        buttonList.add(web = new GuiButton(++id, 89, 192, ""));
    }

    /**
     * Test if buttons in different order are sorted from lowest yPos and xPos to highest yPos and xPos
     * {@link GuiUtil#SORT_LEFT_RIGHT}
     */
    @Test
    public void testSortingLeftToRight()
    {
        Collections.shuffle(this.buttonList);
        this.buttonList.sort(GuiUtil.SORT_LEFT_RIGHT);
        Assert.assertEquals("Realms button is not in index 3 after sorting", this.realms, this.buttonList.get(3));

    }

    /**
     * Test if buttons in exact same coordinates remain unchanged upon sorting. This will most likely, like 300% sure
     * this won't happen, ever, but y'know, it's good to understand what will happen if this is the case.
     * {@link GuiUtil#SORT_LEFT_RIGHT}
     */
    @Test
    public void testSortingButtonsInEqualCoordinates()
    {
        GuiButton buttonA = this.singlePlayer, buttonB = this.multiPlayer;
        buttonA.xPosition = 100;
        buttonB.xPosition = 100;
        buttonA.yPosition = 100;
        buttonB.yPosition = 100;

        Collections.sort(buttonList, GuiUtil.SORT_LEFT_RIGHT);
        Assert.assertEquals("buttonA is not in position 1", this.buttonList.get(0), buttonA);
        Assert.assertEquals("buttonB is not in position 2", this.buttonList.get(1), buttonB);
    }
}