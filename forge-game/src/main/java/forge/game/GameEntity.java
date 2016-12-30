/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
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
package forge.game;

import forge.game.card.Card;
import forge.game.card.CardCollection;
import forge.game.card.CardCollectionView;
import forge.game.card.CardDamageMap;
import forge.game.card.CounterType;
import forge.game.event.GameEventCardAttachment;
import forge.game.event.GameEventCardAttachment.AttachMethod;
import forge.util.collect.FCollection;

import java.util.Map;

import com.google.common.collect.Maps;


public abstract class GameEntity extends GameObject implements IIdentifiable {
    protected final int id;
    private String name = "";
    private int preventNextDamage = 0;
    private CardCollection enchantedBy;
    private Map<Card, Map<String, String>> preventionShieldsWithEffects = Maps.newTreeMap();
    protected Map<CounterType, Integer> counters = Maps.newEnumMap(CounterType.class);

    protected GameEntity(int id0) {
        id = id0;
    }

    @Override
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public void setName(final String s) {
        name = s;
        getView().updateName(this);
    }

    public int addDamage(final int damage, final Card source, final CardDamageMap damageMap) {
        int damageToDo = damage;

        damageToDo = replaceDamage(damageToDo, source, false, true, damageMap);
        damageToDo = preventDamage(damageToDo, source, false);

        return addDamageAfterPrevention(damageToDo, source, false, damageMap);
    }

    public int addDamageWithoutPrevention(final int damage, final Card source, final CardDamageMap damageMap) {
        int damageToDo = replaceDamage(damage, source, false, false, damageMap);
        return addDamageAfterPrevention(damageToDo, source, false, damageMap);
    }

    public int replaceDamage(final int damage, final Card source, final boolean isCombat, final boolean prevention, final CardDamageMap damageMap) {
     // Replacement effects
        final Map<String, Object> repParams = Maps.newHashMap();
        repParams.put("Event", "DamageDone");
        repParams.put("Affected", this);
        repParams.put("DamageSource", source);
        repParams.put("DamageAmount", damage);
        repParams.put("IsCombat", isCombat);

        switch (getGame().getReplacementHandler().run(repParams)) {
        case NotReplaced:
            return damage;
        case Updated:
            int newDamage = (int) repParams.get("DamageAmount");
            GameEntity newTarget = (GameEntity)repParams.get("Affected");
            // check if this is still the affected card or player
            if (this.equals(newTarget)) {
                return newDamage;
            } else {
                if (prevention) {
                    newDamage = newTarget.preventDamage(newDamage, source, isCombat);
                }
                newTarget.addDamageAfterPrevention(newDamage, source, isCombat, damageMap);
            }
        default:
            return 0;
        }
    }

    // This function handles damage after replacement and prevention effects are applied
    public abstract int addDamageAfterPrevention(final int damage, final Card source, final boolean isCombat, CardDamageMap damageMap);

    // This should be also usable by the AI to forecast an effect (so it must
    // not change the game state)
    public abstract int staticDamagePrevention(final int damage, final Card source, final boolean isCombat, final boolean isTest);

    // This should be also usable by the AI to forecast an effect (so it must
    // not change the game state)
    public abstract int staticReplaceDamage(final int damage, final Card source, final boolean isCombat);

    public abstract int preventDamage(final int damage, final Card source, final boolean isCombat);

    public int getPreventNextDamage() {
        return preventNextDamage;
    }
    public void setPreventNextDamage(final int n) {
        preventNextDamage = n;
    }
    public void addPreventNextDamage(final int n) {
        preventNextDamage += n;
    }
    public void subtractPreventNextDamage(final int n) {
        preventNextDamage -= n;
    }
    public void resetPreventNextDamage() {
        preventNextDamage = 0;
    }

    // PreventNextDamageWithEffect
    public Map<Card, Map<String, String>> getPreventNextDamageWithEffect() {
        return preventionShieldsWithEffects;
    }
    public int getPreventNextDamageTotalShields() {
        int shields = preventNextDamage;
        for (final Map<String, String> value : preventionShieldsWithEffects.values()) {
            shields += Integer.valueOf(value.get("ShieldAmount"));
        }
        return shields;
    }
    /**
     * Adds a damage prevention shield with an effect that happens at time of prevention.
     * @param shieldSource - The source card which generated the shield
     * @param effectMap - A map of the effect occurring with the damage prevention
     */
    public void addPreventNextDamageWithEffect(final Card shieldSource, Map<String, String> effectMap) {
        if (preventionShieldsWithEffects.containsKey(shieldSource)) {
            int currentShields = Integer.valueOf(preventionShieldsWithEffects.get(shieldSource).get("ShieldAmount"));
            currentShields += Integer.valueOf(effectMap.get("ShieldAmount"));
            effectMap.put("ShieldAmount", Integer.toString(currentShields));
            preventionShieldsWithEffects.put(shieldSource, effectMap);
        } else {
            preventionShieldsWithEffects.put(shieldSource, effectMap);
        }
    }
    public void subtractPreventNextDamageWithEffect(final Card shieldSource, final int n) {
        int currentShields = Integer.valueOf(preventionShieldsWithEffects.get(shieldSource).get("ShieldAmount"));
        if (currentShields > n) {
            preventionShieldsWithEffects.get(shieldSource).put("ShieldAmount", String.valueOf(currentShields - n));
        } else {
            preventionShieldsWithEffects.remove(shieldSource);
        }
    }
    public void resetPreventNextDamageWithEffect() {
        preventionShieldsWithEffects.clear();
    }

    public abstract boolean hasKeyword(final String keyword);

    // GameEntities can now be Enchanted
    public final CardCollectionView getEnchantedBy(boolean allowModify) {
        return CardCollection.getView(enchantedBy, allowModify);
    }
    public final void setEnchantedBy(final CardCollection cards) {
        enchantedBy = cards;
        getView().updateEnchantedBy(this);
    }
    public final void setEnchantedBy(final Iterable<Card> cards) {
        if (cards == null) {
            enchantedBy = null;
        }
        else {
            enchantedBy = new CardCollection(cards);
        }
        getView().updateEnchantedBy(this);
    }
    public final boolean isEnchanted() {
        return FCollection.hasElements(enchantedBy);
    }
    public final boolean isEnchantedBy(Card c) {
        return FCollection.hasElement(enchantedBy, c);
    }
    public final boolean isEnchantedBy(final String cardName) {
        for (final Card aura : getEnchantedBy(false)) {
            if (aura.getName().equals(cardName)) {
                return true;
            }
        }
        return false;
    }
    public final void addEnchantedBy(final Card c) {
        if (enchantedBy == null) {
            enchantedBy = new CardCollection();
        }
        if (enchantedBy.add(c)) {
            getView().updateEnchantedBy(this);
            getGame().fireEvent(new GameEventCardAttachment(c, null, this, AttachMethod.Enchant));
        }
    }
    public final void removeEnchantedBy(final Card c) {
        if (enchantedBy == null) { return; }

        if (enchantedBy.remove(c)) {
            if (enchantedBy.isEmpty()) {
                enchantedBy = null;
            }
            getView().updateEnchantedBy(this);
            getGame().fireEvent(new GameEventCardAttachment(c, this, null, AttachMethod.Enchant));
        }
    }
    public final void unEnchantAllCards() {
        if (isEnchanted()) {
            for (Card c : getEnchantedBy(true)) {
                c.unEnchantEntity(this);
            }
        }
    }

    public abstract boolean hasProtectionFrom(final Card source);

    // Counters!
    public boolean hasCounters() {
        return !counters.isEmpty();
    }

    // get all counters from a card
    public final Map<CounterType, Integer> getCounters() {
        return counters;
    }

    public final int getCounters(final CounterType counterName) {
        Integer value = counters.get(counterName);
        return value == null ? 0 : value;
    }

    public void setCounters(final CounterType counterType, final Integer num) {
        counters.put(counterType, num);
    }

    abstract public void setCounters(final Map<CounterType, Integer> allCounters);

    abstract public boolean canReceiveCounters(final CounterType type);
    abstract protected void addCounter(final CounterType counterType, final int n, final boolean applyMultiplier, final boolean fireEvents);
    abstract public void subtractCounter(final CounterType counterName, final int n);
    abstract public void clearCounters();


    @Override
    public final boolean equals(Object o) {
        if (o == null) { return false; }
        return o.hashCode() == id && o.getClass().equals(getClass());
    }

    @Override
    public final int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }

    public abstract Game getGame();
    public abstract GameEntityView getView();
}
