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

package l1j.server.server.serverpackets;

import java.util.logging.Logger;
import l1j.server.server.datatables.ItemTable;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.templates.L1Item;

import l1j.server.server.Opcodes;
import l1j.server.server.model.Instance.L1ItemInstance;

// Referenced classes of package l1j.server.server.serverpackets:
// ServerBasePacket

public class S_TradeAddItem extends ServerBasePacket {
	private static Logger _log = Logger.getLogger(S_TradeAddItem.class
			.getName());

	private static final String S_TRADE_ADD_ITEM = "[S] S_TradeAddItem";

	public S_TradeAddItem(L1ItemInstance item, int count, int type) {
		writeC(Opcodes.S_OPCODE_TRADEADDITEM);
		writeC(type); // 0:トレードウィンドウ上段 1:トレードウィンドウ下段
		writeH(item.getItem().getGfxId());
		writeS(item.getNumberedViewName(count));
		// 0:祝福  1:通常 2:呪い 3:未鑑定
		// 128:祝福&封印 129:&封印 130:呪い&封印 131:未鑑定&封印
		writeC(item.getBless());
		//TODO 交易可見素質 
	    L1ItemInstance dummy = new L1ItemInstance();
	    L1Item template = ItemTable.getInstance().getTemplate(item.getItemId());
	    if (template == null) {
	      writeC(0x00);
	    } else {
	      //dummy.setItem(template);
	      byte[] status = item.getStatusBytes();
	      writeC(status.length);
	      for (byte b : status)
	        writeC(b);
	    }
	}

    @Override
	public byte[] getContent() {
		return getBytes();
	}

	@Override
	public String getType() {
		return S_TRADE_ADD_ITEM;
	}
}
