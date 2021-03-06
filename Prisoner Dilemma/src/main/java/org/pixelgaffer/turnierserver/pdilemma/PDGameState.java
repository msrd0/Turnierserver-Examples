/*
 * PDGameState.java
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
package org.pixelgaffer.turnierserver.pdilemma;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Pure;
import org.pixelgaffer.turnierserver.gamelogic.interfaces.Ai;
import org.pixelgaffer.turnierserver.gamelogic.interfaces.GameState;
import org.pixelgaffer.turnierserver.pdilemma.PDNextRound;
import org.pixelgaffer.turnierserver.pdilemma.PDResponse;

@SuppressWarnings("all")
public class PDGameState implements GameState<PDNextRound, PDResponse> {
	
	@Accessors({ AccessorType.PUBLIC_GETTER, AccessorType.PRIVATE_SETTER })
	private List<List<Boolean>> responses = new ArrayList<List<Boolean>>();
	
	private List<Integer> points = new ArrayList<Integer>();
	
	@Accessors({ AccessorType.PUBLIC_GETTER, AccessorType.PRIVATE_SETTER })
	private ArrayList<PDResponse> lastResponse = new ArrayList<PDResponse>();
	
	public PDGameState() {
		ArrayList<Boolean> _arrayList = new ArrayList<Boolean>();
		this.responses.add(_arrayList);
		ArrayList<Boolean> _arrayList_1 = new ArrayList<Boolean>();
		this.responses.add(_arrayList_1);
		this.points.add(Integer.valueOf(0));
		this.points.add(Integer.valueOf(0));
		PDResponse _pDResponse = new PDResponse();
		final Procedure1<PDResponse> _function = (PDResponse it) -> {
			it.output = "Diese KI hat in der ersten Runde schon verloren";
			it.response = false;
		};
		PDResponse noResponse = ObjectExtensions.<PDResponse> operator_doubleArrow(_pDResponse, _function);
		this.lastResponse.add(noResponse);
		this.lastResponse.add(noResponse);
	}
	
	@Override
	public void clearChanges(final Ai ai) {
	}
	
	@Override
	public PDNextRound getChanges(final Ai ai) {
		PDNextRound _pDNextRound = new PDNextRound();
		final Procedure1<PDNextRound> _function = (PDNextRound it) -> {
			Boolean _elvis = null;
			int _xifexpression = (int) 0;
			int _index = ai.getIndex();
			boolean _equals = (_index == 0);
			if (_equals) {
				_xifexpression = 1;
			} else {
				_xifexpression = 0;
			}
			List<Boolean> _get = this.responses.get(_xifexpression);
			Boolean _last = IterableExtensions.<Boolean> last(_get);
			if (_last != null) {
				_elvis = _last;
			} else {
				_elvis = Boolean.valueOf(false);
			}
			it.enemyResponse = (_elvis).booleanValue();
			Boolean _elvis_1 = null;
			int _xifexpression_1 = (int) 0;
			int _index_1 = ai.getIndex();
			boolean _equals_1 = (_index_1 == 0);
			if (_equals_1) {
				_xifexpression_1 = 0;
			} else {
				_xifexpression_1 = 1;
			}
			List<Boolean> _get_1 = this.responses.get(_xifexpression_1);
			Boolean _last_1 = IterableExtensions.<Boolean> last(_get_1);
			if (_last_1 != null) {
				_elvis_1 = _last_1;
			} else {
				_elvis_1 = Boolean.valueOf(false);
			}
			it.ownResponse = (_elvis_1).booleanValue();
		};
		return ObjectExtensions.<PDNextRound> operator_doubleArrow(_pDNextRound, _function);
	}
	
	@Override
	public void applyChanges(final PDResponse response, final Ai ai) {
		int _index = ai.getIndex();
		List<Boolean> _get = this.responses.get(_index);
		_get.add(Boolean.valueOf(response.response));
		int _index_1 = ai.getIndex();
		this.lastResponse.set(_index_1, response);
	}
	
	@Override
	public void applyChanges(final PDNextRound changes) {
		List<Boolean> _get = this.responses.get(0);
		_get.add(Boolean.valueOf(changes.ownResponse));
		List<Boolean> _get_1 = this.responses.get(1);
		_get_1.add(Boolean.valueOf(changes.enemyResponse));
		this.updatePoints();
	}
	
	public List<Boolean> getOwnChanges() {
		return this.responses.get(0);
	}
	
	public List<Boolean> getEnemyChanges() {
		return this.responses.get(1);
	}
	
	public Integer getOwnPoints() {
		return this.points.get(0);
	}
	
	public Integer getEnemyPoints() {
		return this.points.get(1);
	}
	
	public Integer getPoints(final int player) {
		return this.points.get(player);
	}
	
	public void updatePoints() {
		Integer _get = this.points.get(0);
		Boolean _elvis = null;
		List<Boolean> _get_1 = this.responses.get(0);
		Boolean _last = IterableExtensions.<Boolean> last(_get_1);
		if (_last != null) {
			_elvis = _last;
		} else {
			_elvis = Boolean.valueOf(false);
		}
		Boolean _elvis_1 = null;
		List<Boolean> _get_2 = this.responses.get(1);
		Boolean _last_1 = IterableExtensions.<Boolean> last(_get_2);
		if (_last_1 != null) {
			_elvis_1 = _last_1;
		} else {
			_elvis_1 = Boolean.valueOf(false);
		}
		int _points = this.getPoints((_elvis).booleanValue(), (_elvis_1).booleanValue());
		int _plus = ((_get).intValue() + _points);
		this.points.set(0, Integer.valueOf(_plus));
		Integer _get_3 = this.points.get(1);
		Boolean _elvis_2 = null;
		List<Boolean> _get_4 = this.responses.get(1);
		Boolean _last_2 = IterableExtensions.<Boolean> last(_get_4);
		if (_last_2 != null) {
			_elvis_2 = _last_2;
		} else {
			_elvis_2 = Boolean.valueOf(false);
		}
		Boolean _elvis_3 = null;
		List<Boolean> _get_5 = this.responses.get(0);
		Boolean _last_3 = IterableExtensions.<Boolean> last(_get_5);
		if (_last_3 != null) {
			_elvis_3 = _last_3;
		} else {
			_elvis_3 = Boolean.valueOf(false);
		}
		int _points_1 = this.getPoints((_elvis_2).booleanValue(), (_elvis_3).booleanValue());
		int _plus_1 = ((_get_3).intValue() + _points_1);
		this.points.set(1, Integer.valueOf(_plus_1));
	}
	
	public int getPoints(final boolean ownReaction, final boolean enemyReaction) {
		boolean _and = false;
		if (!enemyReaction) {
			_and = false;
		} else {
			_and = ownReaction;
		}
		if (_and) {
			return (-1);
		}
		if (((!ownReaction) && (!enemyReaction))) {
			return 1;
		}
		boolean _and_1 = false;
		if (!ownReaction) {
			_and_1 = false;
		} else {
			_and_1 = (!enemyReaction);
		}
		if (_and_1) {
			return 2;
		}
		return (-3);
	}
	
	@Pure
	public List<List<Boolean>> getResponses() {
		return this.responses;
	}
	
	private void setResponses(final List<List<Boolean>> responses) {
		this.responses = responses;
	}
	
	@Pure
	public ArrayList<PDResponse> getLastResponse() {
		return this.lastResponse;
	}
	
	private void setLastResponse(final ArrayList<PDResponse> lastResponse) {
		this.lastResponse = lastResponse;
	}
}
