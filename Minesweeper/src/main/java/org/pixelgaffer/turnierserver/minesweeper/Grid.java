/*
 * Grid.java
 *
 * Copyright (C) 2015 Pixelgaffer
 *
 * This work is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or any later
 * version.
 *
 * This work is distributed in the hope that it will be useful, but without
 * any warranty; without even the implied warranty of merchantability or
 * fitness for a particular purpose. See version 2 and version 3 of the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.pixelgaffer.turnierserver.minesweeper;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import lombok.Getter;
import lombok.Setter;

import org.pixelgaffer.turnierserver.gamelogic.interfaces.BuilderSolverGameState;
import org.pixelgaffer.turnierserver.gamelogic.messages.BuilderSolverChange;
import org.pixelgaffer.turnierserver.minesweeper.Cell.CellForAi;
import org.pixelgaffer.turnierserver.minesweeper.Cell.Type;
import org.pixelgaffer.turnierserver.minesweeper.logic.MinesweeperRenderData;

public class Grid extends BuilderSolverGameState<Map<String, CellForAi>, MinesweeperBuilderResponse, MinesweeperSolverResponse> {
	
	public static Logger logger = Logger.getLogger("GameLogic");
	
	private Cell[][] field;
	private boolean won;
	@Getter
	private int moves;
	
	@Getter
	@Setter
	private boolean building;
	
	public Grid(Cell[][] field) {
		this.field = field;
	}
	
	public Grid() {
	
	}
	
	@Override
	public void applyChanges(BuilderSolverChange<Map<String, CellForAi>> changes) {
		if (changes.building) {
			return;
		}
		for (int i = 0; i < Cell.FIELD_SIZE; i++) {
			for (int j = 0; j < Cell.FIELD_SIZE; j++) {
				if (changes.change.containsKey(i + ":" + j)) {
					System.out.println(field);
					System.out.println(changes.change.get(i + ":" + j));
					System.out.println(changes.change.containsKey(i + ":" + j));
					field[i][j] = changes.change.get(i + ":" + j).toCell();
				}
			}
		}
	}
	
	@Override
	public Response<Map<String, CellForAi>> build(MinesweeperBuilderResponse response) {
		field = Cell.getArrayFromAi(response.field);
		Response<Map<String, CellForAi>> result = new Response<>();
		result.finished = true;
		System.out.println("Anzahl an Bomben: " + getBombs());
		System.out.println("Hat Feld leere Zellen: " + hasEmpty());
		result.valid = !hasEmpty() && getBombs() == Cell.BOMB_COUNT;
		if (result.valid) {
			countSurroundingBombs();
		}
		MinesweeperRenderData data = new MinesweeperRenderData();
		data.aiID = getAi().getId();
		data.calculationTime = getAi().getObject().millisLeft;
		data.field = Cell.getArrayForAi(field);
		data.output = response.output;
		result.renderData = data;
		return result;
	}
	
	@Override
	public Response<Map<String, CellForAi>> solve(MinesweeperSolverResponse response) {
		logger.entering(getClass().toString(), "solve");
		Response<Map<String, CellForAi>> result = new Response<>();
		result.changes = new HashMap<>();
		result.valid = true;
		
		MinesweeperRenderData data = new MinesweeperRenderData();
		data.aiID = getAi().getId();
		data.calculationTime = getAi().getObject().millisLeft;
		data.field = Cell.getArrayForAi(field);
		data.output = response.output;
		result.renderData = data;
		
		Cell cell = get(response.xFlag, response.yFlag);
		if (cell != null) {
			cell.setFlagged(!cell.isFlagged());
			result.changes.put(response.xFlag + ":" + response.yFlag, new CellForAi(cell));
		}
		
		cell = get(response.xStep, response.yStep);
		if (cell != null) {
			Map<String, CellForAi> uncover = uncover(response.xStep, response.yStep);
			
			moves++;
			
			if (uncover == null) {
				result.finished = true;
				result.valid = false;
				result.changes = null;
				return result;
			}
			
			result.changes.putAll(uncover);
			if (won()) {
				result.finished = true;
				result.changes = null;
				((MinesweeperRenderData) result.renderData).solved = true;
			}
			
		}
		return result;
	}
	
	@Override
	public Map<String, CellForAi> getState() {
		Map<String, CellForAi> response = new HashMap<>();
		for (int i = 0; i < Cell.FIELD_SIZE; i++) {
			for (int j = 0; j < Cell.FIELD_SIZE; j++) {
				response.put(i + ":" + j, new CellForAi(get(i, j)));
			}
		}
		return response;
	}
	
	private Map<String, CellForAi> uncover(int x, int y) {
		return uncover(x, y, new HashMap<>());
	}
	
	private Map<String, CellForAi> uncover(int x, int y, Map<String, CellForAi> map) {
		if (!Cell.isInField(x, y)) {
			return null;
		}
		Cell cell = field[x][y];
		if (cell.getType() == Type.BOMB) {
			return null;
		}
		if (cell.isUncovered()) {
			return map;
		}
		cell.uncover();
		map.put(x + ":" + y, new CellForAi(cell));
		
		uncover(x + 1, y, map);
		uncover(x, y + 1, map);
		uncover(x, y - 1, map);
		uncover(x - 1, y, map);
		
		return map;
	}
	
	private void countSurroundingBombs() {
		for (int i = 0; i < Cell.FIELD_SIZE; i++) {
			for (int j = 0; j < Cell.FIELD_SIZE; j++) {
				countSurroundingBombs(i, j);
			}
		}
	}
	
	private void countSurroundingBombs(int x, int y) {
		Cell cell = field[x][y];
		int bombsArround = 0;
		bombsArround += isBomb(x + 1, y, field);
		bombsArround += isBomb(x + 1, y + 1, field);
		bombsArround += isBomb(x + 1, y - 1, field);
		bombsArround += isBomb(x, y + 1, field);
		bombsArround += isBomb(x, y - 1, field);
		bombsArround += isBomb(x - 1, y, field);
		bombsArround += isBomb(x - 1, y + 1, field);
		bombsArround += isBomb(x - 1, y - 1, field);
		cell.setBombsArround(bombsArround);
	}
	
	private int isBomb(int x, int y, Cell[][] field) {
		if (x < 0 || x >= Cell.FIELD_SIZE || y < 0 || y >= Cell.FIELD_SIZE) {
			return 0;
		}
		return field[x][y].getType() == Type.BOMB ? 1 : 0;
	}
	
	public Cell[][] getField() {
		return field;
	}
	
	private boolean hasEmpty() {
		for (int i = 0; i < Cell.FIELD_SIZE; i++) {
			for (int j = 0; j < Cell.FIELD_SIZE; j++) {
				if (field[i][j] == null || field[i][j].getType() == Type.COVERED) {
					return true;
				}
			}
		}
		return false;
	}
	
	private int getBombs() {
		int bombs = 0;
		for (int i = 0; i < Cell.FIELD_SIZE; i++) {
			for (int j = 0; j < Cell.FIELD_SIZE; j++) {
				if (field[i][j].getType() == Type.BOMB) {
					bombs++;
				}
			}
		}
		return bombs;
	}
	
	private Cell get(int x, int y) {
		if (!Cell.isInField(x, y)) {
			return null;
		}
		return field[x][y];
	}
	
	private boolean won() {
		if (won) {
			return true;
		}
		for (int i = 0; i < Cell.FIELD_SIZE; i++) {
			for (int j = 0; j < Cell.FIELD_SIZE; j++) {
				Cell cell = get(i, j);
				if (!cell.isUncovered() && cell.getType() == Type.EMPTY) {
					return false;
				}
			}
		}
		won = true;
		return true;
	}
	
}
