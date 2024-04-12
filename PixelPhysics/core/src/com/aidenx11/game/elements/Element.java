package com.aidenx11.game.elements;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.aidenx11.game.CellularMatrix;
import com.aidenx11.game.pixelPhysicsGame;
import com.aidenx11.game.color.CustomColor;
import com.aidenx11.game.elements.immovable.Fire;
import com.aidenx11.game.elements.immovable.Steel;
import com.aidenx11.game.elements.movable.gas.Smoke;
import com.aidenx11.game.elements.movable.gas.Steam;
import com.aidenx11.game.elements.movable.liquid.Water;
import com.aidenx11.game.elements.movable.movable_solid.Dirt;
import com.aidenx11.game.elements.movable.movable_solid.Sand;
import com.aidenx11.game.elements.movable.movable_solid.WetDirt;
import com.aidenx11.game.elements.movable.movable_solid.WetSand;
import com.badlogic.gdx.graphics.Color;

/**
 * Super class of all elements. Contains fields for parent matrix, element type,
 * location, color, density, lifetime, and more.
 * 
 * @author Aiden Schroeder
 */
public abstract class Element {

	/** Parent matrix of this element */
	public CellularMatrix parentMatrix = pixelPhysicsGame.matrix;

	/** Type of this element */
	public static ElementTypes type;

	/** Row location of this element */
	private int row;

	/** Column location of this element */
	private int column;

	/** Color of this element */
	private CustomColor color;

	/** Density of this element */
	private float density;

	/** Whether or not this element has a limited life (can die) */
	private boolean limitedLife;

	/** Number of frames the element can stay alive if it has limited life */
	private int lifetime;

	/** Melting point of this element */
	private int meltingPoint;

	/** Whether or not this element is flammable */
	private boolean isFlammable;

	/** Whether or not this element extinguishes other elements */
	private boolean extinguishesThings;

	/** Chance for this element to catch on fire */
	private float chanceToCatch;

	/** Whether or not this element moves down */
	private boolean movesDown;

	/** Whether or not this element is on fire */
	private boolean onFire;

	/** Whether or not this element is falling through air */
	private boolean fallingThroughAir = false;

	/**
	 * Public enumeration that contains all the types of elements in the simulation
	 */
	public enum ElementTypes {
		SAND, EMPTY, WOOD, SMOKE, FIRE, WATER, STEAM, WET_SAND, LEAF, DIRT, WET_DIRT, STONE, LAVA, OBSIDIAN, STEEL,
		RUST;
	}

	/**
	 * CustomColor array to keep track of the colors an element alternates through
	 * when it is burning
	 */
	public static CustomColor[] fireColors = new CustomColor[] { new CustomColor(253, 207, 88),
			new CustomColor(242, 125, 12), new CustomColor(199, 14, 14), new CustomColor(240, 127, 19) };

	/**
	 * Abstract update method that varies based on the element inheriting it
	 */
	public abstract void update();

	/**
	 * Default constructor for Element. Every subclass of Element calls up to this
	 * constructor.
	 * 
	 * @param type               Type of this element, from ElementTypes enumeration
	 * @param row                row location of this element
	 * @param column             column location of this element
	 * @param color              color of this element
	 * @param canDie             whether or not this element can die/has limited
	 *                           life
	 * @param lifetime           lifetime of this element
	 * @param flammable          whether or not this element is flammable
	 * @param extinguishesThings whether or not this element extinguishes other
	 *                           elements
	 * @param chanceToCatch      chance for this element to catch on fire when in
	 *                           contact with another element that is on fire,
	 *                           ranging from 0-1
	 * @param movesDown          whether or not this element moves downward
	 * @param temperature        temperature of this element
	 */
	public Element(ElementTypes type, int row, int column, CustomColor color, boolean canDie, int lifetime,
			boolean flammable, boolean extinguishesThings, float chanceToCatch, boolean movesDown, int temperature) {
		setType(type);
		setRow(row);
		setColumn(column);
		setColor(color);
		setType(type);
		setLifetime(lifetime);
		setFlammable(flammable);
		setExtinguishesThings(extinguishesThings);
		setChanceToCatch(chanceToCatch);
		setLimitedLife(canDie);
		setMovesDown(movesDown);
		setTemperature(temperature);
	}

	/**
	 * Updates the lifetime of this element. Also causes the element to flicker if
	 * it is on fire.
	 */
	public void updateElementLife() {
		if (this.limitedLife() && this.getLifetime() < 1) {
			if (this instanceof Smoke || this instanceof Steam) {
				parentMatrix.clearElement(this);
			} else if (this instanceof Fire || this.isOnFire()) {
				if (Math.random() < 0.3) {
					parentMatrix.setElement(new Smoke(this.getRow(), this.getColumn()));
				} else {
					parentMatrix.clearElement(this);
				}
			} else if (this instanceof Steel) {
				parentMatrix.setNewElement(this, ElementTypes.RUST);
			}
		}

		if (this.isOnFire() && Math.random() < 0.1) {
			this.flicker();
		}

		this.setLifetime(this.getLifetime() - 1);
	}

	/**
	 * When called by an element, causes elements that can rust to turn into rust
	 * around the element.
	 */
	public void causeRust() {
		Element[] adjacentElements = parentMatrix.getAdjacentElements(this);
		List<Element> shuffledElements = Arrays.asList(adjacentElements);
		Collections.shuffle(shuffledElements);
		Element nextElement;
		for (int i = 0; i < shuffledElements.size(); i++) {
			nextElement = shuffledElements.get(i);
			if (nextElement instanceof Steel && !nextElement.limitedLife()
					&& Math.random() < ((Steel) nextElement).getChanceToRust()) {
				nextElement.setLimitedLife(true);
			}
		}
	}

	/**
	 * Updates the logic concerning fire turning water into steam and drying wet
	 * elements
	 * 
	 * @param elements array of elements to check
	 * @return whether or not this element was extinguished
	 */
	private boolean updateDryingLogic(Element[] elements) {
		boolean extinguished = false;
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] != null && elements[i].extinguishesThings() && this.isOnFire()) {
				if (elements[i] instanceof Water && Math.random() < 0.4) {
					parentMatrix.setNewElement(elements[i], ElementTypes.STEAM);
					extinguished = true;
				} else if (elements[i] instanceof WetSand) {
					parentMatrix.setNewElement(this, ElementTypes.STEAM);
					parentMatrix.setNewElement(elements[i], ElementTypes.SAND);
					extinguished = true;
				} else if (elements[i] instanceof WetDirt) {
					parentMatrix.setNewElement(this, ElementTypes.STEAM);
					parentMatrix.setNewElement(elements[i], ElementTypes.DIRT);
					extinguished = true;
				}
			}
		}
		return extinguished;
	}

	/**
	 * Returns the number of elements in the given element array that are on fire
	 * 
	 * @param elements elements to check
	 * @return number of elements in the given element array that are on fire
	 */
	private int updateNumberOfAdjacentFire(Element[] elements) {
		int numberOfFire = 0;
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] != null && elements[i].isOnFire()) {
				numberOfFire++;
			}
		}
		return numberOfFire;
	}

	/**
	 * Causes elements that can get wet to get wet if they are adjacent to this
	 * element, and this element causes wetness.
	 */
	public void causeWetness() {

		Element[] adjacentElements = parentMatrix.getAdjacentElements(this);
		List<Element> shuffledElements = Arrays.asList(adjacentElements);
		Collections.shuffle(shuffledElements);
		Element nextElement;

		for (int i = 0; i < shuffledElements.size(); i++) {
			nextElement = shuffledElements.get(i);
			if (nextElement instanceof Sand) {
				parentMatrix.setNewElement(nextElement, ElementTypes.WET_SAND);
				parentMatrix.clearElement(this);
			}
			if (nextElement instanceof Dirt) {
				parentMatrix.setNewElement(nextElement, ElementTypes.WET_DIRT);
				parentMatrix.clearElement(this);
			}

		}
	}

	/**
	 * Updates logic concerning elements being burned. If the element was on fire
	 * and gets extinguished, turns into smoke.
	 */
	public void updateBurningLogic() {

		Element[] adjacentElements = parentMatrix.getAdjacentElements(this);
		boolean extinguished = updateDryingLogic(adjacentElements);
		int numberOfFire = updateNumberOfAdjacentFire(adjacentElements);

		float chanceToCatch = this.getChanceToCatch() * numberOfFire;
		if (Math.random() < chanceToCatch && !this.isOnFire()) {
			this.setOnFire(true);
		}

		if (extinguished) {
			if (this.isOnFire()) {
				parentMatrix.setNewElement(this, ElementTypes.SMOKE);
			}
		}

	}

	/**
	 * Causes this element's color to flicker between the colors in the fireColors
	 * array
	 */
	public void flicker() {
		int idx = (int) (Math.round(Math.random() * 3));
		this.setColor(fireColors[idx]);
	}

	/**
	 * @return the type of this element
	 */
	public ElementTypes getType() {
		return type;
	}

	/**
	 * Sets the type of this element
	 * 
	 * @param type type to set
	 */
	public void setType(ElementTypes type) {
		Element.type = type;
	}

	/**
	 * @return the density of this element
	 */
	public float getDensity() {
		return density;
	}

	/**
	 * Sets the density of this element
	 * 
	 * @param density density to set
	 */
	public void setDensity(float density) {
		this.density = density;
	}

	/**
	 * Sets the color of this element to the given CustomColor
	 * 
	 * @param color color to set
	 */
	public void setColor(CustomColor color) {
		this.color = color;
	}

	/**
	 * Sets the color of this element to the given rgb values
	 * 
	 * @param rgb integer array of rgb values
	 */
	public void setColor(int[] rgb) {
		this.color = new CustomColor(rgb);
	}

	/**
	 * Sets the row location of this element
	 * 
	 * @param row row to set
	 */
	public void setRow(int row) {
		this.row = row;
	}

	/**
	 * Sets the column location of this element
	 * 
	 * @param column column to set
	 */
	public void setColumn(int column) {
		this.column = column;
	}

	/**
	 * @return the row location of this element
	 */
	public int getRow() {
		return row;
	}

	/**
	 * @return the column location of this element
	 */
	public int getColumn() {
		return column;
	}

	public Color getColor() {
		return new Color(color.getR() / 255f, color.getG() / 255f, color.getB() / 255f, 1f);
	}

	public boolean limitedLife() {
		return limitedLife;
	}

	public void setLimitedLife(boolean limitedLife) {
		this.limitedLife = limitedLife;
	}

	public int getLifetime() {
		return lifetime;
	}

	public void setLifetime(int lifetime) {
		this.lifetime = lifetime;
	}

	public void setFlammable(boolean flammable) {
		if (flammable) {
			this.setLimitedLife(true);
		}
	}

	public boolean isFlammable() {
		return isFlammable;
	}

	public boolean extinguishesThings() {
		return extinguishesThings;
	}

	public float getChanceToCatch() {
		return chanceToCatch;
	}

	private void setChanceToCatch(float chanceToCatch) {
		this.chanceToCatch = chanceToCatch;
	}

	private void setExtinguishesThings(boolean extinguishesThings) {
		this.extinguishesThings = extinguishesThings;
	}

	public boolean movesDown() {
		return movesDown;
	}

	public void setMovesDown(boolean movesDown) {
		this.movesDown = movesDown;
	}

	public boolean isOnFire() {
		return onFire;
	}

	public void setOnFire(boolean onFire) {
		this.onFire = onFire;
		this.setLimitedLife(true);
	}

	public int getTemperature() {
		return meltingPoint;
	}

	public void setTemperature(int temperature) {
		this.meltingPoint = temperature;
	}

	public boolean isFallingThroughAir() {
		return fallingThroughAir;
	}

	public void setFallingThroughAir(boolean fallingThroughAir) {
		this.fallingThroughAir = fallingThroughAir;
	}

}
