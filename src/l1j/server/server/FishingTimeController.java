/**
 *                            License
 * THE WORK (AS DEFINED BELOW) IS PROVIDED UNDER THE TERMS OF THIS  
 * CREATIVE COMMONS PUBLIC LICENSE ("CCPL" OR "LICENSE"). 
 * THE WORK IS PROTECTED BY COPYRIGHT AND/OR OTHER APPLICABLE LAW.  
 * ANY USE OF THE WORK OTHER THAN AS AUTHORIZED UNDER THIS LICENSE OR  
 * COPYRIGHT LAW IS PROHIBITED.
 * 
 * BY EXERCISING ANY RIGHTS TO THE WORK PROVIDED HERE, YOU ACCEPT AND  
 * AGREE TO BE BOUND BY THE TERMS OF THIS LICENSE. TO THE EXTENT THIS LICENSE  
 * MAY BE CONSIDERED TO BE A CONTRACT, THE LICENSOR GRANTS YOU THE RIGHTS CONTAINED 
 * HERE IN CONSIDERATION OF YOUR ACCEPTANCE OF SUCH TERMS AND CONDITIONS.
 * 
 */
package l1j.server.server;

import java.util.List;
import java.util.Random;

import l1j.server.server.datatables.ItemTable;
import l1j.server.server.model.L1Inventory;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_CharVisualUpdate;
import l1j.server.server.serverpackets.S_OwnCharStatus;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.utils.collections.Lists;
import l1j.server.server.serverpackets.S_SystemMessage; //TODO 修改魚上鉤訊息by0968026609

/** 自動釣魚系統
 *
 * @author by99團隊
 */

public class FishingTimeController implements Runnable {
	private static FishingTimeController _instance;
	private static Random _random = new Random();
	private final List<L1PcInstance> _fishingList = Lists.newArrayList();

	public static FishingTimeController getInstance() {
		if (_instance == null) {
			_instance = new FishingTimeController();
		}
		return _instance;
	}

	@Override
	public void run() {
		try {
			while (true) {
				Thread.sleep(300);
				fishing();
			}
		} catch (Exception e1) {
		}
	}

	public void addMember(L1PcInstance pc) {
		if ((pc == null) || _fishingList.contains(pc)) {
			return;
		}
		_fishingList.add(pc);
	}

	public void removeMember(L1PcInstance pc) {
		if ((pc == null) || !_fishingList.contains(pc)) {
			return;
		}
		_fishingList.remove(pc);
	}

	private void fishing() {
		if (_fishingList.size() > 0) {
			long currentTime = System.currentTimeMillis();
			for (int i = 0; i < _fishingList.size(); i++) {
				L1PcInstance pc = _fishingList.get(i);
				if (pc.isFishing()) { //TODO 釣魚中
					long time = pc.getFishingTime();
					if ((currentTime <= (time + 500))
							&& (currentTime >= (time - 500))
							&& !pc.isFishingReady()) {
						pc.setFishingReady(true);
						finishFishing(pc);
					}
				}
			}
		}
	}

	//TODO 釣魚完成
	private void finishFishing(L1PcInstance pc) {
		int chance = _random.nextInt(215) + 1;
		boolean finish = false;
		int[] fish = { 41298, 41300, 41299, 41296, 41297, 41301, 41302, 41303, 41304, 41306,
				41307, 41305, 21051, 21052, 21053, 21054, 21055, 21056, 41252, 47104 };
		int[] random = { 20, 40, 60, 80, 100, 110, 120, 130, 140, 145,
				150, 155, 160, 165, 170, 175, 180, 185, 190, 195, 198, 201, 204 };
		for (int i = 0; i < fish.length; i++) {
			if (random[i] > chance) {
				successFishing(pc, fish[i]);
				finish = true;
				break;
			}
		}
		if (!finish) {
			pc.sendPackets(new S_SystemMessage("真可惜沒有釣到寶物下次再接再力。"));//TODO 修改魚上鉤訊息by0968026609
			if (pc.isFishingReady()) {
				restartFishing(pc);
			}
		}
	}

	//TODO 釣魚成功
	private void successFishing(L1PcInstance pc, int itemId) {
		L1ItemInstance item = ItemTable.getInstance().createItem(itemId);
		if (item != null) {
			pc.sendPackets(new S_ServerMessage(403, item.getItem().getName()));
			pc.addExp(2);
			pc.sendPackets(new S_OwnCharStatus(pc));
			item.setCount(1);
			if (pc.getInventory().checkAddItem(item, 1) == L1Inventory.OK) {
				pc.getInventory().storeItem(item);
			} else { //TODO 負重過重，結束釣魚
				stopFishing(pc);
				item.startItemOwnerTimer(pc);
				L1World.getInstance().getInventory(pc.getX(), pc.getY(), pc.getMapId()).storeItem(item);
				return;
			}
		} else { //TODO 結束釣魚
			pc.sendPackets(new S_SystemMessage("真可惜沒有釣到寶物下次再接再力。"));//TODO 修改魚上鉤訊息by0968026609
			stopFishing(pc);
			return;
		}

		if (pc.isFishingReady()) {
			if (itemId == 47104) {
				pc.sendPackets(new S_SystemMessage("釣到傳說的閃爍的鱗片，自動釣魚已停止。"));//TODO 修改魚上鉤訊息by0968026609
				stopFishing(pc);
				return;
			}
			restartFishing(pc);
		}
	}

	//TODO 重新釣魚
	private void restartFishing(L1PcInstance pc) {
		if (pc.getInventory().consumeItem(41295, 1)) { //TODO 消耗餌，重新釣魚
			long fishTime = System.currentTimeMillis() + 10000 + _random.nextInt(5) * 1000;
			pc.setFishingTime(fishTime);
			pc.setFishingReady(false);
		} else {
			pc.sendPackets(new S_SystemMessage("想要釣到寶物就需要有餌。"));//TODO 修改魚上鉤訊息by0968026609
			stopFishing(pc);
		}
	}

	//TODO 停止釣魚
	private void stopFishing(L1PcInstance pc) {
		pc.setFishingTime(0);
		pc.setFishingReady(false);
		pc.setFishing(false);
		pc.sendPackets(new S_CharVisualUpdate(pc));
		pc.broadcastPacket(new S_CharVisualUpdate(pc));
		FishingTimeController.getInstance().removeMember(pc);
	}
}
