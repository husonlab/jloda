/*
 *  HTMLConvert.java Copyright (C) 2022 Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jloda.util;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * convert HTML characters entities into characters and vice versa
 * Daniel Huson, 3.2022
 */
public class HTMLConvert {
	private final static String nameHtmlUnicode = """
			lceil	8968
			rceil	8969
			lfloor	8970
			rfloor	8971
			lang	9001
			rang	9002
			spades	9824
			clubs	9827
			hearts	9829
			diams	9830
			ndash	8211
			mdash	8212
			dagger	8224
			ddagger	8225
			permil	8240
			lsaquo	8249
			rsaquo	8250
			BlackSquare	9632
			WhiteSquare	9633
			WhiteSquareWithRoundedCorners	9634
			WhiteSquareContainingBlackSmallSquare	9635
			SquareWithHorizontalFill	9636
			SquareWithVerticalFill	9637
			SquareWithOrthogonalCrosshatch	9638
			SquareWithUpperLeftToLowerRightFill	9639
			SquareWithUpperRightToLowerLeftFill	9640
			SquareWithDiagonalCrosshatchFill	9641
			BlackSmallSquare	9642
			WhiteSmallSquare	9643
			BlackRectangle	9644
			WhiteRectangle	9645
			BlackVerticalRectangle	9646
			WhiteVerticalRectangle	9647
			BlackParallelogram	9648
			WhiteParallelogram	9649
			BlackUpPointingTriangle	9650
			WhiteUpPointingTriangle	9651
			BlackUpPointingSmallTriangle	9652
			WhiteUpPointingSmallTriangle	9653
			BlackRightPointingTriangle	9654
			WhiteRightPointingTriangle	9655
			BlackRightPointingSmallTriangle	9656
			WhiteRightPointingSmallTriangle	9657
			BlackRightPointingPointer	9658
			WhiteRightPointingPointer	9659
			BlackDownPointingTriangle	9660
			WhiteDownPointingTriangle	9661
			BlackDownPointingSmallTriangle	9662
			WhiteDownPointingSmallTriangle	9663
			BlackLeftPointingTriangle	9664
			WhiteLeftPointingTriangle	9665
			BlackLeftPointingSmallTriangle	9666
			WhiteLeftPointingSmallTriangle	9667
			BlackLeftPointingPointer	9668
			WhiteLeftPointingPointer	9669
			BlackDiamond	9670
			WhiteDiamond	9671
			WhiteDiamondContainingBlackSmallDiamond	9672
			Fisheye	9673
			Lozenge	9674
			WhiteCircle	9675
			DottedCircle	9676
			CircleWithVerticalFill	9677
			Bullseye	9678
			BlackCircle	9679
			CircleWithLeftHalfBlack	9680
			CircleWithRightHalfBlack	9681
			CircleWithLowerHalfBlack	9682
			CircleWithUpperHalfBlack	9683
			CircleWithUpperRightQuadrantBlack	9684
			CircleWithAllButUpperLeftQuadrantBlack	9685
			LeftHalfBlackCircle	9686
			RightHalfBlackCircle	9687
			InverseBullet	9688
			InverseWhiteCircle	9689
			BlackLowerRightTriangle	9698
			BlackLowerLeftTriangle	9699
			BlackUpperLeftTriangle	9700
			BlackUpperRightTriangle	9701
			WhiteBullet	9702
			SquareWithLeftHalfBlack	9703
			SquareWithRightHalfBlack	9704
			SquareWithUpperLeftDiagonalHalfBlack	9705
			SquareWithLowerRightDiagonalHalfBlack	9706
			WhiteUpPointingTriangleWithDot	9708
			UpPointingTriangleWithLeftHalfBlack	9709
			UpPointingTriangleWithRightHalfBlack	9710
			LargeCircle	9711
			UpperLeftTriangle	9720
			UpperRightTriangle	9721
			LowerLeftTriangle	9722
			WhiteMediumSquare	9723
			BlackMediumSquare	9724
			WhiteMediumSmallSquare	9725
			BlackMediumSmallSquare	9726
			LowerRightTriangle	9727
			CIRCLE_DONE	10112
			CIRCLED_TWO	10113
			CIRCLED_THREE	10114
			CIRCLED_FOUR	10115
			CIRCLED_FIVE	10116
			CIRCLED_SIX	10117
			CIRCLED_SEVEN	10118
			CIRCLED_EIGHT	10119
			CIRCLED_NINE	10120
			CIRCLED_TEN	10121
			BLACK_STAR	9733
			WHITE_STAR	9734
			CHECK_BOX	9745
			CHECKED_BOX	9746
			PEACE_SIGN	9774
			YIN_YANG	9775
			FROWNING_FACE	9785
			SMILING_FACE	9786
			WARNING_SIGN	9888
			HIGH_VOLTAGE	9889
			CHECK_MARK	10003
			HEAVY_CHECKMARK	10004
			MULTIPLICATION_X	10005
			HEAVY_MULTIPLICATION_X	10006
			BALLOT_	10007
			HEAVY_BALLOT	10008
			""";

	public final static Map<String, Character> htmlCharacterMap = new HashMap<>();
	public final static Map<Character, String> characterHtmlMap = new HashMap<>();

	static {
		nameHtmlUnicode.lines().map(line -> line.split("\t")).filter(tokens -> tokens.length == 2)
				.forEach(tokens -> {
					var htmlName = String.format("&%s;", tokens[0].trim());
					var ch = (char) Integer.parseInt(tokens[1]);
					htmlCharacterMap.put(htmlName.toLowerCase(), ch);
					if (htmlName.contains("_")) // is not an offical HTML name
						characterHtmlMap.put(ch, "&#%s;".formatted(tokens[1]));
					else
						characterHtmlMap.put(ch, htmlName.toLowerCase());
				});
	}

	/**
	 * converts HTML entities into characters
	 *
	 * @param text source text
	 * @return text in which HTML entities have been replaced by characters
	 */
	public static String convertHtmlToCharacters(String text) {
		var buf = new StringBuilder();
		for (var pos = 0; pos < text.length(); pos++) {
			var ch = text.charAt(pos);
			var replaced = false;
			if (ch == '\\' && pos + 5 < text.length() && text.charAt(pos + 1) == 'u') {
				var endPos = pos + 6;
				var word = text.substring(pos + 2, endPos);
				if (NumberUtils.isInteger(word, 16)) {
					var character = (char) NumberUtils.parseInt(word, 16);
					buf.append(character);
					pos = endPos - 1;
					replaced = true;
				}
			} else if (ch == '&') {
				var endPos = text.indexOf(";", pos + 2);
				if (endPos != -1) {
					var word = text.substring(pos, endPos + 1);
					var character = htmlCharacterMap.get(word.toLowerCase());
					if (character == null) {
						if (word.startsWith("&#x") && NumberUtils.isInteger(word.substring(3, word.length() - 1), 16)) {
							character = (char) NumberUtils.parseInt(word.substring(3, word.length() - 1), 16);
						} else if (word.startsWith("&#") && NumberUtils.isInteger(word.substring(2, word.length() - 1))) {
							character = (char) NumberUtils.parseInt(word.substring(2, word.length() - 1));
						}
					}
					if (character != null) {
						buf.append(character);
						pos = endPos;
						replaced = true;
					}
				}
			}
			if (!replaced) {
				buf.append(ch);
			}
		}
		return buf.toString();
	}

	public static void main(String[] args) {


		if (true) {
			for (var html : new TreeSet<>(htmlCharacterMap.keySet())) {
				System.err.println(html + " = " + htmlCharacterMap.get(html));
			}
		} else {
			var row = 0;
			var col = 0;

			for (var html : new TreeSet<>(htmlCharacterMap.keySet())) {
				var ch = htmlCharacterMap.get(html);
				System.err.printf("<Button alignment=\"CENTER\" layoutX=\"23.0\" layoutY=\"8.0\" mnemonicParsing=\"false\" prefHeight=\"16.0\" prefWidth=\"16.0\" style=\"-fx-background-color: transparent; -fx-border-color: transparent;\" text=\"%c\"  GridPane.rowIndex=\"%d\" GridPane.columnIndex=\"%d\" />%n",
						ch, row, col);
				if (++col == 12) {
					row++;
					col = 0;
				}
			}
		}

		System.err.println("Count: " + htmlCharacterMap.size());
	}
}
