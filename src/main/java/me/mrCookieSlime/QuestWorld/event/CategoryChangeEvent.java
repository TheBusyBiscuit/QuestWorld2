package me.mrCookieSlime.QuestWorld.event;

import org.bukkit.event.HandlerList;

import me.mrCookieSlime.QuestWorld.api.contract.ICategory;
import me.mrCookieSlime.QuestWorld.api.contract.ICategoryWrite;

public class CategoryChangeEvent extends CancellableEvent {
	private ICategoryWrite nextState;
	
	public CategoryChangeEvent(ICategoryWrite nextState) {
		this.nextState = nextState;
	}
	
	public ICategory getCategory() {
		return nextState.getSource();
	}

	public ICategoryWrite getNextState() {
		return nextState;
	}
	
	public boolean hasChange(ICategoryWrite.Member field) {
		return nextState.hasChange(field);
	}
	
	// Boilerplate copy/paste from CancellableEvent
	@Override
	public HandlerList getHandlers() { return handlers;	}
	public static HandlerList getHandlerList() { return handlers; }
	private static final HandlerList handlers = new HandlerList();
}
