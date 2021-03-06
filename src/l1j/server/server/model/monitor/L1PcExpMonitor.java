﻿/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package l1j.server.server.model.monitor;

import l1j.server.Config;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_Karma;//TODO 友好度實裝
import l1j.server.server.serverpackets.S_Lawful;
public class L1PcExpMonitor extends L1PcMonitor {

	private int _old_lawful;

	private int _old_exp;

	private int _old_karma;//TODO 友好度實裝

	private int _oldFight;//TODO 上一次判斷時的戰鬥特化類別

	public L1PcExpMonitor(int oId) {
		super(oId);
	}

	@Override
	public void execTask(L1PcInstance pc) {

		// ロウフルが変わった場合はS_Lawfulを送信
		// // ただし色が変わらない場合は送信しない
		// if (_old_lawful != pc.getLawful()
		// && !((IntRange.includes(_old_lawful, 9000, 32767) && IntRange
		// .includes(pc.getLawful(), 9000, 32767)) || (IntRange
		// .includes(_old_lawful, -32768, -2000) && IntRange
		// .includes(pc.getLawful(), -32768, -2000)))) {
		if (_old_lawful != pc.getLawful()) {
			_old_lawful = pc.getLawful();
			S_Lawful s_lawful = new S_Lawful(pc.getId(), _old_lawful);
			pc.sendPackets(s_lawful);
			pc.broadcastPacket(s_lawful);
		}

		//TODO 處理戰鬥特化系統
		if (Config.FIGHT_IS_ACTIVE) {
			int fightType = _old_lawful / 10000;
			if (_oldFight != fightType) {
				pc.changeFightType(_oldFight, fightType);
				_oldFight = fightType;
			}
		}
		//TODO 處理戰鬥特化系統

		//TODO 友好度實裝
		if (_old_karma != pc.getKarma()) {
			_old_karma = pc.getKarma();
			pc.sendPackets(new S_Karma(pc));
		}
		//TODO 友好度實裝
		if (_old_exp != pc.getExp()) {
			_old_exp = pc.getExp();
			pc.onChangeExp();
		}
	}
}
