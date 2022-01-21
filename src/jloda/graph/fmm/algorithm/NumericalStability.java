/*
 * NumericalStability.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jloda.graph.fmm.algorithm;

import jloda.graph.fmm.geometry.DPoint;
import jloda.graph.fmm.geometry.DPointMutable;

import java.util.Random;

/**
 * implementation of the fast multilayer method (note: without multipole algorithm)
 * Original C++ author: Stefan Hachul, original license: GPL
 * Reimplemented in Java by Daniel Huson, 3.2021
 */
public class NumericalStability {
	public static final double epsilon = 0.1;
	public static final double POS_SMALL_DOUBLE = 1e-300;
	public static final double POS_BIG_DOUBLE = 1e+300;
	public static final double POS_BIG_LIMIT = POS_BIG_DOUBLE * 1e-190;
	public static final double POS_SMALL_LIMIT = POS_SMALL_DOUBLE * 1e190;


	private static final Random random = new Random();

	public static boolean repulsionNearMachinePrecision(double distance, DPointMutable force) {

		if (distance > POS_BIG_LIMIT) {
			//create random number in range (0,1)
			var randx = random.nextDouble();
			var randy = random.nextDouble();
			var rand_sign_x = random.nextInt(2);
			var rand_sign_y = random.nextInt(2);
			force.setX(POS_SMALL_LIMIT * (1 + randx) * Math.pow(-1.0, rand_sign_x));
			force.setY(POS_SMALL_LIMIT * (1 + randy) * Math.pow(-1.0, rand_sign_y));
			return true;

		} else if (distance < POS_SMALL_LIMIT) {
			//create random number in range (0,1)
			var randx = random.nextDouble();
			var randy = random.nextDouble();
			var rand_sign_x = random.nextInt(2);
			var rand_sign_y = random.nextInt(2);
			force.setX(POS_BIG_LIMIT * randx * Math.pow(-1.0, rand_sign_x));
			force.setY(POS_BIG_LIMIT * randy * Math.pow(-1.0, rand_sign_y));
			return true;

		} else
			return false;
	}

	public static boolean nearMachinePrecision(double distance, DPointMutable force) {
		if (distance < POS_SMALL_LIMIT) {
			//create random number in range (0,1)
			double randx = random.nextDouble();
			double randy = random.nextDouble();
			int rand_sign_x = random.nextInt(2);
			int rand_sign_y = random.nextInt(2);
			force.setX(POS_SMALL_LIMIT * (1 + randx) * Math.pow(-1.0, rand_sign_x));
			force.setY(POS_SMALL_LIMIT * (1 + randy) * Math.pow(-1.0, rand_sign_y));
			return true;
		} else if (distance > POS_BIG_LIMIT) {
			//create random number in range (0,1)
			double randx = random.nextDouble();
			double randy = random.nextDouble();
			int rand_sign_x = random.nextInt(2);
			int rand_sign_y = random.nextInt(2);
			force.setX(POS_BIG_LIMIT * randx * Math.pow(-1.0, rand_sign_x));
			force.setY(POS_BIG_LIMIT * randy * Math.pow(-1.0, rand_sign_y));
			return true;

		} else
			return false;
	}

	public static DPoint chooseDistinctRandomPointInRadiusEpsilon(DPoint old_pos) {
		double xmin = old_pos.getX() - 1 * epsilon;
		double xmax = old_pos.getX() + 1 * epsilon;
		double ymin = old_pos.getY() - 1 * epsilon;
		double ymax = old_pos.getY() + 1 * epsilon;

		return chooseDistinctRandomPointInDisk(old_pos, xmin, xmax, ymin, ymax);
	}

	private static DPoint chooseDistinctRandomPointInDisk(DPoint oldPoint, double xmin, double xmax, double ymin, double ymax) {
		var mindist_to_xmin = oldPoint.getX() - xmin;
		var mindist_to_xmax = xmax - oldPoint.getX();
		var mindist_to_ymin = oldPoint.getY() - ymin;
		var mindist_to_ymax = ymax - oldPoint.getY();

		var mindist = Math.min(Math.min(mindist_to_xmin, mindist_to_xmax), Math.min(mindist_to_ymin, mindist_to_ymax));

		if (mindist > 0) {
			DPointMutable newPoint = new DPointMutable();
			do {
				//assign random double values in range (-1,1)
				var rand_x = 2 * random.nextDouble() - 1;
				var rand_y = 2 * random.nextDouble() - 1;

				newPoint.setPosition(oldPoint.getX() + mindist * rand_x * epsilon, oldPoint.getY() + mindist * rand_y * epsilon);
			} while (oldPoint.equals(newPoint) || oldPoint.distance(newPoint) >= mindist * epsilon);
			return newPoint;
		} else if (mindist == 0) {
			double mindist_x = 0;
			double mindist_y = 0;

			if (mindist_to_xmin > 0)
				mindist_x = (-1) * mindist_to_xmin;
			else if (mindist_to_xmax > 0)
				mindist_x = mindist_to_xmax;
			if (mindist_to_ymin > 0)
				mindist_y = (-1) * mindist_to_ymin;
			else if (mindist_to_ymax > 0)
				mindist_y = mindist_to_ymax;

			DPointMutable newPoint = new DPointMutable();

			if ((mindist_x != 0) || (mindist_y != 0))
				do {
					//assign random double values in range (0,1)
					var rand_x = random.nextDouble();
					var rand_y = random.nextDouble();
					newPoint.setPosition(oldPoint.getX() + mindist_x * rand_x * epsilon, oldPoint.getY() + mindist_y * rand_y * epsilon);
				} while (oldPoint.equals(newPoint));
			else
				System.err.println("Error chooseDistinctRandomPointInDisk(): box is equal to oldPoint");
			return newPoint;
		} else {
			System.err.println("Error chooseDistinctRandomPointInDisk(): oldPoint not in box");
			return oldPoint;
		}
	}
}
