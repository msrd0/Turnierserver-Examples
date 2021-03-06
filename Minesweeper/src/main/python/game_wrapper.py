####################################################################################
# game_wrapper.py
#
# Copyright (C) 2015 Pixelgaffer
#
# This work is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as published by the
# Free Software Foundation; either version 2 of the License, or any later
# version.
#
# This work is distributed in the hope that it will be useful, but without
# any warranty; without even the implied warranty of merchantability or
# fitness for a particular purpose. See version 2 and version 3 of the
# GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
####################################################################################
from wrapper import AIWrapper
from collections import namedtuple

FIELD_SIZE = 9
START_BOMBS = 10

class CellType:
	BOMB = "BOMB"
	EMPTY = "EMPTY"
	COVERED = "COVERED"

Cell = namedtuple("Cell", ["type", "flagged", "bombsAround"])

class State:
	def __init__(self, grid=None):
		self._flag = (-1, -1)
		self._step = (-1, -1)
		if grid:
			self.grid = grid
		else:
			self.grid = [[Cell(type=CellType.COVERED, flagged=False, bombsAround=-1) for _ in range(FIELD_SIZE)] for _ in range(FIELD_SIZE)]
		self.isBuilding = False

	def check_is_field(self, x, y):
		if x < 0 or x > FIELD_SIZE or y < 0 or y > FIELD_SIZE:
			raise RuntimeError("({}, {}) ist nicht im {}er Feld enthalten".format(x, y, FIELD_SIZE))
		if x != int(x) or y != int(y):
			raise RuntimeError("Nur Ints erlaubt! x: {} ({}), y: {} ({})".format(x, type(x), y, type(y)))

	def flag(self, x: int, y: int):
		self.check_is_field(x, y)
		self._flag = (x, y)

	def unflag(self, x=None, y=None):
		if not x:
			x = self._flag[0]
		if not y:
			y = self._flag[1]

		if (x, y) == self._flag:
			self._flag = (-1, -1)

	def uncover(self, x: int, y: int):
		self.check_is_field(x, y)
		self._step = (x, y)

	def cover(self, x=None, y=None):
		if not x:
			x = self._step[0]
		if not y:
			y = self._step[1]

		if (x, y) == self._step:
			self._step = (-1, -1)


class GameWrapper(AIWrapper):
	state = None

	def update(self, updates):
		state = self.getState(updates)
		if state.isBuilding:
			field = self.ai.generateField()
			response = {
				"build": {
					"output": self.output.read(),
					"field": [[{"type": CellType.BOMB} if f else {"type": CellType.EMPTY} for f in row] for row in field]
				}
			}
		else:
			self.ai.step(state)
			response = {
				"solve": {
					"output": self.output.read(),
					"xStep": state._step[0], "yStep": state._step[1],
					"xFlag": state._flag[0], "yFlag": state._flag[1]
				}
			}
		return response

	def getState(self, d):
		state = State()
		if "building" in d:
			state.isBuilding = d["building"]

		if not state.isBuilding:
			if self.state:
				state.grid = self.state.grid
			for pos, change in d["change"].items():
				x, y = pos.split(":")
				x, y = int(x), int(y)
				state.grid[x][y] = Cell(type=change["type"], flagged=change["flagged"], bombsAround=change["bombsAround"])

		self.state = state

		return state

	def del_output(self, d):
		del d[list(d.keys())[0]]["output"]

	def add_output(self, d, o):
		d[list(d.keys())[0]]["output"] += o


