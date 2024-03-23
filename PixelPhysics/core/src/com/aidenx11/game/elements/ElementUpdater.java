package com.aidenx11.game.elements;

import com.aidenx11.game.CellularMatrix;
import com.aidenx11.game.color.ColorManager;
import com.aidenx11.game.color.CustomColor;

public class ElementUpdater {
	
	public static void updateVelocity(Element element) {
		float newVelocity = element.getVelocity() + element.getAcceleration();

		if (Math.abs(newVelocity) > element.getMaxSpeed()) {
			newVelocity = Math.signum(newVelocity) * element.getMaxSpeed();
		}

		element.setVelocity(newVelocity);

	}
	
	public static int getUpdateCount(Element element) {
		float abs = Math.abs(element.getVelocity());
		int floored = (int) Math.floor(abs);
		float mod = abs - floored;

		return floored + (Math.random() < mod ? 1 : 0);
	}
	
	public static void clearParticle(Element element, CellularMatrix matrix) {
		matrix.setElement(new Empty(element.getRow(), element.getColumn()));
	}
	
	public static void updateElementLife(Element element, CellularMatrix matrix) {
		
		if (element.getLifetime() < 0) {
			return;
		}
		if (element.getLifetime() == 0) {
			clearParticle(element, matrix);
		}
		
		int lifetime = element.getLifetime();
		
		element.setLifetime(lifetime - 1);
	}
	
	public static void update(Element element, CellularMatrix matrix) {
		updateVelocity(element);
		
		for (int v = 0; v < getUpdateCount(element); v++) {

			int delta = (int) Math.signum(element.getVelocity());
			Element nextVertical = matrix.getElement(element.getRow() - delta, element.getColumn());
			int randDirection = Math.random() > 0.5 ? 1 : -1;
			Element nextVertical1 = matrix.getElement(element.getRow() - delta, element.getColumn() - randDirection);
			Element nextVertical2 = matrix.getElement(element.getRow() - delta, element.getColumn() + randDirection);
			
			Element sideways1 = matrix.getElement(element.getRow(), element.getColumn() - randDirection);
			Element sideways2 = matrix.getElement(element.getRow(), element.getColumn() + randDirection);
			
			if (nextVertical != null && nextVertical.getDensity() < element.getDensity()) {
				matrix.swap(element, nextVertical);
			} else if (nextVertical1 != null && nextVertical1.getDensity() < element.getDensity()) {
				matrix.swap(element, nextVertical1);
				element.setVelocity((float) (element.getVelocity() - 0.1 * delta));
			} else if (nextVertical2 != null && nextVertical2.getDensity() < element.getDensity()) {
				matrix.swap(element, nextVertical2);
				element.setVelocity((float) (element.getVelocity() - 0.1 * delta));
			} else if (sideways1 != null && element.movesSideways() && sideways1.getDensity() < element.getDensity()) {
				matrix.swap(element, sideways1);
			} else if (sideways2 != null && element.movesSideways() && sideways2.getDensity() < element.getDensity()) {
				matrix.swap(element, sideways2);
			}
			else {
				element.setVelocity(0);
			}
			
		}

		element.setModified(element.getVelocity() != 0);
		updateElementLife(element, matrix);
		
	}
}
