/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2024, Arnaud Roques
 *
 * Project Info:  https://plantuml.com
 * 
 * If you like this project or if you find it useful, you can support us at:
 * 
 * https://plantuml.com/patreon (only 1$ per month!)
 * https://plantuml.com/paypal
 * 
 * This file is part of PlantUML.
 *
 * PlantUML is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PlantUML distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 *
 * Original Author:  Arnaud Roques
 *
 *
 */
package net.sourceforge.plantuml.activitydiagram3.ftile.vcompact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sourceforge.plantuml.activitydiagram3.Branch;
import net.sourceforge.plantuml.activitydiagram3.ftile.AbstractConnection;
import net.sourceforge.plantuml.activitydiagram3.ftile.AbstractFtile;
import net.sourceforge.plantuml.activitydiagram3.ftile.Connection;
import net.sourceforge.plantuml.activitydiagram3.ftile.Ftile;
import net.sourceforge.plantuml.activitydiagram3.ftile.FtileFactory;
import net.sourceforge.plantuml.activitydiagram3.ftile.FtileGeometry;
import net.sourceforge.plantuml.activitydiagram3.ftile.FtileUtils;
import net.sourceforge.plantuml.activitydiagram3.ftile.Hexagon;
import net.sourceforge.plantuml.activitydiagram3.ftile.Snake;
import net.sourceforge.plantuml.activitydiagram3.ftile.Swimlane;
import net.sourceforge.plantuml.activitydiagram3.ftile.vertical.FtileDiamond;
import net.sourceforge.plantuml.activitydiagram3.ftile.vertical.FtileDiamondInside;
import net.sourceforge.plantuml.decoration.Rainbow;
import net.sourceforge.plantuml.klimt.LineBreakStrategy;
import net.sourceforge.plantuml.klimt.UStroke;
import net.sourceforge.plantuml.klimt.UTranslate;
import net.sourceforge.plantuml.klimt.color.HColor;
import net.sourceforge.plantuml.klimt.creole.CreoleMode;
import net.sourceforge.plantuml.klimt.creole.Display;
import net.sourceforge.plantuml.klimt.creole.Sheet;
import net.sourceforge.plantuml.klimt.creole.SheetBlock1;
import net.sourceforge.plantuml.klimt.creole.SheetBlock2;
import net.sourceforge.plantuml.klimt.drawing.UGraphic;
import net.sourceforge.plantuml.klimt.font.FontConfiguration;
import net.sourceforge.plantuml.klimt.font.StringBounder;
import net.sourceforge.plantuml.klimt.geom.HorizontalAlignment;
import net.sourceforge.plantuml.klimt.geom.XDimension2D;
import net.sourceforge.plantuml.klimt.geom.XPoint2D;
import net.sourceforge.plantuml.klimt.shape.TextBlock;
import net.sourceforge.plantuml.style.ISkinParam;
import net.sourceforge.plantuml.style.Style;
import net.sourceforge.plantuml.style.StyleSignatureBasic;
import net.sourceforge.plantuml.svek.ConditionStyle;

class FtileIfAndStop extends AbstractFtile {

	private final Ftile tile1;
	private final Ftile diamond1;
	private final Ftile stop2;

	private final Rainbow arrowColor;

	private FtileIfAndStop(Ftile diamond1, Ftile tile1, Rainbow arrowColor, Ftile stopFtile) {
		super(tile1.skinParam());
		this.diamond1 = diamond1;
		this.tile1 = tile1;
		this.stop2 = stopFtile;

		this.arrowColor = arrowColor;

	}

	public Set<Swimlane> getSwimlanes() {
		final Set<Swimlane> result = new HashSet<>();
		if (getSwimlaneIn() != null)
			result.add(getSwimlaneIn());

		result.addAll(tile1.getSwimlanes());
		return Collections.unmodifiableSet(result);
	}

	public Swimlane getSwimlaneIn() {
		return diamond1.getSwimlaneIn();
	}

	public Swimlane getSwimlaneOut() {
		return getSwimlaneIn();
	}

	static Ftile create(Swimlane swimlane, HColor borderColor, HColor backColor, Rainbow arrowColor,
			FtileFactory ftileFactory, ConditionStyle conditionStyle, Branch nonStop, ISkinParam skinParam,
			StringBounder stringBounder, Display labelTest) {

		final Ftile tileNonStop = nonStop.getFtile();

		final Ftile stopFtile = ftileFactory.stop(swimlane);

		final Style style = StyleSignatureBasic.activityDiamond().getMergedStyle(skinParam.getCurrentStyleBuilder());
		final UStroke thickness = tileNonStop.getThickness(style);
		final FontConfiguration fcTest = FontConfiguration.create(skinParam, style);

		final Sheet sheet = skinParam
				.sheet(fcTest, skinParam.getDefaultTextAlignment(HorizontalAlignment.LEFT), CreoleMode.FULL)
				.createSheet(labelTest);
		final SheetBlock1 sheetBlock1 = new SheetBlock1(sheet, LineBreakStrategy.NONE, skinParam.getPadding());

		final TextBlock tbTest = new SheetBlock2(sheetBlock1, Hexagon.asStencil(sheetBlock1), thickness);

		final Ftile diamond1;
		if (conditionStyle == ConditionStyle.INSIDE_HEXAGON)
			diamond1 = new FtileDiamondInside(tbTest, tileNonStop.skinParam(), backColor, borderColor, swimlane);
		else if (conditionStyle == ConditionStyle.EMPTY_DIAMOND)
			diamond1 = new FtileDiamond(tileNonStop.skinParam(), backColor, borderColor, swimlane).withNorth(tbTest);
		else
			throw new IllegalStateException();

		final FtileIfAndStop result = new FtileIfAndStop(diamond1, tileNonStop, arrowColor, stopFtile);

		final List<Connection> conns = new ArrayList<>();
		conns.add(result.new ConnectionHorizontal(arrowColor));
		return FtileUtils.addConnection(result, conns);
	}

	private UTranslate getTranslate1(StringBounder stringBounder) {
		final FtileGeometry dimTotal = calculateDimension(stringBounder);
		final FtileGeometry dimDiamond1 = diamond1.calculateDimension(stringBounder);
		final FtileGeometry dim1 = tile1.calculateDimension(stringBounder);

		final double x1 = calculateDimension(stringBounder).getLeft() - dim1.getLeft();
		final double y1 = dimDiamond1.getHeight() + getSuppHeight();
		return new UTranslate(x1, y1);
	}

	private int getSuppHeight() {
		return 30;
	}

	private UTranslate getTranslateDiamond1(StringBounder stringBounder) {
		final double y1 = 0;
		final XDimension2D dimDiamond1 = diamond1.calculateDimension(stringBounder);
		final double x1 = calculateDimension(stringBounder).getLeft() - dimDiamond1.getWidth() / 2;
		return new UTranslate(x1, y1);
	}

	private UTranslate getTranslateStop(StringBounder stringBounder) {
		final XDimension2D dimDiamond1 = diamond1.calculateDimension(stringBounder);
		final XDimension2D dimStop = stop2.calculateDimension(stringBounder);
		final double y1 = (dimDiamond1.getHeight() - dimStop.getHeight()) / 2;
		final double x1 = calculateDimension(stringBounder).getLeft() + dimDiamond1.getWidth() / 2
				+ getDiamondStopDistance();
		return new UTranslate(x1, y1);
	}

	private double getDiamondStopDistance() {
		return 40;
	}

	class ConnectionHorizontal extends AbstractConnection {

		private final Rainbow color;

		public ConnectionHorizontal(Rainbow color) {
			super(diamond1, stop2);
			this.color = color;
		}

		public void drawU(UGraphic ug) {
			final StringBounder stringBounder = ug.getStringBounder();
			final XPoint2D p1 = getP1(stringBounder);
			final XPoint2D p2 = getP2(stringBounder);

			final Snake snake = Snake.create(skinParam(), color, skinParam().arrows().asToRight());
			snake.addPoint(p1);
			snake.addPoint(p2);
			ug.draw(snake);
		}

		private XPoint2D getP1(StringBounder stringBounder) {
			final XDimension2D dimDiamond1 = getFtile1().calculateDimension(stringBounder);
			final XPoint2D p = new XPoint2D(dimDiamond1.getWidth(), dimDiamond1.getHeight() / 2);

			return getTranslateDiamond1(stringBounder).getTranslated(p);
		}

		private XPoint2D getP2(StringBounder stringBounder) {
			final XDimension2D dimStop = getFtile2().calculateDimension(stringBounder);
			final XPoint2D p = new XPoint2D(0, dimStop.getHeight() / 2);
			return getTranslateStop(stringBounder).getTranslated(p);
		}

	}

	@Override
	public UTranslate getTranslateFor(Ftile child, StringBounder stringBounder) {
		if (child == diamond1)
			return getTranslateDiamond1(stringBounder);
		if (child == tile1)
			return getTranslate1(stringBounder);
		throw new UnsupportedOperationException();
	}

	public void drawU(UGraphic ug) {
		final StringBounder stringBounder = ug.getStringBounder();

		ug.apply(getTranslateDiamond1(stringBounder)).draw(diamond1);
		ug.apply(getTranslate1(stringBounder)).draw(tile1);
		ug.apply(getTranslateStop(stringBounder)).draw(stop2);
	}

	@Override
	protected FtileGeometry calculateDimensionFtile(StringBounder stringBounder) {
		final XDimension2D dimStop2 = stop2.calculateDimension(stringBounder);
		final FtileGeometry dim1 = tile1.calculateDimension(stringBounder).addDim(0,
				getDiamondStopDistance() + dimStop2.getWidth());
		final FtileGeometry dimDiamond1 = diamond1.calculateDimension(stringBounder);
		return dimDiamond1.appendBottom(dim1).addDim(0, getSuppHeight());

	}

}
