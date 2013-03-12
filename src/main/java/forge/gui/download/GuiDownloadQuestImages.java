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
package forge.gui.download;

import java.util.ArrayList;

import forge.properties.NewConstants;

/** */
@SuppressWarnings("serial")
public class GuiDownloadQuestImages extends GuiDownloader {
    /**
     * <p>
     * Constructor for GuiDownloadQuestImages.
     * </p>
     */
    public GuiDownloadQuestImages() {
        super();
    }

    /**
     * <p>
     * getNeededCards.
     * </p>
     * 
     * @return an array of {@link forge.gui.download.GuiDownloadSetPicturesLQ} objects.
     */
    @Override
    protected final ArrayList<DownloadObject> getNeededImages() {
        // read all card names and urls
        final ArrayList<DownloadObject> urls = new ArrayList<DownloadObject>();

        for (final DownloadObject questOpponent : GuiDownloader.readFile(NewConstants.IMAGE_LIST_QUEST_OPPONENT_ICONS_FILE, NewConstants.CACHE_ICON_PICS_DIR)) {
            if (!questOpponent.getDestination().exists()) {
                urls.add(questOpponent);
            }
        }

        for (final DownloadObject packImage : GuiDownloader.readFile(NewConstants.IMAGE_LIST_QUEST_BOOSTERS_FILE, NewConstants.CACHE_BOOSTER_PICS_DIR)) {
            if (!packImage.getDestination().exists()) {
                urls.add(packImage);
            }
        }

        for (final DownloadObject packImage : GuiDownloader.readFile(NewConstants.IMAGE_LIST_QUEST_FATPACKS_FILE, NewConstants.CACHE_FATPACK_PICS_DIR)) {
            if (!packImage.getDestination().exists()) {
                urls.add(packImage);
            }
        }

        for (final DownloadObject packImage : GuiDownloader.readFile(NewConstants.IMAGE_LIST_QUEST_PRECONS_FILE, NewConstants.CACHE_PRECON_PICS_DIR)) {
            if (!packImage.getDestination().exists()) {
                urls.add(packImage);
            }
        }

        for (final DownloadObject packImage : GuiDownloader.readFile(NewConstants.IMAGE_LIST_QUEST_TOURNAMENTPACKS_FILE, NewConstants.CACHE_TOURNAMENTPACK_PICS_DIR)) {
            if (!packImage.getDestination().exists()) {
                urls.add(packImage);
            }
        }

        for (final DownloadObject petIcon : GuiDownloader.readFile(NewConstants.IMAGE_LIST_QUEST_PET_SHOP_ICONS_FILE, NewConstants.CACHE_ICON_PICS_DIR)) {
            if (!petIcon.getDestination().exists()) {
                urls.add(petIcon);
            }
        }

        for (final DownloadObject questPet : GuiDownloader.readFile(NewConstants.IMAGE_LIST_QUEST_TOKENS_FILE, NewConstants.CACHE_TOKEN_PICS_DIR)) {
            if (!questPet.getDestination().exists()) {
                urls.add(questPet);
            }
        }

        return urls;
    }
}
